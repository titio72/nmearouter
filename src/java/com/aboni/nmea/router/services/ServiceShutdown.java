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

package com.aboni.nmea.router.services;

import com.aboni.nmea.router.utils.Log;
import com.aboni.utils.LogStringBuilder;
import org.json.JSONObject;

import javax.inject.Inject;

public class ServiceShutdown extends JSONWebService {

    public static final String SHUTDOWN_SERVICE_CATEGORY = "ShutdownService";
    public static final String SHUTDOWN_KEY_NAME = "Shutdown";

    @Inject
    public ServiceShutdown(Log log) {
        super(log);
        setLoader(this::getResult);
    }

    private JSONObject getResult(ServiceConfig config) {
        try {
            getLogger().info(LogStringBuilder.start(SHUTDOWN_SERVICE_CATEGORY).wO(SHUTDOWN_KEY_NAME).toString());
            ProcessBuilder b = new ProcessBuilder("./shutdown");
            Process process = b.start();
            int retCode = process.waitFor();
            getLogger().info(LogStringBuilder.start(SHUTDOWN_SERVICE_CATEGORY).wO(SHUTDOWN_KEY_NAME).wV("return code", retCode).toString());
            return getOk();
        } catch (InterruptedException e) {
            getLogger().errorForceStacktrace(LogStringBuilder.start(SHUTDOWN_SERVICE_CATEGORY).wO(SHUTDOWN_KEY_NAME).toString(), e);
            Thread.currentThread().interrupt();
            return getError("Error " + e.getMessage());
        } catch (Exception e) {
            getLogger().errorForceStacktrace(LogStringBuilder.start(SHUTDOWN_SERVICE_CATEGORY).wO(SHUTDOWN_KEY_NAME).toString(), e);
            return getError("Error " + e.getMessage());
        }
    }
}
