package com.keli.authserver.config;

import feign.Client;
import feign.hc5.ApacheHttp5Client;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.client5.http.impl.io.PoolingHttpClientConnectionManagerBuilder;
import org.apache.hc.client5.http.io.HttpClientConnectionManager;
import org.apache.hc.client5.http.ssl.NoopHostnameVerifier;
import org.apache.hc.client5.http.ssl.SSLConnectionSocketFactory;
import org.apache.http.ssl.SSLContexts;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.client.loadbalancer.LoadBalancerClient;
import org.springframework.cloud.loadbalancer.support.LoadBalancerClientFactory;
import org.springframework.cloud.openfeign.loadbalancer.FeignBlockingLoadBalancerClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;

import javax.net.ssl.SSLContext;
import java.io.InputStream;
import java.security.KeyStore;

@Configuration
public class FeignTlsConfig {


    @Value("${server.ssl.key-store-client}")
    private Resource keyStoreResource;

    @Value("${server.ssl.key-store-password}")
    private String keyStorePassword;

    @Value("${server.ssl.trust-store}")
    private Resource trustStoreResource;

    @Value("${server.ssl.trust-store-password}")
    private String trustStorePassword;

    @Bean
    public Client sslFeignClient(LoadBalancerClient loadBalancerClient,LoadBalancerClientFactory loadBalancerClientFactory) throws Exception {
        // 1. 加载客户端密钥库（包含客户端证书和私钥）
        KeyStore keyStore = KeyStore.getInstance("PKCS12");
        try (InputStream is = keyStoreResource.getInputStream()) {
            keyStore.load(is, keyStorePassword.toCharArray());
        }

        // 2. 加载信任库（包含根 CA 证书）
        KeyStore trustStore = KeyStore.getInstance("PKCS12");
        try (InputStream is = trustStoreResource.getInputStream()) {
            trustStore.load(is, trustStorePassword.toCharArray());
        }

        // 3. 构建 SSLContext
        SSLContext sslContext = SSLContexts.custom()
                .loadKeyMaterial(keyStore, keyStorePassword.toCharArray())
                .loadTrustMaterial(trustStore, (chain, authType) -> true) // 信任所有由根 CA 签发的证书
                .build();

        // 4. 创建 SSL 连接工厂（HttpClient 5.x 的 SSLConnectionSocketFactory）
        SSLConnectionSocketFactory sslSocketFactory = new SSLConnectionSocketFactory(
                sslContext,
                NoopHostnameVerifier.INSTANCE  // 开发环境跳过主机名验证
        );
        // 5. 配置连接管理器
        HttpClientConnectionManager connectionManager = PoolingHttpClientConnectionManagerBuilder.create()
                .setSSLSocketFactory(sslSocketFactory)
                .build();

        // 6. 创建 HttpClient 5.x 的 CloseableHttpClient
        CloseableHttpClient httpClient = HttpClients.custom()
                .setConnectionManager(connectionManager)
                .build();

        Client lbClient = new FeignBlockingLoadBalancerClient(
                new ApacheHttp5Client(httpClient),   // 底层 HTTP 客户端
                loadBalancerClient,
                loadBalancerClientFactory);
        return lbClient;
    }
}
