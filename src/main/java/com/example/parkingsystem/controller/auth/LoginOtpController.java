package com.example.parkingsystem.controller.auth;

import com.example.parkingsystem.dto.auth.AdminDTO;
import com.example.parkingsystem.dto.auth.ValidationDTO;
import com.example.parkingsystem.service.auth.AdminService;
import com.example.parkingsystem.service.auth.ValidationService;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import lombok.extern.log4j.Log4j2;

import java.io.IOException;
import java.time.LocalDateTime;

@WebServlet("/login/otp")
@Log4j2
public class LoginOtpController extends HttpServlet {
    private final AdminService adminService = AdminService.INSTANCE;
    private final ValidationService validationService = ValidationService.INSTANCE;

    // OTP 인증이 끝나면 임시 세션을 실제 로그인 세션으로 전환
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        HttpSession session = req.getSession(false);
        String otpCode = trim(req.getParameter("otpCode"));
        log.info("로그인 OTP 검증 요청");

        if (session == null || otpCode == null) {
            log.warn("로그인 OTP 검증 실패: 세션 또는 코드 누락");
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            return;
        }

        String tempAdminId = (String) session.getAttribute("tempAdminId");
        if (tempAdminId == null) {
            log.warn("로그인 OTP 검증 실패: 임시 세션 없음");
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            return;
        }

        String otpResult = validateOtp(tempAdminId, otpCode);
        switch (otpResult) {
            case "Success" -> {
                log.info("로그인 OTP 검증 성공 tempAdminId={}", tempAdminId);
                completeLogin(req, resp, session, tempAdminId);
            }
            case "Expired" -> {
                log.warn("로그인 OTP 만료 tempAdminId={}", tempAdminId);
                resp.setStatus(HttpServletResponse.SC_FORBIDDEN);
            }
            case "Fail" -> {
                log.warn("로그인 OTP 불일치 tempAdminId={}", tempAdminId);
                resp.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            }
            default -> {
                log.warn("로그인 OTP 검증 오류 tempAdminId={}", tempAdminId);
                resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            }
        }
    }

    // 비밀번호 재설정 대상 계정은 메인 대신 마이페이지로 바로 이동
    private void completeLogin(HttpServletRequest req, HttpServletResponse resp, HttpSession session, String adminId)
            throws IOException {
        session.setAttribute("adminId", adminId);
        session.removeAttribute("tempAdminId");
        adminService.renewalLog(adminId, req.getRemoteAddr());

        AdminDTO admin = adminService.getAdminById(adminId);
        if (admin != null && admin.isPasswordReset()) {
            resp.sendRedirect(req.getContextPath() + "/main/mypage");
            return;
        }

        resp.setStatus(HttpServletResponse.SC_OK);
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
