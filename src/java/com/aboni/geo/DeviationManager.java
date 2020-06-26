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

package com.aboni.geo;

import java.io.InputStream;

public interface DeviationManager {

    /**
     * Get the magnetic north given the compass reading by applying the deviation map.
     * @param reading The compass reading in decimal degrees to be converted.
     * @return The magnetic north in decimal degrees [0..360].
     */
    double getMagnetic(double reading);

    /**
     * Get the compass north given the magnetic reading by applying the deviation map.
     * @param magnetic Magnetic north in decimal degrees to be converted.
     * @return The compass north in decimal degrees [0..360].
     */
    @SuppressWarnings("unused")
    double getCompass(double magnetic);

    void reset();

    boolean load(InputStream s);
}