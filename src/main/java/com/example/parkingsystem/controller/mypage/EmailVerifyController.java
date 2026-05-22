package com.example.parkingsystem.controller.mypage;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.log4j.Log4j2;

import java.io.IOException;

@Log4j2
@WebServlet("/main/mypage/email")
public class EmailVerifyController extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        // 이메일 OTP 팝업 화면을 반환
        log.info("마이페이지 이메일 인증 팝업 조회");
        req.getRequestDispatcher("/WEB-INF/login/emailVerification.jsp").forward(req, resp);
    }
}
