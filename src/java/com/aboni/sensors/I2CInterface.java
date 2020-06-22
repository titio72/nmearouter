/*
(C) 2020, Andrea Boni
This file is part of NMEARouter.
NMEARouter is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.
NMEARouter is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.
You should have received a copy of the GNU General Public License
along with NMEARouter.  If not, see <http://www.gnu.org/licenses/>.
*/

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

    public I2CInterface(int busId, int deviceAddress) throws IOException, UnsupportedBusNumberException {
        I2CBus bus = I2CFactory.getInstance(busId);
        device = bus.getDevice(deviceAddress);
    }

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
            return device.read(reg);
        }
    }

    /**
     * Read a signed byte from the I2C device
     */
    public int readS8(int reg) throws IOException {
        synchronized (device) {
            int result = device.read(reg);
            if (result > 127) result -= 256;
            return result;
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
                return (hi << 8) + lo;
            else
                return (lo << 8) + hi;
        }
    }

    public int readS16(int register, Endianness endianness) throws IOException {
        synchronized (device) {
            int hi;
            int lo;
            if (endianness == Endianness.BIG_ENDIAN) {
                hi = readS8(register);
                lo = readU8(register + 1);
            } else {
                lo = readU8(register);
                hi = readS8(register + 1);
            }
            return ((hi << 8) + lo);
        }
    }

    public int readS16LE(int register) throws IOException {
        return readS16(register, Endianness.LITTLE_ENDIAN);
    }
}
