package com.campus.trade.config;

import com.campus.trade.exception.BusinessException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.NoHandlerFoundException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

@ControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(AccessDeniedException.class)
    public String handleAccessDeniedException(AccessDeniedException e, Model model) {
        logger.warn("访问被拒绝: {}", e.getMessage());
        model.addAttribute("errorCode", 403);
        model.addAttribute("errorMessage", "抱歉，您没有权限访问该页面");
        model.addAttribute("errorDetail", e.getMessage());
        return "error/403";
    }

    @ExceptionHandler(NoHandlerFoundException.class)
    public String handleNotFoundException(NoHandlerFoundException e, Model model) {
        logger.warn("资源未找到: {}", e.getRequestURL());
        model.addAttribute("errorCode", 404);
        model.addAttribute("errorMessage", "抱歉，您访问的页面不存在");
        model.addAttribute("errorDetail", e.getRequestURL());
        return "error/404";
    }

    @ExceptionHandler(NoResourceFoundException.class)
    public String handleNoResourceFoundException(NoResourceFoundException e, Model model) {
        logger.debug("静态资源未找到: {}", e.getResourcePath());
        return null;
    }


    @ExceptionHandler(Exception.class)
    public String handleException(Exception e, Model model) {
        logger.error("系统异常: ", e);
        model.addAttribute("errorCode", 500);
        model.addAttribute("errorMessage", "服务器内部错误，请稍后重试");
        model.addAttribute("errorDetail", e.getMessage());
        return "error/500";
    }

    @ExceptionHandler(RuntimeException.class)
    public String handleRuntimeException(RuntimeException e, Model model) {
        logger.error("运行时异常: ", e);
        model.addAttribute("errorCode", 500);
        model.addAttribute("errorMessage", "操作失败，请稍后重试");
        model.addAttribute("errorDetail", e.getMessage());
        return "error/500";
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public String handleIllegalArgumentException(IllegalArgumentException e, Model model) {
        logger.warn("参数错误: {}", e.getMessage());
        model.addAttribute("errorCode", 400);
        model.addAttribute("errorMessage", "请求参数错误");
        model.addAttribute("errorDetail", e.getMessage());
        return "error/400";
    }

    @ExceptionHandler(BusinessException.class)
    public String handleBusinessException(BusinessException e, Model model) {
        logger.warn("业务异常: {}", e.getMessage());
        model.addAttribute("errorCode", e.getCode() != null ? e.getCode() : 500);
        model.addAttribute("errorMessage", e.getMessage());
        model.addAttribute("errorDetail", null);
        return "error/error";
    }
}