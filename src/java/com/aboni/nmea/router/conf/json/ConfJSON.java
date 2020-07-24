package com.aboni.nmea.router.conf.json;

import com.aboni.nmea.router.Constants;
import com.aboni.nmea.router.agent.QOS;
import com.aboni.nmea.router.conf.MalformedConfigurationException;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class ConfJSON {

    public static class AgentDef {
        private String type;
        private String name;
        private QOS qos;
        private JSONObject configuration;

        public String getType() {
            return type;
        }

        public String getName() {
            return name;
        }

        public QOS getQos() {
            return qos;
        }

        public JSONObject getConfiguration() {
            return configuration;
        }
    }

    private final JSONObject conf;

    public ConfJSON() {
        conf = new JSONObject(Constants.CONF_DIR + "/router.json");
    }

    public String getLogLevel() {
        return conf.has("logLevel")?conf.getString("logLevel"):"INFO";
    }

    public String[] getGPSPriority() throws MalformedConfigurationException {
        if (conf.has("gpsPriority")) {
            try {
                JSONArray a = conf.getJSONArray("gpsPriorty");
                List<Object> l = a.toList();
                l.sort(Comparator.comparingInt((Object o) -> ((JSONObject) o).getInt("priority")));
                List<String> res = new ArrayList<>();
                for (Object o : l) {
                    res.add(((JSONObject) o).getString("source"));
                }
                return res.toArray(new String[0]);
            } catch (JSONException e) {
                throw new MalformedConfigurationException("Error reading GPS priority", e);
            }
        }
        return new String[0];
    }

    public List<AgentDef> getAgents()  throws MalformedConfigurationException {
        try {
            List<AgentDef> res = new ArrayList<>();
            JSONArray agentsJ = conf.getJSONArray("agents");
            for (Object agentO: agentsJ) {
                JSONObject agentJ = (JSONObject)agentO;
                AgentDef def = new AgentDef();
                def.type = agentJ.getString("type");
                def.name = agentJ.getString("name");
                def.configuration = agentJ;
                def.qos = agentJ.has("qos")?QOS.parse(agentJ.getString("qos")):new QOS();
                res.add(def);
            }
            return res;
        } catch (JSONException | ClassCastException e) {
            throw new MalformedConfigurationException("Error reading agents", e);
        }
    }
}
