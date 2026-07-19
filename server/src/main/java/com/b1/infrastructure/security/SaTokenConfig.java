package com.b1.infrastructure.security;

import cn.dev33.satoken.context.SaHolder;
import cn.dev33.satoken.interceptor.SaInterceptor;
import cn.dev33.satoken.router.SaRouter;
import cn.dev33.satoken.stp.StpUtil;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class SaTokenConfig implements WebMvcConfigurer {

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new SaInterceptor(handle -> {
                    // Skip OPTIONS preflight requests
                    if ("OPTIONS".equals(SaHolder.getRequest().getMethod())) {
                        return;
                    }
                    SaRouter.match("/api/v1/auth/login", () -> StpUtil.checkLogin());
                    SaRouter.match("/api/v1/auth/refresh", () -> StpUtil.checkLogin());
                    SaRouter.match("/api/v1/user/**", () -> StpUtil.checkLogin());
                    SaRouter.match("/api/v1/student/**", () -> StpUtil.checkLogin());
                    SaRouter.match("/api/v1/teacher/**", () -> StpUtil.checkLogin());
                    SaRouter.match("/api/v1/admin/**", () -> StpUtil.checkRole("admin"));
                    SaRouter.match("/api/v1/files/**", () -> StpUtil.checkLogin());
                }))
                .addPathPatterns("/api/**")
                .excludePathPatterns(
                        "/api/v1/auth/login",
                        "/api/v1/auth/refresh",
                        "/api/v1/files/*/download",
                        "/v3/api-docs/**",
                        "/swagger-ui/**",
                        "/swagger-ui.html",
                        "/doc.html",
                        "/webjars/**"
                );
    }
}
