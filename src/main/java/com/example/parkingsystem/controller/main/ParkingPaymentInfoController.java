package com.example.parkingsystem.controller.main;

import com.example.parkingsystem.dto.main.ParkingHistoryDTO;
import com.example.parkingsystem.dto.setting.PaymentInfoDTO;
import com.example.parkingsystem.service.main.ParkingHistoryService;
import com.example.parkingsystem.service.setting.PaymentInfoService;
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
    private final PaymentInfoService paymentInfoService = PaymentInfoService.INSTANCE;

    // 출차 계산에 필요한 요금 정책을 입차 시간 기준으로 조회
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        resp.setContentType("application/json;charset=UTF-8");

        Long parkNo = Long.valueOf(req.getParameter("parkNo"));
        try {
            ParkingHistoryDTO parkingHistoryDTO = parkingService.getParkingHistory(parkNo);
            PaymentInfoDTO dto = paymentInfoService.getInfoByEntryTime(parkingHistoryDTO.getEntryTime());

            resp.getWriter().write(
                    "{\"freeTime\": " + dto.getFreeTime() +
                            ", \"basicTime\": " + dto.getBasicTime() +
                            ", \"extraTime\": " + dto.getExtraTime() +
                            ", \"basicCharge\": " + dto.getBasicCharge() +
                            ", \"extraCharge\": " + dto.getExtraCharge() +
                            ", \"maxCharge\": " + dto.getMaxCharge() +
                            ", \"smallCarDiscount\": " + dto.getSmallCarDiscount() +
                            ", \"disabledDiscount\": " + dto.getDisabledDiscount() + "}"
            );
        } catch (Exception e) {
            log.error("getPaymentInfo 에러", e);
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            resp.getWriter().write("{\"error\": \"" + e.getMessage() + "\"}");
        }
    }
}

