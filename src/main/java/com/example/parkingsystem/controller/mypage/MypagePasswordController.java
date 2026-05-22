package com.example.parkingsystem.controller.mypage;

import com.example.parkingsystem.dto.auth.AdminDTO;
import com.example.parkingsystem.service.auth.AdminService;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import lombok.extern.log4j.Log4j2;
import org.mindrot.jbcrypt.BCrypt;

import java.io.IOException;

@Log4j2
@WebServlet("/main/mypage/password")
public class MypagePasswordController extends HttpServlet {
    private final AdminService adminService = AdminService.INSTANCE;

    // 현재 비밀번호를 확인한 뒤 새 비밀번호로 갱신
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        HttpSession session = req.getSession(false);
        if (session == null) {
            log.warn("마이페이지 비밀번호 변경 실패: 세션 없음");
            resp.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            return;
        }

        String adminId = (String) session.getAttribute("adminId");
        String password = req.getParameter("password");
        String newPassword = req.getParameter("newPassword");
        String newPasswordCheck = req.getParameter("newPasswordCheck");
        log.info("마이페이지 비밀번호 변경 요청 adminId={}", adminId);

        if (adminId == null || password == null || newPassword == null) {
            log.warn("마이페이지 비밀번호 변경 실패: 필수 파라미터 누락 adminId={}", adminId);
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            return;
        }

        if (newPasswordCheck != null && !newPassword.equals(newPasswordCheck)) {
            log.warn("마이페이지 비밀번호 변경 실패: 새 비밀번호 확인 불일치 adminId={}", adminId);
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            return;
        }

        AdminDTO admin = adminService.getAdminById(adminId);
        if (admin == null || !BCrypt.checkpw(password, admin.getPassword())) {
            log.warn("마이페이지 비밀번호 변경 실패: 기존 비밀번호 불일치 adminId={}", adminId);
            resp.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            return;
        }

        AdminDTO modifiedAdmin = AdminDTO.builder()
                .adminId(adminId)
                .password(BCrypt.hashpw(newPassword, BCrypt.gensalt(12)))
                .adminEmail(admin.getAdminEmail())
                .isPasswordReset(false)
                .build();

        adminService.modifyAdmin(modifiedAdmin);
        log.info("마이페이지 비밀번호 변경 완료 adminId={}", adminId);
        resp.setStatus(HttpServletResponse.SC_OK);
    }
}
