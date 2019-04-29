package com.aboni.utils;

import java.util.logging.Logger;

public interface LogAdmin extends Log {

    void setError();
    
    void setWarning();
    
    void setInfo();
    
    void setDebug();
    
    void setNone();

    Logger getBaseLogger();

}