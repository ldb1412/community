package com.nowcoder.community.controller;/**
 * @author DB1412
 * @create 2023-03-14 21:06
 */

import com.nowcoder.community.util.CommunityUtil;
import com.nowcoder.community.util.MailClient;
import com.nowcoder.community.util.RedisKeyUtil;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import com.google.code.kaptcha.Producer;
import com.nowcoder.community.entity.User;
import com.nowcoder.community.service.UserService;
import com.nowcoder.community.util.CommunityConstant;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import javax.imageio.ImageIO;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 *@ClassName LoginController
 *@Description TODO
 *@Author DB1412
 *@Date 2023-03-14 21:06
 */
@Controller
public class LoginController implements CommunityConstant {

    private static final Logger logger = LoggerFactory.getLogger(LoginController.class);

    @Autowired
    private UserService userService;

    @Autowired
    private Producer kaptchaProducer;

    @Autowired
    private TemplateEngine templateEngine;

    @Autowired
    private MailClient mailClient;

    @Autowired
    private RedisTemplate redisTemplate;

    @Value("${server.servlet.context-path}")
    private String contextPath;

    @RequestMapping(path = "/register", method = RequestMethod.GET)
    public String getRegisterPage() {
        return "/site/register";
    }

    @RequestMapping(path = "/login", method = RequestMethod.GET)
    public String getLoginPage() {
        return "/site/login";
    }

    //用户注册时需要实现的控制；若失败则提示错误信息，成功则发送激活邮件并跳转页面
    @RequestMapping(path = "/register", method = RequestMethod.POST)
    public String register(Model model, User user) {
        Map<String, Object> map = userService.register(user);
        if (map == null || map.isEmpty()) {//注册成功
//            传入页面模板的提示信息
            model.addAttribute("msg", "注册成功,我们已经向您的邮箱发送了一封激活邮件,请尽快激活!");
//            传入页面的跳转路径
            model.addAttribute("target", "/index");
            return "/site/operate-result";
        } else {
            //有可能get到空值
            model.addAttribute("usernameMsg", map.get("usernameMsg"));
            model.addAttribute("passwordMsg", map.get("passwordMsg"));
            model.addAttribute("emailMsg", map.get("emailMsg"));
            return "/site/register";//注册失败则跳回注册页面，并返回错误信息
        }
    }

    //当用户通过邮件进行激活时，通过判断状态实现页面的跳转
    //@PathVariable用于从请求路径中获取参数值
    // http://localhost:8080/community/activation/101/code（发送到邮箱的链接的格式）
    @RequestMapping(path = "/activation/{userId}/{code}", method = RequestMethod.GET)
    public String activation(Model model, @PathVariable("userId") int userId, @PathVariable("code") String code) {
        int result = userService.activation(userId, code);
        if (result == ACTIVATION_SUCCESS) {
            model.addAttribute("msg", "激活成功,您的账号已经可以正常使用了!");
            model.addAttribute("target", "/login");
        } else if (result == ACTIVATION_REPEAT) {
            model.addAttribute("msg", "无效操作,该账号已经激活过了!");
            model.addAttribute("target", "/index");
        } else {
            model.addAttribute("msg", "激活失败,您提供的激活码不正确!");
            model.addAttribute("target", "/index");
        }
//        跳转到中间提示页面
        return "/site/operate-result";
    }

    //生成验证码并返回给登录页面
    @RequestMapping(path = "/kaptcha", method = RequestMethod.GET)
    public void getKaptcha(HttpServletResponse response/*, HttpSession session*/) {
        // 生成验证码
        String text = kaptchaProducer.createText();//根据设置生成验证码
        BufferedImage image = kaptchaProducer.createImage(text);//生成图片

        // 将验证码存入session：因为这是敏感数据，因此存入session
//        session.setAttribute("kaptcha", text);

        //验证码的归属
        String kaptchaOwner = CommunityUtil.generateUUID();
        Cookie cookie = new Cookie("kaptchaOwner", kaptchaOwner);
        cookie.setMaxAge(60);//生存时间 60s
        cookie.setPath(contextPath);//生效的路径 整个项目
        response.addCookie(cookie);//发送给客户端
        // 将验证码存入Redis(有效时间为 60s)
        String redisKey = RedisKeyUtil.getKaptchaKey(kaptchaOwner);
        redisTemplate.opsForValue().set(redisKey, text, 60, TimeUnit.SECONDS);

        // 将突图片输出给浏览器
        response.setContentType("image/png");//返回的内容的格式
        try {
            OutputStream os = response.getOutputStream();
            ImageIO.write(image, "png", os);//输出给浏览器
        } catch (IOException e) {
            logger.error("响应验证码失败:" + e.getMessage());
        }
    }

    /**
     * @description: 登录功能
     * @param: username 用户名
 * @param: password 密码
 * @param: code 验证码
 * @param: rememberme 是否勾选记住我
 * @param: model 向request域中共享对象
 * @param: session 需要从session中获取域对象
 * @param: response 将cookie存入客户端必须要！
     * @return: java.lang.String
     * @author DB1412
     * @date: 20:54 2023-03-17
     */
    @RequestMapping(path ="/login", method = RequestMethod.POST)
    public String login(String username, String password, String code, boolean rememberme,
                        Model model/*, HttpSession session*/, HttpServletResponse response,
                        @CookieValue("kaptchaOwner") String kaptchaOwner){
        //从session中获取验证码，用于和传入的code进行比较从而判断是否登录（但是可能存在共享session问题）
//        String kaptcha = (String) session.getAttribute("kaptcha");

        //从redis中取验证码！！而取验证码需要从cookie中知道验证码归属者
        String kaptcha = null;
        if (StringUtils.isNotBlank(kaptchaOwner)) {//判断失效否
            String redisKey = RedisKeyUtil.getKaptchaKey(kaptchaOwner);//得到key，然后从redis中取到验证码
            kaptcha = (String) redisTemplate.opsForValue().get(redisKey);
        }

        if (StringUtils.isBlank(kaptcha) || StringUtils.isBlank(code) || !kaptcha.equalsIgnoreCase(code)) {
            model.addAttribute("codeMsg", "验证码不正确!");
            return "/site/login";
        }
        // 检查账号,密码，通过判断是否勾选记住我来选择失效时间
        int expiredSeconds = rememberme ? REMEMBER_EXPIRED_SECONDS : DEFAULT_EXPIRED_SECONDS;
        //调取业务层得到Map，其中若登录成功则包含ticket，否则则包含登陆失败的原因
        Map<String, Object> map = userService.login(username, password, expiredSeconds);
        if (map.containsKey("ticket")) {//账号密码正确时
            //将ticket（登陆凭证）存入cookie中
            Cookie cookie = new Cookie("ticket", map.get("ticket").toString());
            cookie.setPath(contextPath);//设置cookie路径
            cookie.setMaxAge(expiredSeconds);
            response.addCookie(cookie);//将cookie存入客户端
            return "redirect:/index";//重定向
        } else {//得到错误信息
            model.addAttribute("usernameMsg", map.get("usernameMsg"));
            model.addAttribute("passwordMsg", map.get("passwordMsg"));
            return "/site/login";
        }
    }

    /**
     * @description: 退出功能
     * @param: ticket
     * @return: java.lang.String
     * @author DB1412
     * @date: 10:17 2023-03-21
     */
    @RequestMapping(path = "/logout", method = RequestMethod.GET)
    public String logout(@CookieValue("ticket") String ticket) {
        userService.logout(ticket);
        SecurityContextHolder.clearContext();
        return "redirect:/login";//重定向默认为get请求
    }

    // 忘记密码页面
    @RequestMapping(path = "/forget", method = RequestMethod.GET)
    public String getForgetPage() {
        return "/site/forget";
    }

    /**
     * @description: 忘记密码后，点击获取验证码后需要将验证码发送给用户邮箱
     * @param: email
 * @param: session 用于保存验证码；用于之后比较页面传回的验证码是否一致，一致才允许修改
     * @return: java.lang.String
     * @author DB1412
     * @date: 21:22 2023-03-20
     */
    @RequestMapping(path = "/forget/code", method = RequestMethod.GET)
    public String getForgetCode(String email, HttpSession session){
        if (StringUtils.isBlank(email)) {
            return CommunityUtil.getJSONString(1, "邮箱不能为空！");
        }

        if(!userService.isEmailExist(email)){
            return CommunityUtil.getJSONString(1, "邮箱尚未注册");
        }

        // 发送邮件
        Context context = new Context();
        context.setVariable("email", email);
        String code = CommunityUtil.generateUUID().substring(0, 4);
        context.setVariable("verifyCode", code);
        String content = templateEngine.process("/mail/forget", context);
        mailClient.sendMail(email, "找回密码", content);

        // 保存验证码（为了防止修改其他用户，因此需要在保存验证码对验证码进行处理）
        session.setAttribute(email + "_verifyCode", code);

        return CommunityUtil.getJSONString(0);
    }

    /**
     * @description: 提交忘记密码的表单后进行密码修改
     * @param: email
 * @param: verifyCode
 * @param: password
 * @param: model
 * @param: session
     * @return: java.lang.String
     * @author DB1412
     * @date: 21:57 2023-03-20
     */
    @RequestMapping(path = "/forget/password", method = RequestMethod.POST)
    public String resetPassword(String email, String verifyCode, String password, Model model, HttpSession session) {
        String code = (String) session.getAttribute(email + "_verifyCode");//获取之前获取验证码的用户的验证码
        if (StringUtils.isBlank(verifyCode) || StringUtils.isBlank(code) || !code.equalsIgnoreCase(verifyCode)) {
            model.addAttribute("codeMsg", "验证码错误!");
            return "/site/forget";
        }
        //获取业务层中的map
        Map<String, Object> map = userService.resetPassword(email, password);
        if (map.containsKey("user")) {//如果包含用户说明修改成功
            return "redirect:/login";
        } else {//否则修改错误
            model.addAttribute("emailMsg", map.get("emailMsg"));
            model.addAttribute("passwordMsg", map.get("passwordMsg"));
            return "/site/forget";
        }
    }

}
