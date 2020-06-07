package com.aboni.nmea.router.agent.impl;

import com.aboni.nmea.router.NMEACache;
import com.aboni.nmea.router.agent.QOS;
import com.aboni.nmea.router.processors.CANBOATDecoder;
import net.sf.marineapi.nmea.parser.SentenceFactory;
import net.sf.marineapi.nmea.sentence.Sentence;
import org.json.JSONObject;

import javax.inject.Inject;
import javax.validation.constraints.NotNull;
import java.net.DatagramPacket;
import java.net.DatagramSocket;

public class NMEAUDPN2KReceiver extends NMEAAgentImpl {

    private DatagramSocket socket;
    private int port;
    private boolean stop;
    private boolean setup;
    private final CANBOATDecoder decoder;

    @Inject
    public NMEAUDPN2KReceiver(@NotNull NMEACache cache) {
        super(cache);
        setSourceTarget(true, false);
        decoder = new CANBOATDecoder();
    }

    public void setup(String name, QOS q, int port) {
        if (!setup) {
            setup = true;
            setup(name, q);
            this.port = port;
            getLogger().info(String.format("Setting up UDP receiver: Port {%d}", port));
        } else {
            getLogger().info("Cannot setup UDP receiver - already set up");
        }
    }

    @Override
    public String getDescription() {
        return "UDP Receiver " + port;
    }

    @Override
    protected boolean onActivate() {

	    synchronized (this) {
	        if (socket == null) {
    	        try {
                    socket = new DatagramSocket(port);
                    getLogger().info("Opened Datagram socket {" + port + "}");
                    
                    Thread t = new Thread(() -> {
						byte[] buffer = new byte[256];
						while (!stop) {
							try {
								DatagramPacket p = new DatagramPacket(buffer, 256);
								socket.receive(p);
								String sSentence = new String(p.getData(), 0, p.getLength());
								if (sSentence.startsWith("{\"timestamp\":\"")) {
                                    try {
                                        JSONObject m = new JSONObject(sSentence);
                                        Sentence s = decoder.getSentence(m);
                                        if (s != null) {
                                            onSentenceRead(s);
                                        }
                                    } catch (Exception e) {
                                        getLogger().debug("Can't read N2K sentence {" + sSentence + "} {" + e + "}");
                                    }
                                } else {
                                    Sentence s = SentenceFactory.getInstance().createParser(sSentence);
                                    onSentenceRead(s);
                                }
							} catch (Exception e) {
                                getLogger().warning("Error receiving sentence {" + e.getMessage() + "}");
                            }
						}
						socket.close();
					});
                    t.start();
                    
                    return true;
                } catch (Exception e) {
                    getLogger().error("Error initializing socket {" + port + "} ", e);
                    socket = null;
                }
            }
	    }
        return false;
	}
	
	@Override
	protected void onDeactivate() {
	    synchronized (this) {
	    	stop = true;
	    }
	}

	private void onSentenceRead(Sentence e) {
		notify(e);
	}
	
    @Override
    public String toString() {
        return " {UDP " + port + " R}";
    }

}