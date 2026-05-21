package com.example.parkingsystem.controller.main;

import com.example.parkingsystem.dto.main.ParkingHistoryDTO;
import com.example.parkingsystem.dto.payment.PaymentHistoryDTO;
import com.example.parkingsystem.dto.setting.PaymentInfoDTO;
import com.example.parkingsystem.service.main.ParkingHistoryService;
import com.example.parkingsystem.service.payment.PaymentHistoryService;
import com.example.parkingsystem.service.setting.PaymentInfoService;
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
    private final PaymentInfoService paymentInfoService = PaymentInfoService.INSTANCE;

    // 출차 전 결제 이력을 생성하고 정산 화면용 데이터를 반환
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
        paymentHistoryService.calculateFinalCharge(carNum);

        PaymentHistoryDTO paymentHistoryDTO = paymentHistoryService.getRecentPayment(carNum);
        PaymentInfoDTO paymentInfoDTO = paymentInfoService.getInfo();
        if (paymentHistoryDTO == null) {
            log.error("주차 결제 생성 실패: 결제 이력 조회 실패 carNum={}", carNum);
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            resp.getWriter().write("{\"success\": false, \"message\": \"결제 정보 조회 실패\"}");
            return;
        }
        log.info("주차 결제 생성 완료 carNum={}, parkNo={}", carNum, parkNo);

        resp.getWriter().write(
                "{\"success\": true" +
                        ", \"carNum\": \"" + carNum + "\"" +
                        ", \"entryTime\": \"" + paymentHistoryDTO.getEntryTime() + "\"" +
                        ", \"exitTime\": \"" + paymentHistoryDTO.getExitTime() + "\"" +
                        ", \"totalMinutes\": \"" + paymentHistoryDTO.getTotalMinutes() + "\"" +
                        ", \"basicCharge\": \"" + paymentInfoDTO.getBasicCharge() + "\"" +
                        ", \"extraCharge\": \"" + (paymentHistoryDTO.getTotalCharge() - paymentInfoDTO.getBasicCharge()) + "\"" +
                        ", \"discountAmount\": \"" + paymentHistoryDTO.getDiscountAmount() + "\"" +
                        ", \"totalCharge\": " + paymentHistoryDTO.getTotalCharge() + "}"
        );
    }
}

