package com.nowcoder.community.dao;

import com.nowcoder.community.entity.User;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Repository;

/**
 * @author DB1412
 * @create 2023-03-10 20:06
 */
//@Repository
@Mapper
public interface UserMapper {

//    通过id查询用户
    User selectById(int id);

    User selectByName(String userName);

    User selectByEmail(String email);

    int insertUser(User user);
//    更新状态
    int updateStatus(int id, int status);
//    更新用户头像
    int updateHeader(int id, String headerUrl);

    int updatePassword(int id, String password);
}
