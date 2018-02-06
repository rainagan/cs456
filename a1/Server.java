import java.io.*;
import java.net.*;
import java.util.Random;
import java.lang.*;
import java.util.Scanner;


class Server {
	public static void main(String args[]) throws Exception {
		// stage 1
		// initialize inout parameter
		String str_req_code = args[0];
		int req_code = Integer.parseInt(str_req_code);
		
		// allocate a free port number and create server UDP with that port number
		ServerSocket UDPsock = new ServerSocket(0);
		int n_port = UDPsock.getLocalPort();
		DatagramSocket serverSocket = new DatagramSocket(n_port);
		System.out.println("SERVER_PORT="+n_port);
		
		// create bytes to send and receive data
		byte[] receiveData = new byte[1024];
		byte[] sendData = new byte[1024];
		while(true) {
			// receive client's req_code
			DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
			// System.out.println("haven't receive req_code");
			serverSocket.receive(receivePacket);
			String received_s_req_code = new String(receivePacket.getData()).trim();
			// System.out.println("received req_code=" + received_s_req_code);
			int received_req_code = Integer.parseInt(received_s_req_code);
			InetAddress IPAddress = receivePacket.getAddress();
			int port = receivePacket.getPort();

			// if receive correct req_code
			if (received_req_code == req_code) {
				// this automatically allocate a free port and print it on the screen
				ServerSocket TCPsock = new ServerSocket(0);
				int r_port = TCPsock.getLocalPort();
				System.out.println("SERVER_TCP_PORT=" + r_port);

				// send r_port back to client
				String s_r_port = Integer.toString(r_port);
				sendData = s_r_port.getBytes();
				DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, IPAddress, port);
				serverSocket.send(sendPacket);
				// System.out.println("send r_port " + s_r_port);
				
				// receive client's confirmation of r_port
				receiveData = new byte[1024];
				serverSocket.receive(receivePacket);
				String s_confirm = new String(receivePacket.getData()).trim();
				int confirm = Integer.parseInt(s_confirm);
				// System.out.println("receive confirmation " + s_confirm);


				// if received r_port equals allocated r_port, send ok; otherwise send no
				String acknowledge;
				if (confirm == r_port) {
					acknowledge = "ok";
					// System.out.println("req_code ok");
				} else {
					acknowledge = "no";
					// System.out.println("req_code not ok");
				}
				sendData = acknowledge.getBytes();
				sendPacket = new DatagramPacket(sendData, sendData.length, IPAddress, port);
				serverSocket.send(sendPacket);

				// stage 2
				String clientSentence;
				String reversedSentence;
				
				// System.out.println("now in stage 2");
				Socket connectionSocket = TCPsock.accept();

				// create a TCP connection
				BufferedReader inFromClient = new BufferedReader(new InputStreamReader(connectionSocket.getInputStream ()));
				DataOutputStream outToClient = new DataOutputStream(connectionSocket.getOutputStream());

				// receive client's string and reverse it
				clientSentence = inFromClient.readLine();
				System.out.println("SERVER_RCV_MSG=" + clientSentence);
				StringBuilder input_s = new StringBuilder();
				input_s.append(clientSentence);
				input_s = input_s.reverse();

				// send back the modified string
				reversedSentence = input_s.toString() + '\n';
				outToClient.writeBytes(reversedSentence);
				// }
			}
		}
	}
}