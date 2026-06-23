package com.campus.trade.config;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationFailureHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class CustomAuthenticationFailureHandler extends SimpleUrlAuthenticationFailureHandler {

    @Override
    public void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response,
                                        AuthenticationException exception) throws IOException, ServletException {
        // 不保存任何用户名，让输入框保持空白
        request.getSession().removeAttribute("loginUsername");

        // 重定向到登录页并带上错误参数
        setDefaultFailureUrl("/toLoginPage?error=true");
        super.onAuthenticationFailure(request, response, exception);
    }
}