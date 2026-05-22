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
@WebServlet("/parking/payment")
public class ParkingPaymentController extends HttpServlet {
    private final ParkingHistoryService parkingService = ParkingHistoryService.INSTANCE;
    private final PaymentHistoryService paymentHistoryService = PaymentHistoryService.INSTANCE;

    // 정산완료 클릭 시 DB insert 후 영수증 데이터 반환
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        resp.setContentType("application/json;charset=UTF-8");

        String parkNoStr = req.getParameter("parkNo");
        log.info("주차 결제 생성 요청 parkNo={}", parkNoStr);

        if (parkNoStr == null) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            resp.getWriter().write("{\"success\": false, \"message\": \"parkNo가 필요합니다\"}");
            return;
        }

        Long parkNo = Long.valueOf(parkNoStr);
        ParkingHistoryDTO parkingHistoryDTO = parkingService.getParkingHistory(parkNo);
        if (parkingHistoryDTO == null) {
            log.warn("주차 결제 생성 실패: 주차 정보 없음 parkNo={}", parkNo);
            resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
            resp.getWriter().write("{\"success\": false, \"message\": \"주차 정보를 찾을 수 없습니다\"}");
            return;
        }

        String carNum = parkingHistoryDTO.getCarNum();

        // calculateFinalCharge()가 계산 + insert + 결과 반환을 한 번에 처리
        PaymentHistoryDTO result = paymentHistoryService.calculateFinalCharge(carNum);
        if (result == null) {
            log.error("주차 결제 생성 실패: 요금 계산 실패 carNum={}", carNum);
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            resp.getWriter().write("{\"success\": false, \"message\": \"결제 정보 조회 실패\"}");
            return;
        }
        log.info("주차 결제 생성 완료 carNum={}, parkNo={}", carNum, parkNo);

        resp.getWriter().write(
                "{\"success\": true" +
                        ", \"carNum\": \""       + carNum                    + "\"" +
                        ", \"entryTime\": \""    + result.getEntryTime()     + "\"" +
                        ", \"exitTime\": \""     + result.getExitTime()      + "\"" +
                        ", \"totalMinutes\": "   + result.getTotalMinutes()  +
                        ", \"baseCharge\": "     + result.getBaseCharge()    +
                        ", \"extraCharge\": "    + result.getExtraCharge()   +
                        ", \"discountAmount\": " + result.getDiscountAmount()+
                        ", \"discountName\": \"" + result.getDiscountName()  + "\"" +
                        ", \"finalCharge\": "    + result.getFinalCharge()   + "}"
        );
    }
}

