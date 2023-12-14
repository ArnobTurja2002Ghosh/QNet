import java.net.ServerSocket;
import java.net.Socket;
import java.io.PrintWriter;
import java.io.BufferedReader;
import java.io.OutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.IOException;

import java.util.LinkedList;
import java.util.Queue;

class Server {
    private static ServerSocket serverSocket;
    private static Socket clientSocket;
    private static BufferedReader in;
    private static PrintWriter out;
    private static String output = "";
    private static String eor = "[EOR]"; // a code for end-of-response
    private static String version = "230828";
    
    // establishing a connection
    private static void setup() throws IOException {
        
        serverSocket = new ServerSocket(0);
        toConsole("Server port is " + serverSocket.getLocalPort());
        
        clientSocket = serverSocket.accept();

        // get the input stream and attach to a buffered reader
        in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
        
        // get the output stream and attach to a printwriter
        out = new PrintWriter(clientSocket.getOutputStream(), true);

        toConsole("Accepted connection from "
                 + clientSocket.getInetAddress() + " at port "
                 + clientSocket.getPort());
            
        sendGreeting();
    }
    
    // the initial message sent from server to client
    private static void sendGreeting()
    {
        appendOutput("Welcome to Qnet!\n");
        appendOutput("Enter username:");
        sendOutput();
    }
    
    // what happens while client and server are connected
    private static void talk() throws IOException {
        /* placing echo functionality into a separate private method allows it to be easily swapped for a different behaviour */
        verifyClient();
        //echoClient();
        followCommands();
        disconnect();
    }
    private static void verifyClient() throws IOException
    {
        String inputLine; int inputAttempt=0;
        while(!(inputLine=in.readLine()).equals("Sammy")){
            inputAttempt+=1;
            appendOutput("Username not recognised");
            if(inputAttempt == 5){
                disconnect();
            }
            appendOutput("Enter username:");
            sendOutput();
            toConsole(inputLine);
        }
        appendOutput("Enter password:");
        sendOutput();
        while(!(inputLine=in.readLine()).equals("woof")){
            inputAttempt+=1;
            appendOutput("Password not correct");
            if(inputAttempt == 5){
                disconnect();
            }
            appendOutput("Enter password:");
            sendOutput();
            toConsole(inputLine);
        }
        appendOutput("Welcome Sammy!");
        appendOutput("Enter Commands (add x/ remove/print): ");
        sendOutput();
    }
    private static void followCommands() throws IOException
    {
        Queue<String> queue = new LinkedList<>();
        String[] inputLine=in.readLine().split(" ");
        while(inputLine.length>0){
            if(inputLine.length>1 && inputLine[0].equals("add")){
                queue.add(inputLine[1]);
                appendOutput(inputLine[1] + " added to queue");
            }
            else if(inputLine.length==1 && inputLine[0].equals("add")){
                appendOutput("add what?");
            }
            else if(queue.size()==0){
                appendOutput("queue is empty!");
            }
            else if(inputLine[0].equals("print")){
                appendOutput(""+queue);
            }
            else if(inputLine[0].equals("remove")){
                appendOutput("Item removed from queue: "+queue.remove());
            }
            else{
                appendOutput("Command not recognized.");
            }
            appendOutput("Enter Commands (add x/ remove/print): ");
            sendOutput();
            inputLine=in.readLine().split(" ");
        }
    }
    // repeatedly take input from client and send back in upper case
    private static void echoClient() throws IOException
    {
        String inputLine;
        while ((inputLine = in.readLine()) != null) {
            appendOutput(inputLine.toUpperCase());
            sendOutput();
            toConsole(inputLine);
        }
    }
    
    private static void disconnect() throws IOException {
        out.close();
        toConsole("Disconnected.");
        System.exit(0);
    }
    
    // add a line to the next message to be sent to the client
    private static void appendOutput(String line) {
        output += line + "\r";
    }
    
    // send next message to client
    private static void sendOutput() {
        out.println( output + "[EOR]");
        out.flush();
        output = "";
    }
    
    // because it makes life easier!
    private static void toConsole(String message) {
        System.out.println(message);
    }
    
    public static void main(String[] args) {
        try {
            setup();
            talk();
        }
        catch( IOException ioex ) {
            toConsole("Error: " + ioex );
        }
    }
}
