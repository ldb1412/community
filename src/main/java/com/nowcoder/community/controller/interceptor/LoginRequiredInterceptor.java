package com.nowcoder.community.controller.interceptor;/**
 * @author DB1412
 * @create 2023-03-20 15:21
 */

import com.nowcoder.community.annotation.LoginRequired;
import com.nowcoder.community.util.HostHolder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.lang.reflect.Method;

/**
 *@ClassName LoginRequiredInterceptor
 *@Description TODO
 *@Author DB1412
 *@Date 2023-03-20 15:21
 */
@Component
public class LoginRequiredInterceptor implements HandlerInterceptor {

    @Autowired
    private HostHolder hostHolder;//用于获取当前线程登录的用户的信息

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        if(handler instanceof HandlerMethod){//判断拦截的是否是方法
            HandlerMethod handlerMethod = (HandlerMethod) handler;
            Method method = handlerMethod.getMethod();//是方法则得到该方法
            LoginRequired loginRequired = method.getAnnotation(LoginRequired.class);//获取到指定注解
            if(loginRequired != null && hostHolder.getUser() == null){//当注解不为空，但是传入的user为空则需要拦截
                response.sendRedirect(request.getContextPath() + "/login");//重定向回登录页面
                return false;
            }
        }
        return true;
    }
}
