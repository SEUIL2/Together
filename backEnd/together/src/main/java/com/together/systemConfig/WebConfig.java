package com.together.systemConfig;

import com.together.util.customAnnotation.CurrentProjectArgumentResolver;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.List;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    private final CurrentProjectArgumentResolver currentProjectArgumentResolver;

    public WebConfig(CurrentProjectArgumentResolver resolver) {
        this.currentProjectArgumentResolver = resolver;
    }

    @Override
    public void addArgumentResolvers(List<HandlerMethodArgumentResolver> resolvers) {
        resolvers.add(currentProjectArgumentResolver);
    }

}
