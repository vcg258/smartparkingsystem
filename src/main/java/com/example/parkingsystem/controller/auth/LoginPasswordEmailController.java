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
@WebServlet("/password/email")
public class LoginPasswordEmailController extends HttpServlet {
    private final AdminService adminService = AdminService.INSTANCE;
    private final ValidationService validationService = ValidationService.INSTANCE;

    // 아이디와 연결된 이메일이 맞으면 비밀번호 재설정 OTP 발송
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        HttpSession session = req.getSession(false);
        String adminId = session == null ? null : (String) session.getAttribute("logAdminId");
        String email = trim(req.getParameter("email"));
        log.info("비밀번호 재설정 이메일 확인 요청 adminId={}, email={}", adminId, email);

        if (adminId == null || email == null) {
            log.warn("비밀번호 재설정 이메일 확인 실패: 필수값 누락");
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            return;
        }

        AdminDTO admin = adminService.getAdminById(adminId);
        if (admin == null || !email.equals(admin.getAdminEmail())) {
            log.warn("비밀번호 재설정 이메일 확인 실패: 계정/이메일 불일치 adminId={}", adminId);
            resp.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            return;
        }

        session.setAttribute("logEmail", email);
        validationService.otpShipment(adminId);
        log.info("비밀번호 재설정 이메일 확인 성공 및 OTP 발송 adminId={}", adminId);
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
