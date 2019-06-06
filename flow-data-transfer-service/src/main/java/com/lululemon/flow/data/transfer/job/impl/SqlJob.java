package com.lululemon.flow.data.transfer.job.impl;

import com.lululemon.flow.data.transfer.params.Parameters;
import com.lululemon.flow.data.transfer.Utils;
import com.lululemon.flow.data.transfer.job.AbstractJob;
import com.lululemon.flow.data.transfer.config.Config;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ResourceLoader;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;

import static com.google.common.base.Preconditions.checkNotNull;

@Slf4j
public class SqlJob extends AbstractJob {

    @Autowired
    DataSource batchDataSource;


    public SqlJob(String name) {
        super(name);
    }

    SqlJob(String name, String jobType) {
        super(name, jobType);
    }

    @Override
    protected void execute(ResourceLoader resourceLoader, Parameters parameters) throws Exception {
        String file = parameters.get(Parameters.PARAM_SQL);
        log.info("File to execute {}", file);

        checkNotNull(file, "sql parameter must be provided");

        String sql = Utils.fileAsString(file,resourceLoader);
        log.debug("Query is {}", sql);

        try (Connection conn = connect(parameters, false, true); Statement stmt = conn.createStatement()) {
            conn.setAutoCommit(false);
            stmt.execute(sql);
            conn.commit();
        }

        log.info("Query successfully executed");
    }

    /**
     * Create connection
     *
     * @param parameters parameters
     * @param readOnly   if we can use readOnly instance
     * @return {@link Connection}
     */
    public Connection connect(Parameters parameters, boolean readOnly, boolean allowMutiQuries) {
        try {
            String dbUrl = parameters.get(Parameters.PARAM_DB_URL);
            if(dbUrl!=null){
                Class.forName(Config.DB_DRIVER);
                return DriverManager.getConnection(dbUrl, null, null);
            }else{
                return batchDataSource.getConnection();
            }
        } catch (Exception e) {
            throw new RuntimeException("Can't connect to database", e);
        }
    }
}