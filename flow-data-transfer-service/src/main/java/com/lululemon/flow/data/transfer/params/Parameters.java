package com.lululemon.flow.data.transfer.params;

import java.util.HashMap;
import java.util.Map;

public class Parameters {

    public static final String PARAM_SQL = "sql";
    public static final String PARAM_DB_URL = "db.url";

    private String prefix;
    private Map<String, String> parameters;

    public Parameters() {
        prefix = "";
        parameters = new HashMap<>();
    }

    public Parameters(String prefix, Map<String, String> parameters) {
        this.prefix = prefix;
        this.parameters = parameters;
    }

    public Parameters(Map<String, String> parameters) {
        this.parameters = parameters;
    }

    public String get(String key) {
        return get(key, null);
    }

    public String get(String key, String defaultValue) {
        if (key == null) {
            throw  new IllegalArgumentException("Parameters key cannot be null");
        }
        String value = null;
        if (prefix != null) {
            value = getInternal(prefix.toLowerCase() + "." + key);
        }
        if (value == null) {
           value = getInternal(key);
        }
        if (value == null) {
            value = defaultValue;
        }
        return value;
    }

    private String getInternal(String key) {

        String value = System.getenv(key);
        if (value == null) {
            value = System.getProperty(key);
        }
        if (value == null) {
            value = parameters.get(key);
        }
        if (value != null) {
            int start = value.indexOf("${");
            int end = value.indexOf("}");
            if (start != -1 && end != -1) {
                key = value.substring(start + 2, end);
                value = value.replace("${" + key + "}", get(key));
            }
            return value;
        }
        return null;
    }

    public void put(String key, String value) {
        if (parameters != null) {
            parameters.put(key ,value);
        }
    }
}
