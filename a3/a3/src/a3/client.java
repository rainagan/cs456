
import java.io.*;
import java.net.*;
import java.nio.file.*;
import java.util.*;
import java.lang.*;
import java.util.concurrent.TimeUnit;

public class client {
	public static void main(String args[]) throws Exception {
		// host name
		String host;
		// port number
		int port;
		// clinet command
		String command;
		// filename to be read from/written to
		String filename;
		// downloader receive size
		int recvSize;
		// uploader wait time
		int waitTime;
		// uploader send size
		int sendSize;
		
		// check if input is valid
		if (args.length != 3 && args.length != 6 && args.length != 5) {
			System.out.println("incorrect number of arguments");
			System.exit(1);
		} else {
			host = args[0];
			port = Integer.parseInt(args[1]);
			command = args[2];

			// terminate server: client <host> <port> F
			if (args.length == 3 && command.substring(0, 1).equals("F")) {
//				System.out.println("terminate");
				
				// create tcp socket
				Socket sock = new Socket(host, port);
				// pad and send command
				for (int i = command.length(); i < 9; i++) {
					command += '\0';
				}
				DataOutputStream outToServer = new DataOutputStream(sock.getOutputStream());
				outToServer.writeBytes(command);
				// close stream and socket after sending command
				outToServer.close();
				sock.close();
			}
			// downlaod: client <host> <port> G<key> <file name> <recv size>
			else if (args.length == 5 && command.substring(0, 1).equals("G")) {
				filename = args[3];
				recvSize = Integer.parseInt(args[4]);
				
				// create tcp socket
				try {
					// connect to server
					Socket sock = new Socket(host, port);

					// pad and send command
					for (int i = command.length(); i < 9; i++) {
						command += '\0';
					}
					DataOutputStream outToServer = new DataOutputStream(sock.getOutputStream());
					InputStream stream = sock.getInputStream();
					BufferedReader inFromServer = new BufferedReader(new InputStreamReader(stream));

					// send command to server
					outToServer.writeBytes(command);
					// System.out.println("send G to server");
//					 System.out.println("download client send "+command.length()+" byte data");

					// receive data from server and write to file called <filename>
					byte[] buffer = new byte[recvSize];
					FileOutputStream fos = new FileOutputStream(filename);
					// BufferedWriter buff = new BufferedWriter(new FileWriter(filename));
					int c;
					while ((c = stream.read(buffer, 0, recvSize)) != -1) {
//						System.out.println("download "+c+" bytes data to "+filename);
						fos.write(buffer, 0, c);;
//						buffer = new byte[recvSize];
						// System.out.println("read from server "+(char) c);
					}
					
					// close stream and socket after finished reading
					fos.close();
					outToServer.close();
					// System.out.println("close stream");
					inFromServer.close();
					// System.out.println("close stream");
					sock.close();
					// System.out.println("close download sock");
				} catch (Exception e) {
					System.out.println("Downloader connection refused");
				}
			}
			
			// upload: client <host> <port> P<key> <file name> <send size> <wait time>
			else if (args.length == 6 && command.substring(0, 1).equals("P")) {
				filename = args[3];
				sendSize = Integer.parseInt(args[4]);
				waitTime = Integer.parseInt(args[5]);
				
				try {
					// create tcp socket and connect to server
					Socket sock = new Socket(host, port);
					
					// pad and send command
					for (int i = command.length(); i < 9; i++) {
						command += '\0';
					}
					DataOutputStream outToServer = new DataOutputStream(sock.getOutputStream());
					BufferedReader inFromServer = new BufferedReader(new InputStreamReader(sock.getInputStream()));
					outToServer.writeBytes(command);
					
					// read from file called <filename>
					String content, temp;
					byte[] slice = null;
					try {
						Path path = Paths.get(filename);
						byte[] file = Files.readAllBytes(path);
						
						// divide data into chuncks of recvSize
						int num = (int) Math.ceil((double) file.length / sendSize);
						int index = 0;
						for (int i=0; i<num-1; i++) {
							// read a chunk of recvSize except the last one and send to server 
							slice = Arrays.copyOfRange(file, i*sendSize, (i+1)*sendSize);
							outToServer.write(slice);
							// sleep a bit after send
							Thread.sleep(waitTime);
//							System.out.println("uploader send data from "+i*sendSize+" to "+(i+1)*sendSize +" from "+filename);
						}
						
						// read the final chunk and send to server
						slice = Arrays.copyOfRange(file, (num-1)* sendSize, file.length);
//						System.out.println("uploader send data from "+(num-1) * sendSize+" to "+file.length +" from "+filename);
						outToServer.write(slice);
						
						// close stream and socket after finished sending data
						outToServer.close();
						// System.out.println("close upload stream");
						sock.close();
						// System.out.println("close uploder");
					} catch (FileNotFoundException e) {
						e.printStackTrace();
					}
				} catch (Exception e) {
					System.out.println("Uploader connection refused");
				}
			} else {
				System.out.println("Incorrect argument");
				System.exit(1);
			}

		}
	}
}