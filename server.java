import java.net.*;
import java.io.*;
import java.nio.*;
import java.nio.channels.*;
import java.util.*;
public class server {
    //server port number defined as final variable
    private static final int sPort = 1007; 

    public static void main(String[] args) throws Exception {
        
        //start the server at given port
        ServerSocket listener = new ServerSocket(sPort);
        System.out.println("The server is running at port "+ sPort);

        //we keep track of the number of clients connected
        int clientNum = 1;
        try {
            while(true) {
                //server starts listeding and accepting the client connections
                Socket socket = listener.accept();
                //we need to spawn new threads for each new client connection
                new Handler(socket,clientNum).start();
                System.out.println("Client " + clientNum + " is connected at port "+ socket.getPort());
                clientNum++;
            }
        } 
        catch(Exception ex){
            System.out.println("FTP server error: "+ ex.getMessage());
        }
        finally {
            listener.close();
        }
    }

    /*
    * A handler thread class. Handlers are spawned from the listening
    * loop and are responsible for dealing with a single client's requests.
    * (One thread per client)
    */
    private static class Handler extends Thread {
        
        // define input/output streams
        private Socket connection;
        private DataInputStream in; 
        private DataOutputStream out; 

        private int clientIndex; //client index
        private String command;
        private int clientPort;

        //handler constructor
        public Handler(Socket connection, int clientIndex) {
            this.connection = connection;
            this.clientIndex = clientIndex;
            this.clientPort =  connection.getPort();
        }

        public void run() {
            try{
                
                out = new DataOutputStream(connection.getOutputStream());
                in = new DataInputStream(connection.getInputStream());

                try{
                    while(true)
                    {
                        String command = in.readUTF();      // get the command from the client

                        //if user exits from a client, disconnect client and return, server keeps listening for new connections
                        if (command == null || command.equalsIgnoreCase("quit")) {
                            connection.close();
                            System.out.println("Client " + clientIndex + " disconnected at port "+ clientPort);
                            return;
                        }

                        
                        // get - to download files from server
                        // upload - to upload files to server

                        switch (command.split(" ")[0]) {
                        case "get":
                            String filename = command.split(" ")[1];
                            File file = new File(filename);
                            byte[] buffer = new byte[1024];
                            int bytesRead;
                            if (file.exists() && !file.isDirectory() ) {
                                // if file exists at server, inform client to get ready and start receiving the file
                                out.writeUTF("FILE_OK "+ file.length());
                                FileInputStream fis = new FileInputStream(file);
                                
                                //buffer is an array with a length of 1024, 
                                //in each pass, the code reads 1024 bytes from the inputFile
                                while ((bytesRead = fis.read(buffer)) > 0 ) {           //(bytesRead = fis.read(buffer)) != -1
                                    out.write(buffer,0,bytesRead);                      //out.write(Arrays.copyOf(buffer, bytesRead));
                                }

                                fis.close();
                                System.out.println("File " + filename + " sent to client " + clientIndex + " at port "+ clientPort);
                                
                            } else {
                                out.writeUTF("GET ERROR");
                                System.out.println("ERROR: File not found at server");
                            }
                            break;

                        case "upload":
                            String filename2 = command.split(" ")[1];
                            // inform client that the server is ready to start receiving the file
                            // server will get the file size from client as well
                            out.writeUTF("UPLOAD_OK");
                            String response = in.readUTF();
                            long file_size = Long.parseLong(response.split(" ")[1]);

                            byte[] buffer2 = new byte[1024];
                            int bytesRead2;
                            FileOutputStream fos = new FileOutputStream(new File("new"+filename2));  

                            //buffer is an array with a length of 1024, 
                            //in each pass, the code reads 1024 bytes from the inputFile                  
                            while (file_size > 0 && (bytesRead2 = in.read(buffer2, 0, (int) Math.min(buffer2.length, file_size))) != -1) {
                                fos.write(buffer2, 0, bytesRead2);
                                file_size -=bytesRead2;
                            }
                            fos.close();

                            System.out.println("File " + filename2 + " received from client " + clientIndex + " at port "+ clientPort);
                            break;

                        default:
                            out.writeUTF("Invalid command");
                            break;
                        }
                    }
                }
                catch(Exception ex){
                    System.err.println(ex.getMessage());
                }
            }
            catch(IOException ioException){
                System.out.println("Disconnect with Client " + clientIndex + " at port "+ clientPort);
            }
            finally{
                //Close connections
                try{
                    connection.close();
                }
                catch(Exception ex){
                    System.out.println("Disconnect with Client " + clientIndex + " at port "+ clientPort);
                }
            }
        }
    }
}