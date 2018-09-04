package com.log3405.client;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.util.Iterator;
import java.util.Scanner;

public class Client {
	private Selector selector;
	private SocketChannel socket;
	private ByteBuffer buffer;

	public Client(String ipAddress, int port) throws IOException {
		selector = Selector.open();
		socket = SocketChannel.open(new InetSocketAddress(ipAddress, port));
		socket.configureBlocking(false);
		buffer = ByteBuffer.allocate(1024);
		socket.register(selector, SelectionKey.OP_CONNECT);
	}

	public void run() throws IOException{
		while (true) {
			int readyChannels = selector.select();
			if(readyChannels == 0) continue;

			Iterator keys = selector.selectedKeys().iterator();
			while (keys.hasNext()) {
				SelectionKey key = (SelectionKey) keys.next();
				keys.remove();//prevent a key from being handled twice

				if (!key.isValid()) {
					continue;
				}
				if (key.isConnectable()) {
					finishConnect(key);
				}
			}
		}
	}

	private void finishConnect(SelectionKey key) throws IOException{
		SocketChannel socketChannel = (SocketChannel) key.channel();
		try {
			socketChannel.finishConnect();
		}catch (IOException e){
			e.printStackTrace();
			key.cancel();
			return;
		}
		socketChannel.register(selector, SelectionKey.OP_WRITE);
	}

	public void sendMessage(String command) throws IOException {
		buffer = ByteBuffer.wrap(command.getBytes());

	}

	public void close() throws IOException {
		socket.close();
	}
}
