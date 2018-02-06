//package a2;

import java.nio.ByteBuffer;

public class Packet {
	/* 0 indicate data packet
	 * 1 indicate ACK packet
	 * 2 indicate EOT packet
	 */
	private static long type;
	// species total length of the packet in bytes, including 
	// the packet header. 
	// for ACK and EOT packets, the size of the packet is just
	// the size of the header
	private static long length;
	/* for data packets, the sequence number range is [0...255]
	 * for ACK packets, sequence number is the sequence number 
	 * of the packet being acknowledged
	 */
	private static long seqNumber;
	private static byte[] payload;
	
	Packet (int t, int sn, byte[] p) throws Exception {
		if (p.length > 500) {
			throw new Exception("Exceed maximum payload length.");
		} else {
			type = unsignedInt(t);
			length = unsignedInt(p.length+12);
			seqNumber = unsignedInt(sn % 256);
			payload = p;
		}
	}
	
	// function that cast int to unsigned int
	public long unsignedInt(int n) {
		return n&0xffffffffl;
	}
	
	public long getType() {
		return type;
	}
	public long getLength() {
		return length;
	}
	public long getSeqNum() {
		return seqNumber;
	}
	public byte[] getPayload() {
		return payload;
	}
	
	// create 3 types of packets
	public static Packet dataPacket (int sn, byte[] p) throws Exception {
		return new Packet(0,sn,p);
	}
	public static Packet ACKPacket (int sn) throws Exception {
		return new Packet(1,sn,new byte[0]);
	}
	public static Packet EOTPacket (int sn) throws Exception {
		return new Packet(2,sn,new byte[0]);
	}
	
	// retrieve data from byte array
	public static Packet byteToPacket(byte[] b) throws Exception {
		ByteBuffer wrapped = ByteBuffer.wrap(b);
		int type = wrapped.getInt();
		int length = wrapped.getInt();
		int seqNumber = wrapped.getInt();
		// type, length and seqNumber are all 32-bit unsigned int, so in total 12 bytes
		byte[] payload = new byte[length-12];
		wrapped.get(payload, 0, length-12);
		Packet p = new Packet(type,seqNumber,payload);
		return p;
	}
	// put data into byte array
	public byte[] PacketToByte() throws Exception {
		// 12 byte of type, length, seqNumber and 500 byte of payload
		ByteBuffer buff = ByteBuffer.allocate((int)length);
		buff.putInt((int)type);
		buff.putInt((int)length);
		buff.putInt((int)seqNumber);
		buff.put(payload,0,payload.length);
		return buff.array();
	}
}
