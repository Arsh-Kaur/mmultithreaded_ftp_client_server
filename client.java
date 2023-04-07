import java.net.*;
import java.io.*;
import java.util.*;

public class client {
    public static Integer portNumber;
    public static void main(String[] args) throws Exception {
        // using this as separate method so as to allow client to retsart in case of ConnectException/ UnknownHostException
        startClient();
    }

    public static void startClient() {
        // define input/output streams
        Socket requestSocket = null;
        DataOutputStream out = null; 
        DataInputStream in = null; 

        try{
            BufferedReader userInputReader = new BufferedReader(new InputStreamReader(System.in));
            String connectionCommand;
            boolean validPort = false;

                //this block checks if the user enters correct command to connect to server. 
                while(!validPort){
                    System.out.println("To connect to a server at port, enter 'ftpclient <port>'");
                    connectionCommand = userInputReader.readLine();
                    try{
                        if(connectionCommand.split(" ")[0].equals("ftpclient")){
                            portNumber = Integer.parseInt(connectionCommand.split(" ")[1]);
                            validPort = true;
                        }
                    }catch(Exception e){
                        System.out.println("Invalid command");
                    }         
                }
                
                // connect client to server at localhost and portnumber mentioned by user
                requestSocket = new Socket("localhost", portNumber);
                System.out.println("Connected to localhost in port " + portNumber);

                // initialize input/output streams
                out = new DataOutputStream(requestSocket.getOutputStream());
                in = new DataInputStream(requestSocket.getInputStream());
                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(System.in));
                
                while(true)
                {
                    System.out.print("Enter the command for server(get/upload/quit): ");
                    String userInput = bufferedReader.readLine();

                    //client can disconnect from the server with quit command, the socket is closed
                    if (userInput == null || userInput.equalsIgnoreCase("quit")) {
                        out.writeUTF("quit");
                        requestSocket.close();
                        System.out.println("FTP client stopped");
                        return;
                    }

                    String[] inputTokens = userInput.split(" ");
                    
                    // get - to download files from server
                    // upload - to upload files to server

                    switch (inputTokens[0]) {
                        case "get":
                            out.writeUTF("get " + inputTokens[1]);
                            String response = in.readUTF();

                            // start downloading file once client received OK from the server
                            if(response.startsWith("FILE_OK")){
                                long file_size = Long.parseLong(response.split(" ")[1]);
                                String filename = inputTokens[1];
                                FileOutputStream fos = new FileOutputStream("new"+filename);
                                byte[] buffer = new byte[1024];
                                int bytesRead;

                                //buffer is an array with a length of 1024, 
                                //in each pass, the code reads 1024 bytes from the inputFile
                                while (file_size > 0 && (bytesRead = in.read(buffer, 0, (int) Math.min(buffer.length, file_size))) != -1) {
                                    fos.write(buffer, 0, bytesRead);
                                    file_size -=bytesRead;
                                }

                                fos.close();
                                System.out.println("File " + filename + " received from server");
                            } else if(response.equals("GET ERROR")){
                                System.out.println("ERROR: File not found at server");
                            }else {
                                System.out.println("\nUnable to get file ");
                            }

                            break;

                        case "upload":
                            String filename2 = inputTokens[1];
                            File file2 = new File(filename2);

                            if (file2.exists() && !file2.isDirectory()) {
                                // if file exits at client to upload to the server, inform server to be ready to start receiving file
                                out.writeUTF("upload " + filename2);
                                String response2 = in.readUTF();
                                
                                //once server sends acknowledgement, start sending the file in chunks
                                if (response2.equals("UPLOAD_OK")){
                                    out.writeUTF("l " + file2.length());        // mention file size to server 
                                    FileInputStream fis = new FileInputStream(file2);

                                    byte[] buffer2 = new byte[1024];
                                    int bytesRead2;

                                    //buffer is an array with a length of 1024, 
                                    //in each pass, the code reads 1024 bytes from the inputFile
                                    while ((bytesRead2 = fis.read(buffer2)) > 0) {
                                        out.write(buffer2, 0, bytesRead2);
                                    }

                                    fis.close();
                                    System.out.println("File " + filename2 + " sent to server");
                                }
                                
                            } else {
                                System.out.println("ERROR: File not found at client to upload");
                            }

                            break;

                        default:
                            System.out.println("Invalid Command");
                            break;
                    }
                }
            }
            //start client again in case of any connection exception
            catch (ConnectException e) {
                System.err.println("Connection refused. No server running at this port");
                startClient();
            }
            catch(UnknownHostException unknownHost){
                System.err.println("You are trying to connect to an unknown host!");
                startClient();
            }
            catch(IOException ioException){
                System.err.println("io exception at client!");
            }
            finally{
                try{
                    if(requestSocket !=null)
                        requestSocket.close();
                }
                catch(IOException ioException){
                    System.err.println("io exception at client closing connections!");
                }
            }
    }
}