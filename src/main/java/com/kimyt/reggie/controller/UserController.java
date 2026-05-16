package com.kimyt.reggie.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.kimyt.reggie.common.R;
import com.kimyt.reggie.entity.User;
import com.kimyt.reggie.service.EmailService;
import com.kimyt.reggie.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.MailException;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Locale;
import java.util.Map;
import java.util.regex.Pattern;

@Slf4j
@RestController
@RequestMapping("/user")
public class UserController {

    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$");
    private static final long EMAIL_CODE_EXPIRE_MILLIS = 5 * 60 * 1000L;
    private static final long SEND_INTERVAL_MILLIS = 60 * 1000L;

    @Autowired
    private UserService userService;

    @Autowired
    private EmailService emailService;

    @PostMapping("/sendMsg")
    public R<String> sendMsg(HttpServletRequest request, @RequestBody Map<String, String> params) {
        if (params == null) {
            return R.error("邮箱不能为空");
        }
        String email = normalizeEmail(params.get("email"));
        if (!isValidEmail(email)) {
            return R.error("邮箱格式不正确");
        }

        Long lastSendTime = emailService.getSendTimeFromRedis(email);
        if (lastSendTime != null && System.currentTimeMillis() - lastSendTime < SEND_INTERVAL_MILLIS) {
            return R.error("验证码已发送，请1分钟后再试");
        }

        String code = emailService.generateCode();
        try {
            emailService.sendVerificationCode(email, code);
        } catch (MailException e) {
            log.error("邮箱验证码发送失败，email={}", email, e);
            return R.error("验证码发送失败，请检查邮箱配置");
        }

        emailService.saveCodeToRedis(email, code);
        emailService.saveSendTimeToRedis(email);
        return R.success("验证码发送成功");
    }

    @PostMapping("/login")
    public R<User> login(HttpServletRequest request, @RequestBody Map<String, String> params) {
        if (params == null) {
            return R.error("请输入邮箱和验证码");
        }
        String email = normalizeEmail(params.get("email"));
        String code = params.get("code");

        if (!isValidEmail(email)) {
            return R.error("邮箱格式不正确");
        }
        if (!StringUtils.hasText(code)) {
            return R.error("请输入验证码");
        }

        String sessionCode = emailService.getCodeFromRedis(email);
        if (sessionCode == null || !code.equals(sessionCode)) {
            return R.error("验证码错误或已过期");
        }

        LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(User::getPhone, email);
        User user = userService.getOne(queryWrapper);

        if (user == null) {
            user = new User();
            user.setPhone(email);
            user.setName(email);
            user.setStatus(1);
            userService.save(user);
        } else if (user.getStatus() != null && user.getStatus() == 0) {
            return R.error("账号已禁用");
        }

        request.getSession().setAttribute("user", user.getId());
        emailService.deleteCodeFromRedis(email);
        return R.success(user);
    }

    @PostMapping("/loginout")
    public R<String> loginout(HttpServletRequest request) {
        request.getSession().removeAttribute("user");
        return R.success("退出成功");
    }

    private String normalizeEmail(String email) {
        if (email == null) {
            return "";
        }
        return email.trim().toLowerCase(Locale.ROOT);
    }

    private boolean isValidEmail(String email) {
        return StringUtils.hasText(email) && EMAIL_PATTERN.matcher(email).matches();
    }
}
