package com.aboni.nmea.router.agent.impl;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import com.aboni.nmea.router.NMEACache;

import com.aboni.nmea.router.agent.NMEAAgent;
import com.aboni.nmea.router.agent.QOS;
import com.aboni.utils.ServerLog;

import net.sf.marineapi.nmea.parser.SentenceFactory;
import net.sf.marineapi.nmea.sentence.Sentence;

public class NMEASocketServer extends NMEAAgentImpl {

	private int port;
	private Selector selector;
	private ServerSocketChannel serverSocket;
	private static final int DEFAULT_PORT = 1111;
	private final ByteBuffer writeBuffer = ByteBuffer.allocate(16384);
	private final ByteBuffer readBuffer = ByteBuffer.allocate(16384);
	private final Map<SocketChannel, ClientDescriptor> clients;
	
	private class ClientDescriptor {
		
		ClientDescriptor(String ip) {
			this.ip = ip;
		}
		
		final String ip;
		
		int errors = 0;
		
		@Override
		public String toString() {
			return ip;
		}
	}
	
	public NMEASocketServer(NMEACache cache, String name, int port, boolean allowReceive, boolean allowTransmit, QOS q) {
		super(cache, name, q);
		this.port = port;
        setSourceTarget(allowReceive, allowTransmit);
        
        clients = new HashMap<>();
	}
	
	public NMEASocketServer(NMEACache cache, String name, int port, QOS q) {
		this(cache, name, port, false, true, q);
	}

	public NMEASocketServer(NMEACache cache, String name) {
		this(cache, name, DEFAULT_PORT, null);
	}

	public int getPort() {
		return port;
	}
	

    @Override
    public String getType() {
    	return "TCP NMEA Server";
    }

	
	@Override
	public String getDescription() {
		return "Port " + getPort();
	}
	
	@Override
	protected boolean onActivate() {
		createServerSocket();
		startServer();
		return true;
	}

	private boolean isSelectorOpen() {
		synchronized (selector) {
			return selector.isOpen();
		}
	}
	
	private void startServer() {
		Thread t = new Thread(new Runnable() {
			@Override
			public void run() {
		        if (selector!=null && serverSocket!=null) {
		        	while (isSelectorOpen()) {
		        		try {
							selector.select();
						} catch (IOException e) {
	                		getLogger().Error("Unexpected exception", e);
						}
			            Iterator<SelectionKey> iter = selector.selectedKeys().iterator();
			            while (iter.hasNext()) {
			                SelectionKey ky = iter.next();
			                if (ky.isValid()) {
				                if (ky.isAcceptable()) {
				                	handleConnection();
				                } else if (ky.isReadable()) {
				                	handleRead(ky);
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
	}

	private void handleRead(SelectionKey ky) {
		try {
			SocketChannel client = (SocketChannel) ky.channel();
			readBuffer.clear();
			try {
				int readBytes = client.read(readBuffer);
				if (readBytes>2) {
					String sentence = new String(readBuffer.array(), 0, readBytes).trim();
					NMEASocketServer.this.notify(SentenceFactory.getInstance().createParser(sentence));
				} else if (readBytes==0) {
		    		handleDisconnection(client);
				}
			} catch (IOException e) {
				handleDisconnection(client);
			}
		} catch (Exception ee) {
			getLogger().Error("Error reading socket", ee);
		}
	}

	private void handleDisconnection(SocketChannel client) {
		try {
			getLogger().Info("Disconnection {" + clients.getOrDefault(client, null) + "} Agent {" + getName() + "}");
			synchronized (clients) {
				clients.remove(client);
			}
			client.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void handleConnection() {
		try {
		    SocketChannel client = serverSocket.accept();
		    	
		    client.configureBlocking(false);
		    client.register(selector, SelectionKey.OP_READ);
		    synchronized (clients) {
			    clients.put(client, new ClientDescriptor(client.getRemoteAddress().toString()));
			}
			getLogger().Info("Connecting {" + client.getRemoteAddress() + "} Agent {" + getName() + "}");

		} catch (Exception ee) {
			getLogger().Error("Error accepting connection", ee);
		}
	}

	@Override
	protected void onDeactivate() {
		synchronized (clients) {
			for (SocketChannel c: clients.keySet()) {
				try {c.close();} catch (Exception e) {}
			}
			clients.clear();
			try {serverSocket.close();} catch (Exception e) {}
			try {selector.close();} catch (Exception e) {}
		}
	}
	
	@Override
	protected void doWithSentence(Sentence s, NMEAAgent src) {
		synchronized (clients) {
		    if (isTarget()) {
    			if (!clients.isEmpty()) {
    				String output = getOutSentence(s);
    				writeBuffer.clear();
    				writeBuffer.put(output.getBytes());
    				writeBuffer.put("\r\n".getBytes());
    				int p = writeBuffer.position();
    				Iterator<Entry<SocketChannel, ClientDescriptor>> iter = clients.entrySet().iterator();
    				while (iter.hasNext()) {
    					Entry<SocketChannel, ClientDescriptor> itm = iter.next();
    					SocketChannel sc = itm.getKey();
    					ClientDescriptor cd = itm.getValue();
    					if (!sendMessageToClient(output, p, sc, cd)) {
    						iter.remove();
    					}
    				}
    			}
		    }
		}
	}

	private boolean sendMessageToClient(String output, int p, SocketChannel sc, ClientDescriptor cd) {
		writeBuffer.position(0);
		writeBuffer.limit(p);
		try {
			int written = sc.write(writeBuffer);
			if (written==0) {
				cd.errors++;
				ServerLog.getLogger().Warning("Couldn't write {" + output + "} to {" + sc.getRemoteAddress() + "} p {" + p + "} e {" + cd.errors + "}" );
			} else {
				cd.errors = 0;
			}
			return cd.errors < 10 /* allow a max of 10 failure, then close the channel */;
		} catch (IOException e) {
			try { 
				getLogger().Info("Disconnection {" + sc.getRemoteAddress() + "} "
						+ "Agent {" + getName() + "} Reason {" + e.getMessage() + "}");
				sc.close(); 
			} catch (IOException e1) {}
		} catch (Exception e) {
			ServerLog.getLogger().Error("Error sending {" + output + "} to client", e);
		}
		return false;
	}
	
	protected String getOutSentence(Sentence s) {
		return s.toSentence();
	}
	
	private void createServerSocket() {
		try {
		    selector = Selector.open();
		    getLogger().Info("Selector Open {" + selector.isOpen() + "} Agent {" + getName() + "}");
		    serverSocket = ServerSocketChannel.open();
		    InetSocketAddress hostAddress = new InetSocketAddress(getPort());
		    serverSocket.bind(hostAddress);
		    serverSocket.configureBlocking(false);
		    int ops = serverSocket.validOps();
		    serverSocket.register(selector, ops, null);
		} catch (Exception e) {
			getLogger().Error("Cannot open server socket", e);
		}
	}
    
    @Override
    public String toString() {
        return "{TCP " + port+ " " + (isSource() ? "R" : "")
                + (isTarget() ? "X" : "") + "}";
    }
}
