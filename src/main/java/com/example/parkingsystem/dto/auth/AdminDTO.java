package com.example.parkingsystem.dto.auth;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdminDTO {
    private String adminId; // 관리자 아이디
    private String password; // 관리자 비밀번호
    private String adminName; // 관리자 이름
    private String adminEmail; // 관리자 이메일
    private boolean isActive; // 활동 여부 (계정활동 허용, 비허용)
    private LocalDateTime lastLogin; // 마지막 로그인 날짜
    private String lastLoginIp; // 마지막 로그인 아이피
    private boolean isPasswordReset; // 최초 로그인 여부 True여부, False일반
    private String uuid; // 로그인 상태유지 토큰
}
