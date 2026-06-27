package com.campus.trade.config;

import com.campus.trade.entity.User;
import com.campus.trade.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

@Component
public class UserContextInterceptor implements HandlerInterceptor {

    private static final Logger logger = LoggerFactory.getLogger(UserContextInterceptor.class);

    @Autowired
    private UserService userService;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        HttpSession session = request.getSession();

        if (auth != null && auth.isAuthenticated() && !(auth instanceof AnonymousAuthenticationToken)) {
            String username = auth.getName();

            // 从Session获取用户信息，避免重复查询数据库
            User user = (User) session.getAttribute("currentUser");
            if (user == null || !username.equals(user.getUsername())) {
                user = userService.getUserByUsername(username);
                if (user != null) {
                    // 确保头像路径包含 /avatars/ 前缀
                    String avatar = user.getAvatar();
                    if (avatar != null && !avatar.isEmpty() && !avatar.startsWith("/")) {
                        // 如果只有文件名，添加 /avatars/ 前缀
                        avatar = "/avatars/" + avatar;
                        user.setAvatar(avatar);
                    } else if (avatar != null && avatar.startsWith("/uploads/avatars/")) {
                        // 如果是旧路径格式，转换为新格式
                        avatar = avatar.replace("/uploads/avatars/", "/avatars/");
                        user.setAvatar(avatar);
                    }

                    session.setAttribute("currentUser", user);
                    session.setAttribute("username", user.getUsername());
                    session.setAttribute("userNickname", user.getNickname());
                    session.setAttribute("userAvatar", user.getAvatar());
                    String roleName = (user.getRole() != null && user.getRole() == 1) ? "ADMIN" : "USER";
                    session.setAttribute("userRole", roleName);
                    session.setAttribute("userId", user.getId());
                    logger.info("拦截器 - 用户信息已存入Session: {}, 头像: {}", username, user.getAvatar());
                }
            } else {
                logger.debug("拦截器 - 从Session获取用户信息: {}", username);
            }
        } else {
            // 未登录用户，清除Session中的用户信息
            session.removeAttribute("currentUser");
            session.removeAttribute("username");
            session.removeAttribute("userNickname");
            session.removeAttribute("userAvatar");
            session.removeAttribute("userRole");
            session.removeAttribute("userId");
            logger.debug("拦截器 - 未登录用户，已清除Session");
        }

        return true;
    }

    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) throws Exception {
        // 只对视图渲染的请求添加Model属性（不是所有请求都有modelAndView）
        if (modelAndView != null && !isStaticResource(request.getRequestURI())) {
            HttpSession session = request.getSession(false);
            if (session != null) {
                Object username = session.getAttribute("username");
                if (username != null) {
                    modelAndView.addObject("currentUser", session.getAttribute("currentUser"));
                    modelAndView.addObject("username", username);
                    modelAndView.addObject("userNickname", session.getAttribute("userNickname"));
                    modelAndView.addObject("userAvatar", session.getAttribute("userAvatar"));
                    modelAndView.addObject("userRole", session.getAttribute("userRole"));
                    logger.info("拦截器 - 已将用户信息添加到Model: {}", username);
                }
            }
        }
    }

    /**
     * 判断是否为静态资源请求
     */
    private boolean isStaticResource(String uri) {
        return uri.startsWith("/css/") || uri.startsWith("/js/") || uri.startsWith("/images/")
                || uri.startsWith("/avatars/") || uri.startsWith("/static/") || uri.endsWith(".css")
                || uri.endsWith(".js") || uri.endsWith(".jpg") || uri.endsWith(".png") || uri.endsWith(".ico");
    }
}