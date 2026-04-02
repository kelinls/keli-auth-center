package com.keli.authlogin.config;

import com.aliyun.captcha20230305.Client;
import com.aliyun.teaopenapi.models.Config;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;


@Configuration
@Slf4j
public class AliyunConfig {
    @Value("${alibabacloud.endpoint}")
    private String endpoint;
    @Value("${alibabacloud.accessKeyId}")
    private String accessKeyId;
    @Value("${alibabacloud.accessKeySecret}")
    private String accessKeySecret;
    @Bean
    public Client getClient() throws Exception {
        log.info("accessKeyId:{}, accessKeySecret:{}, endpoint:{}", accessKeyId, accessKeySecret,endpoint);
        com.aliyun.credentials.Client client = new com.aliyun.credentials.Client();
        Config config = new Config();
        config.setAccessKeyId(accessKeyId);
        config.setAccessKeySecret(accessKeySecret);
        config.endpoint = endpoint;
        config.setCredential(client);
        return new Client(config);
    }
}
