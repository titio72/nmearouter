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

package com.aboni.utils;

public class Pair<T, U> {
    public final T first;
    public final U second;

    public Pair(T first, U second) {
        this.first = first;
        this.second = second;
    }

    @Override
    public boolean equals(Object x) {
        if (x instanceof Pair<?, ?>) {
            Pair<?, ?> pair = (Pair<?, ?>)x;
            if (first!=null && second!=null)
                return first.equals(pair.first) && second.equals(pair.second);
            else if (first==null && second!=null)
                return pair.first==null && second.equals(pair.second);
            else if (first!=null)
                return first.equals(pair.first) && pair.second == null;
            else
                return pair.first == null && pair.second == null;
        } else {
            return false;
        }
    }

    @Override
    public int hashCode() {
        return ((first!=null)?first.hashCode():0) + ((second!=null)?second.hashCode():0);
    }
}
