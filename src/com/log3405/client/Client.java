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

	public Client(Socket socket, Scanner scanner) throws IOException {
		this.socket = socket;
		this.in = new DataInputStream(socket.getInputStream());
		this.out = new DataOutputStream(socket.getOutputStream());
		this.scanner = scanner;

		byte[] starter = BytesUtils.readBytes(in, MAX_BUFFER_SIZE, false);
		System.out.println(BytesUtils.bytesToString(starter));
	}

	public void run() throws IOException {
		try {
			exec:
			while (true) {
				String userInput = "";
				while (userInput.trim().isEmpty()) {
					userInput = scanner.nextLine();
				}

				String action[] = userInput.split(" ", 2);
				String inputPayload = "";
				String inputType = action[0];
				if (action.length > 1) {
					inputPayload = action[1];
				}

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
					case "upload": {
						byte[] packetType = BytesUtils.intToBytes(PacketTypeCode.UPLOAD.ordinal());
						File fileToUpload = new File(inputPayload);
						byte[] filePayload = Files.readAllBytes(fileToUpload.toPath());
						byte[] fileLengthPayload = BytesUtils.intToBytes(filePayload.length);
						byte[] fileNamePayload = fileToUpload.getName().getBytes();
						byte[] tempPacket = BytesUtils.concat(packetType, fileLengthPayload);
						byte[] finalPacket = BytesUtils.concat(tempPacket, fileNamePayload);

						BytesUtils.writeBytes(out, finalPacket);
						System.out.println("Server: " + BytesUtils
								.bytesToString(BytesUtils.readBytes(in, MAX_BUFFER_SIZE, false)));//read until server is ready to handle the file
						BytesUtils.writeBytes(out, filePayload);
						logResponse();
						break;
					}
					case "download": {
						byte[] requestPacket = processBasePacket(PacketTypeCode.DOWNLOAD.ordinal(), inputPayload);
						BytesUtils.writeBytes(out, requestPacket);

						byte[] response = BytesUtils.readBytes(in, MAX_BUFFER_SIZE, false);
						int fileStatus = BytesUtils.bytesToInt(BytesUtils.extractSubByteArray(response, 0, 4));
						if (fileStatus == 0) {
							int downloadSize = BytesUtils.bytesToInt(BytesUtils.extractSubByteArray(response, 4, 8));
							String fileName = BytesUtils.bytesToString(BytesUtils.extractSubByteArray(response, 8, response.length));
							String messageMidUpload = "Ready to read:" + downloadSize + " bytes";
							BytesUtils.writeBytes(out, messageMidUpload.getBytes());

							byte[] file = BytesUtils.readBytes(in, downloadSize, true);
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
							String errorMsg = BytesUtils.bytesToString(BytesUtils.extractSubByteArray(response, 8, response.length));
							System.out.println(errorMsg);
						}
						break;
					}
					case "exit": {
						byte[] finalPacket = processBasePacket(PacketTypeCode.EXIT.ordinal(), inputPayload);
						BytesUtils.writeBytes(out, finalPacket);
						logResponse();
						break exec;
					}
					default: {
						// UNKNOWN COMMAND
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

	private byte[] processBasePacket(int typeCode, String inputPayload) {
		byte[] packetType = BytesUtils.intToBytes(typeCode);
		byte[] packetPayload = inputPayload.getBytes();
		return BytesUtils.concat(packetType, packetPayload);
	}

	private void logResponse() throws IOException {
		byte[] starter = BytesUtils.readBytes(in, MAX_BUFFER_SIZE, false);
		System.out.println(BytesUtils.bytesToString(starter));
	}

	private void close() throws IOException {
		socket.close();
		in.close();
		out.close();
		scanner.close();
	}
}
