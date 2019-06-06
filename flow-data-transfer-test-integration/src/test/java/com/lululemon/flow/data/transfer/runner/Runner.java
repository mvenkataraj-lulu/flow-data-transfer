package com.lululemon.flow.data.transfer.runner;

import cucumber.api.CucumberOptions;
import cucumber.api.junit.Cucumber;
import org.junit.runner.RunWith;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;

@EnableAutoConfiguration
@RunWith(Cucumber.class)
@CucumberOptions(
        glue = {"com.lululemon.flow.data.transfer.steps"},
        features = "classpath:features",
        plugin = {"pretty", "html:target/results", "json:target/results/result.json"},
        tags = {"@Sanity"}
)
public class Runner {
}
