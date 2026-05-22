package com.example.parkingsystem.controller.setting;


import com.example.parkingsystem.dto.setting.PaymentInfoDTO;
import com.example.parkingsystem.service.setting.PaymentInfoService;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import lombok.extern.log4j.Log4j2;

import java.io.IOException;
import java.time.LocalDateTime;

@Log4j2
@WebServlet(name = "setting", value = "/setting")
public class PaymentInfoController extends HttpServlet {
    private final PaymentInfoService paymentInfoService = PaymentInfoService.INSTANCE;

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        PaymentInfoDTO paymentInfoDTO = paymentInfoService.getInfo();

        req.setAttribute("paymentInfoDTO", paymentInfoDTO);

        // 로그인 후 정책 없어서 리다이렉트된 경우 JSP에 noPolicy 플래그 전달
        String noPolicy = req.getParameter("noPolicy");
        if ("true".equals(noPolicy) && paymentInfoDTO == null) {
            req.setAttribute("noPolicy", true);
        }

        req.getRequestDispatcher("/WEB-INF/setting/setting.jsp").forward(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        HttpSession session = req.getSession(false);
        log.info("setting post");

        if (req.getParameter("freeTime") == null || req.getParameter("freeTime").trim().isEmpty() ||
                req.getParameter("basicTime") == null || req.getParameter("basicTime").trim().isEmpty() ||
                req.getParameter("extraTime") == null || req.getParameter("extraTime").trim().isEmpty() ||
                req.getParameter("basicCharge") == null || req.getParameter("basicCharge").trim().isEmpty() ||
                req.getParameter("extraCharge") == null || req.getParameter("extraCharge").trim().isEmpty() ||
                req.getParameter("maxCharge") == null || req.getParameter("maxCharge").trim().isEmpty() ||
                req.getParameter("memberCharge") == null || req.getParameter("memberCharge").trim().isEmpty() ||
                req.getParameter("smallCarDiscount") == null || req.getParameter("smallCarDiscount").trim().isEmpty() ||
                req.getParameter("disabledDiscount") == null || req.getParameter("disabledDiscount").trim().isEmpty()) {

            log.warn("필수 파라미터 누락");
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST); // 400
            return;
        }

        PaymentInfoDTO paymentInfoDTO = PaymentInfoDTO.builder()
                .freeTime(Integer.parseInt(req.getParameter("freeTime")))
                .basicTime(Integer.parseInt(req.getParameter("basicTime")))
                .extraTime(Integer.parseInt(req.getParameter("extraTime")))
                .basicCharge(Integer.parseInt(req.getParameter("basicCharge")))
                .extraCharge(Integer.parseInt(req.getParameter("extraCharge")))
                .maxCharge(Integer.parseInt(req.getParameter("maxCharge")))
                .memberCharge(Integer.parseInt(req.getParameter("memberCharge")))
                .smallCarDiscount(Double.parseDouble(req.getParameter("smallCarDiscount")))
                .disabledDiscount(Double.parseDouble(req.getParameter("disabledDiscount")))
                .updatedAt(LocalDateTime.now())
                .adminId((String) session.getAttribute("adminId"))
                .build();
        log.info("DTO add {}", paymentInfoDTO);

        paymentInfoService.addInfo(paymentInfoDTO);
        resp.setStatus(HttpServletResponse.SC_OK);
        resp.sendRedirect(req.getContextPath() + "/setting");
    }
}
