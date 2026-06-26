package com.campus.trade.dto;

import lombok.Data;

@Data
public class UserUpdateDTO {
    private String nickname;
    private String phone;
    private String password;       // 原密码
    private String newPassword;    // 新密码
    private String confirmPassword;// 确认密码

    // Getter and Setter methods
    public String getNickname() { return nickname; }
    public void setNickname(String nickname) { this.nickname = nickname; }
    
    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }
    
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
    
    public String getNewPassword() { return newPassword; }
    public void setNewPassword(String newPassword) { this.newPassword = newPassword; }
    
    public String getConfirmPassword() { return confirmPassword; }
    public void setConfirmPassword(String confirmPassword) { this.confirmPassword = confirmPassword; }
}