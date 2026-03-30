package com.keli.authlogin.config;


import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {
    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("颗粒认证中心API 文档")
                        .version("1.0.0")
                        .description("颗粒认证中心是由kelinls开发的一个登录认证中心，此文档为其 API 文档")
                        .contact(new Contact()
                                .name("技术支持")
                                .email("mupa18852075@163.com")
                                .url("https://example.com"))
                        .license(new License()
                                .name("Apache 2.0")
                                .url("https://www.apache.org/licenses/LICENSE-2.0.html")))
                .addServersItem(new Server()
                        .url("https://api.example.com/v1")
                        .description("生产环境"));
    }
}
