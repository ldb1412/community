package com.nowcoder.community.controller.advice;/**
 * @author DB1412
 * @create 2023-03-26 20:28
 */

import com.nowcoder.community.util.CommunityUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;

/**
 *@ClassName ExceptionAdvice
 *@Description TODO
 *@Author DB1412
 *@Date 2023-03-26 20:28
 */
@ControllerAdvice(annotations = Controller.class)//只扫描带有controller注解的类
public class ExceptionAdvice {

    private static final Logger logger = LoggerFactory.getLogger(ExceptionAdvice.class);

    @ExceptionHandler({Exception.class})//注解这个方法是处理异常的方法
    public void handleException(Exception e, HttpServletRequest request, HttpServletResponse response) throws IOException {
        logger.error("服务器发生异常: " + e.getMessage());
        for (StackTraceElement element : e.getStackTrace()) {
            logger.error(element.toString());//异常的详细信息
        }

        //异步请求时不需要返回一个错误页面
        //从请求头中获取请求的方式
        String xRequestedWith = request.getHeader("x-requested-with");
        if ("XMLHttpRequest".equals(xRequestedWith)) {//说明是异步请求
            response.setContentType("application/plain;charset=utf-8");//向浏览器返回一个普通的字符串（字符集为UTF-8）
            PrintWriter writer = response.getWriter();
            writer.write(CommunityUtil.getJSONString(1, "服务器异常!"));
        } else {//否则重定向到错误页面
            response.sendRedirect(request.getContextPath() + "/error");
        }
    }
}
