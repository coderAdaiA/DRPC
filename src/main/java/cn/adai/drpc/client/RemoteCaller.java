package cn.adai.drpc.client;

import cn.adai.drpc.config.DRPCConfig;
import cn.adai.drpc.config.DRPCSocketConfig;
import cn.adai.drpc.consts.DRPCConstants;
import com.alibaba.fastjson2.JSON;
import cn.adai.drpc.exception.DRPCException;
import cn.adai.drpc.model.DRPCRequest;
import cn.adai.drpc.model.DRPCResponse;
import okhttp3.*;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;
import java.net.URL;
import java.util.concurrent.TimeUnit;

/**
 * @author: coderAdai
 * @date 2022/08/18 11:36
 * @description: TODO
 */
@Component
public class RemoteCaller implements ApplicationContextAware {

    private static final MediaType MEDIA_TYPE_JSON = MediaType.get("application/json; charset=utf-8");

    private static final String DRPC_CONTROLLER_PATH = "/drpc";

    private static ApplicationContext applicationContext;

    private volatile OkHttpClient httpClient;

    public DRPCResponse call(DRPCRequest request) throws DRPCException {
        DRPCConfig.RemoteServerConfig remoteServer = this.getDRPConfig().getRemoteServer(request.getServerName());
        if (remoteServer == null) {
            throw new DRPCException("Server [{}] not found.", request.getServerName());
        }

        try {
            URL url = UriComponentsBuilder.newInstance().scheme(remoteServer.getSchema())
                    .host(remoteServer.getEndpoint())
                    .port(remoteServer.getPort())
                    .path(DRPC_CONTROLLER_PATH)
                    .build().toUri().toURL();
            return JSON.parseObject(this.sendPost(url, request), DRPCResponse.class);
        } catch (Exception e) {
            return DRPCResponse.makeFailResult("Remote call failed, message: " + e.getMessage());
        }
    }

    private String sendPost(URL url, Object obj) throws IOException {
        OkHttpClient client = this.getHttpClient();
        RequestBody body = RequestBody.create(JSON.toJSONString(obj), MEDIA_TYPE_JSON);
        Request request = new Request.Builder().url(url).post(body)
                .header(HttpHeaders.CONNECTION, "close")
                .header(HttpHeaders.USER_AGENT, DRPCConstants.REMOTE_CALLER_USERAGENT)
                .build();

        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                return null;
            }

            try (ResponseBody responseBody = response.body()) {
                if (responseBody != null) {
                    return responseBody.string();
                }
            }
        }

        return null;
    }

    private OkHttpClient getHttpClient() {
        if (this.httpClient == null) {
            this.prepareHttpClient();
        }

        return this.httpClient;
    }

    private void prepareHttpClient() {
        synchronized (this) {
            if (this.httpClient != null) {
                return;
            }

            DRPCSocketConfig socketConfig = this.getDRPConfig().getSocket();
            ConnectionPool connectionPool = new ConnectionPool(socketConfig.getMaxIdleConnections(), socketConfig.getKeepAliveDurationInMin(),
                    TimeUnit.MINUTES);
            this.httpClient = new OkHttpClient().newBuilder()
                    .connectTimeout(socketConfig.getConnectTimeoutInMs(), TimeUnit.MILLISECONDS)
                    .readTimeout(socketConfig.getReadTimeoutInMs(), TimeUnit.MILLISECONDS)
                    .writeTimeout(socketConfig.getWriteTimeoutInMs(), TimeUnit.MILLISECONDS)
                    .connectionPool(connectionPool).build();
        }
    }

    private DRPCConfig getDRPConfig() {
        DRPCConfig drpcConfig = applicationContext.getBean(DRPCConfig.class);
        Assert.notNull(drpcConfig, "The DRPC config must config.");
        return drpcConfig;
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        RemoteCaller.applicationContext = applicationContext;
    }
}
