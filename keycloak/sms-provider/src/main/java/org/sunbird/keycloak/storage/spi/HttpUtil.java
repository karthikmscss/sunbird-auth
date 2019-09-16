package org.sunbird.keycloak.storage.spi;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.jboss.logging.Logger;
import org.sunbird.keycloak.utils.Constants;

import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.Map;

public class HttpUtil {
    private static Logger logger = Logger.getLogger(HttpUtil.class);
    public static Map<String, Object> post(Map<String, Object> requestBody, String uri,
                                           String authorizationKey) {
        logger.info("UserSearchService: post called");
        try (CloseableHttpClient client = HttpClients.createDefault()) {
            ObjectMapper mapper = new ObjectMapper();
            HttpPost httpPost = new HttpPost(uri);
            logger.info("UserSearchService:post: uri = " + uri+ ", body = "+requestBody);
            String authKey = Constants.BEARER + " " + authorizationKey;
            StringEntity entity = new StringEntity(mapper.writeValueAsString(requestBody));
            httpPost.setEntity(entity);
            httpPost.setHeader(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON);
            httpPost.setHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON);
            if (StringUtils.isNotBlank(authKey)) {
                httpPost.setHeader(HttpHeaders.AUTHORIZATION, authKey);
            }
            CloseableHttpResponse response = client.execute(httpPost);
            logger.info("UserSearchService:post: statusCode = " + response.getStatusLine().getStatusCode());
            return mapper.readValue(response.getEntity().getContent(),
                    new TypeReference<Map<String, Object>>() {});
        } catch (Exception e) {
            logger.error("UserSearchService:post: Exception occurred = " + e);
        }
        return null;
    }

    public static HttpResponse post2(Map<String, Object> requestBody, String uri,
                                           String authorizationKey) {
        logger.info("UserSearchService: post called");
        try (CloseableHttpClient client = HttpClients.createDefault()) {
            ObjectMapper mapper = new ObjectMapper();
            HttpPost httpPost = new HttpPost(uri);
            logger.info("UserSearchService:post: uri = " + uri+ ", body = "+requestBody);
            String authKey = Constants.BEARER + " " + authorizationKey;
            StringEntity entity = new StringEntity(mapper.writeValueAsString(requestBody));
            httpPost.setEntity(entity);
            httpPost.setHeader(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON);
            httpPost.setHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON);
            if (StringUtils.isNotBlank(authKey)) {
                httpPost.setHeader(HttpHeaders.AUTHORIZATION, authKey);
            }
            CloseableHttpResponse response = client.execute(httpPost);
            logger.info("UserSearchService:post: statusCode = " + response.getStatusLine().getStatusCode());
            return response;
        } catch (Exception e) {
            logger.error("UserSearchService:post: Exception occurred = " + e);
        }
        return null;
    }

    public static Map<String, Object> getResponse(HttpResponse response) {
        InputStream inStream = null;
        BufferedReader reader = null;
        StringBuilder builder = new StringBuilder();
        if (200 == response.getStatusLine().getStatusCode()) {
            try {
                inStream = response.getEntity().getContent();
                reader = new BufferedReader(new InputStreamReader(inStream, StandardCharsets.UTF_8));
                String line = null;
                while ((line = reader.readLine()) != null) {
                    builder.append(line);
                    String res = builder.toString();
                    if (StringUtils.isNotBlank(res)) {
                        ObjectMapper mapper = new ObjectMapper();
                        return mapper.readValue(res, new TypeReference<Map<String, Object>>() {});
                    }
                }
            } catch (Exception ex) {
                logger.error("UserSearchService:getResponse: Exception occurred = " + ex);
            }
        }
        return Collections.emptyMap();
    }
}
