package com.student.security;

import io.jsonwebtoken.Claims;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.student.common.ApiResponse;
import com.student.system.mapper.UserMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    private final JwtService jwtService;
    private final StringRedisTemplate redis;
    private final UserMapper userMapper;
    private final ObjectMapper objectMapper;

    public JwtAuthenticationFilter(JwtService jwtService, StringRedisTemplate redis,
                                   UserMapper userMapper, ObjectMapper objectMapper) {
        this.jwtService = jwtService;
        this.redis = redis;
        this.userMapper = userMapper;
        this.objectMapper = objectMapper;
    }

    @Override protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        String header = request.getHeader("Authorization");
        if (header != null && header.startsWith("Bearer ")) {
            String token = header.substring(7);
            try {
                Claims claims = jwtService.parse(token);
                long userId = Long.parseLong(claims.getSubject());
                if (Boolean.TRUE.equals(redis.hasKey("jwt:blacklist:" + token))) {
                    chain.doFilter(request, response);
                    return;
                }
                String activeToken = redis.opsForValue().get("session:" + userId);
                if (!token.equals(activeToken)) {
                    chain.doFilter(request, response);
                    return;
                }
                var user = userMapper.findAuthenticationUser(userId);
                if (user != null && "1".equals(String.valueOf(user.get("status")))) {
                    String role = String.valueOf(user.get("role"));
                    if (List.of("ADMIN", "TEACHER", "STUDENT").contains(role)) {
                        var auth = new UsernamePasswordAuthenticationToken(String.valueOf(userId), null,
                                List.of(new SimpleGrantedAuthority("ROLE_" + role)));
                        auth.setDetails(claims);
                        SecurityContextHolder.getContext().setAuthentication(auth);
                    }
                }
            } catch (DataAccessException e) {
                writeError(response, 503, "Redis 服务不可用，暂时无法完成认证");
                return;
            } catch (Exception ignored) {
                SecurityContextHolder.clearContext();
            }
        }
        chain.doFilter(request, response);
    }

    private void writeError(HttpServletResponse response, int code, String message) throws IOException {
        response.setStatus(HttpServletResponse.SC_SERVICE_UNAVAILABLE);
        response.setCharacterEncoding("UTF-8");
        response.setContentType("application/json");
        response.getWriter().write(objectMapper.writeValueAsString(ApiResponse.error(code, message)));
    }
}
