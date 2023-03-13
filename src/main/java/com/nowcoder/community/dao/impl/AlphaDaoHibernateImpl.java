package com.nowcoder.community.dao.impl;/**
 * @author DB1412
 * @create 2023-02-27 21:52
 */

import com.nowcoder.community.dao.AlphaDao;
import org.springframework.stereotype.Repository;

/**
 *@ClassName AlphaDaoHibernateImpl
 *@Description TODO
 *@Author DB1412
 *@Date 2023-02-27 21:52
 */
@Repository("alphaDaoHibernate")//可以保证自动扫描
public class AlphaDaoHibernateImpl implements AlphaDao {
    @Override
    public String select() {
        return "Hibernate";
    }
}
