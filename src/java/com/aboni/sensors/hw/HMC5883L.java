package com.aboni.sensors.hw;

import com.aboni.sensors.I2CInterface;

import java.io.IOException;

public class HMC5883L {

	private final I2CInterface device;
	
	public HMC5883L(I2CInterface device) {
		this.device = device;
		scale = Scale.Gauss_1_30;
	}
	
    public static final int HMC5883_I2C_ADDRESS = 0x1e;

    @SuppressWarnings("unused")
    public static class Scale {
        private final double value;
        private final int reg;

        private Scale(double s, int reg) { 
            this.value = s;
            this.reg = reg; 
        }

        public int getReg() {
            return reg;
        }

        public double getValue() {
            return value;
        }

        public static final Scale Gauss_0_88 = new Scale(0.73, 0x00);
        public static final Scale Gauss_1_30 = new Scale(0.92, 0x01);
        public static final Scale Gauss_1_90 = new Scale(1.22, 0x02);
        public static final Scale Gauss_2_50 = new Scale(1.52, 0x03);
        public static final Scale Gauss_4_00 = new Scale(2.27, 0x04);
        public static final Scale Gauss_4_70 = new Scale(2.56, 0x05);
        public static final Scale Gauss_5_60 = new Scale(3.03, 0x06);
        public static final Scale Gauss_8_10 = new Scale(4.65, 0x07);
    }

    @SuppressWarnings("unused")
    public static class Constants {
        private Constants() {}
        public static final int OUTPUT_RATE_0_75_HZ = 0;
        public static final int OUTPUT_RATE_1_5_HZ = 1;
        public static final int OUTPUT_RATE_3_HZ = 2;
        public static final int OUTPUT_RATE_7_5_HZ = 3;
        public static final int OUTPUT_RATE_15_HZ = 4;
        public static final int OUTPUT_RATE_30_HZ = 5;
        public static final int OUTPUT_RATE_75_HZ = 6;
        
        public static final int SAMPLES_AVERAGE_1 = 0;
        public static final int SAMPLES_AVERAGE_2 = 1;
        public static final int SAMPLES_AVERAGE_4 = 2;
        public static final int SAMPLES_AVERAGE_8 = 3;
        
        public static final int CONFIGURATION_REGISTER_A = 0x00;
    	public static final int CONFIGURATION_REGISTER_B = 0x01;
    	public static final int MODE_REGISTER = 0x02;
    	public static final int AXIS_X_DATA_REGISTER_MSB = 0x03;
		public static final int AXIS_X_DATA_REGISTER_LSB = 0x04;
    	public static final int AXIS_Z_DATA_REGISTER_MSB = 0x05;
		public static final int AXIS_Z_DATA_REGISTER_LSB = 0x06;
    	public static final int AXIS_Y_DATA_REGISTER_MSB = 0x07;
		public static final int AXIS_Y_DATA_REGISTER_LSB = 0x08;
		public static final int STATUS_REGISTER = 0x09;
		public static final int IDENTIFICATION_REGISTER_A = 0x10;
		public static final int IDENTIFICATION_REGISTER_B = 0x11;
		public static final int IDENTIFICATION_REGISTER_C = 0x12;
	
    	public static final int MEASUREMENT_CONTINUOUS = 0x00;
		public static final int MEASUREMENT_SINGLE_SHOT = 0x01;
		public static final int MEASUREMENT_IDLE = 0x03;

    	public static final int X = 0;
    	public static final int Y = 1;
    	public static final int Z = 2;
    }
    
    private Scale scale;

    public void setContinuousMode() throws IOException {
    	synchronized (this) {
    		device.write(Constants.MODE_REGISTER, (byte)Constants.MEASUREMENT_CONTINUOUS);
    	}
    }
    
    public void enable() throws IOException {
        synchronized (this) {
            // set high freq 
            device.write(2, (byte)0);
            device.write(0, (byte)((Constants.SAMPLES_AVERAGE_8 << 5) + (Constants.OUTPUT_RATE_75_HZ << 2))); // # Set to 8 samples @ 15Hz
                                        
                                        
        }
    }

    public void setScale(Scale s) throws IOException {
    	synchronized (this) {
	        if (s==null) scale = Scale.Gauss_1_30;
	        else scale = s;
	        byte scaleReg = (byte) (scale.getReg() << 5);
	        device.write(Constants.CONFIGURATION_REGISTER_B, scaleReg);
    	}
    }

    public int[] readMagnetometer() throws IOException {
    	synchronized (this) {
    	    
    	    byte[] data = new byte[6];
            int r = device.read(Constants.AXIS_X_DATA_REGISTER_MSB, data, 0, 6);
            if (r != 6) {
                throw new IOException("Couldn't read compass data; r=" + r);
            }
            return new int[] { getWord(data, 0), getWord(data, 4), getWord(data, 2)}; 
    	}
    }
    
    private short getWord(byte[] b, int i) {
        byte high = (byte)(b[i] & 0xFF);
        byte low  = (byte)(b[i + 1] & 0xFF);
        return (short)(((high << 8) + (low & 0xFF)) & 0xFFFF); // Little endian
    }
    
    public double[] getScaledMag() throws IOException {
        int[] m = readMagnetometer();
        return new double[] { 
                (m[Constants.X] != -4096)?(m[Constants.X] * scale.getValue()):0.0,
                (m[Constants.Y] != -4096)?(m[Constants.Y] * scale.getValue()):0.0,
                (m[Constants.Z] != -4096)?(m[Constants.Z] * scale.getValue()):0.0
        };
    }
}
