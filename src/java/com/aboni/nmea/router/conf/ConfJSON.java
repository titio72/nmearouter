package com.aboni.nmea.router.conf;

import com.aboni.nmea.router.Constants;
import com.aboni.nmea.router.agent.QOS;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

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

        public InOut getInOut() {
            if (configuration.has("inout")) {
                return InOut.fromValue(configuration.getString("inout"));
            } else {
                return InOut.INOUT;
            }
        }
    }

    private final JSONObject conf;

    public ConfJSON() throws IOException {
        StringBuilder b = new StringBuilder();
        try (FileReader reader = new FileReader(Constants.ROUTER_CONF_JSON)) {
            char[] bf = new char[1024];
            int bytesRead;
            while ((bytesRead = reader.read(bf)) != -1) {
                b.append(bf, 0, bytesRead);
            }
        }
        conf = new JSONObject(b.toString());
    }

    public LogLevelType getLogLevel() {
        return conf.has("logLevel") ? LogLevelType.fromValue(conf.getString("logLevel")) : LogLevelType.INFO;
    }

    public List<String> getGPSPriority() throws MalformedConfigurationException {
        List<String> res = new ArrayList<>();
        if (conf.has("gpsPriority")) {
            try {
                JSONArray a = conf.getJSONArray("gpsPriority");
                List<Object> l = a.toList();
                l.sort(Comparator.comparingInt((Object o) -> (Integer) ((Map<?, ?>) o).get("priority")));
                for (Object o : l) {
                    res.add((String) ((Map<?, ?>) o).get("source"));
                }
            } catch (JSONException e) {
                throw new MalformedConfigurationException("Error reading GPS priority", e);
            }
        }
        return res;
    }

    public List<AgentDef> getAgents()  throws MalformedConfigurationException {
        try {
            int c = 1;
            List<AgentDef> res = new ArrayList<>();
            JSONArray agentsJ = conf.getJSONArray("agents");
            for (Object agentO: agentsJ) {
                JSONObject agentJ = (JSONObject) agentO;
                if (!(agentJ.has("disabled") && agentJ.getBoolean("disabled")) && agentJ.has("type")) {
                    AgentDef def = new AgentDef();
                    def.type = agentJ.getString("type");
                    def.name = agentJ.has("name") ? agentJ.getString("name") : String.format("AGENT_%d", c++);
                    def.configuration = agentJ;
                    def.qos = agentJ.has("qos") ? QOS.parse(agentJ.getString("qos")) : new QOS();
                    res.add(def);
                }
            }
            return res;
        } catch (JSONException | ClassCastException e) {
            throw new MalformedConfigurationException("Error reading agents", e);
        }
    }
}
