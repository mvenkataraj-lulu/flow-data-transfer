package com.lululemon.flow.data.transfer;

import lombok.Builder;
import lombok.Value;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.support.PropertiesLoaderUtils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Properties;
import java.util.stream.Collectors;

@Slf4j
public class Utils {

    private Utils() { // $COVERAGE-IGNORE$

        // empty
    } // $COVERAGE-IGNORE$


    public static final String TRUE = "true";

    public static String fileAsString(String fileName, ResourceLoader resourceLoader) throws IOException {
        String newLine = System.getProperty("line.separator");
        InputStream inputStream;
        if (resourceLoader != null) {
            inputStream = resourceLoader.getResource("classpath:" + fileName).getInputStream();
        } else {
            inputStream = ClassLoader.getSystemClassLoader().getResourceAsStream(fileName);
        }
        if (inputStream == null) {
            throw new RuntimeException("Can not find file " + fileName);
        }
        return new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8))
                .lines().collect(Collectors.joining(newLine));
    }

    public static void logMdc(final String key, final String value) {
        MDC.put(key, value);
    }

    public static void clearMdc(){
        MDC.clear();
    }
}
