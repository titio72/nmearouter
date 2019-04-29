package com.aboni.utils;

public interface Log {

	void Error(String msg);

	void Error(String msg, Throwable t);

	void Warning(String msg);

	void Info(String msg);

	void Debug(String msg);

}