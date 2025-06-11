package xyz.waranim.orderservice.config;

import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.ArrayList;
import java.util.List;

@Configuration
@SecurityScheme(
        name = "bearerAuth",
        type = SecuritySchemeType.HTTP,
        scheme = "bearer",
        bearerFormat = "JWT"
)
public class OpenApiConfig {
    @Value("${springdoc.server-url}")
    private String urlServer;

    @Bean
    public OpenAPI customOpenAPI() {
        List<Server> servers = new ArrayList<>();
        Server server = new Server();
        server.setUrl(urlServer);
        servers.add(server);

        return new OpenAPI()
                .info(new io.swagger.v3.oas.models.info.Info()
                        .title("API Сервиса заказов")
                        .version("1.0")
                        .description("API работы с заказами интернет-магазинов"))
                .servers(servers);
    }
}
