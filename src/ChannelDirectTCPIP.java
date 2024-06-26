import java.io.*;

public class ChannelDirectTCPIP extends Channel{

  static private final int LOCAL_WINDOW_SIZE_MAX=0x20000;
  static private final int LOCAL_MAXIMUM_PACKET_SIZE=0x4000;
  static private final byte[] _type = Util.str2byte("direct-tcpip");
  String host;
  int port;

  String originator_IP_address="127.0.0.1";
  int originator_port=0;

  ChannelDirectTCPIP(){
    super();
    type = _type;
    setLocalWindowSizeMax(LOCAL_WINDOW_SIZE_MAX);
    setLocalWindowSize(LOCAL_WINDOW_SIZE_MAX);
    setLocalPacketSize(LOCAL_MAXIMUM_PACKET_SIZE);
  }

  void init (){
    io=new IO();
  }

  public void connect(int connectTimeout) throws JSchException{
    this.connectTimeout=connectTimeout;
    try{
      Session _session=getSession();
      if(!_session.isConnected()){
        throw new JSchException("session is down");
      }

      if(io.in!=null){
        thread=new Thread(this);
        thread.setName("DirectTCPIP thread "+_session.getHost());
        if(_session.daemon_thread){
          thread.setDaemon(_session.daemon_thread);
        }
        thread.start();
      }
      else {
        sendChannelOpen();
      }
    }
    catch(Exception e){
        LoadClass.DebugPrintException("ex_13");
      io.close();
      io=null;
      Channel.del(this);
      if (e instanceof JSchException) {
        throw (JSchException) e;
      }
    }
  }

  public void run(){

    try{
      sendChannelOpen();

      Buffer buf=new Buffer(rmpsize);
      Packet packet=new Packet(buf);
      Session _session=getSession();
      int i=0;

      while(isConnected() &&
            thread!=null && 
            io!=null && 
            io.in!=null){
        i=io.in.read(buf.buffer, 
                     14, 
                     buf.buffer.length-14
                     -Session.buffer_margin
                     );
        if(i<=0){
          eof();
          break;
        }
        packet.reset();
        buf.putByte((byte)Session.SSH_MSG_CHANNEL_DATA);
        buf.putInt(recipient);
        buf.putInt(i);
        buf.skip(i);
        synchronized(this){
          if(close)
            break;
          _session.write(packet, this, i);
        }
      }
    }
    catch(Exception e){
        LoadClass.DebugPrintException("ex_14");
      if(!connected){
        connected=true;
      }
      disconnect();
      return;
    }

    eof();
    disconnect();
  }

  public void setInputStream(InputStream in){
    io.setInputStream(in);
  }
  public void setOutputStream(OutputStream out){
    io.setOutputStream(out);
  }

  public void setHost(String host){this.host=host;}
  public void setPort(int port){this.port=port;}
  public void setOrgIPAddress(String foo){this.originator_IP_address=foo;}
  public void setOrgPort(int foo){this.originator_port=foo;}

  protected Packet genChannelOpenPacket(){
    Buffer buf = new Buffer(50 + // 6 + 4*8 + 12
                            host.length() + originator_IP_address.length() +
                            Session.buffer_margin);
    Packet packet = new Packet(buf);
    // byte   SSH_MSG_CHANNEL_OPEN(90)
    // string channel type         //
    // uint32 sender channel       // 0
    // uint32 initial window size  // 0x100000(65536)
    // uint32 maxmum packet size   // 0x4000(16384)
    packet.reset();
    buf.putByte((byte)90);
    buf.putString(this.type);
    buf.putInt(id);
    buf.putInt(lwsize);
    buf.putInt(lmpsize);
    buf.putString(Util.str2byte(host));
    buf.putInt(port);
    buf.putString(Util.str2byte(originator_IP_address));
    buf.putInt(originator_port);
    return packet;
  }
}
