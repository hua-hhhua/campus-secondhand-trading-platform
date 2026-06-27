package com.campus.trade.config;

import cn.hutool.core.util.StrUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.LocaleResolver;

import java.util.Locale;

@Configuration
public class MyLocaleResolver implements LocaleResolver {

    @Override
    public Locale resolveLocale(HttpServletRequest request) {
        String lang = request.getParameter("lang");
        Locale locale = null;

        // 优先从请求参数获取语言
        if (StrUtil.isNotBlank(lang)) {
            String[] split = lang.split("_");
            if (split.length == 2) {
                locale = new Locale(split[0], split[1]);
            }
        }

        // 从请求头获取
        if (locale == null) {
            String header = request.getHeader("Accept-Language");
            if (StrUtil.isNotBlank(header)) {
                String[] splits = header.split(",");
                if (splits.length > 0) {
                    String[] split = splits[0].split("-");
                    if (split.length == 2) {
                        locale = new Locale(split[0], split[1]);
                    }
                }
            }
        }

        // 默认中文
        if (locale == null) {
            locale = Locale.CHINA;
        }

        return locale;
    }

    @Override
    public void setLocale(HttpServletRequest request, HttpServletResponse response, Locale locale) {
        if (locale != null) {
            response.setHeader("Content-Language", locale.toLanguageTag());
        }
    }
}