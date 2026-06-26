package com.pn.warehouse_manager.controller;

import com.google.code.kaptcha.Producer;
import com.pn.warehouse_manager.entity.CurrentUser;
import com.pn.warehouse_manager.entity.LoginUser;
import com.pn.warehouse_manager.entity.Result;
import com.pn.warehouse_manager.entity.User;
import com.pn.warehouse_manager.service.UserService;
import com.pn.warehouse_manager.utils.DigestUtil;
import com.pn.warehouse_manager.utils.TokenUtils;
import com.pn.warehouse_manager.utils.WarehouseConstants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.imageio.ImageIO;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.concurrent.TimeUnit;

@RestController
public class LoginController {
    //注入DefaultCaptcha的bean对象 -- 生成验证码图片
    @Autowired
    private Producer producer;

    //注入redis模板
    @Autowired
    private StringRedisTemplate redisTemplate;

    /**
     * 生成验证码图片的url接口/captcha/captchaImage
     * @param response
     */
    @RequestMapping("/captcha/captchaImage")
    public void captchaImage(HttpServletResponse response) {
        ServletOutputStream out = null;
        try {
            //生成验证码图片的文本
            String text = producer.createText();
            //使用验证码文本生成验证码图片 -- BufferedImage对象就代表生成的图片存在内存中
            BufferedImage image = producer.createImage(text);
            //将验证码文本保存到redis -- 设置键的过期时间10分钟
            redisTemplate.opsForValue().set(text, "", 60 * 2, TimeUnit.SECONDS);

            /*
              将验证码图片相应给前端：
             */
            //设置相应正文image/jpeg
            response.setContentType("image/jpeg");
            //将验证码图片写给前端
            out = response.getOutputStream();
            ImageIO.write(image, "jpg", out); // 使用相应对象的字节输出流写入验证码图片，自然是给前端写入
            //刷新
            out.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }finally {
            //关闭字节输出流
            if(out!=null){
                try {
                    out.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }


    /**
     * 登录的url接口/login
     * @RequestBody LoginUser loginUser表示接收并封装前端传递的登录用户信息的json数据
     * 返回值Result对象 -- 表示向前端响应结果对象转的json串，包含响应状态码、成功失败响应、响应信息、响应数据
     */
    //注入UserService
    @Autowired
    private UserService userService;
    //注入TokenUtils的bean对象
    @Autowired
    private TokenUtils tokenUtils;

    @RequestMapping("/login")
    public Result login(@RequestBody LoginUser loginUser){
        //拿到客户输入的验证码
        String code = loginUser.getVerificationCode();
        if(!redisTemplate.hasKey(code)){
            return Result.err(Result.CODE_ERR_BUSINESS,"验证码错误");
        }
        //根据账号查询用户
        User user = userService.queryUserByCode(loginUser.getUserCode());
        //账号存在
        if (user != null) {
            //用户已审核
            if(user.getUserState().equals(WarehouseConstants.USER_STATE_PASS)){
                //拿到用户录入的密码
                String userPwd = loginUser.getUserPwd();
                //进行md5加密
                String md5Pwd = DigestUtil.hmacSign(userPwd);
                //密码合法
                if(md5Pwd.equals(user.getUserPwd())){
                    //生成jwt token并存入redis
                    CurrentUser currentUser =
                            new CurrentUser(user.getUserId(),user.getUserCode(),user.getUserName());
                    String token = tokenUtils.loginSign(currentUser, md5Pwd);
                    //向客户端相应token
                    return Result.ok("登录成功！",token);
                }else {
                    return Result.err(Result.CODE_ERR_BUSINESS,"密码错误！");
                }
            }else {
                //用户未审核
                return Result.err(Result.CODE_ERR_BUSINESS,"用户未审核！");
            }
        }else {
            return Result.err(Result.CODE_ERR_BUSINESS,"账号不存在！");
        }
    }

    /**获取当前登录的用户的用户信息的url接口/curr-user
     * 参数@RequestHeader(WarehouseConstants.HEADER_TOKEN_NAME) String token --
     * 表示将请求头token的值（前端归还的token赋值给请求处理方法入参变量token）;
     * @param token
     * @return
     */
    @RequestMapping("/curr-user")
    public Result currentUser(@RequestHeader(WarehouseConstants.HEADER_TOKEN_NAME) String token){
        //解析token拿到封装了当前登录用户信息的CurrentUser对象
        CurrentUser currentUser = tokenUtils.getCurrentUser(token);
        //响应
        return Result.ok(currentUser);
    }
}
