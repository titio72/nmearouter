package com.aboni.sensors;


import com.pi4j.io.i2c.I2CBus;
import com.pi4j.io.i2c.I2CDevice;
import com.pi4j.io.i2c.I2CFactory;
import com.pi4j.io.i2c.I2CFactory.UnsupportedBusNumberException;

import java.io.IOException;

public class I2CInterface {

	public enum Endianness {
		LITTLE_ENDIAN,
		BIG_ENDIAN
	}

	private final I2CDevice device;

	public I2CInterface(int busId, int deviceAddr) throws IOException, UnsupportedBusNumberException {
		I2CBus bus = I2CFactory.getInstance(busId);
		device = bus.getDevice(deviceAddr);
	}

	/*public int readI2C(int add) throws IOException {
		synchronized (device) {
			int res = device.read(add);
			if (res<0) throw new RuntimeException();
			return res;
		}
	}*/

	public int read(int reg, byte[] data, int from, int to) throws IOException {
	    synchronized (device) {
	        return device.read(reg, data, from, to);
	    }
	}
	
	public int readWord(int add) throws IOException {
		synchronized (device) {
			int b1 = readU8(add);
			int b2 = readU8(add + 1);
			int res = (b1 << 8) + b2;
			
			if (res>=0x8000) {
				return -((65535 - res) + 1);
			} else {
				return res;
			}
		}
	}
    
    public void write(int add, byte data) throws IOException {
        synchronized (device) {
            device.write(add, data);
        }
    }

    public void write(byte[] data, int start, int end) throws IOException {
        synchronized (device) {
            device.write(data, start, end);
        }
    }

    
    public void write(byte data) throws IOException {
        synchronized (device) {
            device.write(data);
        }
    }

    public int read(byte[] buffer, int from, int to) throws IOException {
        synchronized (device) {
            return device.read(buffer, from, to);
        }
    }
    
	/**
	 * Read an unsigned byte from the I2C device
	 */
	public int readU8(int reg) throws IOException {
	    synchronized (device) {
			return device.read(reg); // & 0xFF;
	    }
	}

	/**
	 * Read a signed byte from the I2C device
	 */
	public int readS8(int reg) throws IOException {
        synchronized (device) {
    		int result = device.read(reg); // & 0x7F;
    		if (result > 127) result -= 256;
    		return result; // & 0xFF;
        }
	}

	public int readU16LE(int register) throws IOException {
		return readU16(register, Endianness.LITTLE_ENDIAN);
	}

	public int readU16(int register, Endianness endianness) throws IOException {
	    synchronized (device) {
    	    int hi = readU8(register);
    		int lo = readU8(register + 1);
    		if (endianness == Endianness.BIG_ENDIAN) 
    		    return (hi << 8) + lo; // & 0xFFFF;
    		else
    		    return (lo << 8) + hi; // & 0xFFFF;
	    }
	}

	public int readS16(int register, Endianness endianness) throws IOException {
		synchronized (device) {
    	    int hi, lo;
    		if (endianness == Endianness.BIG_ENDIAN) {
    			hi = readS8(register);
    			lo = readU8(register + 1);
    		} else {
    			lo = readU8(register);
    			hi = readS8(register + 1);
    		}
    		return ((hi << 8) + lo); // & 0xFFFF;
		}
	}

	public int readS16LE(int register) throws IOException {
		return readS16(register, Endianness.LITTLE_ENDIAN);
	}
}
