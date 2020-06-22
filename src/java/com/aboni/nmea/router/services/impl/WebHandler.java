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

import com.aboni.nmea.router.services.WebService;
import com.aboni.nmea.router.services.WebServiceFactory;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.handler.AbstractHandler;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.constraints.NotNull;

public class WebHandler extends AbstractHandler {

    private final WebServiceFactory factory;

    @Inject
    public WebHandler(@NotNull WebServiceFactory factory) {
        this.factory = factory;
    }

    @Override
    public void handle(String target,
                       Request baseRequest,
                       HttpServletRequest request,
                       HttpServletResponse response) {
        WebService s = getService(target);
        if (s != null) {
            s.doIt(new ServletRequestServiceConfig(request),
                    new ServletResponseOutput(response));
            baseRequest.setHandled(true);
        } else {
            baseRequest.setHandled(false);
        }
    }

    private WebService getService(String target) {
        return factory.getService(target);
    }
}
