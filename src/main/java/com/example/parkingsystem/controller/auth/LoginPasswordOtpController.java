package com.example.parkingsystem.controller.auth;

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
@WebServlet("/password/otp")
public class LoginPasswordOtpController extends HttpServlet {
    private final ValidationService validationService = ValidationService.INSTANCE;

    // OTP 검증이 끝나면 임시 정보를 지우고 재설정 메일 발송 처리
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        HttpSession session = req.getSession(false);
        String adminId = session == null ? null : (String) session.getAttribute("logAdminId");
        String otpCode = trim(req.getParameter("otpCode"));
        log.info("비밀번호 재설정 OTP 검증 요청 adminId={}", adminId);

        if (adminId == null || otpCode == null) {
            log.warn("비밀번호 재설정 OTP 검증 실패: 필수값 누락");
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            return;
        }

        String result = validateOtp(adminId, otpCode);
        if ("Success".equals(result)) {
            validationService.uuidPassword(adminId);
            session.removeAttribute("logAdminId");
            session.removeAttribute("logEmail");
            log.info("비밀번호 재설정 OTP 검증 성공 adminId={}", adminId);
            resp.setStatus(HttpServletResponse.SC_OK);
        } else if ("Expired".equals(result)) {
            log.warn("비밀번호 재설정 OTP 만료 adminId={}", adminId);
            resp.setStatus(HttpServletResponse.SC_FORBIDDEN);
        } else if ("Fail".equals(result)) {
            log.warn("비밀번호 재설정 OTP 불일치 adminId={}", adminId);
            resp.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        } else {
            log.warn("비밀번호 재설정 OTP 검증 오류 adminId={}", adminId);
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
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
