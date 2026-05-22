package com.example.parkingsystem.controller.auth;


import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.log4j.Log4j2;

import java.io.IOException;

@Log4j2
@WebServlet("/password")
public class LoginPasswordController extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        // 비밀번호 재설정 화면을 반환
        log.info("비밀번호 재설정 화면 조회");
        req.getRequestDispatcher("/WEB-INF/login/password.jsp").forward(req,resp);
    }
}
