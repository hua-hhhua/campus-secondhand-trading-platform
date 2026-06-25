package com.campus.trade.dto;

import lombok.Data;

@Data
public class UserUpdateDTO {
    private String nickname;
    private String phone;
    private String password;       // 原密码
    private String newPassword;    // 新密码
    private String confirmPassword;// 确认密码
}