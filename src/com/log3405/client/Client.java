package com.log3405.client;

import java.io.*;
import java.net.Socket;
import java.net.SocketException;
import java.util.Scanner;

public class Client {

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
				System.out.println("i am here");
				System.out.println(in.readUTF());//temp, replace by process pckt

				System.out.println("Write a command to continue (\"bye\" to exit)");
				String command = scanner.nextLine();
				byte[] datatoSend;

				//process command
				if ("bye".equals(command)) {
					out.write(command.getBytes());//send exit command to server, so he can abort too
					datatoSend = null;
				} else {
					datatoSend = command.getBytes();
				}

				//send data if any, else exit
				if (datatoSend == null) {
					socket.close();
					break;
				} else {
					out.write(datatoSend);
				}
			}
			close();
		} catch (
				SocketException s) {
			System.out.println("Something went wrong in the server, exiting client");
		}
	}

	private void close() throws IOException {
		in.close();
		out.close();
		scanner.close();
	}
}
