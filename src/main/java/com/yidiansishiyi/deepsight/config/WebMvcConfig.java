package com.yidiansishiyi.deepsight.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.io.File;

@Configuration
public class WebMvcConfig implements WebMvcConfigurer {
    @Value("${file.upload-path}")
    private String uploadPath;

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // 将访问路径 /api/files/** 映射到物理路径 file:/data/uploads/
        registry.addResourceHandler("/file/download/**")
                .addResourceLocations("file:D:/sanqi/doc/deepsight/deepsight/");
    }
}