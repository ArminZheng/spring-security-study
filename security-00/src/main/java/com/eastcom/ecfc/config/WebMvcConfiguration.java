package com.eastcom.ecfc.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * 对 Spring MVC 进行自定义配置
 *
 * @author zy
 * @version 2022/5/10
 */
@Configuration
public class WebMvcConfiguration implements WebMvcConfigurer {
    /*
    1 WebMvcConfigurerAdapter 是 WebMvcConfigurer 的实现类大部分为空方法
        （由于Java8中可以使用default关键字为接口添加默认方法，所以在源代码中spring5.0之后就已经弃用本类）
        如果需要我接着可以实现WebMvcConfigurer类。
    2 WebMvcConfigurationSupport 是 mvc 的基本实现并包含了 WebMvcConfigurer 接口中的方法
    3 WebMvcAutoConfiguration 是 mvc 的自动装在类并部分包含了 WebMvcConfigurer 接口中的方法
    4 如果在 springboot 项目中没有使用到以上类，那么会自动启用 WebMvcAutoConfiguration 类做自动加载；项目中的配置都是默认的，比如静态资源文件的访问

    注意：
    1 重写 WebMvcConfigurationSupport 后 SpringBoot 自动配置失效
    2 @EnableWebMvc 实现原理实际上是导入了 DelegatingWebMvcConfiguration 配置类，等价于 @Configuration + 继承该类
    3 引用了 @EnableWebMVC 注解，就会往spring容器中注入了一个 DelegatingWebMvcConfiguration 来统一管理所有的配置类

    总结：
    0 自动配置类：WebMvcAutoConfiguration ！！！（包括内部配置类：WebMvcAutoConfigurationAdapter#addResourceHandlers 等）
    1 实现 WebMvcConfigurer： 不会覆盖 WebMvcAutoConfiguration 的配置
    2 实现 WebMvcConfigurer + 注解 @EnableWebMvc：会覆盖 WebMvcAutoConfiguration 的配置
    3 （推荐）继承 DelegatingWebMvcConfiguration：会覆盖 WebMvcAutoConfiguration 的配置
    4 继承 WebMvcConfigurationSupport：会覆盖 WebMvcAutoConfiguration 的配置
     */

    /**
     * 为了搭配 thymeleaf 使用
     *
     * @param registry 视图自动化注册器
     */
    @Override
    public void addViewControllers(ViewControllerRegistry registry) {
        registry.addViewController("login.html").setViewName("login");
        registry.addViewController("logout.html").setViewName("logout");
    }
}
