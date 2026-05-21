package com.example.parkingsystem.vo.auth;

import lombok.*;

import java.time.LocalDateTime;

@Getter
@Builder
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class AdminVO {
    private String adminId; // 관리자 아이디(PK)
    private String password; // 관리자 비밀번호
    private String adminName; // 관리자 이름
    private String adminEmail; // 관리자 이메일(UNIQUE)
    private boolean isActive; // 활동 여부 (계정활동 허용, 비허용)
    private LocalDateTime lastLogin; // 마지막 로그인 날짜
    private String lastLoginIp; // 마지막 로그인 아이피
    private boolean isPasswordReset; // 최조 로그인 여부 True여부, False일반
    private String uuid; // 로그인 상태유지 토큰
    private LocalDateTime createdAt; // 계정 생성일
}
