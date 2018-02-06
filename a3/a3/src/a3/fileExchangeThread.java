//package a3;

import java.io.*;
import java.net.*;

public class fileExchangeThread extends Thread {
	// uploader socket
	private Socket up;
	// downloader socket
	private Socket down;
	// uploader input stream
	private InputStream in;
	// output stream to downloader
	private DataOutputStream out;
	// server buffer to forward data
	private byte[] buff = new byte[1024];
	
	public fileExchangeThread(Socket upload, Socket download, InputStream in) {
		this.up = upload;
		this.down = download;
		this.in = in;
	}
	
	public void run() {
//		System.out.println("file exchange thread start");
		try {
			// open output stream to downloader in order to send file
			out = new DataOutputStream(down.getOutputStream());
//			out.write(data);
			
			// read from uploader into buffer
			int c=0;
			while((c=in.read(buff, 0, 1024)) != -1) {
				// write from buffer to downloader
				out.write(buff,0,c);
//				buff = new byte[1024];
			}
//			System.out.println("finish file transfer");
//			in.close();
			
			// close stream after forwarding data
			out.close();
		} catch (Exception e) {
			// if failed forward, exception
			e.printStackTrace(); 
		}
	}
}
