package com.findear.main.common.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.*;

@Configuration
@EnableWebMvc
public class WebConfig implements WebMvcConfigurer {


    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/**").addResourceLocations("classpath:/static/");
    }

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedOrigins("https://j10a706.p.ssafy.io",
                        "http://localhost:5173", "http://localhost:4173")
                .allowedHeaders("*")
                .allowedMethods("GET", "POST", "DELETE", "PATCH", "OPTIONS")
                .allowCredentials(true);
    }

}