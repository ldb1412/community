package com.nowcoder.community.dao;

import com.nowcoder.community.entity.DiscussPost;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * @author DB1412
 * @create 2023-03-10 20:50
 */
@Mapper
public interface DiscussPostMapper {

    //由于查询到以后需要进行分页
    /**
     * @description:
     * @param: userId
     * @param: offset 起始行号
     * @param: limit  每一页的最多显示多少数据
     * @return: java.util.List<com.nowcoder.community.entity.DiscussPost>
     * @author DB1412
     * @date: 20:53 2023-03-10
     */
    List<DiscussPost> selectDiscussPosts(int userId, int offset, int limit, int orderMode);

    /** 
     * @description: 查询表中数据总和
     * @param: userId 可选条件，当用于查询个人主页时才传入，因此需要写动态SQL
     * @return: int
     * @author DB1412
     * @date: 20:57 2023-03-10
     */
    //@Param注解用于给参数取别名
    //如果只有一个参数，并且在<if>里使用，则必须加别名
    int selectDiscussPostRows(@Param("userId") int userId);

    /** 
     * @description: 发布帖子
     * @param: discussPost
     * @return: int
     * @author DB1412
     * @date: 21:52 2023-03-22
     */ 
    int insertDiscussPost(DiscussPost discussPost);

    /** 
     * @description: 通过帖子id查询帖子
     * @param: id
     * @return: com.nowcoder.community.entity.DiscussPost
     * @author DB1412
     * @date: 14:55 2023-03-23
     */ 
    DiscussPost selectDiscussPostById(int id);

    //更新评论帖子数量(表格中有一个)
    int updateCommentCount(int id, int commentCount);

    //更改类型
    int updateType(int id, int type);

    //更改状态
    int updateStatus(int id, int status);

    //更新帖子分数
    int updateScore(int id, double score);
}
