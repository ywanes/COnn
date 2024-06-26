class UserAuthKeyboardInteractive extends UserAuth{
  public boolean start(Session session) throws Exception{
    super.start(session);

    if(userinfo!=null && !(userinfo instanceof UIKeyboardInteractive)){
      return false;
    }

    String dest=username+"@"+session.host;
    if(session.port!=22){
      dest+=(":"+session.port);
    }
    byte[] password=session.password;

    boolean cancel=false;

    byte[] _username=null;
    _username=Util.str2byte(username);

    while(true){

      if(session.auth_failures >= session.max_auth_tries){
	return false;
      }

      // send
      // byte      SSH_MSG_USERAUTH_REQUEST(50)
      // string    user name (ISO-10646 UTF-8, as defined in [RFC-2279])
      // string    service name (US-ASCII) "ssh-userauth" ? "ssh-connection"
      // string    "keyboard-interactive" (US-ASCII)
      // string    language tag (as defined in [RFC-3066])
      // string    submethods (ISO-10646 UTF-8)
      packet.reset();
      buf.putByte((byte)SSH_MSG_USERAUTH_REQUEST);
      buf.putString(_username);
      buf.putString(Util.str2byte("ssh-connection"));
      //buf.putString("ssh-userauth".getBytes());
      buf.putString(Util.str2byte("keyboard-interactive"));
      buf.putString(Util.empty);
      buf.putString(Util.empty);
      session.write(packet);

      boolean firsttime=true;
      loop:
      while(true){
        buf=session.read(buf);
        int command=buf.getCommand()&0xff;

	if(command==SSH_MSG_USERAUTH_SUCCESS){
	  return true;
	}
	if(command==SSH_MSG_USERAUTH_BANNER){
	  buf.getInt(); buf.getByte(); buf.getByte();
	  byte[] _message=buf.getString();
	  byte[] lang=buf.getString();
	  String message=Util.byte2str(_message);
	  if(userinfo!=null){
	    userinfo.showMessage(message);
	  }
	  continue loop;
	}
	if(command==SSH_MSG_USERAUTH_FAILURE){
	  buf.getInt(); buf.getByte(); buf.getByte(); 
	  byte[] foo=buf.getString();
	  int partial_success=buf.getByte();
//	  System.err.println(new String(foo)+
//			     " partial_success:"+(partial_success!=0));

	  if(partial_success!=0){
	    throw new JSchPartialAuthException(Util.byte2str(foo));
	  }

	  if(firsttime){
	    return false;
	    //throw new JSchException("USERAUTH KI is not supported");
	    //cancel=true;  // ??
	  }
          session.auth_failures++;
	  break;
	}
	if(command==SSH_MSG_USERAUTH_INFO_REQUEST){
	  firsttime=false;
	  buf.getInt(); buf.getByte(); buf.getByte();
	  String name=Util.byte2str(buf.getString());
	  String instruction=Util.byte2str(buf.getString());
	  String languate_tag=Util.byte2str(buf.getString());
	  int num=buf.getInt();
	  String[] prompt=new String[num];
	  boolean[] echo=new boolean[num];
	  for(int i=0; i<num; i++){
	    prompt[i]=Util.byte2str(buf.getString());
	    echo[i]=(buf.getByte()!=0);
	  }

	  byte[][] response=null;

          if(password!=null && 
             prompt.length==1 &&
             !echo[0] &&
             prompt[0].toLowerCase().indexOf("password:") >= 0){
            response=new byte[1][];
            response[0]=password;
            password=null;
          }
          else if(num>0
	     ||(name.length()>0 || instruction.length()>0)
	     ){
	    if(userinfo!=null){
              UIKeyboardInteractive kbi=(UIKeyboardInteractive)userinfo;
              String[] _response=kbi.promptKeyboardInteractive(dest,
                                                               name,
                                                               instruction,
                                                               prompt,
                                                               echo);
              if(_response!=null){
                response=new byte[_response.length][];
                for(int i=0; i<_response.length; i++){
                  response[i]=Util.str2byte(_response[i]);
                }
              }
	    }
	  }

	  // byte      SSH_MSG_USERAUTH_INFO_RESPONSE(61)
	  // int       num-responses
	  // string    response[1] (ISO-10646 UTF-8)
	  // ...
	  // string    response[num-responses] (ISO-10646 UTF-8)
	  packet.reset();
	  buf.putByte((byte)SSH_MSG_USERAUTH_INFO_RESPONSE);
	  if(num>0 &&
	     (response==null ||  // cancel
	      num!=response.length)){

            if(response==null){  
              // working around the bug in OpenSSH ;-<
              buf.putInt(num);
              for(int i=0; i<num; i++){
                buf.putString(Util.empty);
              }
            }
            else{
              buf.putInt(0);
            }

	    if(response==null)
	      cancel=true;
	  }
	  else{
	    buf.putInt(num);
	    for(int i=0; i<num; i++){
	      buf.putString(response[i]);
	    }
	  }
	  session.write(packet);
          /*
	  if(cancel)
	    break;
          */
	  continue loop;
	}
	//throw new JSchException("USERAUTH fail ("+command+")");
	return false;
      }
      if(cancel){
	throw new JSchAuthCancelException("keyboard-interactive");
	//break;
      }
    }
    //return false;
  }
}
