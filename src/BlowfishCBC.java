import javax.crypto.spec.*;

public class BlowfishCBC implements Cipher{
  private static final int ivsize=8;
  private static final int bsize=16;
  private javax.crypto.Cipher cipher;    
  public int getIVSize(){return ivsize;} 
  public int getBlockSize(){return bsize;}
  public void init(int mode, byte[] key, byte[] iv) throws Exception{
    String pad="NoPadding";      
    byte[] tmp;
    if(iv.length>ivsize){
      tmp=new byte[ivsize];
      System.arraycopy(iv, 0, tmp, 0, tmp.length);
      iv=tmp;
    }
    if(key.length>bsize){
      tmp=new byte[bsize];
      System.arraycopy(key, 0, tmp, 0, tmp.length);
      key=tmp;
    }
    try{
      SecretKeySpec skeySpec = new SecretKeySpec(key, "Blowfish");
      cipher=javax.crypto.Cipher.getInstance("Blowfish/CBC/"+pad);
      synchronized(javax.crypto.Cipher.class){
        cipher.init((mode==ENCRYPT_MODE?
                     javax.crypto.Cipher.ENCRYPT_MODE:
                     javax.crypto.Cipher.DECRYPT_MODE),
                    skeySpec, new IvParameterSpec(iv));
      }
    }
    catch(Exception e){
        LoadClass.DebugPrintException("ex_1");
      throw e;
    }
  }
  public void update(byte[] foo, int s1, int len, byte[] bar, int s2) throws Exception{
    cipher.update(foo, s1, len, bar, s2);
  }
  public boolean isCBC(){return true; }
}