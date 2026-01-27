package com.example.community.config;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
@RequiredArgsConstructor
public class WebConfig implements WebMvcConfigurer {

    private final LogInterceptor logInterceptor;

    @Value("${file.upload-path}") // "file:/app/upload/" 값을 가져옴
    private String uploadPath;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(logInterceptor)
                .addPathPatterns("/**") // 모든 경로에 대해 로그를 남깁니다
                .excludePathPatterns("/css/**", "/js/**", "/images/**", "/*.ico",
                        "/*.css", "/*.js", "/*.jpg", "/*.png", "/*.gif");// 루트에 있는 경우); // 정적 리소스는 제외
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        String location = uploadPath;

        if (!location.startsWith("file:")) {
            if (location.contains(":")) {
                location = "file:///" + location; // Windows용
            } else {
                location = "file:" + location;    // Linux용
            }
        }
        registry.addResourceHandler("/upload/**")
                .addResourceLocations(location);
    }
}