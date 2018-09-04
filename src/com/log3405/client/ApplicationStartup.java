package com.log3405.client;

import java.io.IOException;
import java.util.Scanner;

public class ApplicationStartup {

	public static void main(String[] args) {
		Scanner consoleInputReader = new Scanner(System.in);

		String ipAddress = getIPAddress(consoleInputReader);
		int port = getPort(consoleInputReader);

		try {
			Client client = new Client(ipAddress, port);
			run(client);
		} catch (IOException e){
			e.printStackTrace();
		}

	}

	private static void run(Client client) throws IOException{
			boolean done = false;
			Scanner inputs = new Scanner(System.in);

			while(!done){
				System.out.println("Enter command: ");
				String command = inputs.nextLine();

				switch (command){
					case "FUCK YOU!":{
						client.close();
						done = true;
						break;
					}
					default:{
						//client.sendMessage(command);
						break;
					}
				}
			}
	}

	private static String getIPAddress(Scanner consoleInputReader) {
		System.out.println("Hello, Please Enter the IPAddress of the server");
		String ipAddress = consoleInputReader.nextLine();
		System.out.println("IPAddress is: " + ipAddress);
		return ipAddress;
	}

	private static int getPort(Scanner consoleInputReader) {
		System.out.println("Please Enter the Port of the server");
		int port = consoleInputReader.nextInt();
		System.out.println("Port is: " + port);
		return port;
	}
}
