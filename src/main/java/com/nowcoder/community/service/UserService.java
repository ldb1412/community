package com.nowcoder.community.service;/**
 * @author DB1412
 * @create 2023-03-10 21:17
 */

import com.nowcoder.community.dao.LoginTicketMapper;
import com.nowcoder.community.dao.UserMapper;
import com.nowcoder.community.entity.LoginTicket;
import com.nowcoder.community.entity.User;
import com.nowcoder.community.util.CommunityConstant;
import com.nowcoder.community.util.CommunityUtil;
import com.nowcoder.community.util.MailClient;
import com.nowcoder.community.util.RedisKeyUtil;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.security.SecurityProperties;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 *@ClassName UserService
 *@Description TODO
 *@Author DB1412
 *@Date 2023-03-10 21:17
 */
@Service
public class UserService implements CommunityConstant {
    @Autowired
    private UserMapper userMapper;

    @Autowired
    private MailClient mailClient;

    @Autowired
    private TemplateEngine templateEngine;

//    @Autowired
//    private LoginTicketMapper loginTicketMapper;

    @Autowired
    private RedisTemplate redisTemplate;

    @Value("${community.path.domain}")//注入固定值
    private String domain;

    @Value("${server.servlet.context-path}")
    private String contextPath;


    public User findUserById(int id){//通过从redis缓存中获取用户
//        return userMapper.selectById(id);
        User user = getCache(id);
        if (user == null) {
            user = initCache(id);
        }
        return user;
    }

    /** 
     * @description: 用于注册用户
     * @param: user
     * @return: java.util.Map<java.lang.String,java.lang.Object>
     * @author DB1412
     * @date: 20:29 2023-03-17
     */ 
    public Map<String, Object> register(User user) {
        Map<String, Object> map = new HashMap<>();

        //空值处理
        if (user == null) {
            throw new IllegalArgumentException("参数不能为空!");
        }
        if (StringUtils.isBlank(user.getUsername())) {
            map.put("usernameMsg", "账号不能为空!");
            return map;
        }
        if (StringUtils.isBlank(user.getPassword())) {
            map.put("passwordMsg", "密码不能为空!");
            return map;
        }
        if (StringUtils.isBlank(user.getEmail())) {
            map.put("emailMsg", "邮箱不能为空!");
            return map;
        }

        // 验证账号
        User u = userMapper.selectByName(user.getUsername());
        if (u != null) {
            map.put("usernameMsg", "该账号已存在!");
            return map;
        }

        // 验证邮箱
        u = userMapper.selectByEmail(user.getEmail());
        if (u != null) {
            map.put("emailMsg", "该邮箱已被注册!");
            return map;
        }

        // 注册用户
        user.setSalt(CommunityUtil.generateUUID().substring(0, 5));//随机字符串，为了让密码更加复杂
        user.setPassword(CommunityUtil.md5(user.getPassword() + user.getSalt()));
        user.setType(0);
        user.setStatus(0);//0激活 1未激活
        user.setActivationCode(CommunityUtil.generateUUID());
//        随机头像，%d用随机数代替
        user.setHeaderUrl(String.format("http://images.nowcoder.com/head/%dt.png", new Random().nextInt(1000)));
//        注册的时间
        user.setCreateTime(new Date());
//        添加到库中
        userMapper.insertUser(user);

        // 发送激活邮件（固定步骤）
        Context context = new Context();
        //传给动态页面的值
        context.setVariable("email", user.getEmail());
        //设置url并传给动态页面
        // http://localhost:8080/community/activation/101/code（激活码）
        String url = domain + contextPath + "/activation/" + user.getId() + "/" + user.getActivationCode();
        context.setVariable("url", url);
        //生成模板
        String content = templateEngine.process("/mail/activation", context);
        //发送邮件到注册用户
        mailClient.sendMail(user.getEmail(), "激活账号", content);

        //此处map为空
        return map;
    }

    /**
     * @description: 用于处理激活请求；判断激活码是否正确
     * @param: userId 用户的id
 * @param: code 激活码
     * @return: int
     * @author DB1412
     * @date: 21:10 2023-03-15
     */
    public int activation(int userId, String code){
        User user = userMapper.selectById(userId);
        if(user.getStatus() == 1){//说明已经激活
            return ACTIVATION_REPEAT;//返回重复激活
        }else if(user.getActivationCode().equals(code)){//判断激活码是否正确
            userMapper.updateStatus(userId, 1);
            clearCache(userId);//由于对用户信息进行了修改，因此需要删除之前的缓存
            return ACTIVATION_SUCCESS;
        }else {//否则激活失败
            return ACTIVATION_FAILURE;
        }
    }

    /**
     * @description: 用户登录；因为可能有多个返回所以返回map
     * @param: username 传入账号
 * @param: password 传入密码，需要进行加密，因为数据库中存的是加密的密码
 * @param: expiredSeconds 传入过期时间
     * @return: java.util.Map<java.lang.String,java.lang.Object>
     * @author DB1412
     * @date: 20:31 2023-03-17
     */
    public Map<String, Object> login(String username, String password, long expiredSeconds){
        Map<String, Object> map = new HashMap<>();

        // 空值处理
        if (StringUtils.isBlank(username)) {//使用工具类
            map.put("usernameMsg", "账号不能为空!");
            return map;
        }
        if (StringUtils.isBlank(password)) {
            map.put("passwordMsg", "密码不能为空!");
            return map;
        }

        // 验证账号
        User user = userMapper.selectByName(username);//查询该用户
        if (user == null) {
            map.put("usernameMsg", "该账号不存在!");
            return map;
        }

        // 验证状态
        if (user.getStatus() == 0) {
            map.put("usernameMsg", "该账号未激活!");
            return map;
        }
        // 验证密码
        password = CommunityUtil.md5(password + user.getSalt());//因为密码在数据库存的是加密的，所以需要进行加密
        if (!user.getPassword().equals(password)) {
            map.put("passwordMsg", "密码不正确!");
            return map;
        }

        // 通过以上判断，则说明可以登录，则需生成登录凭证
        LoginTicket loginTicket = new LoginTicket();
        loginTicket.setUserId(user.getId());
        loginTicket.setTicket(CommunityUtil.generateUUID());
        loginTicket.setStatus(0);
        loginTicket.setExpired(new Date(System.currentTimeMillis() + expiredSeconds * 1000));
//        loginTicketMapper.insertLoginTicket(loginTicket);//将登录凭证存入数据库

        //登录凭证存入redis
        String redisKey = RedisKeyUtil.getTicketKey(loginTicket.getTicket());
        redisTemplate.opsForValue().set(redisKey, loginTicket);//会自动序列化为json数据

        map.put("ticket", loginTicket.getTicket());//将登录凭证返回
        return map;
    }

    /**
     * @description: 退出登录
     * @param: ticket 传入登陆凭证
     * @return: void
     * @author DB1412
     * @date: 21:39 2023-03-17
     */
    public void logout(String ticket){
//        loginTicketMapper.updateStatus(ticket, 1);//将状态设置为1则为退出
        String redisKey = RedisKeyUtil.getTicketKey(ticket);
        LoginTicket loginTicket = (LoginTicket) redisTemplate.opsForValue().get(redisKey);
        loginTicket.setStatus(1);//将状态设置为1
        redisTemplate.opsForValue().set(redisKey, loginTicket);//覆盖原有值
    }

    /**
     * @description: 通过ticket查询用户登录凭证，从而根据凭证查询用户信息
     * @param: ticket
     * @return: com.nowcoder.community.entity.LoginTicket
     * @author DB1412
     * @date: 19:53 2023-03-19
     */
    public LoginTicket findLoginTicket(String ticket){
//        return loginTicketMapper.selectByTicket(ticket);
        String redisKey = RedisKeyUtil.getTicketKey(ticket);
        return (LoginTicket) redisTemplate.opsForValue().get(redisKey);//查询
    }

    /** 
     * @description: 更新用户头像，因此需要修改url
     * @param: userId
 * @param: headerUrl
     * @return: int
     * @author DB1412
     * @date: 20:26 2023-03-19
     */ 
    public int updateHeader(int userId, String headerUrl){
//        return userMapper.updateHeader(userId, headerUrl);
        int rows = userMapper.updateHeader(userId, headerUrl);
        clearCache(userId);
        return rows;
    }

    // 判断邮箱是否已注册
    public boolean isEmailExist(String email) {
        User user = userMapper.selectByEmail(email);
        return user != null;
    }

    // 重置密码
    public Map<String, Object> resetPassword(String email, String password) {
        Map<String, Object> map = new HashMap<>();

        // 空值处理
        if(StringUtils.isBlank(email)){
            map.put("emailMsg", "邮箱不能为空!");
            return map;
        }
        if (StringUtils.isBlank(password)) {
            map.put("passwordMsg", "密码不能为空!");
            return map;
        }

        // 验证邮箱
        User user = userMapper.selectByEmail(email);
        if (user == null) {
            map.put("emailMsg", "该邮箱尚未注册!");
            return map;
        }
        // 重置密码
        password = CommunityUtil.md5(password + user.getSalt());
        userMapper.updatePassword(user.getId(), password);

        map.put("user", user);
        return map;
    }

    //修改密码
    public Map<String , Object> updatePassword(int userId, String oldPassword, String newPassword) {
        Map<String, Object> map = new HashMap<>();

        // 空值处理
        if (StringUtils.isBlank(oldPassword)) {
            map.put("oldPasswordMsg", "原密码不能为空!");
            return map;
        }
        if (StringUtils.isBlank(newPassword)) {
            map.put("newPasswordMsg", "新密码不能为空!");
            return map;
        }

        // 验证原始密码
        User user = userMapper.selectById(userId);
        oldPassword = CommunityUtil.md5(oldPassword + user.getSalt());
        if (!user.getPassword().equals(oldPassword)) {
            map.put("oldPasswordMsg", "原密码输入有误!");
            return map;
        }

        // 更新密码
        newPassword = CommunityUtil.md5(newPassword + user.getSalt());
        userMapper.updatePassword(userId, newPassword);

        return map;
    }

    //通过用户名查用户
    public User findUserByName(String username) {
        return userMapper.selectByName(username);
    }

    // 1.优先从缓存中取值
    private User getCache(int userId) {
        String redisKey = RedisKeyUtil.getUserKey(userId);
        return (User) redisTemplate.opsForValue().get(redisKey);
    }

    // 2.取不到时初始化缓存数据
    private User initCache(int userId) {
        User user = userMapper.selectById(userId);
        String redisKey = RedisKeyUtil.getUserKey(userId);
        //一小时后失效
        redisTemplate.opsForValue().set(redisKey, user, 3600, TimeUnit.SECONDS);
        return user;
    }

    // 3.数据变更时清除缓存数据
    private void clearCache(int userId) {
        String redisKey = RedisKeyUtil.getUserKey(userId);
        redisTemplate.delete(redisKey);
    }

    //得到用户权限
    public Collection<? extends GrantedAuthority> getAuthorities(int userId) {
        User user = this.findUserById(userId);//通过id查用户

        List<GrantedAuthority> list = new ArrayList<>();
        list.add(new GrantedAuthority() {

            @Override
            public String getAuthority() {
                switch (user.getType()) {
                    case 1:
                        return AUTHORITY_ADMIN;
                    case 2:
                        return AUTHORITY_MODERATOR;
                    default:
                        return AUTHORITY_USER;
                }
            }
        });
        return list;
    }
}
