package com.nowcoder.community.config;/**
 * @author DB1412
 * @create 2023-02-28 20:16
 */

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.text.SimpleDateFormat;

/**
 *@ClassName AlphaConfig
 *@Description TODO
 *@Author DB1412
 *@Date 2023-02-28 20:16
 */
@Configuration
public class AlphaConfig {

    @Bean
    public SimpleDateFormat simpleDateFormat(){
        return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    }

}
