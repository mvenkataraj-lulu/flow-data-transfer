package com.lululemon.flow.data.transfer.params;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

public class ParametersTest {


    Map<String, String> params = new HashMap<>();

    @Before
    public void before() {
        params.clear();
    }


    @Test
    public void getTest() {
        String key = "k";
        String value = "v";
        params.put(key, value);
        Parameters parameters = new Parameters(params);
        Assert.assertEquals(value, parameters.get(key));
    }


    @Test
    public void getDefaultTest() {
        String key = "k";
        String defaultValue = "d";
        Parameters parameters = new Parameters(params);
        Assert.assertEquals(defaultValue, parameters.get(key, defaultValue));
    }

    @Test
    public void getWithPrefixTest() {
        String key = "k";
        String value = "v";
        String prefix = "p";
        String prefixedKey = prefix + "." + key;
        String prefixedValue = "pv";
        params.put(key, value);
        params.put(prefixedKey, prefixedValue);
        Parameters parameters = new Parameters(prefix, params);
        Assert.assertEquals(prefixedValue, parameters.get(key));
    }

    @Test
    public void getFromEnvTest() {
        String key = "k";
        String value = "v";
        params.put(key, value);
        String envValue = "ev";
        setEnv(key, envValue);
        Parameters parameters = new Parameters(params);
        Assert.assertEquals(envValue, parameters.get(key));
    }

    @Test
    public void getFromSystemPropertyTest() {
        String key = "k";
        String value = "v";
        params.put(key, value);
        String sysValue = "sv";
        System.setProperty(key, sysValue);
        Parameters parameters = new Parameters(params);
        Assert.assertEquals(sysValue, parameters.get(key));
    }


    public static void setEnv(String key, String value) {
        try {
            Map<String, String> env = System.getenv();
            Class<?> cl = env.getClass();
            Field field = cl.getDeclaredField("m");
            field.setAccessible(true);
            Map<String, String> writableEnv = (Map<String, String>) field.get(env);
            writableEnv.put(key, value);
        } catch (Exception e) {
            throw new IllegalStateException("Failed to set environment variable", e);
        }
    }
}
