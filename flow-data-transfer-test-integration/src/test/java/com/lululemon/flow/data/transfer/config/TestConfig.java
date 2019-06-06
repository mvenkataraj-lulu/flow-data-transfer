package com.lululemon.flow.data.transfer.config;

import org.apache.http.config.Registry;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.conn.socket.ConnectionSocketFactory;
import org.apache.http.conn.socket.PlainConnectionSocketFactory;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.ssl.SSLContextBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.web.client.RestTemplate;

import javax.net.ssl.SSLContext;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;

@EnableAutoConfiguration(exclude = {DataSourceAutoConfiguration.class})
@Configuration
@ComponentScan(basePackages = {"com.lululemon.flow.data.transfer.context",
                "com.lululemon.flow.data.transfer.client"})
public class TestConfig {

    @Value("${data-transfer-service.client.read.timeout}")
    private Integer readTimeout;

    @Value("${data-transfer-service.client.connect.timeout}")
    private Integer connectionTimeout;

    @Value("${truststore.path}")
    private String keyStorePath;

    @Value("${truststore.password}")
    private char[] storePassword;

    @Value("${data-transfer-service.ssl.enabled}")
    private boolean sslEnabled;


    @Bean
    public KeyStore keyStore() throws IOException, KeyStoreException, NoSuchAlgorithmException, CertificateException {
        KeyStore keystore = KeyStore.getInstance("jks");
        if(sslEnabled) {
            try (InputStream inputStream = new FileInputStream(keyStorePath)) {
                // load keystore
                keystore.load(inputStream, storePassword);
            }
        }
        return keystore;
    }

    @Bean
    public RestTemplate restTemplate(KeyStore keyStore)
            throws KeyManagementException, UnrecoverableKeyException, NoSuchAlgorithmException, KeyStoreException {

        Registry<ConnectionSocketFactory> socketFactoryRegistry;
        if(sslEnabled) {
            SSLContext sslContext = SSLContextBuilder.create()
                    .loadTrustMaterial(keyStore, null).build();

            SSLConnectionSocketFactory sslConnectionSocketFactory = new SSLConnectionSocketFactory(sslContext);

            socketFactoryRegistry = RegistryBuilder.<ConnectionSocketFactory>create()
                    .register("https", sslConnectionSocketFactory)
                    .register("http", new PlainConnectionSocketFactory())
                    .build();
        }else{
            socketFactoryRegistry = RegistryBuilder.<ConnectionSocketFactory>create()
                    .register("http", new PlainConnectionSocketFactory())
                    .build();
        }

        PoolingHttpClientConnectionManager connectionManager = new PoolingHttpClientConnectionManager(
                socketFactoryRegistry);


        CloseableHttpClient httpClient = HttpClients.custom().setConnectionManager(connectionManager).build();

        HttpComponentsClientHttpRequestFactory clientHttpRequestFactory = new HttpComponentsClientHttpRequestFactory(
                httpClient);
        clientHttpRequestFactory.setReadTimeout(readTimeout);
        clientHttpRequestFactory.setConnectTimeout(connectionTimeout);

        RestTemplate restTemplate = new RestTemplate(clientHttpRequestFactory);
        restTemplate.getMessageConverters().add(0, new StringHttpMessageConverter(Charset.forName("UTF-8")));
        return restTemplate;
    }
}
