package com.example.parkingsystem.controller.auth;


import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import lombok.extern.log4j.Log4j2;

import java.io.IOException;

@Log4j2
@WebServlet("/logout")
public class Logout extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        HttpSession session = req.getSession(false);

        // 유효성
        if (session != null) {
            String adminId = (String) session.getAttribute("adminId");
            log.info("{} 로그아웃", adminId);
            session.removeAttribute("adminId");
            session.invalidate();
        }
        Cookie[] cookie = req.getCookies();
        if (cookie != null) {
            for (Cookie c : cookie) {
                c.setMaxAge(0);
                resp.addCookie(c);
            }
        }
        resp.sendRedirect(req.getContextPath() + "/login");
    }
}
