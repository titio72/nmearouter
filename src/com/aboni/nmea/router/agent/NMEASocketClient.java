package com.aboni.nmea.router.agent;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.LinkedList;
import java.util.List;

import com.aboni.utils.ServerLog;

public class NMEASocketClient implements Runnable {
	
	private Socket clientSocket;
	private PrintWriter out;
	private List<String> queue;
	private boolean closed;
	private static long counter;
	private long id;
	
	public NMEASocketClient(Socket socket) {
		closed = false;
		counter++;
		id = counter;
		ServerLog.getLogger().Info("New Client attached {" + id + "} {" + socket.getRemoteSocketAddress().toString() + "}");
		clientSocket = socket;
		queue = new LinkedList<String>();
	    try {
			out = new PrintWriter(clientSocket.getOutputStream(), true);
		} catch (IOException e) {
			ServerLog.getLogger().Error("Error", e);
		}
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
		out.println(sentence);
		if (out.checkError()) {
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
