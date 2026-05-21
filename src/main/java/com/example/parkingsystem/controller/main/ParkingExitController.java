package com.example.parkingsystem.controller.main;

import com.example.parkingsystem.dto.main.ParkingHistoryDTO;
import com.example.parkingsystem.service.main.ParkingHistoryService;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.log4j.Log4j2;

import java.io.IOException;

@Log4j2
@WebServlet("/parking/exit")
public class ParkingExitController extends HttpServlet {
    private final ParkingHistoryService parkingService = ParkingHistoryService.INSTANCE;

    // 결제 완료된 차량의 출차 시점을 확정
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        resp.setContentType("application/json;charset=UTF-8");

        long parkNo = Long.parseLong(req.getParameter("parkNo"));
        log.info("출차 확정 요청 parkNo={}", parkNo);

        ParkingHistoryDTO parkingHistoryDTO = ParkingHistoryDTO.builder().parkNo(parkNo).build();
        parkingService.registerExit(parkingHistoryDTO);
        log.info("출차 확정 완료 parkNo={}", parkNo);

        resp.getWriter().write("{\"success\": true}");
    }
}

