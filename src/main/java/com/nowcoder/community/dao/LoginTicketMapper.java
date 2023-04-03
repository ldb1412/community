package com.nowcoder.community.dao;

import com.nowcoder.community.entity.LoginTicket;
import org.apache.ibatis.annotations.*;

/**
 * @author DB1412
 * @create 2023-03-17 19:48
 */
@Mapper
@Deprecated
public interface LoginTicketMapper {

    //登录成功后需要插入一个凭证
    @Insert({
            "insert into login_ticket(user_id,ticket,status,expired) ",
            "values(#{userId},#{ticket},#{status},#{expired})"
    })
    int insertLoginTicket(LoginTicket loginTicket);

//    可以查询登录的用户
    @Select({
            "select id,user_id,ticket,status,expired ",
            "from login_ticket where ticket=#{ticket}"
    })
    @Options(useGeneratedKeys = true, keyProperty = "id")//自动生成id自增
    LoginTicket selectByTicket(String ticket);

//    修改凭证的状态
//写动态SQL的方式
    @Update({
            "<script>",
            "update login_ticket set status=#{status} where ticket=#{ticket} ",
            "<if test=\"ticket!=null\"> ",
            "and 1=1 ",
            "</if>",
            "</script>"
    })
    int updateStatus(String ticket, int status);
}
