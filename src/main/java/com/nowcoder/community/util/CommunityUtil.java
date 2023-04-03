package com.nowcoder.community.util;

import com.alibaba.fastjson.JSONObject;
import org.apache.commons.lang3.StringUtils;
import org.springframework.util.DigestUtils;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class CommunityUtil {

    // 生成随机字符串
    public static String generateUUID() {
        return UUID.randomUUID().toString().replaceAll("-", "");//替换所有的-
    }

    // MD5加密，防止被盗
    // hello -> abc123def456 加密结果是固定的
    // hello + 3e4a8 -> abc123def456abc 为你的密码加入一个随机字符串，这样生成的加密结果就不一样了
    public static String md5(String key) {
        if (StringUtils.isBlank(key)) {//StringUtils用于判空
            return null;
        }
        return DigestUtils.md5DigestAsHex(key.getBytes());//要求传入的是byte
    }

    /**
     * @description: 得到json字符串
     * @param: code
 * @param: msg
 * @param: map 业务数据
     * @return: java.lang.String
     * @author DB1412
     * @date: 20:08 2023-03-20
     */
    public static String getJSONString(int code, String msg, Map<String, Object> map){
        JSONObject json = new JSONObject();
        json.put("code", code);
        json.put("msg", msg);
        if(map != null){
            for(String key : map.keySet()){
                json.put(key, map.get(key));
            }
        }
        return json.toJSONString();
    }

    public static String getJSONString(int code, String msg){
        return getJSONString(code, msg, null);
    }

    public static String getJSONString(int code){
        return getJSONString(code, null, null);
    }

    public static void main(String[] args) {
        Map<String, Object> map = new HashMap<>();
        map.put("name", "zhangsan");
        map.put("age", 25);
        System.out.println(getJSONString(0,"ok", map));
    }
}
