package com.lululemon.flow.data.transfer.client;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriTemplate;

@Component
public class DataTransferService {

    private static final MultiValueMap<String, String> HEADERS = new HttpHeaders() {{
        add("Content-type", "Application/json");
    }};

    @Value("${data-transfer-service.auth.header.name}")
    private String authHeaderKey;

    @Value("${data-transfer-service.auth.header.value}")
    private String authHeaderValue;

    @Value("${data-transfer-service.url}")
    private String url;

    @Autowired
    private RestTemplate restTemplate;

    public String executeRequest(Actions action, String body) {
        switch (action) {
            case Transform:
                return transform(body);
            case Extract:
                return extract(body);
            case Load:
                return load(body);
            case GetInfo:
                return getInfo(body);
        }
        return null;
    }

    private String transform(String body) {
        RequestEntity<String> request = RequestEntity
                .post(new UriTemplate(url).expand("transform"))
                .contentType(MediaType.APPLICATION_JSON)
                .header(authHeaderKey, authHeaderValue)
                .accept(MediaType.APPLICATION_JSON)
                .body(body);
        return execute(request);
    }

    private String extract(String body) {
        RequestEntity<String> request = RequestEntity
                .post(new UriTemplate(url).expand("extract"))
                .contentType(MediaType.APPLICATION_JSON)
                .header(authHeaderKey, authHeaderValue)
                .accept(MediaType.APPLICATION_JSON)
                .body(body);
        return execute(request);
    }

    private String load(String body) {
        RequestEntity<String> request = RequestEntity
                .post(new UriTemplate(url).expand("load"))
                .contentType(MediaType.APPLICATION_JSON)
                .header(authHeaderKey, authHeaderValue)
                .accept(MediaType.APPLICATION_JSON)
                .body(body);
        return execute(request);
    }

    private String getInfo(String id) {
        RequestEntity request = RequestEntity
                .get(new UriTemplate(url).expand(id))
                .header(authHeaderKey, authHeaderValue)
                .accept(MediaType.APPLICATION_JSON)
                .build();
        return execute(request);
    }

    private String execute(RequestEntity requestEntity) {
        ResponseEntity<String> response = restTemplate.exchange(requestEntity, String.class);
        return response.getBody();
    }

    public enum Actions {
        Transform,
        Extract,
        Load,
        GetInfo;
    }

}
