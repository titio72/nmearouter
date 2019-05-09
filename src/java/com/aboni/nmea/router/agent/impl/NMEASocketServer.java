package com.aboni.nmea.router.agent.impl;

import com.aboni.nmea.router.NMEACache;
import com.aboni.nmea.router.agent.NMEAAgent;
import com.aboni.nmea.router.agent.QOS;
import net.sf.marineapi.nmea.parser.SentenceFactory;
import net.sf.marineapi.nmea.sentence.Sentence;

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

public class NMEASocketServer extends NMEAAgentImpl {

	private final int port;
	private Selector selector;
	private ServerSocketChannel serverSocket;
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
		return selector.isOpen();
	}
	
	private void startServer() {
		Thread t = new Thread(() -> {
			if (selector!=null && serverSocket!=null) {
				while (isSelectorOpen()) {
					try {
						selector.select();
						for (SelectionKey ky: selector.selectedKeys()) { handleSelectionKey(ky); }
						selector.selectedKeys().clear();
					} catch (IOException e) {
						getLogger().error("Unexpected exception", e);
					}
				}
			}
		});
		t.setDaemon(true);
		t.start();
	}

	private void handleSelectionKey(SelectionKey ky) {
		if (ky.isValid()) {
			if (ky.isAcceptable()) {
				handleConnection();
			} else if (ky.isReadable()) {
				handleRead(ky);
			}
		}
	}

	private void handleRead(SelectionKey ky) {
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
	}

	private void handleDisconnection(SocketChannel client) {
		try {
			getLogger().info("Disconnection {" + clients.getOrDefault(client, null) + "}");
			synchronized (clients) {
				clients.remove(client);
			}
			client.close();
		} catch (Exception e) {
			getLogger().error("Error accepting disconnection", e);
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
			getLogger().info("Connecting {" + client.getRemoteAddress() + "}");

		} catch (Exception ee) {
			getLogger().error("Error accepting connection", ee);
		}
	}

	@Override
	protected void onDeactivate() {
		synchronized (clients) {
			for (SocketChannel c: clients.keySet()) {
				try {
					c.close();
				} catch (Exception e) {
					getLogger().error("Error trying to close socket with client", e);
				}
			}
			clients.clear();
			try {
				serverSocket.close();
			} catch (Exception e) {
				getLogger().error("Error trying to close server socket", e);
			}
			try {
				selector.close();
			} catch (Exception e)
			{
				getLogger().error("Error trying to close selector", e);
			}
		}
	}
	
	@Override
	protected void doWithSentence(Sentence s, NMEAAgent src) {
		synchronized (clients) {
		    if (isTarget() && !clients.isEmpty()) {
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

	private boolean sendMessageToClient(String output, int p, SocketChannel sc, ClientDescriptor cd) {
		writeBuffer.position(0);
		writeBuffer.limit(p);
		try {
			int written = sc.write(writeBuffer);
			if (written==0) {
				cd.errors++;
				getLogger().warning("Couldn't write {" + output + "} to {" + sc.getRemoteAddress() + "} p {" + p + "} e {" + cd.errors + "}" );
			} else {
				cd.errors = 0;
			}
			return cd.errors < 10 /* allow a max of 10 failure, then close the channel */;
		} catch (IOException e) {
			try { 
				getLogger().info("Disconnection {" + sc.getRemoteAddress() + "} "
						+ "Agent {" + getName() + "} Reason {" + e.getMessage() + "}");
				sc.close(); 
			} catch (IOException e1) {
				getLogger().error("Error closing socket with client", e1);
			}
		} catch (Exception e) {
			getLogger().error("Error sending {" + output + "} to client", e);
		}
		return false;
	}
	
	protected String getOutSentence(Sentence s) {
		return s.toSentence();
	}
	
	private void createServerSocket() {
		try {
		    selector = Selector.open();
		    getLogger().info("Selector Open {" + selector.isOpen() + "}");
		    serverSocket = ServerSocketChannel.open();
		    InetSocketAddress hostAddress = new InetSocketAddress(getPort());
		    serverSocket.bind(hostAddress);
		    serverSocket.configureBlocking(false);
		    int ops = serverSocket.validOps();
		    serverSocket.register(selector, ops, null);
		} catch (Exception e) {
			getLogger().error("Cannot open server socket", e);
		}
	}
    
    @Override
    public String toString() {
        return "{TCP " + port+ " " + (isSource() ? "R" : "")
                + (isTarget() ? "X" : "") + "}";
    }
}
