package com.kimyt.reggie.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import java.util.Random;
import java.util.concurrent.TimeUnit;

@Service
public class EmailService {
    
    @Autowired
    private JavaMailSender mailSender;

    @Autowired
    private RedisTemplate<String, String> redisTemplate;

    @Value("${spring.mail.username}")
    private String fromEmail;

    public String generateCode() {
        return String.format("%06d", new Random().nextInt(999999));
    }

    public void sendVerificationCode(String toEmail, String code) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(fromEmail);
        message.setTo(toEmail);
        message.setSubject("【曼巴烧烤】登录验证码");
        message.setText("你的登录验证码是：" + code + "，有效期5分钟。");
        mailSender.send(message);
    }

    public void saveCodeToRedis(String email, String code) {
        String redisKey = "emailCode:" + email;
        redisTemplate.opsForValue().set(redisKey, code, 5, TimeUnit.MINUTES);
    }

    public void saveSendTimeToRedis(String email) {
        String redisKey = "emailCodeTime:" + email;
        redisTemplate.opsForValue().set(redisKey, String.valueOf(System.currentTimeMillis()), 10, TimeUnit.MINUTES);
    }

    public String getCodeFromRedis(String email) {
        String redisKey = "emailCode:" + email;
        return redisTemplate.opsForValue().get(redisKey);
    }

    public Long getSendTimeFromRedis(String email) {
        String redisKey = "emailCodeTime:" + email;
        String timeStr = redisTemplate.opsForValue().get(redisKey);
        if (timeStr != null) {
            return Long.parseLong(timeStr);
        }
        return null;
    }

    public void deleteCodeFromRedis(String email) {
        String redisKey = "emailCode:" + email;
        String timeKey = "emailCodeTime:" + email;
        redisTemplate.delete(redisKey);
        redisTemplate.delete(timeKey);
    }
}
