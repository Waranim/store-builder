package xyz.waranim.apigateway.filter;

import lombok.RequiredArgsConstructor;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.cloud.gateway.support.ServerWebExchangeUtils;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;
import xyz.waranim.common.jwt.JwtUtils;

import java.util.List;
import java.util.Objects;

@Component
@RequiredArgsConstructor
public class JwtAuthFilter implements GlobalFilter {
    private final JwtUtils jwtUtils;

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        String originalPath = Objects.requireNonNull(
                exchange.getAttribute(ServerWebExchangeUtils.GATEWAY_ORIGINAL_REQUEST_URL_ATTR))
                .toString()
                .toLowerCase();

        if (originalPath.split("/")[4].equals("swagger-ui")
                || originalPath.split("/")[4].equals("swagger-ui.html")
                || originalPath.split("/")[4].equals("v3")) {
            return chain.filter(exchange);
        }

        String token = extractToken(request);

        if (token == null) {
            return sendError(exchange, HttpStatus.UNAUTHORIZED, "Missing token");
        }

        if (!jwtUtils.validateToken(token)) {
            return sendError(exchange, HttpStatus.FORBIDDEN, "Invalid token");
        }

        String userId = jwtUtils.extractUserId(token);
        String username = jwtUtils.extractEmail(token);
        List<String> roles = jwtUtils.extractRoles(token);

        ServerHttpRequest modifiedRequest = request.mutate()
                .header("X-User-Id", userId)
                .header("X-Username", username)
                .header("X-User-Roles", String.join(",", roles))
                .headers(headers -> headers.remove("Authorization"))
                .build();

        return chain.filter(exchange.mutate().request(modifiedRequest).build());
    }

    private String extractToken(ServerHttpRequest request) {
        String header = request.getHeaders().getFirst("Authorization");
        return (header != null && header.startsWith("Bearer "))
                ? header.substring(7)
                : null;
    }

    private Mono<Void> sendError(ServerWebExchange exchange, HttpStatus status, String message) {
        exchange.getResponse().setStatusCode(status);
        exchange.getResponse().getHeaders().add("Content-Type", "text/plain");
        return exchange.getResponse().writeWith(
                Mono.just(exchange.getResponse().bufferFactory().wrap(message.getBytes()))
        );
    }
}
