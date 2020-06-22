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

import com.aboni.nmea.router.services.ServiceOutput;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;

public class ServletResponseOutput implements ServiceOutput {

    private final HttpServletResponse r;

    public ServletResponseOutput(HttpServletResponse r) {
        this.r = r;
    }

    @Override
    public void setContentType(String type) {
        r.setContentType(type);
    }

    @Override
    public PrintWriter getWriter() throws IOException {
        return r.getWriter();
    }

    @Override
    public void ok() {
        r.setStatus(HttpServletResponse.SC_OK);
    }

    @Override
    public void error(String msg) throws IOException {
        r.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        r.getWriter().write(msg);
    }

    @Override
    public void setHeader(String string, String string2) {
        r.setHeader(string, string2);
    }
}
