package com.example.parkingsystem.filter;


import com.example.parkingsystem.service.auth.AdminService;
import com.example.parkingsystem.service.setting.PaymentInfoService;
import jakarta.servlet.*;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import lombok.extern.log4j.Log4j2;

import java.io.IOException;

@Log4j2
@WebFilter("/*")
public class AuthFilter implements Filter {
    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        HttpServletRequest req = (HttpServletRequest) request;
        HttpServletResponse resp = (HttpServletResponse) response;

        AdminService adminService = AdminService.INSTANCE;
        String uri = req.getRequestURI();
        String contextPath = req.getContextPath();
        String servletPath = req.getServletPath();

        String loginPath = "/login";
        String passwordPath = "/password";
        String logoutPath = "/logout";
        String mypagePath = "/main/mypage";
        String mypageEmailPath = "/main/mypage/email";

        // 속성 파일 필터 제외
        if (uri.endsWith(".css") || uri.endsWith(".js") ||
                uri.endsWith(".png") || uri.endsWith(".jpg") ||
                uri.endsWith(".ico")) {
            chain.doFilter(request, response);
            return;
        }

        // 세션없이 뒤로가기 금지(캐시 저장금지, 재검증) = JS, CSS는 캐시해도 괜찮고 성능향상하기 때문에 제외
        if (!servletPath.startsWith(loginPath) && !servletPath.startsWith(passwordPath) &&
                !uri.endsWith(".css") && !uri.endsWith(".js")) {
            resp.setHeader("Cache-Control", "no-cache, no-store, must-revalidate");
            resp.setHeader("Pragma", "no-cache");
            resp.setHeader("Expires", "0");
        }

        // 제외 폴더 지정 (로그인, 비밀번호 재설정, 로그아웃)
        if (servletPath.startsWith(loginPath) || servletPath.startsWith(passwordPath) ||
                servletPath.startsWith(logoutPath) || servletPath.startsWith(mypageEmailPath)) {
            chain.doFilter(request, response);
            return;
        }

        HttpSession session = req.getSession(false);

        if (session == null || session.getAttribute("adminId") == null) {
            log.info("인증된 세션이 없음 접근 제한");
            resp.sendRedirect(contextPath + loginPath);
            return;
        }

        String adminId = (String) session.getAttribute("adminId");
        log.info(adminId);
        if (adminService.getAdminById(adminId).isPasswordReset()) {
            if (!servletPath.startsWith(mypagePath)) {
                log.info("비밀번호 재설정후 최초 로그인 이동제한");
                resp.sendRedirect(contextPath + mypagePath);
                return;
            }
        }

        // 정책이 없으면 setting 페이지 외 모든 접근 차단
        if (PaymentInfoService.INSTANCE.getInfo() == null) {
            if (!servletPath.startsWith("/setting")) {
                log.info("정책 미설정 → setting 페이지로 강제 이동 adminId={}", adminId);
                resp.sendRedirect(contextPath + "/setting?noPolicy=true");
                return;
            }
        }

        chain.doFilter(request, response);
    }
}
