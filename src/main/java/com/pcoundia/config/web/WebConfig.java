package com.pcoundia.config.web;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Value("${file.save-path:./files}")
    private String savePath;

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/files/**") // Endpoint to access files
                .addResourceLocations("file:" + savePath + "/"); // Absolute file path

        registry.addResourceHandler("/static/**")
                .addResourceLocations( "classpath:/static/");
    }
}