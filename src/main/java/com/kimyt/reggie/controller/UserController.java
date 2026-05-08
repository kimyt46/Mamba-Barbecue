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
    private static final String EMAIL_CODE_PREFIX = "emailCode:";
    private static final String EMAIL_CODE_TIME_PREFIX = "emailCodeTime:";
    private static final long EMAIL_CODE_EXPIRE_MILLIS = 5 * 60 * 1000L;

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

        String code = emailService.generateCode();
        try {
            emailService.sendVerificationCode(email, code);
        } catch (MailException e) {
            log.error("邮箱验证码发送失败，email={}", email, e);
            return R.error("验证码发送失败，请检查邮箱配置");
        }

        request.getSession().setAttribute(EMAIL_CODE_PREFIX + email, code);
        request.getSession().setAttribute(EMAIL_CODE_TIME_PREFIX + email, System.currentTimeMillis());
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

        String sessionKey = EMAIL_CODE_PREFIX + email;
        String sessionTimeKey = EMAIL_CODE_TIME_PREFIX + email;
        Object sessionCode = request.getSession().getAttribute(sessionKey);
        Object sessionCodeTime = request.getSession().getAttribute(sessionTimeKey);
        if (sessionCode == null || !code.equals(sessionCode.toString())) {
            return R.error("验证码错误");
        }
        if (!(sessionCodeTime instanceof Long)
                || System.currentTimeMillis() - (Long) sessionCodeTime > EMAIL_CODE_EXPIRE_MILLIS) {
            request.getSession().removeAttribute(sessionKey);
            request.getSession().removeAttribute(sessionTimeKey);
            return R.error("验证码已过期，请重新获取");
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
        request.getSession().removeAttribute(sessionKey);
        request.getSession().removeAttribute(sessionTimeKey);
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
