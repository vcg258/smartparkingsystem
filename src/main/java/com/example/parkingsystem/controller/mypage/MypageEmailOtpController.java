package com.example.parkingsystem.controller.mypage;

import com.example.parkingsystem.dto.auth.ValidationDTO;
import com.example.parkingsystem.service.auth.ValidationService;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import lombok.extern.log4j.Log4j2;

import java.io.IOException;
import java.time.LocalDateTime;

@Log4j2
@WebServlet("/main/mypage/email/otp")
public class MypageEmailOtpController extends HttpServlet {
    private final ValidationService validationService = ValidationService.INSTANCE;

    // 이메일 변경 팝업에서 입력한 OTP를 검증
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        HttpSession session = req.getSession(false);
        String adminId = session == null ? null : (String) session.getAttribute("adminId");
        String otpCode = trim(req.getParameter("otpCode"));
        log.info("마이페이지 이메일 OTP 검증 요청 adminId={}", adminId);

        if (adminId == null || otpCode == null) {
            log.warn("마이페이지 이메일 OTP 검증 실패: 필수값 누락");
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            return;
        }

        String result = validateOtp(adminId, otpCode);
        switch (result) {
            case "Success" -> {
                log.info("마이페이지 이메일 OTP 검증 성공 adminId={}", adminId);
                resp.setStatus(HttpServletResponse.SC_OK);
            }
            case "Expired" -> {
                log.warn("마이페이지 이메일 OTP 만료 adminId={}", adminId);
                resp.setStatus(HttpServletResponse.SC_FORBIDDEN);
            }
            case "Fail" -> {
                log.warn("마이페이지 이메일 OTP 불일치 adminId={}", adminId);
                resp.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            }
            default -> {
                log.warn("마이페이지 이메일 OTP 검증 오류 adminId={}", adminId);
                resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            }
        }
    }

    private String validateOtp(String adminId, String otpCode) {
        ValidationDTO validationDTO = validationService.getOTP(adminId);
        if (validationDTO == null) {
            return "Error";
        }
        if (LocalDateTime.now().isAfter(validationDTO.getExpiredTime())) {
            return "Expired";
        }
        return validationDTO.getOtpCode().equals(otpCode) ? "Success" : "Fail";
    }

    private String trim(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}
