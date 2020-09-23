import java.net.*;  
import java.io.*;  
class myftp{  
    public static void main(String args[])throws Exception{  
        String server_name = args[0];
        FTPClient ftp = new FTPClient(server_name);
        ftp.login();
        
        ftp.getUserInput();
    }
}