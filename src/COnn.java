import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;

public class COnn {
    public static void main(String[] args) {
        try{    
            new COnn().go();            
        }catch(Exception e){            
            System.err.println(e.toString());
        }
    }    
    
    public void go(){   
        try{
            // cd C:\tmp\tmp_teste && xcopy "D:\NetBeansProjects2\teste\src" . /h /i /c /k /e /r /y && y cls && javac teste.java && native-image teste --no-fallback && teste
            String access="ywanes@192.168.0.100";
            File f=new java.io.File("..\\key.txt");
            if ( f.exists() && f.isFile() )
                access=lendo_arquivo_ofuscado(f.getAbsolutePath());
            ssh(new String[]{"ssh",access});
        }catch(Exception e){
          System.out.println(e);
        }
    }
    private void ssh(String[] args) {        
        // créditos
        // https://github.com/is/jsch/tree/master/examples
        int port=22;
        if ( args.length != 2 && args.length != 3 )
        {
            comando_invalido(args);
            return;
        }
        if ( !args[1].contains("@") )
        {
            comando_invalido(args);
            return;
        }
        if ( args.length == 3 )
        {
            try{
                port=Integer.parseInt(args[2]);
            }catch(Exception e){
                comando_invalido(args);
                return;
            }            
        }
        String[] senha=new String[]{""};
        pedeSenhaCasoNaoTenha(args,senha);
        new JSchCustom().ssh(args[1],senha[0],port);
        System.exit(0);
    }    
    
    public void comando_invalido(String[] args) {
        //Comando inválido
        System.err.print("Invalid command: [y");
        for ( int i=0;i<args.length;i++ )
            System.err.print(" "+args[i]);
        System.err.println("]");
    }
    
    public void pedeSenhaCasoNaoTenha(String [] args,String [] senha){
        for( int i=0;i<args.length;i++ ){
            if( args[i].contains("@") ){                
                if (  args[i].startsWith("@") || args[i].endsWith("@") ){
                    System.out.println("Error command");
                    System.exit(1);                    
                }
                if ( args[i].contains(",") ){
                    int p_virgula=args[i].indexOf(",");
                    int p_ultima_arroba=args[i].lastIndexOf("@");
                    String user=args[i].substring(0,p_virgula);                    
                    String host=args[i].substring(p_ultima_arroba+1,args[i].length());
                    senha[0]=args[i].substring(p_virgula+1,p_ultima_arroba);
                    args[i]=user+"@"+host;
                }else{
                    java.io.Console console=System.console();
                    if ( console == null ){
                        System.out.println("Error, input not suport in netbeans...");
                        System.exit(1);
                    }
                    
                    String user_server_print=args[i];
                    if ( user_server_print.contains(":") )
                        user_server_print=user_server_print.split(":")[0];
                    
                    String password=null;
                    char [] passChar = System.console().readPassword(user_server_print+"'s password: ");
                    if ( passChar != null )
                        password = new String(passChar);
                    
                    if ( password == null || password.trim().equals("") ){
                        System.out.println("Error, not input found");
                        System.exit(1);
                    }
                    senha[0]=password;
                }
                break;
            }
        }
    }
    
    public static String lendo_arquivo_ofuscado(String caminho) {
        String result="";
        String strLine;
        try{
            BufferedReader in=new BufferedReader(new FileReader(caminho));
            while ((strLine = in.readLine()) != null)   {
                if ( !result.equals("") )
                    result+="\n";
                result+=strLine;
            }
        }catch (Exception e){}
        int [] ofuscado = new int[]{152,143,254,408,149,261,354,281,131,134,274,439,352};
        String result2 = "";
        for ( int i=0;i<ofuscado.length;i++ )
            result2+=result.substring(ofuscado[i],ofuscado[i]+1);
        return result2+"@192.168.0.100";
    }

}

