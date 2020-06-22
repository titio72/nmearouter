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

public interface CompassDataProvider {

    /**
     * Read pitch, roll and heading
     *
     * @return An array of double containing pitch, roll and heading in this order. Null on failure.
     */
    double[] read() throws SensorException;

    /**
     * Initialize the provider for use.
     * Using the the provider may fail if not initialized.
     */
    void init() throws SensorException;

    /**
     * Refresh the configuration (ex: can be used to force reloading of calibration values)
     */
    void refreshConfiguration();
}
