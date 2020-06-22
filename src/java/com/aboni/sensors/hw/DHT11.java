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

import com.aboni.sensors.SensorException;
import com.pi4j.wiringpi.Gpio;
import com.pi4j.wiringpi.GpioUtil;

public class DHT11 {

    private static final int MAX_TIMINGS = 85;
    private float h;
    private float c;
    private final int[] dht11Dat;

    public DHT11() throws SensorException {
        dht11Dat = new int[] { 0, 0, 0, 0, 0 };
        if (Gpio.wiringPiSetup() == -1) {
            throw new SensorException("GPIO Initialization failed for DHT11");
        } else {
            GpioUtil.export(3, GpioUtil.DIRECTION_OUT);
        }
    }

    public float getTemperature() {
        return c;
    }

    public float getPressure() {
        return h;
    }

    public void read() {
        int lastState = Gpio.HIGH;
        int j = 0;
        dht11Dat[0] = dht11Dat[1] = dht11Dat[2] = dht11Dat[3] = dht11Dat[4] = 0;

        Gpio.pinMode(3, Gpio.OUTPUT);
        Gpio.digitalWrite(3, Gpio.LOW);
        Gpio.delay(18);

        Gpio.digitalWrite(3, Gpio.HIGH);
        Gpio.pinMode(3, Gpio.INPUT);

        for (int i = 0; i < MAX_TIMINGS; i++) {
            int counter = 0;
            while (Gpio.digitalRead(3) == lastState) {
                counter++;
                Gpio.delayMicroseconds(1);
                if (counter == 255) {
                    break;
                }
            }

            lastState = Gpio.digitalRead(3);

            if (counter == 255) {
                break;
            }

            /* ignore first 3 transitions */
            if ((i >= 4) && (i % 2 == 0)) {
                /* shove each bit into the storage bytes */
                dht11Dat[j / 8] <<= 1;
                if (counter > 16) {
                    dht11Dat[j / 8] |= 1;
                }
                j++;
            }
        }
        if (j>=40) {
            setTempAndPress();
        }
    }

    private void setTempAndPress() {
        // check we read 40 bits (8bit x 5 ) + verify checksum in the last
        // byte
        if (checkParity()) {
            h = (float)((dht11Dat[0] << 8) + dht11Dat[1]) / 10;
            if ( h > 100 ) {
                h = dht11Dat[0];   // for DHT11
            }
            c = (float)(((dht11Dat[2] & 0x7F) << 8) + dht11Dat[3]) / 10;
            if ( c > 125 ) {
                c = dht11Dat[2];   // for DHT11
            }
            if ( (dht11Dat[2] & 0x80) != 0 ) {
                c = -c;
            }
        }
    }

    private boolean checkParity() {
        return (dht11Dat[4] == ((dht11Dat[0] + dht11Dat[1] + dht11Dat[2] + dht11Dat[3]) & 0xFF));
    }
}