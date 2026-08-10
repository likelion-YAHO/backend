package com.likelion.backend.global.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfig {

  private static final String BEARER_AUTH_SCHEME_NAME = "bearerAuth";

  @Value("${server.servlet.context-path:}")
  private String contextPath;

  @Bean
  public OpenAPI customOpenAPI() {
    Server localServer = new Server();
    localServer.setUrl(contextPath);
    localServer.setDescription("Default Server");

    SecurityScheme bearerAuthScheme = new SecurityScheme()
        .type(SecurityScheme.Type.HTTP)
        .scheme("bearer")
        .bearerFormat("JWT")
        .description("로그인 응답으로 받은 accessToken을 입력");

    return new OpenAPI()
        .addServersItem(localServer)
        .info(new Info()
            .title("MCM 업사이클링 서비스 API 명세서")
            .version("1.0")
            .description("""
                멋쟁이 사자들 해커톤 - MCM 업사이클링 서비스 API 명세서입니다.
                """))
        .components(new Components().addSecuritySchemes(BEARER_AUTH_SCHEME_NAME, bearerAuthScheme))
        .addSecurityItem(new SecurityRequirement().addList(BEARER_AUTH_SCHEME_NAME));
  }
}
