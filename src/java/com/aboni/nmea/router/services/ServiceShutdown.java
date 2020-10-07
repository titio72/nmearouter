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

import com.aboni.utils.Log;
import com.aboni.utils.LogStringBuilder;
import org.json.JSONObject;

import javax.inject.Inject;
import javax.validation.constraints.NotNull;

public class ServiceShutdown extends JSONWebService {

    public static final String SHUTDOWN_SERVICE_CATEGORY = "ShutdownService";
    public static final String SHUTDOWN_KEY_NAME = "Shutdown";
    private final Log log;

    @Inject
    public ServiceShutdown(@NotNull Log log) {
        super(log);
        this.log = log;
        setLoader(this::getResult);
    }

    private JSONObject getResult(ServiceConfig config) {
        try {
            log.info(LogStringBuilder.start(SHUTDOWN_SERVICE_CATEGORY).wO(SHUTDOWN_KEY_NAME).toString());
            ProcessBuilder b = new ProcessBuilder("./shutdown");
            Process process = b.start();
            int retCode = process.waitFor();
            log.info(LogStringBuilder.start(SHUTDOWN_SERVICE_CATEGORY).wO(SHUTDOWN_KEY_NAME).wV("return code", retCode).toString());
            return getOk();
        } catch (Exception e) {
            log.errorForceStacktrace(LogStringBuilder.start(SHUTDOWN_SERVICE_CATEGORY).wO(SHUTDOWN_KEY_NAME).toString(), e);
            return getError("Error " + e.getMessage());
        }
    }
}
