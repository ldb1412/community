package com.nowcoder.community.dao;

import com.nowcoder.community.entity.Comment;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * @author DB1412
 * @create 2023-03-23 21:38
 */
@Mapper
public interface CommentMapper {

    //通过实体（帖子的评论，还是评论的评论）查询评论 还需要分页
    List<Comment> selectCommentsByEntity(int entityType, int entityId, int offset, int limit);

    //查询实体（帖子的评论，还是评论的评论）的评论总数
    int selectCountByEntity(int entityType, int entityId);

    //添加评论
    int insertComment(Comment comment);

    //通过帖子id查询评论
    Comment selectCommentById(int id);

    //通过用户查询评论（分页）
    List<Comment> selectCommentsByUser(int userId, int offset, int limit);

    //查询评论数量
    int selectCountByUser(int userId);
}
