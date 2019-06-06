package com.lululemon.flow.data.transfer.job.impl;

import com.lululemon.flow.data.transfer.params.Parameters;
import org.junit.Before;
import org.junit.Test;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

public class SqlJobTest {
    private Statement stmt;

    @Before
    public void before() throws Exception {
        Connection conn = DriverManager.getConnection("jdbc:h2:mem:test");
        stmt = conn.createStatement();
        stmt.execute("DROP TABLE IF EXISTS TEST_TABLE_2");
        stmt.execute("CREATE TABLE TEST_TABLE_2 (COLUMN1 VARCHAR(10))");
    }

    @Test
    public void testRun() throws Exception {
        SqlJob job = new SqlJob("job1");

        Map<String, String> params = new HashMap<>();
        params.put("reporter.enabled", "false");
        params.put("db.url", "jdbc:h2:mem:test");
        params.put("sql", "query.sql");

        job.run(null, new Parameters(params));

        ResultSet rs = stmt.executeQuery("SELECT * FROM TEST_TABLE_2");
        rs.next();

        assertEquals("value 1", rs.getString(1));
    }

    @Test
    public void testRunWithReadOnlyNoUrl() {
        SqlJob job = new SqlJob("job1");

        Map<String, String> params = new HashMap<String, String>();
        params.put("reporter.enabled", "false");
        params.put(Parameters.PARAM_SQL, "query.sql");

        try {
            job.run(null, new Parameters(params));
        } catch (RuntimeException re) {
            // Verify
            assertEquals("Can't execute job job1", re.getMessage());
            return;
        }
        fail("Test should throw exception");
    }

    @Test
    public void testRunException() {
        SqlJob job = new SqlJob("job1");

        Map<String, String> params = new HashMap<>();
        params.put("reporter.enabled", "false");
        params.put("db.url", "jdbc:h1:mem:test");
        params.put("sql", "query.sql");

        try {
            job.run(null, new Parameters(params));
        } catch (RuntimeException e) {

            // Verify
            assertEquals("Can't execute job job1", e.getMessage());
            return;
        }

        fail("Test should throw exception");
    }

    @Test(expected = RuntimeException.class)
    public void testConnectException() {

        // Setup
        SqlJob job = new SqlJob("job1");

        // Execute
        job.connect(null, false, false);
    }
}
