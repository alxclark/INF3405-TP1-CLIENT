package com.log3405.client;

import java.io.*;
import java.net.Socket;
import java.net.SocketException;
import java.util.Scanner;

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
				System.out.println(new String(starter));

				String command = "";
				while (command.trim().isEmpty()) {
					command = scanner.nextLine();
				}
				byte[] dataToSend;

				//process command
				if ("bye".equals(command)) {
					byte[] exitCommand = command.getBytes();
					writeBytes(exitCommand);//send exit command to server, so he can abort too
					socket.close();
					System.out.println("Exit command, closing client");
					dataToSend = null;
				} else {
					dataToSend = command.getBytes();
				}

				//send data if any, else exit
				if (dataToSend == null) {
					break;
				} else {
					writeBytes(dataToSend);
				}

				//read server response
				byte[] response = readBytes(in);
				System.out.println(new String(response));
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
}
