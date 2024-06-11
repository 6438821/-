package com.heima.app.gateway.filter;
import org.apache.commons.lang.StringUtils;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@Component
public class AuthorizeFilter implements Ordered, GlobalFilter {
    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        ServerHttpResponse response = exchange.getResponse();

        // 如果是登录请求，直接放行
        if (request.getURI().getPath().contains("/login")) {
            return chain.filter(exchange);
        }

        // 获取token
        String token = request.getHeaders().getFirst("token");

        // 如果token不存在或为空，返回未授权状态
        if (StringUtils.isBlank(token)) {
            return setErrorResponse(response, HttpStatus.UNAUTHORIZED);
        }

        // 验证token有效性
        int verificationStatus = AppJwtUtil.verifyToken(AppJwtUtil.getClaimsBody(token));
        if (verificationStatus == 1 || verificationStatus == 2) {
            return setErrorResponse(response, HttpStatus.UNAUTHORIZED);
        }

        return chain.filter(exchange);
    }

    // 设置错误响应状态
    private Mono<Void> setErrorResponse(ServerHttpResponse response, HttpStatus status) {
        response.setStatusCode(status);
        return response.setComplete();
    }

    /**
     * 优先级设置 值越小 优先级越高
     * @return
     */
    @Override
    public int getOrder() {
        return 0;
    }
}
