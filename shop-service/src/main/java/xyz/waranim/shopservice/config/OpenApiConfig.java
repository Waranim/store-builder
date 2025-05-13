package xyz.waranim.shopservice.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.ArrayList;
import java.util.List;

@Configuration
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
                        .title("API Сервиса магазинов")
                        .version("1.0")
                        .description("API работы с интернет-магазинами"))
                .servers(servers);
    }
}
