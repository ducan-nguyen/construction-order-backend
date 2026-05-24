package com.construction.ordersystem.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import io.swagger.v3.oas.annotations.servers.Server;
import org.springframework.context.annotation.Configuration;

@Configuration
@OpenAPIDefinition(
    info = @Info(
        title       = "Construction Order Management API",
        version     = "1.0",
        description = "REST API cho hệ thống quản lý đơn hàng vật liệu xây dựng.\n\n" +
                      "**Hướng dẫn:** Gọi `POST /api/auth/login` để lấy token, " +
                      "sau đó bấm **Authorize** và dán vào ô Bearer token.",
        contact     = @Contact(name = "Construction Order System", email = "admin@construction.com")
    ),
    servers = {
        @Server(url = "http://localhost:8080", description = "Local development"),
        @Server(url = "https://construction-order-api.up.railway.app", description = "Production (Railway)")
    }
)
@SecurityScheme(
    name        = "bearerAuth",
    type        = SecuritySchemeType.HTTP,
    scheme      = "bearer",
    bearerFormat = "JWT",
    description = "Lấy token từ POST /api/auth/login → field \"token\", dán vào đây"
)
public class OpenApiConfig {
    // Cấu hình Swagger UI / OpenAPI 3.0
    // Truy cập: http://localhost:8080/swagger-ui/index.html
}
