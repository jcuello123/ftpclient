
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
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
    private BufferedReader data_in;
    private boolean isDataChannelOpen;
    
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
    
    private void openDataChannel() throws IOException{
        data_socket = new Socket(server, data_channel);
        data_in = new BufferedReader(new InputStreamReader(data_socket.getInputStream()));
        isDataChannelOpen = true;
    }
    
    private void closeDataChannel() throws IOException{
        data_socket.close();
        data_in.close();
        isDataChannelOpen = false;
    }
    
    private void closeAll() throws IOException{
        s.close();
        out.close();
        in.close();
        data_socket.close();
        data_in.close();
        isDataChannelOpen = false;
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
        sendServerRequest("user " + user, out);
        getServerResponse(in);
        
        pass = stdIn.readLine();
        sendServerRequest("pass " + pass, out);
        
        String status = getServerResponse(in).split(" ")[0];
        if (status.equals("230")){
            sendServerRequest("PASV", out);
            String port = getServerResponse(in);
            data_channel = parsePort(port);
            System.out.println("data_channel: " + data_channel);
            setIsLoggedIn(true);
        }
    }
    
    private void sendServerRequest(String request, PrintWriter writer){
        writer.println(request);
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
            handleCommand(user_input);
        }
    }
    
    private int parsePort(String response){
        int num1 = Integer.parseInt(response.split(" ")[4].split(",")[4]);
        int num2 = Integer.parseInt(response.split(" ")[4].split(",")[5].replace(").", ""));
        return num1 * 256 + num2;
    }
    
    private void handleCommand(String command) throws IOException{
        if (isLoggedIn() && !isDataChannelOpen && (command.equals("ls") || command.split(" ")[0].equals("get") || command.split(" ")[0].equals("put"))){
            openDataChannel();
        }
        
        if (command.equals("ls")){
            sendServerRequest("LIST", out);
            getServerResponse(in);
            getServerResponse(data_in);
            getServerResponse(data_in);
            getServerResponse(data_in);
            getServerResponse(data_in);
            getServerResponse(data_in);
            getServerResponse(data_in);
            getServerResponse(data_in);
            getServerResponse(data_in);
            getServerResponse(data_in);
            getServerResponse(data_in);
            getServerResponse(in);
        }
        
        else if (command.split(" ")[0].equals("get")){
            sendServerRequest("RETR " + command.split(" ")[1], out);
            getServerResponse(in);
            getServerResponse(in);
        }
        
        else if (command.split(" ")[0].equals("put")){
            sendServerRequest("APPE " + command.split(" ")[1], out);
            getServerResponse(in);
        }
        
        else if (command.split(" ")[0].equals("delete")){
            sendServerRequest("DELE " + command.split(" ")[1], out);
            getServerResponse(in);
        }
        
        else if (command.split(" ")[0].equals("cd")){
            sendServerRequest("CWD " + command.split(" ")[1], out);
            getServerResponse(in);
        }
        
        else if (command.equals("quit")){
            sendServerRequest("quit", out);
            getServerResponse(in);
            setIsLoggedIn(false);
            closeAll();
        }
        
        else {
            sendServerRequest(command, out);
            getServerResponse(in);
        }
    }
}
