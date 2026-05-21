package com.example.parkingsystem.controller.auth;

import com.example.parkingsystem.dto.auth.AdminDTO;
import com.example.parkingsystem.service.auth.AdminService;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import lombok.extern.log4j.Log4j2;

import java.io.IOException;
import java.util.UUID;

@Log4j2
@WebServlet("/login/authenticate")
public class LoginAuthenticateController extends HttpServlet {
    private final AdminService adminService = AdminService.INSTANCE;

    // 아이디와 비밀번호를 확인하고 OTP 단계로 넘어갈 임시 세션 생성
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String adminId = trim(req.getParameter("adminId"));
        String password = req.getParameter("password");
        boolean rememberMe = "true".equals(req.getParameter("rememberMe"));

        if (adminId == null || password == null || password.isBlank()) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            return;
        }

        if (!adminService.AuthenticateAdmin(adminId, password)) {
            resp.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            return;
        }

        AdminDTO adminDTO = adminService.getAdminById(adminId);
        if (adminDTO == null) {
            resp.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            return;
        }

        if (!adminDTO.isActive()) {
            resp.setStatus(HttpServletResponse.SC_FORBIDDEN);
            return;
        }

        if (rememberMe) {
            issueRememberMeCookie(resp, adminId);
        }

        HttpSession session = req.getSession();
        session.setAttribute("tempAdminId", adminId);
        resp.setStatus(HttpServletResponse.SC_OK);
    }

    // 로그인 상태 유지를 선택한 경우 UUID 쿠키를 발급
    private void issueRememberMeCookie(HttpServletResponse resp, String adminId) {
        String uuid = UUID.randomUUID().toString();

        AdminDTO adminDTO = AdminDTO.builder()
                .adminId(adminId)
                .uuid(uuid)
                .build();

        adminService.modifyUUID(adminDTO);

        Cookie cookie = new Cookie("rememberMe", uuid);
        cookie.setMaxAge(60 * 30);
        cookie.setPath("/");
        resp.addCookie(cookie);
    }

    private String trim(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}
