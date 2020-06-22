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

package com.aboni.nmea.router.services.impl;

import com.aboni.nmea.router.services.ServiceConfig;

import javax.servlet.ServletRequest;
import java.util.Enumeration;

public class ServletRequestServiceConfig implements ServiceConfig {

    private final ServletRequest r;

    public ServletRequestServiceConfig(ServletRequest r) {
        this.r = r;
    }

    @Override
    public String getParameter(String name) {
        return r.getParameter(name);
    }

    @Override
    public String getParameter(String name, String d) {
        String res = r.getParameter(name);
        return (res == null) ? d : res;
    }

    @Override
    public String dump() {
        StringBuilder b = new StringBuilder();
        Enumeration<String> e = r.getParameterNames();
        while (e.hasMoreElements()) {
            String p = e.nextElement();
            b.append(" ").append(p).append(" {").append(r.getParameter(p)).append("}");
        }
        if (b.length() > 0) {
            return b.substring(1);
        } else {
            return "";
        }


    }

}
