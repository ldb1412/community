package com.nowcoder.community.controller;/**
 * @author DB1412
 * @create 2023-03-24 20:35
 */

import com.nowcoder.community.entity.Comment;
import com.nowcoder.community.entity.DiscussPost;
import com.nowcoder.community.entity.Event;
import com.nowcoder.community.event.EventProducer;
import com.nowcoder.community.service.CommentService;
import com.nowcoder.community.service.DiscussPostService;
import com.nowcoder.community.util.CommunityConstant;
import com.nowcoder.community.util.HostHolder;
import com.nowcoder.community.util.RedisKeyUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import java.util.Date;

/**
 *@ClassName CommentController
 *@Description TODO
 *@Author DB1412
 *@Date 2023-03-24 20:35
 */
@Controller
@RequestMapping("/comment")
public class CommentController implements CommunityConstant {
    @Autowired
    private CommentService commentService;

    @Autowired
    private HostHolder hostHolder;

    @Autowired
    private EventProducer eventProducer;

    @Autowired
    private DiscussPostService discussPostService;

    @Autowired
    private RedisTemplate redisTemplate;

    /**
     * @description: 增加评论
     * @param: discussPostId
 * @param: comment
     * @return: java.lang.String
     * @author DB1412
     * @date: 17:30 2023-03-30
     */
    @RequestMapping(path = "/add/{discussPostId}", method = RequestMethod.POST)
    public String addComment(@PathVariable("discussPostId") int discussPostId, Comment comment) {
        comment.setUserId(hostHolder.getUser().getId());//获取评论的用户id
        comment.setStatus(0);//设置评论为有效的
        comment.setCreateTime(new Date());//创建时间
        commentService.addComment(comment);

        //触发评论事件
        Event event = new Event()
                .setTopic(TOPIC_COMMENT)
                .setUserId(hostHolder.getUser().getId())
                .setEntityType(comment.getEntityType())
                .setEntityId(comment.getEntityId())
                .setData("postId", discussPostId);//需要存入postId，因为需要通过链接链接到帖子的详情页
        if (comment.getEntityType() == ENTITY_TYPE_POST) {//当评论的类型为给帖子的评论
            DiscussPost target = discussPostService.findDiscussPostById(comment.getEntityId());//通过帖子的id（该id在评论中的属性就是EntityId）查找到帖子
            event.setEntityUserId(target.getUserId());
        } else if (comment.getEntityType() == ENTITY_TYPE_COMMENT) {//给评论的评论（回复）
            Comment target = commentService.findCommentById(comment.getEntityId());//得到回复的是哪个评论
            event.setEntityUserId(target.getUserId());
        }
        eventProducer.fireEvent(event);//触发了评论事件，然后通过生产者进行发布

        if (comment.getEntityType() == ENTITY_TYPE_POST) {//评论的是帖子的时候才触发
            // 触发发帖事件
            event = new Event()
                    .setTopic(TOPIC_PUBLISH)
                    .setUserId(comment.getUserId())
                    .setEntityType(ENTITY_TYPE_POST)
                    .setEntityId(discussPostId);
            eventProducer.fireEvent(event);

            // 计算帖子分数（只有对帖子进行评论时才触发）
            String redisKey = RedisKeyUtil.getPostScoreKey();
            redisTemplate.opsForSet().add(redisKey, discussPostId);
        }

        //重定向到帖子页面（因此需要加入帖子的id）
        return "redirect:/discuss/detail/" + discussPostId;
    }

}
