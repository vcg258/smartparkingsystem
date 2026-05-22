package com.example.parkingsystem.controller.auth;


import com.example.parkingsystem.dto.auth.AdminDTO;
import com.example.parkingsystem.service.auth.AdminService;
import com.example.parkingsystem.service.setting.PaymentInfoService;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import lombok.extern.log4j.Log4j2;

import java.io.IOException;

@Log4j2
@WebServlet("/login")
public class LoginProcessController extends HttpServlet {
    private final AdminService adminService = AdminService.INSTANCE;
    private final PaymentInfoService paymentInfoService = PaymentInfoService.INSTANCE;

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        // 로그인 화면 진입 시 rememberMe 쿠키 기반 자동 로그인 처리
        Cookie[] cookies = req.getCookies();

        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if ("rememberMe".equals(cookie.getName())) {
                    String uuid = cookie.getValue();
                    AdminDTO adminDTO = adminService.getAdminByUuid(uuid);
                    if (adminDTO != null) {
                        log.info("자동 로그인 성공 adminId={}", adminDTO.getAdminId());
                        HttpSession session = req.getSession();
                        session.setAttribute("adminId", adminDTO.getAdminId());
                        if (paymentInfoService.getInfo() == null) {
                            log.info("자동 로그인 후 정책 없음 -> setting 페이지로 리다이렉트 adminId={}", adminDTO.getAdminId());
                            resp.sendRedirect(req.getContextPath() + "/setting?noPolicy=true");
                        } else {
                            resp.sendRedirect(req.getContextPath() + "/main");
                        }
                        return;
                    }
                }
            }
        }

        log.info("로그인 화면 반환");
        req.getRequestDispatcher("/WEB-INF/login/login.jsp").forward(req, resp);
    }
}
