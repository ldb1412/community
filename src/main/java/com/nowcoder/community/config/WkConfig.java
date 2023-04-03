package com.nowcoder.community.config;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PostConstruct;
import java.io.File;

@Configuration
public class WkConfig {

    private static final Logger logger = LoggerFactory.getLogger(WkConfig.class);

    @Value("${wk.image.storage}")
    private String wkImageStorage;//存放图片的路径

    @PostConstruct//服务启动时调用该方法一次（用于创建保存图片的目录）
    public void init() {
        // 创建WK图片目录
        File file = new File(wkImageStorage);
        if (!file.exists()) {//不存在则创建该路径的目录
            file.mkdir();
            logger.info("创建WK图片目录: " + wkImageStorage);
        }
    }

}
