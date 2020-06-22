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

import com.aboni.utils.ServerLog;
import org.json.JSONObject;

import javax.validation.constraints.NotNull;
import java.io.IOException;

public class JSONWebService implements WebService {

    private static final String APPLICATION_JSON = "application/json";
    private WebServiceJSONLoader loader;

    public JSONWebService(@NotNull WebServiceJSONLoader loader) {
        this.loader = loader;
    }

    public JSONWebService() {
        loader = null;
    }

    public void setLoader(@NotNull WebServiceJSONLoader loader) {
        this.loader = loader;
    }

    @Override
    public final void doIt(ServiceConfig config, ServiceOutput response) {
        ServerLog.getLogger().info("JSONWebService invoked {" + this.getClass().getName() + "} " + config.dump());
        try {
            setResponse(response, getJsonObjectResult(config));
        } catch (Exception e) {
            ServerLog.getLogger().error("Error creating database helper Svc {" + this.getClass().getName() + "}", e);
        }
    }

    private JSONObject getJsonObjectResult(ServiceConfig config) {
        JSONObject res;
        try {
            res = loader.getResult(config);
        } catch (Exception e) {
            ServerLog.getLogger().error("Error extracting JSON {" + e.getMessage() + "}", e);
            res = new JSONObject();
            res.put("Error", "Error extracting JSON {" + e.getMessage() + "}");
        }
        return res;
    }

    private void setResponse(ServiceOutput response, JSONObject res) {
        response.setContentType(APPLICATION_JSON);
        try {
            if (res!=null) {
                response.getWriter().append(res.toString(2));
                response.ok();
            } else {
                response.error("Invalid response detected: something went wrong");
            }
        } catch (IOException e) {
            ServerLog.getLogger().error("Error sending output for Svc {" + this.getClass().getName() + "}", e);
        }
    }

    protected static JSONObject getOk() {
        return getOk("Ok");
    }

    protected static JSONObject getOk(String msg) {
        JSONObject res = new JSONObject();
        res.put("msg", msg);
        return res;
    }

    protected static JSONObject getError(String message) {
        JSONObject res = new JSONObject();
        res.put("error", message);
        return res;
    }
}
