package com.log3405.client;

import java.io.*;
import java.net.Socket;
import java.net.SocketException;
import java.nio.file.Files;
import java.util.Scanner;

public class Client {
	private final static int MAX_BUFFER_SIZE = 1024;

	private Socket socket;
	private DataInputStream in;
	private DataOutputStream out;
	private Scanner scanner;

	/**
	 * Setup streams used for communication and awaits a message from the server
	 * @param socket
	 * @param scanner
	 * @throws IOException
	 */
	public Client(Socket socket, Scanner scanner) throws IOException {
		this.socket = socket;
		this.in = new DataInputStream(socket.getInputStream());
		this.out = new DataOutputStream(socket.getOutputStream());
		this.scanner = scanner;

		//awaits a "ready" message from the server
		byte[] starter = BytesUtils.readBytes(in, MAX_BUFFER_SIZE, false);
		System.out.println(BytesUtils.bytesToString(starter));
	}

	/**
	 * main loop of the client
	 * @throws IOException
	 */
	public void run() throws IOException {
		try {
			exec:
			while (true) {
				//reads the console input from the client
				String userInput = "";
				while (userInput.trim().isEmpty()) {
					userInput = scanner.nextLine();
				}

				//split the command and the argument
				String action[] = userInput.split(" ", 2);
				String inputPayload = "";
				String inputType = action[0];
				if (action.length > 1) {
					inputPayload = action[1];
				}

				//For cd, ls, mkdir, exit; the pipeline is: Build a packet with the command, send the packet, await the response from the server and log it
				switch (inputType) {
					case "cd": {
						byte[] finalPacket = processBasePacket(PacketTypeCode.CD.ordinal(), inputPayload);
						BytesUtils.writeBytes(out, finalPacket);
						logResponse();
						break;
					}
					case "ls": {
						byte[] finalPacket = processBasePacket(PacketTypeCode.LS.ordinal(), inputPayload);
						BytesUtils.writeBytes(out, finalPacket);
						logResponse();
						break;
					}
					case "mkdir": {
						byte[] finalPacket = processBasePacket(PacketTypeCode.MKDIR.ordinal(), inputPayload);
						BytesUtils.writeBytes(out, finalPacket);
						logResponse();
						break;
					}
					//the upload and download operation needs a more complex pipeline
					case "upload": {
						//Send a packet with the file structure
						//0 to 4: Command Type Code
						//4 to 8: Number of bytes of the file to upload
						//8 to end: Name of the file to upload
						byte[] packetType = BytesUtils.intToBytes(PacketTypeCode.UPLOAD.ordinal());
						File fileToUpload = new File(inputPayload);
						byte[] filePayload = Files.readAllBytes(fileToUpload.toPath());
						byte[] fileLengthPayload = BytesUtils.intToBytes(filePayload.length);
						byte[] fileNamePayload = fileToUpload.getName().getBytes();
						byte[] tempPacket = BytesUtils.concat(packetType, fileLengthPayload);
						byte[] finalPacket = BytesUtils.concat(tempPacket, fileNamePayload);
						BytesUtils.writeBytes(out, finalPacket);

						//read until server is ready to handle the file
						System.out.println("Server: " + BytesUtils.bytesToString(BytesUtils.readBytes(in, MAX_BUFFER_SIZE, false)));

						//send a packet containing only the file's bytes
						BytesUtils.writeBytes(out, filePayload);

						//read server response
						logResponse();
						break;
					}
					case "download": {
						//send a normal packet with the command and it's argument
						byte[] requestPacket = processBasePacket(PacketTypeCode.DOWNLOAD.ordinal(), inputPayload);
						BytesUtils.writeBytes(out, requestPacket);

						//Read the packet sent from the server
						//0 to 4: Status code for the file (0 = normal), (1 = error)
						byte[] response = BytesUtils.readBytes(in, MAX_BUFFER_SIZE, false);
						int fileStatus = BytesUtils.bytesToInt(BytesUtils.extractSubByteArray(response, 0, 4));

						if (fileStatus == 0) {
							//4 to 8: Length in bytes of the file
							//8 to end: Length of the file name
							int downloadSize = BytesUtils.bytesToInt(BytesUtils.extractSubByteArray(response, 4, 8));
							String fileName = BytesUtils.bytesToString(BytesUtils.extractSubByteArray(response, 8, response.length));
							String messageMidUpload = "Ready to read:" + downloadSize + " bytes";

							//send a message to the server, to warn that the client is ready to receive the file
							BytesUtils.writeBytes(out, messageMidUpload.getBytes());

							//read file as bytes
							byte[] file = BytesUtils.readBytes(in, downloadSize, true);

							//write the bytes to the file
							File newFile = new File(fileName);
							try (FileOutputStream fos = new FileOutputStream(newFile.getPath())) {
								if (!newFile.exists()) {
									newFile.createNewFile();
								}
								fos.write(file);
								System.out.println("Fichier téléchargé avec sucess. Path: " + newFile.getCanonicalPath());
							} catch (IOException e) {
								System.out.println("Une erreur est arrivee lors du telechargement du fichier");
							}
						} else {
							//4 to end: error message
							String errorMsg = BytesUtils.bytesToString(BytesUtils.extractSubByteArray(response, 4, response.length));
							System.out.println(errorMsg);
						}
						break;
					}
					case "exit": {
						byte[] finalPacket = processBasePacket(PacketTypeCode.EXIT.ordinal(), inputPayload);
						BytesUtils.writeBytes(out, finalPacket);
						logResponse();

						//break the main loop and execute the close function
						break exec;
					}
					default: {
						// UNKNOWN COMMAND, do not communicate with the server, ans starts reading from the console again
						System.out.println("Unknown command. Possible commands are : ls, cd, mkdir, upload, download, exit");
					}
				}
			}
		} catch (SocketException s) {
			socket.close();
			System.out.println("Something went wrong in the server, exiting client");
		} finally {
			close();
		}
	}

	/**
	 * Builds a packet from the data to send
	 * General structure of packet made by this function:
	 * 0 to 4: Command Type Code
	 * 4 to end: data for the operation (named payload)
	 *
	 * @param typeCode Command type Code of the Packet
	 * @param inputPayload Command Argument to append into the packet
	 * @return The packet as a byte array
	 */
	private byte[] processBasePacket(int typeCode, String inputPayload) {
		byte[] packetType = BytesUtils.intToBytes(typeCode);
		byte[] packetPayload = inputPayload.getBytes();
		return BytesUtils.concat(packetType, packetPayload);
	}

	/**
	 * Read the byte array sent from the server and logs to the console
	 * @throws IOException
	 */
	private void logResponse() throws IOException {
		byte[] starter = BytesUtils.readBytes(in, MAX_BUFFER_SIZE, false);
		System.out.println(BytesUtils.bytesToString(starter));
	}

	/**
	 * Close the streams, the socket and the console scanner
	 * @throws IOException
	 */
	private void close() throws IOException {
		socket.close();
		in.close();
		out.close();
		scanner.close();
	}
}
