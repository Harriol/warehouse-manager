package com.pn.warehouse_manager.controller;

import com.google.code.kaptcha.Producer;
import com.pn.warehouse_manager.entity.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
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
     * @return
     */
    @RequestMapping("/login")
    public Result login(){
        return Result.ok();
    }
}
