package com.log3405.client;

import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ApplicationStartup {

	/**
	 * Read the server IPAddress and port and start the client
	 * @param args (not used)
	 */
	public static void main(String[] args) {
		Scanner consoleInputReader = new Scanner(System.in);

		try {
			//Read from console
			InetAddress ipAddress = InetAddress.getByName(getIPAddress(consoleInputReader));
			System.out.println("IPAddress is: " + ipAddress);
			int port = getPort(consoleInputReader);
			System.out.println("Port is: " + port);
			Socket socket = new Socket(ipAddress, port);

			//Starts client
			new Client(socket, consoleInputReader).run();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Read the IpAddress written into the console. Also compares it to a RegEx to validate the IpAddress Format
	 * @param consoleInputReader Console Input Scanner
	 * @return a String representing the IpAddress
	 */
	private static String getIPAddress(Scanner consoleInputReader) {
		System.out.println("Hello, Please Enter the IPAddress of the server");
		String ipAddressPattern = "^(?:(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\.){3}(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)$";
		Pattern pattern = Pattern.compile(ipAddressPattern);
		String ipAddress = consoleInputReader.nextLine();
		Matcher matcher = pattern.matcher(ipAddress);
		if (!matcher.matches()) {
			System.out.println("Invalid IPAddress. Valid example: 192.168.1.1");
			ipAddress = getIPAddress(consoleInputReader);
		}
		return ipAddress;
	}

	/**
	 * Read the port written into the console. Also verifies it's between 5000 and 5050
	 * @param consoleInputReader Console Input Scanner
	 * @return a Integer representing the port
	 */
	private static int getPort(Scanner consoleInputReader) {
		System.out.println("Please Enter the Port of the server");
		int port = consoleInputReader.nextInt();
		if (port != 5000 && port != 5050) {
			System.out.println("Invalid port. Only ports between 5000 and 5050 are supported");
			port = getPort(consoleInputReader);
		}
		return port;
	}
}
