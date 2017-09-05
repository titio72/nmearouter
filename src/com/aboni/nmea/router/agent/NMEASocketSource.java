package com.aboni.nmea.router.agent;

import java.io.InputStream;
import java.net.Socket;

import com.aboni.nmea.router.NMEACache;
import com.aboni.nmea.router.NMEAStream;
import com.aboni.nmea.router.agent.impl.NMEAAgentImpl;

import net.sf.marineapi.nmea.parser.SentenceFactory;
import net.sf.marineapi.nmea.sentence.Sentence;

public class NMEASocketSource extends NMEAAgentImpl {
	
	private class SocketReader implements Runnable {

	    private Socket socket;
	    private InputStream iStream;
	    private String server;
        private int port;
        private boolean stop;
        
        public SocketReader(String s, int p) {
            server = s;
            port = p;
            stop = false;
        }

	    public String getServer() {
            return server;
        }

        public int getPort() {
            return port;
        }
		
		public void stopMe() {
			stop = true;
			closeIt();
		}
		
		private void closeIt() {
			try {
				iStream = null;
				socket.close();
			} catch (Exception e) {}
		}
		
		private boolean openSocket() {
            try {
                getLogger().Info("Creating Socket {" + server + "} Speed {" + port + "}");
                iStream = null;
                socket = new Socket(server, port);
                iStream = socket.getInputStream();
                return true;
            } catch (Exception e1) {
                getLogger().Info("Error creting socket {" + e1.getMessage() + "}");
                try {Thread.sleep(5000);} catch (Exception e) {}
                return false;
            }
		}
		
		
		private void doRead() {
	    	StringBuffer b = new StringBuffer();

			while (!stop) {
			    if (openSocket()) {
			    	b.setLength(0);
                    boolean reset = false;
    				while (!stop && !reset) {
    					try {
    						int ch = iStream.read();
    						if (ch>=0) {
    		                    //connected(true);
    						    if (ch==13 || ch==10 || ch=='!' || ch=='$') {
    						    	if (b.length()>0) {
    						    		processSentence(b.toString());
        						    	b.setLength(0);
    						    	}
    						    	if (ch=='!' || ch=='$') {
        								b.append(new char[] {(char)ch});
    						    	}
    							} else {
    								b.append(new char[] {(char)ch});
    							}
    						} else {
    		                    getLogger().Debug("Socket likely closed");
    						    reset = true;
    						}
    					} catch (Exception e) {
		                    getLogger().Debug("Socket likely closed");
		                    reset = true;
    					}
    				}
    				closeIt();
			    }
			}
		}
		
		
		@Override
		public void run() {
			doRead();
			/*while (!stop) {
			    if (openSocket()) {
                    String temp = "";
    				while (!stop && iStream!=null) {
    					try {
    						int ch = iStream.read();
    						if (ch>=0) {
    						    if (ch==13 || ch==10) {
    							    processSentence(temp);
                                    temp = "";
    							} else {
    								temp += new String(new byte[] {(byte)ch});
    							}
    						} else {
    		                    getLogger().Debug("Socket likely closed");
    						}
    					} catch (Exception e) {
                            getLogger().Debug("Socket likely closed");
    					}
    				}
    				closeIt();
			    }
			}*/
		}
	}
	
	private void processSentence(String s) {
	    try {
            if (s.length()>0) {
                Sentence sentence = SentenceFactory.getInstance().createParser(s);
                sentenceRead(sentence);
            }
	    } catch (Exception e) {
	        if (s!=null && s.length()>256) s = s.substring(0, 255);
            getLogger().Debug("Cannot process sentence {" + s + "} msg {" + e.getMessage() + "}");
	    }
	}
	
	private SocketReader reader;
	
	public NMEASocketSource(NMEACache cache, NMEAStream stream, String name, String server, int port) {
	    this(cache, stream, name, server, port, null);
	}
	
	public NMEASocketSource(NMEACache cache, NMEAStream stream, String name, String server, int port, QOS qos) {
        super(cache, stream, name, qos);
        setSourceTarget(true, false);
		reader = new SocketReader(server, port);
	}
	
	@Override
	public String getDescription() {
		return "TCP " + reader.getServer() + ":" + reader.getPort();
	}
	
	@Override
	protected boolean onActivate() {
        Thread t = new Thread(reader);
        t.setDaemon(true);
        t.start();
        return true;
	}
	
	@Override
	protected void onDeactivate() {
		reader.stopMe();
	}

	private void sentenceRead(Sentence e) {
		notify(e);
	}
	
    @Override
    public String toString() {
        return super.toString() + " {Socket " + reader.getServer() + ":" + reader.getPort() + "}";
    }

    @Override
    protected void doWithSentence(Sentence s, NMEAAgent source) {
        // do nothing - pure source
    }
}