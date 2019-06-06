package com.lululemon.flow.data.transfer.steps;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Stopwatch;
import com.lululemon.flow.data.transfer.client.DataTransferService;
import com.lululemon.flow.data.transfer.config.TestConfig;
import com.lululemon.flow.data.transfer.context.TestContext;
import com.lululemon.flow.data.transfer.exception.DataTransferServiceTestException;
import cucumber.api.java.en.Then;
import cucumber.api.java.en.When;
import org.apache.commons.collections4.CollectionUtils;
import org.junit.Assert;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.ConfigFileApplicationContextInitializer;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.util.StringUtils;
import org.springframework.web.client.HttpServerErrorException;

import java.io.IOException;
import java.util.Collection;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@ContextConfiguration(classes = {TestConfig.class}, initializers = {ConfigFileApplicationContextInitializer.class})
public class TestSteps {

    private static final String RESPDIFF = "Difference for key [%s]: Actual: [%s]  Expected: [%s]";
    private static final String WORK_STARTED = "status\":\"START";
    private static final String ERROR_EXCEED_TIME = "Process time exceeded limit of %d so failing test";
    public static final String JOB_ID = "executionId";
    private static final long WAIT_TIME = 100L;
    private static final long MAX_WAIT_TIME = 80000L;

    @Autowired
    private DataTransferService dataTransferService;

    @When("^(.*) request is sent to batch endpoint with body$")
    public void requestIsSentToBatchEndpoint(String action, String body) throws Throwable {
        if (action.equalsIgnoreCase("GetInfo")) {
            body = TestContext.getExecutionId().toString();
        }
        try {
            String response = dataTransferService.executeRequest(DataTransferService.Actions.valueOf(action), body);

            ObjectMapper objectMapper = new ObjectMapper();
            Map<String, Object> actual = objectMapper.readValue(response, Map.class);

            Hooks.getScenarioInstance().write(response);
            TestContext.setResponse(response);
            TestContext.setExecutionId((Integer) actual.get(JOB_ID));
        } catch (HttpServerErrorException ex) {
            Hooks.getScenarioInstance().write(ex.getMessage());
            TestContext.setResponse(ex.getMessage());
        }

    }

    @Then("^Such response is expected$")
    public void suchResponseIsExpected(String expectedResp) throws Throwable {
        String diff = responseCheck(TestContext.getResponse(), expectedResp);
        Hooks.getScenarioInstance().write(TestContext.getResponse());
        Assert.assertTrue(diff, diff.isEmpty());
    }


    @When("^Batch request completed with response$")
    public void batchRequestCompletedResponseIs(String expected) throws InterruptedException, IOException {
        String actual = queryResponse();
        Hooks.getScenarioInstance().write(actual);
        String diff = responseCheck(actual, expected);
        Assert.assertTrue(diff + "\n Original response" + actual, diff.isEmpty());
    }


    private String queryResponse() throws InterruptedException {
        String result = null;
        Stopwatch stopwatch = Stopwatch.createStarted();
        String id = String.valueOf(TestContext.getExecutionId());
        while (StringUtils.isEmpty(result)) {
            String response = dataTransferService.executeRequest(DataTransferService.Actions.GetInfo, id);
            if (response.contains(WORK_STARTED)) {
                Thread.sleep(WAIT_TIME);
            } else if (stopwatch.elapsed(TimeUnit.MILLISECONDS) > MAX_WAIT_TIME) {
                throw new DataTransferServiceTestException(String.format(ERROR_EXCEED_TIME, MAX_WAIT_TIME));
            } else {
                result = response;
            }
        }
        return result;
    }

    private String responseCheck(String actualResp, String expectedResp) throws IOException {
        ObjectMapper objectMapper = new ObjectMapper();
        Map<String, Object> expected = objectMapper.readValue(expectedResp, Map.class);
        Map<String, Object> actual = objectMapper.readValue(actualResp, Map.class);

        //Check row number
        Collection result = CollectionUtils.subtract(expected.keySet(), actual.keySet());
        StringBuilder difference = new StringBuilder();
        if (!result.isEmpty()) {
            difference.append("Structure diff");
            result.forEach(difference::append);
        }

        for (String key : expected.keySet()) {
            String expValue = getStrValue(expected.get(key));
            String actVal = getStrValue(actual.get(key));

            if (!expValue.equalsIgnoreCase("#IGNORE#")) {
                if (!actVal.contains(expValue)) {
                    difference.append(String.format(RESPDIFF, key, actVal, expValue));
                }
            }
        }
        return difference.toString();
    }

    private String getStrValue(Object object) {
        if (!(object instanceof String)) {
            return String.valueOf(object);
        } else return (String) object;
    }
}

