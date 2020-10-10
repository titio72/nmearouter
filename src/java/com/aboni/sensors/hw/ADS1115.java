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

package com.aboni.sensors.hw;

import com.aboni.sensors.I2CInterface;
import com.pi4j.gpio.extension.ads.ADS1115GpioProvider;

import java.io.IOException;

public class ADS1115 {


    // =======================================================================
    // ADS1115 I2C ADDRESS
    // =======================================================================
    public static final int ADS1115_ADDRESS_0X48 = 0x48; // ADDRESS 1 : 0x48 (1001000) ADR -> GND
    public static final int ADS1115_ADDRESS_0X49 = 0x49; // ADDRESS 2 : 0x49 (1001001) ADR -> VDD
    public static final int ADS1115_ADDRESS_0X4A = 0x4A; // ADDRESS 3 : 0x4A (1001010) ADR -> SDA
    public static final int ADS1115_ADDRESS_0X4B = 0x4B; // ADDRESS 4 : 0x4B (1001011) ADR -> SCL

    // =======================================================================
    // ADS1115 VALUE RANGES
    // =======================================================================
    public static final int ADS1115_RANGE_MAX_VALUE =  32767; //0x7FFF (16 bits)
    public static final int ADS1115_RANGE_MIN_VALUE = -32768; //0xFFFF (16 bits)

    // =======================================================================
    // CONVERSION DELAY (in mS)
    // =======================================================================
    protected static final int ADS1115_CONVERSIONDELAY       = 0x08;

    // =======================================================================
    // POINTER REGISTER
    // =======================================================================
    protected static final int ADS1X15_REG_POINTER_MASK      = 0x03;
    protected static final int ADS1X15_REG_POINTER_CONVERT   = 0x00;
    protected static final int ADS1X15_REG_POINTER_CONFIG    = 0x01;
    protected static final int ADS1X15_REG_POINTER_LOWTHRESH = 0x02;
    protected static final int ADS1X15_REG_POINTER_HITHRESH  = 0x03;

    // =======================================================================
    // CONFIG REGISTER
    // =======================================================================
    protected static final int  ADS1X15_REG_CONFIG_OS_MASK      = 0x8000;
    protected static final int  ADS1X15_REG_CONFIG_OS_SINGLE    = 0x8000;  // Write: Set to start a single-conversion
    protected static final int  ADS1X15_REG_CONFIG_OS_BUSY      = 0x0000;  // Read: Bit = 0 when conversion is in progress
    protected static final int  ADS1X15_REG_CONFIG_OS_NOTBUSY   = 0x8000;  // Read: Bit = 1 when device is not performing a conversion

    protected static final int  ADS1X15_REG_CONFIG_MUX_MASK     = 0x7000;
    protected static final int  ADS1X15_REG_CONFIG_MUX_DIFF_0_1 = 0x0000;  // Differential P = AIN0, N = AIN1 (default)
    protected static final int  ADS1X15_REG_CONFIG_MUX_DIFF_0_3 = 0x1000;  // Differential P = AIN0, N = AIN3
    protected static final int  ADS1X15_REG_CONFIG_MUX_DIFF_1_3 = 0x2000;  // Differential P = AIN1, N = AIN3
    protected static final int  ADS1X15_REG_CONFIG_MUX_DIFF_2_3 = 0x3000;  // Differential P = AIN2, N = AIN3
    protected static final int  ADS1X15_REG_CONFIG_MUX_SINGLE_0 = 0x4000;  // Single-ended AIN0
    protected static final int  ADS1X15_REG_CONFIG_MUX_SINGLE_1 = 0x5000;  // Single-ended AIN1
    protected static final int  ADS1X15_REG_CONFIG_MUX_SINGLE_2 = 0x6000;  // Single-ended AIN2
    protected static final int  ADS1X15_REG_CONFIG_MUX_SINGLE_3 = 0x7000;  // Single-ended AIN3

    protected static final int  ADS1X15_REG_CONFIG_PGA_MASK     = 0x0E00;
    protected static final int  ADS1X15_REG_CONFIG_PGA_6_144V   = 0x0000;  // +/-6.144V range
    protected static final int  ADS1X15_REG_CONFIG_PGA_4_096V   = 0x0200;  // +/-4.096V range
    protected static final int  ADS1X15_REG_CONFIG_PGA_2_048V   = 0x0400;  // +/-2.048V range (default)
    protected static final int  ADS1X15_REG_CONFIG_PGA_1_024V   = 0x0600;  // +/-1.024V range
    protected static final int  ADS1X15_REG_CONFIG_PGA_0_512V   = 0x0800;  // +/-0.512V range
    protected static final int  ADS1X15_REG_CONFIG_PGA_0_256V   = 0x0A00;  // +/-0.256V range

    protected static final int  ADS1X15_REG_CONFIG_MODE_MASK    = 0x0100;
    protected static final int  ADS1X15_REG_CONFIG_MODE_CONTIN  = 0x0000;  // Continuous conversion mode
    protected static final int  ADS1X15_REG_CONFIG_MODE_SINGLE  = 0x0100;  // Power-down single-shot mode (default)

    protected static final int  ADS1X15_REG_CONFIG_DR_MASK      = 0x00E0;
    protected static final int  ADS1X15_REG_CONFIG_DR_128SPS    = 0x0000;  // 128 samples per second
    protected static final int  ADS1X15_REG_CONFIG_DR_250SPS    = 0x0020;  // 250 samples per second
    protected static final int  ADS1X15_REG_CONFIG_DR_490SPS    = 0x0040;  // 490 samples per second
    protected static final int  ADS1X15_REG_CONFIG_DR_920SPS    = 0x0060;  // 920 samples per second
    protected static final int  ADS1X15_REG_CONFIG_DR_1600SPS   = 0x0080;  // 1600 samples per second (default)
    protected static final int  ADS1X15_REG_CONFIG_DR_2400SPS   = 0x00A0;  // 2400 samples per second
    protected static final int  ADS1X15_REG_CONFIG_DR_3300SPS   = 0x00C0;  // 3300 samples per second

    protected static final int  ADS1X15_REG_CONFIG_CMODE_MASK   = 0x0010;
    protected static final int  ADS1X15_REG_CONFIG_CMODE_TRAD   = 0x0000;  // Traditional comparator with hysteresis (default)
    protected static final int  ADS1X15_REG_CONFIG_CMODE_WINDOW = 0x0010;  // Window comparator

    protected static final int  ADS1X15_REG_CONFIG_CPOL_MASK    = 0x0008;
    protected static final int  ADS1X15_REG_CONFIG_CPOL_ACTVLOW = 0x0000;  // ALERT/RDY pin is low when active (default)
    protected static final int  ADS1X15_REG_CONFIG_CPOL_ACTVHI  = 0x0008;  // ALERT/RDY pin is high when active

    protected static final int  ADS1X15_REG_CONFIG_CLAT_MASK    = 0x0004;  // Determines if ALERT/RDY pin latches once asserted
    protected static final int  ADS1X15_REG_CONFIG_CLAT_NONLAT  = 0x0000;  // Non-latching comparator (default)
    protected static final int  ADS1X15_REG_CONFIG_CLAT_LATCH   = 0x0004;  // Latching comparator

    protected static final int  ADS1X15_REG_CONFIG_CQUE_MASK    = 0x0003;
    protected static final int  ADS1X15_REG_CONFIG_CQUE_1CONV   = 0x0000;  // Assert ALERT/RDY after one conversions
    protected static final int  ADS1X15_REG_CONFIG_CQUE_2CONV   = 0x0001;  // Assert ALERT/RDY after two conversions
    protected static final int  ADS1X15_REG_CONFIG_CQUE_4CONV   = 0x0002;  // Assert ALERT/RDY after four conversions
    protected static final int  ADS1X15_REG_CONFIG_CQUE_NONE    = 0x0003;  // Disable the comparator and put ALERT/RDY in high state (default)

    protected static final int[] ADS1X15_REG_CONFIG_MUX_SINGLE = new int[] {
            ADS1X15_REG_CONFIG_MUX_SINGLE_0,
            ADS1X15_REG_CONFIG_MUX_SINGLE_1,
            ADS1X15_REG_CONFIG_MUX_SINGLE_2,
            ADS1X15_REG_CONFIG_MUX_SINGLE_3
    };

    protected enum ProgrammableGainAmplifierValue{
        PGA_6_144V(6.144,ADS1X15_REG_CONFIG_PGA_6_144V),  // +/-6.144V range
        PGA_4_096V(4.096,ADS1X15_REG_CONFIG_PGA_4_096V),  // +/-4.096V range
        PGA_2_048V(2.048,ADS1X15_REG_CONFIG_PGA_2_048V),  // +/-2.048V range
        PGA_1_024V(1.024,ADS1X15_REG_CONFIG_PGA_1_024V),  // +/-1.024V range
        PGA_0_512V(0.512,ADS1X15_REG_CONFIG_PGA_0_512V),  // +/-0.512V range
        PGA_0_256V(0.256,ADS1X15_REG_CONFIG_PGA_0_256V);   // +/-0.256V range

        private final double voltage;
        private final int configValue;

        ProgrammableGainAmplifierValue(double voltage, int configValue){
            this.voltage = voltage;
            this.configValue = configValue;
        }

        public double getVoltage(){
            return this.voltage;
        }

        public int getConfigValue(){
            return this.configValue;
        }
    }

    private final ProgrammableGainAmplifierValue pga;
    private final double multiplier;
    private final I2CInterface device;

    public ADS1115(I2CInterface device) {
        this(device, 1.0);
    }

    public ADS1115(I2CInterface device, double multiplier) {
        this.device = device;
        this.pga = ProgrammableGainAmplifierValue.PGA_4_096V;
        this.multiplier = multiplier;
    }

    private double getImmediateValue(int pin) throws IOException {

        // Start with default values
        int config = ADS1X15_REG_CONFIG_CQUE_NONE    | // Disable the comparator (default val)
                ADS1X15_REG_CONFIG_CLAT_NONLAT  | // Non-latching (default val)
                ADS1X15_REG_CONFIG_CPOL_ACTVLOW | // Alert/Rdy active low   (default val)
                ADS1X15_REG_CONFIG_CMODE_TRAD   | // Traditional comparator (default val)
                ADS1X15_REG_CONFIG_DR_3300SPS   | // 1600 samples per second (default)
                ADS1X15_REG_CONFIG_MODE_SINGLE;   // Single-shot mode (default)

        config |= pga.getConfigValue();
        switch (pin) {
            case 1: config |= ADS1X15_REG_CONFIG_MUX_SINGLE_1; break;
            case 2: config |= ADS1X15_REG_CONFIG_MUX_SINGLE_2; break;
            case 3: config |= ADS1X15_REG_CONFIG_MUX_SINGLE_3; break;
            default: config |= ADS1X15_REG_CONFIG_MUX_SINGLE_0; break; // switch back to 0...
        }
        config |= ADS1X15_REG_CONFIG_OS_SINGLE;

        // Write config register to the ADC
        writeRegister(device, ADS1X15_REG_POINTER_CONFIG, config);

        // Wait for the conversion to complete
        try{ if(ADS1115_CONVERSIONDELAY > 0){
            Thread.sleep(ADS1115_CONVERSIONDELAY); }
            // read the conversion results
            return readRegister(device, ADS1X15_REG_POINTER_CONVERT);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return Double.NaN;
        }
    }

    public double getVoltage0() throws IOException {
        return getVoltage(0);
    }

    public double getVoltage1() throws IOException {
        return getVoltage(1);
    }

    public double getVoltage2() throws IOException {
        return getVoltage(2);
    }

    public double getVoltage3() throws IOException {
        return getVoltage(3);
    }

    public double getVoltage(int pin) throws IOException {
        double v = getImmediateValue(pin);
        double p =  ((v * 100) / ADS1115GpioProvider.ADS1115_RANGE_MAX_VALUE);
        return ProgrammableGainAmplifierValue.PGA_4_096V.getVoltage() * (p/100) * multiplier;
    }

    /**
     * Writes 16-bits to the specified destination register
     * @param device The I2C device
     * @param register  the Tegister value
     * @param value The value to be written inthe registry
     * @throws IOException When the writing on the device fails
     */
    static void writeRegister(I2CInterface device, int register, int value) throws IOException {

        // create packet in data buffer
        byte[] packet = new byte[3];
        packet[0] = (byte)(register);     // register byte
        packet[1] = (byte)(value>>8);     // value MSB 
        packet[2] = (byte)(value & 0xFF); // value LSB 

        // write data to I2C device
        device.write(packet, 0, 3);
    }

    static int readRegister(I2CInterface device, int register) throws IOException {
        device.write((byte)register);
        // create data buffer for receive data
        byte[] buffer = new byte[2];  // receive 16 bits (2 bytes)
        int byteCount = device.read(buffer, 0, 2);
        if(byteCount == 2){
            return getShort(buffer, 0);
        } else{
            return 0;
        }
    }

    private static short getShort(byte[] arr, int off) {
        return (short) (arr[off]<<8 & 0xFF00 | arr[off+1] & 0xFF);
    } 
}
