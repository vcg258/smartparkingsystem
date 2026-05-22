package com.example.parkingsystem.controller.auth;

import com.example.parkingsystem.dto.auth.AdminDTO;
import com.example.parkingsystem.service.auth.AdminService;
import com.example.parkingsystem.service.auth.ValidationService;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import lombok.extern.log4j.Log4j2;

import java.io.IOException;

@Log4j2
@WebServlet("/login/email")
public class LoginEmailController extends HttpServlet {
    private final AdminService adminService = AdminService.INSTANCE;
    private final ValidationService validationService = ValidationService.INSTANCE;

    // 임시 세션의 관리자와 입력한 이메일이 일치하면 OTP 발송
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        HttpSession session = req.getSession(false);
        String email = trim(req.getParameter("email"));
        log.info("로그인 이메일 인증 요청 email={}", email);

        if (session == null || email == null) {
            log.warn("로그인 이메일 인증 실패: 세션 또는 이메일 누락");
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            return;
        }

        String tempAdminId = (String) session.getAttribute("tempAdminId");
        if (tempAdminId == null) {
            log.warn("로그인 이메일 인증 실패: 임시 세션 없음");
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            return;
        }

        AdminDTO admin = adminService.getAdminById(tempAdminId);
        if (admin == null || !email.equals(admin.getAdminEmail())) {
            log.warn("로그인 이메일 인증 실패: 계정 불일치 tempAdminId={}", tempAdminId);
            resp.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            return;
        }

        validationService.otpShipment(tempAdminId);
        log.info("로그인 이메일 인증 성공 및 OTP 발송 tempAdminId={}", tempAdminId);
        resp.setStatus(HttpServletResponse.SC_OK);
    }

    private String trim(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}
