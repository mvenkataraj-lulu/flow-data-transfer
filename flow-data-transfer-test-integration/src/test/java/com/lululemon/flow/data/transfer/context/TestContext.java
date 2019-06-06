package com.lululemon.flow.data.transfer.context;

import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
public class TestContext {

    public static final String RESPONSE = "response";
    public static final String EXECUTIONID = "execId";

    private static ThreadLocal<Map<String, Object>> state = new ThreadLocal<Map<String, Object>>() {
        @Override
        protected Map<String, Object> initialValue() {
            return new HashMap<>();
        }
    };

    private TestContext() {
    }

    public static <T> void put(String name, T value) {
        state.get().put(name, value);
    }

    public static <T> T get(String key) {
        return (T) state.get().get(key);
    }

    public static void putAll(Map map) {
        state.get().putAll(map);
    }

    public static Map<String, Object> getAll(){
        return state.get();
    }

    public static void setResponse(String response) {
        put(RESPONSE, response);
    }

    public static String getResponse() {
        return (String) state.get().get(RESPONSE);
    }

    public static void setExecutionId(Integer executionId) {
        put(EXECUTIONID, executionId);
    }

    public static Integer getExecutionId() {
        return (Integer) state.get().get(EXECUTIONID);
    }
}
