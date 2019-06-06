package com.lululemon.flow.data.transfer.config;

import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.jvm.GarbageCollectorMetricSet;
import com.codahale.metrics.jvm.MemoryUsageGaugeSet;
import com.codahale.metrics.jvm.ThreadStatesGaugeSet;
import com.lululemon.flow.data.transfer.job.AbstractJob;
import com.lululemon.flow.data.transfer.api.JobRequest;
import com.lululemon.flow.data.transfer.job.impl.SqlJob;
import org.coursera.metrics.datadog.DatadogReporter;
import org.coursera.metrics.datadog.transport.UdpTransport;
import org.springframework.batch.core.configuration.JobRegistry;
import org.springframework.batch.core.configuration.annotation.BatchConfigurer;
import org.springframework.batch.core.configuration.annotation.DefaultBatchConfigurer;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.configuration.support.MapJobRegistry;
import org.springframework.batch.core.explore.JobExplorer;
import org.springframework.batch.core.explore.support.JobExplorerFactoryBean;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.batch.core.launch.support.SimpleJobLauncher;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.repository.support.JobRepositoryFactoryBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Scope;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.retry.backoff.ExponentialBackOffPolicy;
import org.springframework.retry.policy.SimpleRetryPolicy;
import org.springframework.retry.support.RetryTemplate;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.transaction.PlatformTransactionManager;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

import javax.sql.DataSource;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import static java.util.concurrent.TimeUnit.SECONDS;

@EnableSwagger2
@Configuration
@EnableBatchProcessing
@Import(value = {WebSecurityConfig.class})
public class Config {

    public static final String DB_DRIVER = "org.postgresql.Driver";

    @Bean
    public MetricRegistry metricRegistry() {
        return new MetricRegistry();
    }

    @Bean
    @ConditionalOnProperty(name = "metrics.reporter.enabled", havingValue = "true", matchIfMissing = true)
    public DatadogReporter datadogReporter(@Autowired MetricRegistry metricRegistry,
                                           @Value("#{${metrics.tags}}") Map<String, String> tags) {

        List<String> metricsTags = Optional.ofNullable(tags)
                .map(t -> t.entrySet()
                        .stream()
                        .map(e -> e.getKey() + ":" + e.getValue())
                        .collect(Collectors.toList()))
                .orElse(new ArrayList<>());

        metricRegistry.register("jvm.memory", new MemoryUsageGaugeSet());
        metricRegistry.register("jvm.thread", new ThreadStatesGaugeSet());
        metricRegistry.register("jvm.gs", new GarbageCollectorMetricSet());

        UdpTransport udpTransport = new UdpTransport.Builder()
                .withStatsdHost(getLocalAddress())
                .build();

        DatadogReporter reporter = DatadogReporter.forRegistry(metricRegistry)
                .withTransport(udpTransport)
                .withTags(metricsTags)
                .build();

        reporter.start(10, SECONDS);
        return reporter;
    }

    @Bean
    @Scope("prototype")
    public AbstractJob job(JobRequest request){
        AbstractJob job = null;
        if (request instanceof JobRequest.Transform) {
            job = new SqlJob("transform");
        }
        if (job == null) {
            throw new IllegalArgumentException("Unknown job type " + request.toString());
        }
        job.setRetryTemplate(retryTemplate(4, 3000,5000, 2));
        return job;
    }


    @Bean
    @ConfigurationProperties("db")
    public DataSource transformDataSource(@Value("${db.url}") String dbUrl,
                                          @Value("${db.username}") String dbUser,
                                          @Value("${db.password}") String dbPassword) {
        DriverManagerDataSource dataSource = new DriverManagerDataSource();
        dataSource.setDriverClassName(DB_DRIVER);
        dataSource.setUrl(dbUrl);
        dataSource.setUsername(dbUser);
        dataSource.setPassword(dbPassword);

        return dataSource;
    }

    @Bean
    public JobRepository getJobRepository(DataSource dataSource, PlatformTransactionManager transactionManager) throws Exception {
        JobRepositoryFactoryBean factory = new JobRepositoryFactoryBean();
        factory.setDataSource(dataSource);
        factory.setTransactionManager(transactionManager);
        factory.afterPropertiesSet();
        return factory.getObject();
    }

    @Bean
    public ThreadPoolTaskExecutor taskExecutor(@Value("${thread.pool.core.size: 5}") int corePoolSize,
                                               @Value("${thread.pool.max.size: 20}") int maxPoolSize,
                                               @Value("${thread.pool.queue.size: 100}") int queuePoolSize) {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(corePoolSize);
        executor.setMaxPoolSize(maxPoolSize);
        executor.setQueueCapacity(queuePoolSize);
        return executor;
    }


    @Bean
    public JobLauncher jobLauncher(ThreadPoolTaskExecutor taskExecutor, JobRepository jobRepository) {
        SimpleJobLauncher launcher = new SimpleJobLauncher();
        launcher.setTaskExecutor(taskExecutor);
        launcher.setJobRepository(jobRepository);
        return launcher;
    }

    @Bean
    public JobExplorer jobExplorer(final DataSource batchDataSource) throws Exception {
        final JobExplorerFactoryBean bean = new JobExplorerFactoryBean();
        bean.setDataSource(batchDataSource);
        bean.setTablePrefix("BATCH_");
        bean.setJdbcOperations(new JdbcTemplate(batchDataSource));
        bean.afterPropertiesSet();
        return bean.getObject();
    }

    @Bean
    public BatchConfigurer batchConfigurer(DataSource batchDataSource) {
        return new DefaultBatchConfigurer(batchDataSource);
    }

    @Bean
    public JobRegistry jobRegistry() {
        return new MapJobRegistry();
    }

    @Bean
    public Docket batchApi() {
        return new Docket(DocumentationType.SWAGGER_2)
                .select().apis(RequestHandlerSelectors.basePackage("com.lululemon.flow"))
                .paths(PathSelectors.any())
                .build();
    }


    private static String getLocalAddress() {
        String hostname = System.getenv().get("HOSTNAME");
        return hostname != null ? hostname : "localhost";
    }

    @Bean
    public RetryTemplate retryTemplate(@Value("${retry.attempts: 4}") int retryAttempts,
                                       @Value("${retry.backoff.initialinterval: 3000}") int retryBackoffInitialinterval,
                                       @Value("${retry.backoff.maxinterval: 5000}") int retryBackoffMaxinterval,
                                       @Value("${retry.backoff.multiplier: 2}") int retryBackoffMultiplier) {
        SimpleRetryPolicy retryPolicy = new SimpleRetryPolicy(retryAttempts);
        ExponentialBackOffPolicy backOffPolicy = new ExponentialBackOffPolicy();

        // Set the max retry attempts
        backOffPolicy.setInitialInterval(retryBackoffInitialinterval);
        backOffPolicy.setMaxInterval(retryBackoffMaxinterval);
        backOffPolicy.setMultiplier(retryBackoffMultiplier);

        // Use the policy...
        RetryTemplate template = new RetryTemplate();
        template.setRetryPolicy(retryPolicy);
        template.setBackOffPolicy(backOffPolicy);
        return template;
    }

}
