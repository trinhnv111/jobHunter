package trinhnv.jobOKO.config.web;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.resource.PathResourceResolver;
import org.springframework.web.servlet.resource.ResourceResolver;

import java.io.IOException;
import java.util.List;

@Configuration
public class StaticResourcesWebConfiguration implements WebMvcConfigurer {

    @Value("${trinhnguyen.upload-file.base-path}")
    private String basePath;

    private static final String[] EXTENSIONS_TO_TRY = { ".jpg", ".jpeg", ".png", ".gif", ".webp",".txt",".pdf" };

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        String location = basePath.endsWith("/") ? basePath : basePath + "/";

        // Path tới handler có thể là /api/v1/storage/... HOẶC /storage/... (sau khi bỏ servlet path)
        registry.addResourceHandler( "/storage/**")
                .addResourceLocations(location)
                .resourceChain(true)
                .addResolver(extensionFriendlyResolver());
    }

    /**
     * Nếu không tìm thấy file thêm đuôi .jpg, .png, ... (vd: anh-png.jpg).
     */
    private ResourceResolver extensionFriendlyResolver() {
        return new PathResourceResolver() {
            @Override
            protected Resource getResource(String resourcePath, Resource location) throws IOException {
                Resource resource = location.createRelative(resourcePath);
                if (resource.exists() && resource.isReadable()) {
                    return resource;
                }
                for (String ext : EXTENSIONS_TO_TRY) {
                    Resource withExt = location.createRelative(resourcePath + ext);
                    if (withExt.exists() && withExt.isReadable()) {
                        return withExt;
                    }
                }
                return null;
            }
        };
    }
}