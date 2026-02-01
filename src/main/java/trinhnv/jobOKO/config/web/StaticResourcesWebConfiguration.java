package trinhnv.jobOKO.config.web;


import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class StaticResourcesWebConfiguration
        implements WebMvcConfigurer {
    @Value("${trinhnguyen.upload-file.base-path}")
    private String basePath;
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        String location = basePath.endsWith("/") ? basePath : basePath + "/";
        registry.addResourceHandler("/api/v1/storage/**").addResourceLocations(location);
    }
}