package com.example.parkingsystem.vo.auth;

import lombok.*;

import java.time.LocalDateTime;

@Getter
@Builder
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class ValidationVO {
    private String no; // 인덱스
    private String adminId; // PK 관리자 아이디
    private String otpCode; // OTP
    private String adminEmail; // OTP 보낸 관리자 이메일
    private LocalDateTime expiredTime; // OTP 만료시간 (유효시간 4분)
}
