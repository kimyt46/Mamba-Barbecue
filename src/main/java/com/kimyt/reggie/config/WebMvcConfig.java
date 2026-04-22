package com.kimyt.reggie.config;


import com.kimyt.reggie.common.JacksonObjectMapper;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

@Slf4j
@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        log.info("开始进行静态资源映射...");
        // 管理端静态资源映射
        registry.addResourceHandler("/backend/**")
                .addResourceLocations("classpath:/backend/");
        // 用户端静态资源映射
        registry.addResourceHandler("/front/**")
                .addResourceLocations("classpath:/front/");
        // 【关键】恢复Spring Boot默认静态资源映射，避免其他资源4041
        registry.addResourceHandler("/**")
                .addResourceLocations("classpath:/static/", "classpath:/resources/", "classpath:/META-INF/resources/");


        registry.addResourceHandler("/**")
                .addResourceLocations(
                        "classpath:/META-INF/resources/",
                        "classpath:/resources/",
                        "classpath:/static/",
                        "classpath:/public/"
                );

    }


    @Override
    public void extendMessageConverters(List<HttpMessageConverter<?>> converters) {
        log.info("扩展消息转换器");
        //1. 创建自定义Jackson消息转换器对象（补齐你截图截断的完整代码）
        MappingJackson2HttpMessageConverter messageConverter = new MappingJackson2HttpMessageConverter();

        //2. 设置自定义对象转换器，底层Jackson把Java对象转json（日期格式化、Long转String防精度丢失）
        messageConverter.setObjectMapper(new JacksonObjectMapper());

        //3. 追加到mvc转换器集合，下标0代表【优先级最高，优先使用我们自定义的转换器】
        // 补齐你截图最后一行：去掉多余index:注释，Java原生写法
        converters.add(0, messageConverter);
    }
}