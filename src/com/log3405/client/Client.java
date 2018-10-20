package com.log3405.client;

import java.io.*;
import java.net.Socket;
import java.net.SocketException;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.Scanner;
import java.util.stream.Stream;

public class Client {
	private final static int MAX_BUFFER_SIZE = 1024;

	private Socket socket;
	private DataInputStream in;
	private DataOutputStream out;
	private Scanner scanner;

	public Client(Socket socket, Scanner scanner) throws IOException {
		this.socket = socket;
		this.in = new DataInputStream(socket.getInputStream());
		this.out = new DataOutputStream(socket.getOutputStream());
		this.scanner = scanner;
	}

	public void run() throws IOException {
		try {
			while (true) {
				//handle data sent from server and print to console
				byte[] starter = readBytes(in);
				System.out.println(new String(starter).trim());

				String userInput = "";
				while (userInput.trim().isEmpty()) {
					userInput = scanner.nextLine();
				}

				String action[] = userInput.split(" ", 2);
				String inputPayload = "";
				String inputType = action[0];
				if(action.length > 1) {
					inputPayload = action[1];
				}

				if ("cd".equals(inputType)) {
					byte[] packetType = intToBytes(0);
					byte[] packetPayload = inputPayload.getBytes();
					byte[] finalPacket = concat(packetType,packetPayload);

					writeBytes(finalPacket);
				}
				else if ("ls".equals(inputType)) {
					byte[] packetType = intToBytes(1);
					byte[] packetPayload = inputPayload.getBytes();
					byte[] finalPacket = concat(packetType,packetPayload);

					writeBytes(finalPacket);
				}
				else if ("mkdir".equals(inputType)) {
					byte[] packetType = intToBytes(2);
					byte[] packetPayload = inputPayload.getBytes();
					byte[] finalPacket = concat(packetType,packetPayload);

					writeBytes(finalPacket);
				}
				else if ("upload".equals(inputType)) {
					byte[] packetType = intToBytes(3);
					byte[] packetPayload = inputPayload.getBytes(); // TODO: GET FILE FROM PATH
					byte[] finalPacket = concat(packetType,packetPayload);

					writeBytes(finalPacket);
					// TODO: SEND THE FILE PROPERLY
				}
				else if ("download".equals(inputType)) {
					byte[] packetType = intToBytes(4);
					byte[] packetPayload = inputPayload.getBytes();
					byte[] finalPacket = concat(packetType,packetPayload);

					writeBytes(finalPacket);
					// TODO: DOWNLOAD THE FILE
				}
				else if ("exit".equals(inputType)) {
					byte[] packetType = intToBytes(5);
					byte[] packetPayload = inputPayload.getBytes();
					byte[] finalPacket = concat(packetType,packetPayload);

					writeBytes(finalPacket);

					// Wait for response from server before closing
					byte[] starterExit = readBytes(in);
					System.out.println(new String(starterExit).trim());
					break;
				} else {
					// UNKNOWN COMMAND
					writeBytes("Unknown command".getBytes());
				}
			}
		} catch (
				SocketException s) {
			socket.close();
			System.out.println("Something went wrong in the server, exiting client");
		} finally {
			close();
		}
	}

	private void close() throws IOException {
		socket.close();
		in.close();
		out.close();
		scanner.close();
	}

	private byte[] readBytes(InputStream in) throws IOException {
		byte[] data = new byte[MAX_BUFFER_SIZE];

		in.read(data, 0, data.length);

		return data;
	}

	private void writeBytes(byte[] data) throws IOException {
		out.write(data, 0, data.length);
	}

	public byte[] intToBytes( final int i ) {
		ByteBuffer bb = ByteBuffer.allocate(4);
		bb.putInt(i);
		return bb.array();
	}

	public byte[] concat(byte[] a, byte[] b) {
		byte[] c = new byte[a.length + b.length];
		System.arraycopy(a, 0, c, 0, a.length);
		System.arraycopy(b, 0, c, a.length, b.length);
		return c;
	}
}
