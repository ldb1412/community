package com.nowcoder.community.service;/**
 * @author DB1412
 * @create 2023-02-28 19:56
 */

import com.nowcoder.community.dao.AlphaDao;
import org.aspectj.weaver.ast.Or;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

/**
 *@ClassName AlphaService
 *@Description TODO
 *@Author DB1412
 *@Date 2023-02-28 19:56
 */
@Service//被管理的bean是单例的
//@Scope("prototype")多例
public class AlphaService {

    @Autowired
    private AlphaDao alphaDao;

    public AlphaService(){
        System.out.println("实例化AlphaService");
    }

    @PostConstruct//在初始化后调用
    public void init(){
        System.out.println("初始化AlphaService");
    }

    @PreDestroy
    public void destroy(){
        System.out.println("销毁AlphaService");
    }

    public String find(){
        return alphaDao.select();
    }
}
