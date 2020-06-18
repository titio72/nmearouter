package com.aboni.nmea.router.agent.impl;

import com.aboni.misc.Utils;
import com.aboni.nmea.router.NMEACache;
import com.aboni.nmea.sentences.NMEASentenceItem;
import net.sf.marineapi.nmea.parser.SentenceFactory;
import net.sf.marineapi.nmea.sentence.Sentence;

import javax.inject.Inject;
import javax.validation.constraints.NotNull;
import java.io.BufferedReader;
import java.io.FileReader;

public class NMEAPlayer extends NMEAAgentImpl {

    private String file;

    @Inject
    public NMEAPlayer(@NotNull NMEACache cache) {
        super(cache);
        setSourceTarget(true, false);
    }

    @Override
    public String getDescription() {
        return "File " + getFile();
    }

    @Override
    public String getType() {
        return "NMEA log player";
    }

    @Override
    public String toString() {
        return getType();
    }


    public void setFile(String file) {
        this.file = file;
    }

    public String getFile() {
        return file;
    }

    @Override
    public void onDeactivate() {
        if (isStarted() && !stop)
			stop = true;
	}
	
	@Override
	public boolean onActivate() {
		if (file!=null) {
			Thread t = new Thread(this::go);
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
			try (FileReader fr = new FileReader(getFile())) {
				try (BufferedReader r = new BufferedReader(fr)) {
					String line;
					long logT0 = 0;
					long t0 = 0;
					while ((line = r.readLine()) != null) {
						if (line.startsWith("[")) {
                            long logT = readLineWithTimestamp(line, logT0, t0);
                            t0 = getCache().getNow();
                            logT0 = logT;
                        } else {
							readLine(line);
						}
					}
				}
			} catch (Exception e) {
				getLogger().error("Error playing file", e);
				Utils.pause(10000);
			}
		} 
		stop = false;
	}

	private void readLine(String line) {
		try {
			Sentence s = SentenceFactory.getInstance().createParser(line);
			Thread.sleep(55);
			notify(s);
		} catch (Exception e) {
			getLogger().error("Error playing sentence {" + line + "}", e);
		}
	}

	private long readLineWithTimestamp(String line, long logT0, long t0) {
		long logT = logT0;
		try {
            NMEASentenceItem itm = new NMEASentenceItem(line);
            long t = getCache().getNow();
            logT = itm.getTimestamp();
            long dt = t - t0;
            long dLogT = logT - logT0;
            if (dLogT > dt) {
                Utils.pause((int) (dLogT - dt));
            }
            notify(itm.getSentence());
        } catch (Exception e) {
			getLogger().error("Error playing sentence {" + line + "}", e);
		}
		return logT;
	}
}
