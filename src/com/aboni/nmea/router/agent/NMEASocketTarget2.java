package com.aboni.nmea.router.agent;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import com.aboni.nmea.router.NMEACache;
import com.aboni.nmea.router.NMEAStream;
import com.aboni.nmea.router.agent.impl.NMEAAgentImpl;
import com.aboni.utils.ServerLog;

import net.sf.marineapi.nmea.parser.SentenceFactory;
import net.sf.marineapi.nmea.sentence.Sentence;

public class NMEASocketTarget2 extends NMEAAgentImpl {

	private int port;
	private Selector selector;
	private ServerSocketChannel serverSocket;
	private boolean stopme;
	private static final int DEFAULT_PORT = 8888;
	
	public NMEASocketTarget2(NMEACache cache, NMEAStream stream, String name, int port, QOS q) {
		super(cache, stream, name, q);
		this.port = port;
        setSourceTarget(true, true);
        
        clients = new HashSet<SocketClient2>();
	}

	public NMEASocketTarget2(NMEACache cache, NMEAStream stream, String name) {
		this(cache, stream, name, DEFAULT_PORT, null);
	}

	public int getPort() {
		return port;
	}
	
	private Set<SocketClient2> clients;

	private void addClient(SocketClient2 c) {
		synchronized (clients) {
			clients.add(c);
		}
	}
	
	@Override
	public String getDescription() {
		return "TCP Port " + getPort();
	}

	private boolean isStopme() {
		synchronized  (this) {
			return stopme;
		}
	}
	
	private void setStopme(boolean b) {
		synchronized (this) {
			stopme = b;
		}
	}
	
	@Override
	protected boolean onActivate() {
		createServerSocket();
		Thread t = new Thread(new Runnable() {
			@Override
			public void run() {
				ByteBuffer b = ByteBuffer.allocate(65536);
		        if (selector!=null && serverSocket!=null) {
		        	while (!isStopme()) {
			            Set<SelectionKey> selectedKeys = selector.selectedKeys();
			            Iterator<SelectionKey> iter = selectedKeys.iterator();
			            while (iter.hasNext()) {
			                SelectionKey ky = iter.next();
			                if (ky.isAcceptable()) {
			                	try {
				                    SocketChannel client = serverSocket.accept();
				                    client.configureBlocking(false);
				                    client.register(selector, SelectionKey.OP_READ);
									SocketClient2 r = new SocketClient2(client);
									addClient(r);
									new Thread(r).start();
			                	} catch (Exception ee) {
			                		getLogger().Error("Error accepting connection", ee);
			                	}
			                }
			                if (ky.isReadable()) {
			                	try {
			                		SocketChannel client = (SocketChannel) ky.channel();
			                		int readBytes = client.read(b);
			                		if (readBytes>2) {
			                			String sentence = new String(b.array(), 0, readBytes).trim();
			                			NMEASocketTarget2.this.notify(SentenceFactory.getInstance().createParser(sentence));
			                		}
			                	} catch (Exception ee) {
			                		getLogger().Error("Error accepting connection", ee);
			                	}
			                }
			                iter.remove();
			            }
		        	}
		        }
			}
		});
		t.setDaemon(true);
		t.start();
		return true;
	}

	@Override
	protected void onDeactivate() {
		setStopme(true);
		try {
			synchronized (clients) {
				for (Iterator<SocketClient2> i = clients.iterator(); i.hasNext(); ) {
					i.next().doClose();
				}				
				purgeClients(new ArrayList<SocketClient2>(clients));
			}
			serverSocket.close();
			selector.close();
		} catch (IOException e) {
			ServerLog.getLogger().Error("Cannot open port " + port);
		}
		setStopme(false);
	}
	
	@Override
	protected void doWithSentence(Sentence s, NMEAAgent src) {
		synchronized (clients) {
			if (!clients.isEmpty()) {
				String output = getOutSentence(s);
				List<SocketClient2> purge = new ArrayList<SocketClient2>();
				for (Iterator<SocketClient2> i = clients.iterator(); i.hasNext(); ) {
					SocketClient2 c = i.next();
					try {
						if (c.isClosed()) {
							purge.add(c);
						} else {
							c.pushSentence(output);
						}
					} catch (Exception e) {
						getLogger().Error("Error dispatching sentence to socket!", e);
					}
				}
				purgeClients(purge);
			}
		}
	}
	
	protected String getOutSentence(Sentence s) {
		return s.toSentence();
	}
	
	private void purgeClients(Collection<SocketClient2> purge) {
		for (Iterator<SocketClient2> i = purge.iterator(); i.hasNext(); ) {
			clients.remove(i.next());
		}
	}

	private void createServerSocket() {
		try {
		    selector = Selector.open();
		    getLogger().Info("Selector Open {" + selector.isOpen() + "} Agent {" + getName() + "}");
		    serverSocket = ServerSocketChannel.open();
		    InetSocketAddress hostAddress = new InetSocketAddress("localhost", getPort());
		    serverSocket.bind(hostAddress);
		    serverSocket.configureBlocking(false);
		    int ops = serverSocket.validOps();
		    serverSocket.register(selector, ops, null);
		} catch (Exception e) {
			getLogger().Error("Cannot open server socket", e);
		}
	}
}
