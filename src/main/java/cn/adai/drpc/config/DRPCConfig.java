package cn.adai.drpc.config;

import lombok.Data;
import lombok.Getter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.Assert;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author: coderAdai
 * @date 2022/08/18 14:59
 * @description: TODO
 */
@Getter
@Configuration
@ConfigurationProperties("drpc")
public class DRPCConfig {
    List<RemoteServerConfig> remoteServers = new ArrayList<>();

    DRPCSocketConfig socket = new DRPCSocketConfig();

    private Map<String, RemoteServerConfig> remotesMapCache = null;

    public void setRemotes(List<RemoteServerConfig> remoteServers) {
        this.remoteServers = remoteServers;
        this.remotesMapCache = this.remoteServers.stream()
                .collect(Collectors.toConcurrentMap(RemoteServerConfig::getServerName, config -> config));
    }

    public RemoteServerConfig getRemoteServer(String serverName) {
        Assert.notNull(serverName, "serverName is required");
        return this.remotesMapCache.get(serverName);
    }

    @Data
    public static class RemoteServerConfig {
        private String serverName = "";
        private String schema = "http";
        private String endpoint = "127.0.0.1";
        private Integer port = 80;
    }
}
