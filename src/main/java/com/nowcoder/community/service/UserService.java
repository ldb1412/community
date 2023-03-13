package com.nowcoder.community.service;/**
 * @author DB1412
 * @create 2023-03-10 21:17
 */

import com.nowcoder.community.dao.UserMapper;
import com.nowcoder.community.entity.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 *@ClassName UserService
 *@Description TODO
 *@Author DB1412
 *@Date 2023-03-10 21:17
 */
@Service
public class UserService {
    @Autowired
    private UserMapper userMapper;

    public User findUserById(int id){
        return userMapper.selectById(id);
    }

}
