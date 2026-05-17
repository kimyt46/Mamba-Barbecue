package com.kimyt.reggie.filter;


import com.alibaba.fastjson2.JSON;
import com.kimyt.reggie.common.BaseContext;
import com.kimyt.reggie.common.R;
import jakarta.servlet.*;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.AntPathMatcher;

import java.io.IOException;
@Slf4j
@WebFilter(filterName = "loginCheckFilter",urlPatterns = "/*")
public class LoginCheckFilter implements Filter {
    public static final AntPathMatcher PATH_MATCHER = new AntPathMatcher();

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {

        HttpServletRequest request = (HttpServletRequest) servletRequest;
        HttpServletResponse response = (HttpServletResponse) servletResponse;

        String[] urls= new String[]{
                "/employee/login",
                "/employee/logout",
                "/employee/register",
                "/user/sendMsg",
                "/user/login",
                "/user/loginout",
                "/backend/**",
                "/front/**",
                "/common/**"

        };

        boolean check = check(urls, request.getRequestURI());
        if(check){
            log.info("此页面不需要验证");
            filterChain.doFilter(request,response);
            return;
        }

        if(request.getSession().getAttribute("employee")!=null){
            log.info("用户已登录，id为：{}",request.getSession().getAttribute("employee"));

            Long empID=(Long) request.getSession().getAttribute("employee");
            BaseContext.setCurrentId(empID);
            filterChain.doFilter(request,response);
            return;
        }

        if(request.getSession().getAttribute("user")!=null){
            Object userObj = request.getSession().getAttribute("user");
            log.info(">>> 移动端用户Session中的user对象: {}, 类型: {}", userObj, userObj.getClass().getName());
            
            Long userId;
            if(userObj instanceof String) {
                userId = Long.parseLong((String) userObj);
                log.info(">>> Session中是String类型，转换为Long: {}", userId);
            } else {
                userId = (Long) userObj;
            }
            
            log.info(">>> 移动端用户已登录，userId={}", userId);
            BaseContext.setCurrentId(userId);
            filterChain.doFilter(request,response);
            return;
        }

        log.info("用户未登录");
        response.getWriter().write(JSON.toJSONString(R.error("NOTLOGIN")));
    }

    public boolean check(String[] urls,String requestURI){
        for(String url:urls){
            boolean match=PATH_MATCHER.match(url,requestURI);
            if(match){
                return true;
            }
        }
        return false;
    }
}
