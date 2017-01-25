package com.aboni.nmea.router.agent;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import com.aboni.nmea.router.impl.NMEAAgentImpl;
import com.aboni.utils.ServerLog;

import net.sf.marineapi.nmea.sentence.Sentence;

public class NMEASocketTarget extends NMEAAgentImpl {

	private int port;
	private ServerSocket serverSocket;

	private static final int DEFAULT_PORT = 8888;
	
	public NMEASocketTarget(String name, int port, QOS q) {
		super(name, q);
		this.port = port;
        setSourceTarget(false, true);
		clients = new HashSet<NMEASocketClient>();
	}

	public NMEASocketTarget(String name) {
		this(name, DEFAULT_PORT, null);
	}

	public int getPort() {
		return port;
	}
	
	private Set<NMEASocketClient> clients;

	private void addClient(NMEASocketClient c) {
		synchronized (clients) {
			clients.add(c);
		}
	}
	
	@Override
	protected boolean onActivate() {
		try {
			serverSocket = new ServerSocket(port);
			new Thread(new Runnable() {
				@Override
				public void run() {
					while (true) {
						Socket s = null;
						try {
							s = serverSocket.accept();
							NMEASocketClient r = new NMEASocketClient(s);
							addClient(r);
							new Thread(r).start();
						} catch (IOException e) {
							ServerLog.getLogger().Error("Error", e);
						}
					}
				}}).start();
			return true;
		} catch (IOException e) {
			ServerLog.getLogger().equals("Cannot open port " + port);
		}
		return false;
	}
	
	@Override
	protected void onDeactivate() {
		try {
			synchronized (clients) {
				for (Iterator<NMEASocketClient> i = clients.iterator(); i.hasNext(); ) {
					i.next().doClose();
				}				
				serverSocket.close();
				purgeClients(new ArrayList<NMEASocketClient>(clients));
			}
		} catch (IOException e) {
			ServerLog.getLogger().equals("Cannot open port " + port);
		}
	}
	
	@Override
	protected void doWithSentence(Sentence s, NMEAAgent src) {
		synchronized (clients) {
			String output = null;
			List<NMEASocketClient> purge = new ArrayList<NMEASocketClient>();
			for (Iterator<NMEASocketClient> i = clients.iterator(); i.hasNext(); ) {
				try {
					if (output==null) {
						output = getOutSentence(s);
					}
					NMEASocketClient c = i.next();
					if (c.isClosed()) {
						purge.add(c);
					} else {
						String toSend = getOutSentence(s);
						if (toSend!=null) {
							c.pushSentence(output);
						}
					}
				} catch (Exception e) {
					getLogger().Error("Error dispatching sentence to socket!", e);
				}
			}
			purgeClients(purge);
		}
	}
	
	protected String getOutSentence(Sentence s) {
		String s1 = s.toSentence();
		return s1;
		//return s1.substring(0, s1.length()-2);
	}
	
	private void purgeClients(Collection<NMEASocketClient> purge) {
		for (Iterator<NMEASocketClient> i = purge.iterator(); i.hasNext(); ) {
			clients.remove(i.next());
		}
	}
}
