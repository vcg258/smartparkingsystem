package com.example.parkingsystem.controller.auth;

import com.example.parkingsystem.service.auth.AdminService;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import lombok.extern.log4j.Log4j2;

import java.io.IOException;

@Log4j2
@WebServlet("/password/id")
public class LoginPasswordIdController extends HttpServlet {
    private final AdminService adminService = AdminService.INSTANCE;

    // 비밀번호 재설정 대상 아이디가 존재하면 임시 세션 생성
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String adminId = trim(req.getParameter("adminId"));
        log.info("비밀번호 재설정 아이디 확인 요청 adminId={}", adminId);

        if (adminId == null) {
            log.warn("비밀번호 재설정 아이디 확인 실패: adminId 누락");
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            return;
        }

        if (adminService.getAdminById(adminId) == null) {
            log.warn("비밀번호 재설정 아이디 확인 실패: 계정 없음 adminId={}", adminId);
            resp.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            return;
        }

        HttpSession session = req.getSession();
        session.setAttribute("logAdminId", adminId);
        log.info("비밀번호 재설정 아이디 확인 성공 adminId={}", adminId);
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
