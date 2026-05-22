package com.example.parkingsystem.controller.main;

import com.example.parkingsystem.dto.main.ParkingHistoryDTO;
import com.example.parkingsystem.dto.payment.PaymentHistoryDTO;
import com.example.parkingsystem.service.main.ParkingHistoryService;
import com.example.parkingsystem.service.payment.PaymentHistoryService;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.log4j.Log4j2;

import java.io.IOException;

@Log4j2
@WebServlet("/parking/getPaymentInfo")
public class ParkingPaymentInfoController extends HttpServlet {
    private final ParkingHistoryService parkingService = ParkingHistoryService.INSTANCE;
    private final PaymentHistoryService paymentHistoryService = PaymentHistoryService.INSTANCE;

    // 카드 클릭(출차) 시 Java에서 계산한 미리보기 요금 반환
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        resp.setContentType("application/json;charset=UTF-8");

        Long parkNo = Long.valueOf(req.getParameter("parkNo"));
        try {
            ParkingHistoryDTO parkingHistoryDTO = parkingService.getParkingHistory(parkNo);
            if (parkingHistoryDTO == null) {
                resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
                resp.getWriter().write("{\"error\": \"주차 정보 없음\"}");
                return;
            }

            String carNum = parkingHistoryDTO.getCarNum();
            PaymentHistoryDTO result = paymentHistoryService.calcCharge(carNum);
            if (result == null) {
                resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                resp.getWriter().write("{\"error\": \"요금 계산 실패\"}");
                return;
            }

            // isMember: 회원 할인 적용 여부 (JS에서 "회원 할인 적용" 텍스트 표시용)
            boolean isMember = "월정액 회원 할인 (100%)".equals(result.getDiscountName());

            resp.getWriter().write(
                "{\"carNum\": \""        + result.getCarNum()        + "\"" +
                ", \"inTime\": \""       + result.getEntryTime()     + "\"" +
                ", \"outTime\": \""      + result.getExitTime()      + "\"" +
                ", \"totalMinutes\": "   + result.getTotalMinutes()  +
                ", \"baseCharge\": "     + result.getBaseCharge()    +
                ", \"extraCharge\": "    + result.getExtraCharge()   +
                ", \"discountAmount\": " + result.getDiscountAmount()+
                ", \"discountName\": \"" + result.getDiscountName()  + "\"" +
                ", \"isMember\": "       + isMember                  +
                ", \"finalCharge\": "    + result.getFinalCharge()   + "}"
            );
        } catch (Exception e) {
            log.error("getPaymentInfo 에러", e);
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            resp.getWriter().write("{\"error\": \"" + e.getMessage() + "\"}");
        }
    }
}
