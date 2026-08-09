package com.likelion.backend.global;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI openAPI() {
        Info info = new Info()
                .title("해커톤 백엔드 API")
                .description("멋쟁이 사자들 해커톤 - YAHO 백엔드 API 명세서입니다.")
                .version("v0.0.1");

        return new OpenAPI()
                .info(info);
    }
}
