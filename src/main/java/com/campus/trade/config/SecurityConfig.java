package com.campus.trade.config;

import com.campus.trade.service.UserService;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.RememberMeServices;
import org.springframework.security.web.authentication.rememberme.JdbcTokenRepositoryImpl;
import org.springframework.security.web.authentication.rememberme.PersistentTokenBasedRememberMeServices;
import org.springframework.security.web.authentication.rememberme.PersistentTokenRepository;

import javax.sql.DataSource;
import java.util.UUID;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final UserService userService;

    @Autowired
    private DataSource dataSource;

    public SecurityConfig(UserService userService) {
        this.userService = userService;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public DaoAuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(userService);
        authProvider.setPasswordEncoder(passwordEncoder());
        return authProvider;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authConfig) throws Exception {
        return authConfig.getAuthenticationManager();
    }

    /**
     * 持久化令牌仓库 - 将记住我 token 存入数据库
     */
    @Bean
    public PersistentTokenRepository persistentTokenRepository() {
        JdbcTokenRepositoryImpl tokenRepository = new JdbcTokenRepositoryImpl();
        tokenRepository.setDataSource(dataSource);
        // ⚠️ 首次启动时打开下面这行，启动成功后注释掉
        // tokenRepository.setCreateTableOnStartup(true);
        return tokenRepository;
    }

    /**
     * 记住我服务
     */
    @Bean
    public RememberMeServices rememberMeServices() {
        PersistentTokenBasedRememberMeServices rememberMeServices =
                new PersistentTokenBasedRememberMeServices(
                        UUID.randomUUID().toString(),
                        userService,
                        persistentTokenRepository());
        rememberMeServices.setParameter("remember-me");
        rememberMeServices.setTokenValiditySeconds(60 * 60 * 24 * 7); // 7天
        return rememberMeServices;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(authorize -> authorize
                        // 所有人可访问
                        .requestMatchers(
                                "/", "/index",
                                "/toLoginPage", "/login", "/logout",
                                "/error", "/favicon.ico",
                                "/css/**", "/js/**", "/images/**", "/avatars/**",
                                "/api/**"
                        ).permitAll()
                        // 用户管理（/admin/users/**）仅限管理员访问（ROLE_ADMIN）
                        .requestMatchers("/admin/users/**").hasRole("ADMIN")
                        // 文章管理（/admin/articles/**）允许 ADMIN 和 USER 访问
                        .requestMatchers("/admin/articles/**").hasAnyRole("ADMIN", "USER")
                        // 登录即可访问
                        .requestMatchers("/users").authenticated()
                        // 管理员后台首页
                        .requestMatchers("/admin/dashboard").hasRole("ADMIN")
                        // 其他请求需要登录
                        .anyRequest().authenticated()
                )
                .formLogin(form -> form
                        .loginPage("/toLoginPage")
                        .loginProcessingUrl("/login")
                        .defaultSuccessUrl("/", true)
                        .failureUrl("/toLoginPage?error=true")  // 改回原来的方式
                        .permitAll()
                )
                // === 记住我配置 ===
                .rememberMe(rememberMe -> rememberMe
                        .tokenRepository(persistentTokenRepository())
                        .tokenValiditySeconds(60 * 60 * 24 * 7)
                        .userDetailsService(userService)
                        .rememberMeParameter("remember-me")
                        .rememberMeServices(rememberMeServices())
                )
                // === 退出配置（清除记住我Cookie） ===
                .logout(logout -> logout
                        .logoutUrl("/logout")
                        .logoutSuccessUrl("/toLoginPage?logout=true")
                        .invalidateHttpSession(true)
                        .clearAuthentication(true)
                        .deleteCookies("JSESSIONID", "remember-me")
                        .permitAll()
                )
                // === 403 权限拒绝处理（直接返回403页面，不跳转登录页） ===
                .exceptionHandling(exception -> exception
                        .accessDeniedHandler((request, response, accessDeniedException) -> {
                            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                            response.setContentType("text/html;charset=UTF-8");
                            response.getWriter().write("<!DOCTYPE html>\n" +
                                    "<html>\n" +
                                    "<head>\n" +
                                    "    <meta charset=\"UTF-8\">\n" +
                                    "    <title>403 - 禁止访问</title>\n" +
                                    "    <style>\n" +
                                    "        body { font-family: 'Microsoft YaHei', Arial, sans-serif; text-align: center; padding: 50px; }\n" +
                                    "        h1 { font-size: 80px; color: #f56c6c; margin: 0; }\n" +
                                    "        p { color: #666; font-size: 18px; margin: 20px 0; }\n" +
                                    "        a { color: #409EFF; text-decoration: none; }\n" +
                                    "        a:hover { text-decoration: underline; }\n" +
                                    "    </style>\n" +
                                    "</head>\n" +
                                    "<body>\n" +
                                    "    <h1>403</h1>\n" +
                                    "    <p>抱歉，您没有权限访问该页面。</p>\n" +
                                    "    <p><a href=\"/\">返回首页</a> | <a href=\"/toLoginPage\">重新登录</a></p>\n" +
                                    "</body>\n" +
                                    "</html>");
                        })
                );

        return http.build();
    }
}