package com.aboni.utils;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.FileHandler;
import java.util.logging.Formatter;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

public class ServerLog implements Log {

    private class MyFormatter extends Formatter {

        DateFormat df;
        
        MyFormatter() {
            df = new SimpleDateFormat("dd-MM-yyyy hh:mm:ss.SSS");
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
	
    private Logger lg;
	
	public ServerLog() {
        lg = Logger.getLogger("NMEARouter");
        lg.setLevel(Level.INFO);
	    
	    FileHandler fh;  
	    try {  
            lg.setUseParentHandlers(false);

            fh = new FileHandler("NMEARouter.log");
            lg.addHandler(fh);

            //Formatter formatter = new SimpleFormatter();  
            Formatter formatter = new MyFormatter();  
	        fh.setFormatter(formatter);  
	    } catch (SecurityException e) {  
	        e.printStackTrace();  
	    } catch (IOException e) {  
	        e.printStackTrace();  
	    } 
	    
	}
	
	private static ServerLog logger = new ServerLog();
	
    public static Log getLogger() {
        return logger;
    }
    
    public static Logger getWebLogger() {
        return ServerLog.getWebLogger();
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
    
	public boolean isDebug() {
		return debug;
	}
	
	/* (non-Javadoc)
	 * @see com.aboni.utils.Log#Error(java.lang.String)
	 */
	@Override
	public void Error(String msg) {
		lg.log(Level.SEVERE, msg);
	}

	/* (non-Javadoc)
	 * @see com.aboni.utils.Log#Error(java.lang.String, java.lang.Throwable)
	 */
	@Override
	public void Error(String msg, Throwable t) {
		lg.log(Level.SEVERE, msg, t);
	}

	/* (non-Javadoc)
	 * @see com.aboni.utils.Log#Warning(java.lang.String)
	 */
	@Override
	public void Warning(String msg) {
		lg.log(Level.WARNING, msg);
	}

	/* (non-Javadoc)
	 * @see com.aboni.utils.Log#Info(java.lang.String)
	 */
	@Override
	public void Info(String msg) {
		lg.log(Level.INFO, msg);
	}
	
	/* (non-Javadoc)
	 * @see com.aboni.utils.Log#Debug(java.lang.String)
	 */
	@Override
	public void Debug(String msg) {
		lg.log(Level.FINER, msg);
	}

	@Override
	public Logger getBaseLogger() {
		return lg;
	}
}
