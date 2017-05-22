package com.aboni.nmea.router.agent;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import com.aboni.nmea.router.impl.NMEAAgentImpl;
import com.aboni.nmea.sentences.NMEASentenceItem;

import net.sf.marineapi.nmea.parser.SentenceFactory;
import net.sf.marineapi.nmea.sentence.Sentence;

public class NMEAPlayer extends NMEAAgentImpl {

	private String file;

	public NMEAPlayer(String name, QOS qos) {
		super(name, qos);
		setSourceTarget(true, false);
	}

	@Override
	public String getDescription() {
		return "Fille " + getFile();
	}
	
	
	public void setFile(String file) {
		this.file = file;
	}
	
	public String getFile() {
		return file;
	}
	
	@Override
	protected void doWithSentence(Sentence s, NMEAAgent source) {
	}
	
	@Override 
	public void onDeactivate() {
		if (isStarted() && stop==false)
			stop = true;
	}
	
	@Override
	public boolean onActivate() {
		if (file!=null) {
			Thread t = new Thread(new Runnable() {
				
				@Override
				public void run() {
					go();
					
				}
			});
			t.setDaemon(true);
			t.start();
			return true;
		} else {
			return false;
		}
	}

	private boolean stop = false;
	
	private void go() {
		while (!stop) {
			try {
				FileReader fr = new FileReader(getFile());
				BufferedReader r = new BufferedReader(fr);
				String line = null;
				long log_t0 = 0;
				long t0 = 0;
				while ((line=r.readLine())!=null) {
					if (line.startsWith("[")) {
						try {
							NMEASentenceItem itm = new NMEASentenceItem(line);
							long t = System.currentTimeMillis();
							long log_t = itm.getTimestamp();
							long dt = t-t0;
							long dLog_t = log_t - log_t0;
							if (dLog_t>dt) {
								try { Thread.sleep(dLog_t-dt); } catch (Exception pp) {}
							}
							notify(itm.getSentence());
							t0 = System.currentTimeMillis();
							log_t0 = log_t;
						} catch (Exception e) {
							getLogger().Error("Error playing sentence {" + line + "}", e);
						}
					} else {
						try {
							Sentence s = SentenceFactory.getInstance().createParser(line);
							Thread.sleep(55);
							notify(s);
						} catch (Exception e) {
							getLogger().Error("Error playing sentence {" + line + "}", e);
						}
					}
				}
				r.close();
				fr.close();
			} catch (Exception e) {
				getLogger().Error("Error playing file", e);
				try {
					Thread.sleep(10000);
				} catch (InterruptedException e1) {}
			}
		} 
		stop = false;
	}
	
	private static ServerSocket serverSocket;
	private static Set<Socket> clients = new HashSet<Socket>();
	
	
	private static void send(String s) {
		try {
			synchronized (clients) {
				for (Iterator<Socket> i = clients.iterator(); i.hasNext(); ) {
					Socket c = i.next();
					if (c.isClosed()) {
						i.remove();
					} else {
						try {
							c.getOutputStream().write((s+"\r\n").getBytes());
						} catch (Exception e) {
							try {
								c.close();
							} catch (Exception ee) {}
							i.remove();
						}
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private static void startServer() {
			try {
				serverSocket = new ServerSocket(1111);
				new Thread(new Runnable() {
					@Override
					public void run() {
						while (true) {
							Socket s = null;
							try {
								s = serverSocket.accept();
								synchronized (clients) {
									clients.add(s);
								}
							} catch (IOException e) {
								e.printStackTrace();
							}
						}
					}}).start();
			} catch (IOException e) {
				e.printStackTrace();
			}
	}
	
	public static void main(String[] args) {
		startServer();
		while (true) {
			try {
				FileReader fr = new FileReader(args[0]);
				BufferedReader r = new BufferedReader(fr);
				String line = null;
				long t0 = System.currentTimeMillis();
				long log_t0 = 0;
				while ((line=r.readLine())!=null) {
					try {
						if (line.startsWith("[")) {
							try {
								NMEASentenceItem itm = new NMEASentenceItem(line);
								long t = System.currentTimeMillis();
								long log_t = itm.getTimestamp();
								long dt = t-t0;
								long dLog_t = log_t - log_t0;
								if (dLog_t>dt && log_t0!=0) {
									try { Thread.sleep(dLog_t-dt); } catch (Exception pp) {}
								}
								send(itm.getString());
								t0 = System.currentTimeMillis();
								log_t0 = log_t;
							} catch (Exception e) {
								e.printStackTrace();
							}
						} else {
							try {
								Sentence s = SentenceFactory.getInstance().createParser(line);
								Thread.sleep(55);
								send(s.toSentence());
							} catch (Exception e) {
								e.printStackTrace();
							}
						}
						//System.out.println(line);
						//send(line);
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
				r.close();
				fr.close();
			} catch (Exception e) {
				e.printStackTrace();
				try {
					Thread.sleep(10000);
				} catch (InterruptedException e1) {}
			}
		} 
	}
	
}
