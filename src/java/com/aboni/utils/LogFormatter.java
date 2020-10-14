package com.aboni.utils;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.Formatter;
import java.util.logging.LogRecord;

public class LogFormatter extends Formatter {

    private final DateFormat df;

    public LogFormatter() {
        df = new SimpleDateFormat("HH:mm:ss.SSS");
    }

    @Override
    public String format(LogRecord record) {
        Date d = new Date(record.getMillis());
        String s = df.format(d) + " " + record.getLevel() + " " + record.getMessage() + "\n";
        if (record.getThrown() != null) {
            StringWriter sw = new StringWriter();
            PrintWriter pw = new PrintWriter(sw);
            record.getThrown().printStackTrace(pw);
            s += sw.toString() + "\n";
        }
        return s;
    }
}
