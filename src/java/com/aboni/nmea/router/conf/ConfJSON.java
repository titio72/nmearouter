package com.aboni.nmea.router.conf;

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

    public static final LogLevelType DEFAULT_LOG_LEVEL = LogLevelType.INFO;

    private static class AgentConf implements AgentConfJSON {

        private String type;
        private String name;
        private QOS qos;
        private JSONObject configuration;

        @Override
        public String getType() {
            return type;
        }

        @Override
        public String getName() {
            return name;
        }

        @Override
        public QOS getQos() {
            return qos;
        }

        @Override
        public JSONObject getConfiguration() {
            return configuration;
        }
    }

    private JSONObject conf;

    public ConfJSON(String file) throws IOException {
        StringBuilder b = new StringBuilder();
        try (FileReader reader = new FileReader(file)) {
            char[] bf = new char[1024];
            int bytesRead;
            while ((bytesRead = reader.read(bf)) != -1) {
                b.append(bf, 0, bytesRead);
            }
        }
        init(new JSONObject(b.toString()));
    }

    public ConfJSON(StringBuffer jsonConf) {
        init(new JSONObject(jsonConf.toString()));
    }

    private void init(JSONObject obj) {
        conf = obj;
    }

    public LogLevelType getLogLevel() {
        return conf.has("logLevel") ? LogLevelType.fromValue(conf.getString("logLevel")) : DEFAULT_LOG_LEVEL;
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

    public List<AgentConfJSON> getAgents() throws MalformedConfigurationException {
        try {
            int c = 1;
            List<AgentConfJSON> res = new ArrayList<>();
            JSONArray agentsJ = conf.getJSONArray("agents");
            for (Object agentO : agentsJ) {
                JSONObject agentJ = (JSONObject) agentO;
                if (!(agentJ.has("disabled") && agentJ.getBoolean("disabled")) && agentJ.has("type")) {
                    AgentConf def = new AgentConf();
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
