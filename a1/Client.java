import java.io.*;
import java.net.*;
import java.util.Scanner;

class Client {
  public static void main(String args[]) throws Exception {
        // stage 1

        // initialize input parameters
    String server_address = args[0];
    String str_n_port = args[1];
    int n_port = Integer.parseInt(str_n_port);
    String str_req_code = args[2];
    int req_code = Integer.parseInt(str_req_code);
    String msg = args[3];

      // create a client UDP socket
    DatagramSocket clientSocket = new DatagramSocket();
    InetAddress IPAddress = InetAddress.getByName(server_address);

      // allocate bytes to send and receive data
    byte[] sendData = new byte[1024];
    byte[] receiveData = new byte[1024];

      // send req_code to server using UDP socket
    sendData = str_req_code.getBytes();
    DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, IPAddress, n_port);
    clientSocket.send(sendPacket);

      // if req_code correct, receive r_port
    DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
    clientSocket.receive(receivePacket);
    String receive_s_r_port = new String(receivePacket.getData()).trim();
    int receive_r_port = Integer.parseInt(receive_s_r_port);
        // System.out.println("receive r_port " + receive_s_r_port);

      // send received r_port for confrimation
    sendData = receive_s_r_port.getBytes();
    sendPacket = new DatagramPacket(sendData, sendData.length, IPAddress, n_port);
    clientSocket.send(sendPacket);

      // receive serer's acknowledgement and then close
    receiveData = new byte[1024];
    receivePacket = new DatagramPacket(receiveData, receiveData.length);
    clientSocket.receive(receivePacket);
    String acknowledgement = new String(receivePacket.getData()).trim();
    // System.out.println("acknowledgement="+acknowledgement);
    if (acknowledgement.equals("ok")) {
      // System.out.println("enter stage 2");
      clientSocket.close();

        // stage 2
        // System.out.println("now in stage 2");
      // create TCP connection on the given r_port
      Socket clientTCPSocket = new Socket(server_address, receive_r_port);
      DataOutputStream outToServer = new DataOutputStream(clientTCPSocket.getOutputStream());
      BufferedReader inFromServer = new BufferedReader(new InputStreamReader(clientTCPSocket.getInputStream()));

      // send a string to server for modification
      outToServer.writeBytes(msg + '\n');

      // receive the modified string from server and then close
      String modifiedSentence = inFromServer.readLine();
      System.out.println("FROM SERVER: " + modifiedSentence);
      clientTCPSocket.close();
    } else {
      clientSocket.close();
    }
  }
}