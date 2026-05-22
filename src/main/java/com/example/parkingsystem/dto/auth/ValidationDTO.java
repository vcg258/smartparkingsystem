package com.example.parkingsystem.dto.auth;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ValidationDTO {
    private String adminId; // PK 관리자 아이디
    private String otpCode; // OTP
    private String uuid; // 재설정 비밀번호 (DTO전용 SMTP)
    private String adminEmail; // OTP 보낸 관리자 이메일
    private LocalDateTime expiredTime; // OTP 만료시간 (유효시간 4분)
}
