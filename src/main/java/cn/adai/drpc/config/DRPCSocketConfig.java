package cn.adai.drpc.config;

import lombok.Data;

/**
 * @author: coderAdai
 * @date 2022/08/23 10:51
 * @description: TODO
 */
@Data
public class DRPCSocketConfig {
    private Integer connectTimeoutInMs = 500;
    private Integer readTimeoutInMs = 500;
    private Integer writeTimeoutInMs = 500;

    private Integer maxIdleConnections = 50;
    private Integer keepAliveDurationInMin = 5;
}
