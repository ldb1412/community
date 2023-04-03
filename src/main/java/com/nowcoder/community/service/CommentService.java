package com.nowcoder.community.service;/**
 * @author DB1412
 * @create 2023-03-23 21:43
 */

import com.nowcoder.community.dao.CommentMapper;
import com.nowcoder.community.entity.Comment;
import com.nowcoder.community.util.CommunityConstant;
import com.nowcoder.community.util.SensitiveFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.util.HtmlUtils;

import java.util.List;

/**
 *@ClassName CommentService
 *@Description TODO
 *@Author DB1412
 *@Date 2023-03-23 21:43
 */
@Service
public class CommentService implements CommunityConstant {

    @Autowired
    private CommentMapper commentMapper;

    @Autowired
    private SensitiveFilter sensitiveFilter;

    @Autowired
    private DiscussPostService discussPostService;

    public List<Comment> findCommentsByEntity(int entityType, int entityId, int offset, int limit) {
        return commentMapper.selectCommentsByEntity(entityType, entityId, offset, limit);
    }

    //根据实体（评论或回复）查数量
    public int findCommentCount(int entityType, int entityId) {
        return commentMapper.selectCountByEntity(entityType, entityId);
    }

    //处理增加评论的业务
//    通过事务管理
    @Transactional(isolation = Isolation.READ_COMMITTED, propagation = Propagation.REQUIRED)
    public int addComment(Comment comment){
        if (comment == null) {
            throw new IllegalArgumentException("参数不能为空!");
        }

        // 添加评论
        comment.setContent(HtmlUtils.htmlEscape(comment.getContent()));//处理特殊字符 如 <
        comment.setContent(sensitiveFilter.filter(comment.getContent()));//处理敏感词
        int rows = commentMapper.insertComment(comment);//插入评论

        // 更新帖子评论数量
        if (comment.getEntityType() == ENTITY_TYPE_POST) {//判断是否为帖子的评论
            //查询到帖子的评论数，然后将该值赋给帖子表中的评论数量这一属性
            int count = commentMapper.selectCountByEntity(comment.getEntityType(), comment.getEntityId());
            discussPostService.updateCommentCount(comment.getEntityId(), count);
        }

        return rows;
    }

    public Comment findCommentById(int id) {
        return commentMapper.selectCommentById(id);
    }

    public List<Comment> findUserComments(int userId, int offset, int limit) {
        return commentMapper.selectCommentsByUser(userId, offset, limit);
    }

    public int findUserCount(int userId) {
        return commentMapper.selectCountByUser(userId);
    }
}
