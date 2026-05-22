package com.example.parkingsystem.controller.mypage;

import com.example.parkingsystem.service.auth.ValidationService;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import lombok.extern.log4j.Log4j2;

import java.io.IOException;

@Log4j2
@WebServlet("/main/mypage/email/resend")
public class MypageEmailResendController extends HttpServlet {
    private final ValidationService validationService = ValidationService.INSTANCE;

    // 이메일 인증 팝업에서 OTP 재발송 요청 처리
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        HttpSession session = req.getSession(false);
        String adminId = session == null ? null : (String) session.getAttribute("adminId");
        log.info("마이페이지 이메일 OTP 재발송 요청 adminId={}", adminId);

        if (adminId == null) {
            log.warn("마이페이지 이메일 OTP 재발송 실패: 세션 없음");
            resp.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            return;
        }

        validationService.otpShipment(adminId);
        log.info("마이페이지 이메일 OTP 재발송 완료 adminId={}", adminId);
        resp.setStatus(HttpServletResponse.SC_OK);
    }
}
