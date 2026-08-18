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
                로그인 후 Authorize에 accessToken을 넣습니다.

                ## 업사이클링 호출 순서
                1. `POST /api/products` - 사진 + 카테고리 등록, AI 상태 분석
                2. `POST /api/products/{productId}/design-analysis` - 시안 추천 (응답의 designOptions, recommendedCharmId, recommendedScarfId 사용)
                3. `GET /api/colors/point`, `GET /api/colors/metal`, `GET /api/add-on-products` - 스와치/추가상품
                4. `POST /api/products/{productId}/design-preview` - 선택한 시안+색+참+스카프 미리보기
                5. `POST /api/products/{productId}/reforms` - 선택 완료/견적 (previewImageUrl은 4의 응답을 넣거나 생략)
                6. 이후 예약은 reformId로 `POST /api/reservations`

                `GET /api/products/{productId}` 로 등록/시안/추천값을 다시 조회할 수 있습니다.

                ## LAB 호출 순서
                1. `GET /api/lab/base-products` — 베이스 가방 확인 (응답 code를 이후 baseProduct에 사용)
                2. `GET /api/lab/missions/current` — 이달의 미션 확인 (출품 시 missionId)
                3. `POST /api/lab/designs/generate` — 시안 생성(미션+베이스당 최대 3회). 응답 imageUrl, recommendedCharmId, recommendedScarfId
                4. 색/추가상품은 업사이클과 동일 (`/api/colors/*`, `/api/add-on-products`)
                5. `POST /api/lab/designs/preview` — sourceImageUrl에 3의 imageUrl -> 선택한 색/참/스카프 합성된 AI 생성 이미지 
                6. `POST /api/lab/designs` — 디자인 출품 (imageUrl에는 3(추가상품X) 또는 5(추가상품O)의 이미지 URL 중 갤러리에 올릴 것을 넣음)
                7. `GET /api/lab/designs` — 디자인 출품 갤러리
                8. `DELETE /api/lab/designs/{designId}` — 본인 가상 출품 삭제 (디자인 출품 갤러리에서 삭제 처리)
                """))
        .components(new Components().addSecuritySchemes(BEARER_AUTH_SCHEME_NAME, bearerAuthScheme))
        .addSecurityItem(new SecurityRequirement().addList(BEARER_AUTH_SCHEME_NAME));
  }
}
