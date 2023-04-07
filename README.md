# multithreaded_ftp_client_server
This allows a server to connect and take requests from multiple clients at a time using multithreading.

A server-client program with multithreading typically consists of two components: a server and a client. The server component listens for incoming connections from clients and processes their requests, while the client component connects to the server and sends requests for data or other resources.

Multithreading is used to allow the server to handle multiple client connections concurrently. This is important because a single-threaded server can only handle one client at a time, which can cause a bottleneck and slow down the entire system. Multithreading enables the server to create multiple threads, each of which can handle a separate client connection.

In a multithreaded server, when a client connects, a new thread is created to handle that client's requests using a Handler class that extends thread. Each instance of this class is created to handle the requests of a single client. The Handler class has access to the input/output streams of the client's socket. The main thread continues to listen for incoming connections, and each new connection is assigned to a new thread. This allows the server to handle multiple clients simultaneously. 

Instructions:
1)	Unzip the files to local directory
2)	Open the command prompt and go to the local directory where project is unzipped.
3)	Start the server using the following command:
java server.java
4)	Open a new command prompt and navigate to local directory
5)	Start the client in multiple terminals and using the following command to start the client on each terminal:
java client.java


Server client Connection:  
The server will be started at localhost.  
The client will be started and connected to server once we run the command:  
ftpclient portNumber  
Get command:  
get <fileName> - this command gets the file from the server and downloads to local directory with new name.  
Upload command:  
upload <fileName> - this command uploads the file to the server from the local directory with new name.
Quit command:
The client can close the connection with server using this command.
