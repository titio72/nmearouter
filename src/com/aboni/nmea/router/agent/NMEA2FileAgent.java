package com.aboni.nmea.router.agent;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import com.aboni.nmea.router.impl.NMEAAgentImpl;
import com.aboni.utils.ServerLog;

import net.sf.marineapi.nmea.sentence.Sentence;

public class NMEA2FileAgent extends NMEAAgentImpl {

	private class SentenceEvent {
		Sentence sentence;
		long time; 
	}

	private static final long DUMP_PERIOD = 10*1000;
	private SimpleDateFormat df = new SimpleDateFormat("yyyyMMdd");
	
	private long lastDump = 0;
	private List<SentenceEvent> queue = new LinkedList<>();
	
	public NMEA2FileAgent(String string, QOS q) {
		super("NMEA2FILE", q);
		setSourceTarget(false, true);
	}

	@Override
	public String getDescription() {
		return "";
	}

	@Override
	protected void doWithSentence(Sentence s, NMEAAgent source) {
		SentenceEvent e = new SentenceEvent();
		e.sentence = s;
		e.time = System.currentTimeMillis();
		synchronized (queue) {
			if (isStarted()) {
				queue.add(e);
				try {
					dump();
				} catch (IOException e1) {
					ServerLog.getLogger().Error("Error dumping NMEA stream", e1);
				}
			}
		}
	}

	private void dump() throws IOException {
		long t = System.currentTimeMillis();
		if (t-lastDump > DUMP_PERIOD) {
			lastDump = t;
			File f = new File("nmea" + df.format(new Date()) + ".log");
			FileWriter w = new FileWriter(f, true);
			BufferedWriter bw = new BufferedWriter(w);
			for (SentenceEvent e: queue) {
				bw.write("[" + e.time + "][**] " + e.sentence.toSentence() + "\n");
			}
			queue.clear();
			bw.flush();
			bw.close();
			w.close();
		}
	}
	
}
