package com.aboni.nmea.router.agent;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.LinkedList;
import java.util.List;

import com.aboni.utils.ServerLog;

public class SocketClient2 implements Runnable {
	
	private SocketChannel clientSocket;
	private List<String> queue;
	private boolean closed;
	private static long counter;
	private long id;
	
	public SocketClient2(SocketChannel socket) throws IOException {
		closed = false;
		counter++;
		id = counter;
		ServerLog.getLogger().Info("New Client attached {" + id + "} {" + socket.getRemoteAddress().toString() + "}");
		clientSocket = socket;
		queue = new LinkedList<String>();
	}
	
	
	private String pop() {
		synchronized(queue) {
			if (queue.size()>0) {
				String s = queue.get(0);
				queue.remove(0);
				return s;
			} else {
				try {
					queue.wait();
				} catch (InterruptedException e) {
					ServerLog.getLogger().Error("Error", e);
				}
				return pop();
			}
		}
	}
	
	public boolean isClosed() {
		return closed;
	}
	
	@Override
	public void run() {
		while (!closed) {
			String s = pop();
			doSentence(s);
		}
	}

	public void doClose() {
		ServerLog.getLogger().Info("Client dropped {" + id + "}");
		closed = true;
		try {
			clientSocket.close();
		} catch (IOException e) {
			ServerLog.getLogger().Warning("Error {" + e.getMessage() + "}");
		}
	}

	private void doSentence(String sentence) {
		try {
			int written = clientSocket.write(ByteBuffer.wrap((sentence + "\r\n").getBytes()));
			if (written==0) {
				ServerLog.getLogger().Warning("Couldn't write {" + sentence + "} to {" + id + "}" );
			}
		} catch (Exception e) {
			doClose();
		}
	}

	public void pushSentence(String sentence) {
		if (sentence!=null) {
			synchronized (queue) {
				queue.add(sentence);
				queue.notify();
			}
		}
	}
		
}
