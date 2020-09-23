
import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author jcuel
 */
public class FTPClient {
    private String server;
    private Socket s;
    private PrintWriter out;
    private BufferedReader in;
    private BufferedReader stdIn;
    private boolean logged_in;
    private int data_channel;
    private Socket data_socket;
    private PrintWriter data_out;
    private BufferedReader data_in;     

    public FTPClient(String server) {
        try {
            this.server = server;
            logged_in = false;
            s = new Socket(server,21);
            out = new PrintWriter(s.getOutputStream(), true);
            in = new BufferedReader(new InputStreamReader(s.getInputStream()));
            stdIn = new BufferedReader(new InputStreamReader(System.in));
            System.out.println("Connected to " + server);
            getServerResponse(in);
        } 
        catch(Exception e){
            e.printStackTrace();
        }
    }
    
    private void useDataChannel(String request) throws IOException{
        data_socket = new Socket(server, data_channel);
        data_out = new PrintWriter(data_socket.getOutputStream(), true);
        data_in = new BufferedReader(new InputStreamReader(data_socket.getInputStream()));
        
        data_out.println(request);
        getServerResponse(data_in);
        closeDataChannel();
    }
    
    private void closeDataChannel() throws IOException{
        data_socket.close();
    }
    
    private String getServerResponse(BufferedReader server) throws IOException{
        String response = server.readLine();
        System.out.println(response);
        return response;
    }
    
    public void login() throws IOException{
        String user;
        String pass;
        
        System.out.println("Enter username: ");
        user = stdIn.readLine();
        sendServerRequest("user " + user);
        getServerResponse(in);
        
        System.out.println("Enter password: ");
        pass = stdIn.readLine();
        sendServerRequest("pass " + pass);
        
        String status = getServerResponse(in).split(" ")[0];
        if (status.equals("230")){
            sendServerRequest("PASV");
            String port = getServerResponse(in);
            data_channel = parsePort(port);

            setIsLoggedIn(true);
        }
    }
    
    private void sendServerRequest(String request){
        out.println(request);
    }
            
    public boolean isLoggedIn(){
        return logged_in;
    }
    
    private void setIsLoggedIn(boolean status){
        this.logged_in = status;
    }
    
    public void getUserInput() throws IOException{
        while (isLoggedIn()){
            System.out.print("myftp> ");
            String user_input = stdIn.readLine();
            useDataChannel(user_input);
            
            if (user_input.equals("quit")){
                setIsLoggedIn(false);
            }
        }   
    }
    
    private int parsePort(String response){
        int num1 = Integer.parseInt(response.split(" ")[4].split(",")[4]);
        int num2 = Integer.parseInt(response.split(" ")[4].split(",")[5].replace(").", ""));
        return num1 * 256 + num2;
    }
}
