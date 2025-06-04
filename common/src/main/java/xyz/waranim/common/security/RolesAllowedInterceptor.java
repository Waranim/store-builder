package xyz.waranim.common.security;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.Arrays;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Component
public class RolesAllowedInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request,
                             HttpServletResponse response,
                             Object handler) throws Exception {

        if (!(handler instanceof HandlerMethod hm)) {
            return true;
        }

        RolesAllowed allowed = Optional.ofNullable(
                        hm.getMethodAnnotation(RolesAllowed.class))
                .orElse(hm.getBeanType().getAnnotation(RolesAllowed.class));

        if (allowed == null) {
            return true;
        }

        String header = request.getHeader("X-User-Roles");
        Set<String> userRoles = header == null
                ? Set.of()
                : Arrays.stream(header.split(","))
                .map(String::trim)
                .collect(Collectors.toSet());

        boolean anyMatch = Arrays.stream(allowed.value())
                .anyMatch(userRoles::contains);

        if (!anyMatch) {
            response.setStatus(HttpStatus.FORBIDDEN.value());
            return false;
        }
        return true;
    }
}
