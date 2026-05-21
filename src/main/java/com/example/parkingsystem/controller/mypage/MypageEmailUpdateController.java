package com.example.parkingsystem.controller.mypage;

import com.example.parkingsystem.dto.auth.AdminDTO;
import com.example.parkingsystem.service.auth.AdminService;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import lombok.extern.log4j.Log4j2;

import java.io.IOException;

@Log4j2
@WebServlet("/main/mypage/email/update")
public class MypageEmailUpdateController extends HttpServlet {
    private final AdminService adminService = AdminService.INSTANCE;

    // OTP 인증이 끝난 새 이메일로 관리자 정보를 수정
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        HttpSession session = req.getSession(false);
        String adminId = session == null ? null : (String) session.getAttribute("adminId");
        String newEmail = trim(req.getParameter("newEmail"));
        log.info("마이페이지 이메일 변경 요청 adminId={}, newEmail={}", adminId, newEmail);

        if (adminId == null || newEmail == null) {
            log.warn("마이페이지 이메일 변경 실패: 필수값 누락");
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            return;
        }

        AdminDTO admin = adminService.getAdminById(adminId);
        if (admin == null) {
            log.warn("마이페이지 이메일 변경 실패: 관리자 정보 없음 adminId={}", adminId);
            resp.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            return;
        }

        AdminDTO modifiedAdmin = AdminDTO.builder()
                .adminId(adminId)
                .password(admin.getPassword())
                .adminEmail(newEmail)
                .isPasswordReset(false)
                .build();

        adminService.modifyAdmin(modifiedAdmin);
        log.info("마이페이지 이메일 변경 완료 adminId={}", adminId);
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
