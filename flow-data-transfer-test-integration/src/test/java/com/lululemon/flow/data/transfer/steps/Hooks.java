package com.lululemon.flow.data.transfer.steps;

import com.lululemon.flow.data.transfer.context.TestContext;
import cucumber.api.Scenario;
import cucumber.api.java.Before;

public class Hooks {
    private static Scenario scenarioInstance;
    public static Scenario getScenarioInstance(){
        return scenarioInstance;
    }

    @Before
    public static void setScenarioInstance(Scenario scenario){
        TestContext.getAll().clear();
        scenarioInstance=scenario;
    }
}
