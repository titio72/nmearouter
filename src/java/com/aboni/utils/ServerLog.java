package com.aboni.utils;

import com.aboni.nmea.router.Constants;

import java.io.IOException;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.*;

public class ServerLog implements LogAdmin {

    private static final PrintStream CONSOLE = System.out;

    public static PrintStream getConsoleOut() {
        return CONSOLE;
    }

    private static class MyFormatter extends Formatter {

        final DateFormat df;

        MyFormatter() {
            df = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss.SSS");
        }
        
        @Override
        public String format(LogRecord record) {
            Date d = new Date(record.getMillis());
            String s = df.format(d) + " " + record.getLevel() + " " + record.getMessage() + "\n";
            if (record.getThrown()!=null) {
            	StringWriter sw = new StringWriter();
            	PrintWriter pw = new PrintWriter(sw);
            	record.getThrown().printStackTrace(pw);
            	s += sw.toString() + "\n";
            }
            return s;
        }
    }
    
	private boolean debug = false;
	
    private final Logger lg;
	
	private final Logger lgConsole;

	private ServerLog() {
        lg = Logger.getLogger("NMEARouter");
        lg.setLevel(Level.INFO);
	    
	    FileHandler fh;  
	    try {  
            lg.setUseParentHandlers(false);

            fh = new FileHandler(Constants.LOG, 0, 1, true);
            lg.addHandler(fh);

            Formatter formatter = new MyFormatter();
	        fh.setFormatter(formatter);  
	    } catch (SecurityException | IOException e) {
	        Logger.getGlobal().log(Level.SEVERE, "Error", e);
	    }

		lgConsole = Logger.getLogger("NMEAConsole");
		lgConsole.setLevel(Level.INFO);
		lgConsole.setUseParentHandlers(false);
		ConsoleHandler c = new ConsoleHandler();
		lgConsole.addHandler(c);
		c.setFormatter(new Formatter() {
			@Override
			public String format(LogRecord logRecord) {
				return logRecord.getMessage() + "\n";
			}
		});
	}
	
	private static final ServerLog logger = new ServerLog();

	public static Log getLogger() {
		return getLoggerAdmin();
	}

	public static LogAdmin getLoggerAdmin() {
		return logger;
	}

    @Override
    public void setDebug() {
        lg.setLevel(Level.FINEST);
        debug = true;
    }
    
    @Override
    public void setError() {
        lg.setLevel(Level.SEVERE);
        debug = false;
    }
    
    @Override
    public void setWarning() {
        lg.setLevel(Level.WARNING);
        debug = false;
    }
    
    @Override
    public void setNone() {
        lg.setLevel(Level.OFF);
        debug = false;
    }
    
    @Override
    public void setInfo() {
        lg.setLevel(Level.INFO);
        debug = false;
    }

	@Override
	public void error(String msg) {
		lg.log(Level.SEVERE, msg);
	}

	@Override
	public void error(final String msg, final Throwable t) {
		if (debug)
			lg.log(Level.SEVERE, msg, t);
		else
			lg.severe( () -> String.format("{%s} error {%s}", msg, t.getMessage()) );
	}

	@Override
	public void errorForceStacktrace(final String msg, final Throwable t) {
		lg.log(Level.SEVERE, msg, t);
	}

	@Override
	public void warning(String msg) {
		lg.log(Level.WARNING, msg);
	}

	@Override
	public void info(String msg) {
		lg.log(Level.INFO, msg);
	}

	@Override
	public void debug(String msg) {
		lg.log(Level.FINER, msg);
	}

	@Override
	public void console(String msg) {
		lgConsole.log(Level.INFO, msg);
	}

	@Override
	public Logger getBaseLogger() {
		return lg;
	}
}
