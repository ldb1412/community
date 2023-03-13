package com.nowcoder.community.dao.impl;/**
 * @author DB1412
 * @create 2023-02-27 21:55
 */

import com.nowcoder.community.dao.AlphaDao;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Repository;

/**
 *@ClassName AlphaDaoMyBatisImpl
 *@Description TODO
 *@Author DB1412
 *@Date 2023-02-27 21:55
 */
@Repository
@Primary
public class AlphaDaoMyBatisImpl implements AlphaDao {
    @Override
    public String select() {
        return "MyBatis";
    }
}
