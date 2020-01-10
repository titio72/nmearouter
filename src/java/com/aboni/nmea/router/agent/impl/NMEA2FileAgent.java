package com.aboni.nmea.router.agent.impl;

import com.aboni.nmea.router.NMEACache;
import com.aboni.nmea.router.agent.QOS;
import com.aboni.nmea.sentences.NMEASentenceItem;
import com.aboni.utils.ServerLog;
import net.sf.marineapi.nmea.sentence.Sentence;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

public class NMEA2FileAgent extends NMEAAgentImpl {

	private static final long DUMP_PERIOD = 10L * 1000L;
	private final SimpleDateFormat df = new SimpleDateFormat("yyyyMMdd");
	
	private long lastDump = 0;
	private final List<NMEASentenceItem> queue = new LinkedList<>();
	
	public NMEA2FileAgent(NMEACache cache, String n, QOS q) {
		super(cache, n, q);
		setSourceTarget(false, true);
	}

	@Override
	public String getDescription() {
		return "Dump the NMEA stream to file";
	}

	@Override
	public String getType() {
		return "StreamDump";
	}

	@Override
	protected void doWithSentence(Sentence s, String source) {
		NMEASentenceItem e = new NMEASentenceItem(s, System.currentTimeMillis(), "  ");
		synchronized (queue) {
			if (isStarted()) {
				queue.add(e);
				try {
					dump();
				} catch (IOException e1) {
					ServerLog.getLogger().error("Error dumping NMEA stream", e1);
				}
			}
		}
	}

	private void dump() throws IOException {
		long t = System.currentTimeMillis();
		if (t-lastDump > DUMP_PERIOD) {
			getLogger().debug("Dumping NMEA log at {" + new Date() + "}");
			lastDump = t;
			File f = new File("nmea" + df.format(new Date()) + ".log");
			FileWriter w = new FileWriter(f, true);
			try (BufferedWriter bw = new BufferedWriter(w)) {
				for (NMEASentenceItem e : queue) {
					bw.write(e.toString());
					bw.write("\n");
				}
				queue.clear();
				bw.flush();
			}
			w.close();
		}
	}
	
}
