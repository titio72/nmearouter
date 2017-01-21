package com.aboni.utils;

import java.util.logging.Logger;

public interface Log {

    void setError();
    
    void setWarning();
    
    void setInfo();
    
    void setDebug();
    
    void setNone();
    
	void Error(String msg);

	void Error(String msg, Throwable t);

	void Warning(String msg);

	void Info(String msg);

	void Debug(String msg);

    Logger getBaseLogger();

}