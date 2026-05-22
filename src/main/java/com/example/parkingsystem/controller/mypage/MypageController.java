package com.example.parkingsystem.controller.mypage;


import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.log4j.Log4j2;

import java.io.IOException;

@Log4j2
@WebServlet("/main/mypage")
public class MypageController extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        // 마이페이지 화면을 반환
        log.info("마이페이지 화면 조회");
        req.getRequestDispatcher("/WEB-INF/login/myPage.jsp").forward(req, resp);
    }
}
