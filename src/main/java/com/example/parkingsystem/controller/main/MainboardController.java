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
import java.util.List;

@Log4j2
@WebServlet(name = "mainboardController", value = "/main")
public class MainboardController extends HttpServlet {
    private final ParkingHistoryService parkingHistoryService = ParkingHistoryService.INSTANCE;

    // 현황 조회
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
//        // 1. 로그인 정보 없을 시 접근 제한
//        HttpSession session = req.getSession();
//        String adminId = (String) session.getAttribute("adminId");
//
//        if (adminId == null || adminId.trim().isEmpty()) {
//            resp.sendRedirect("/login");
//            return;
//        }

        // 2. 로그인이 완료된 경우
        log.info("/main get...");

        // 1) DB에서 현재 주차 중인 차량 목록 조회
        List<ParkingHistoryDTO> occupiedList = parkingHistoryService.getOccupied();

        for (ParkingHistoryDTO dto: occupiedList) {
            log.info("carNum: {}, isMember: {}", dto.getCarNum(), dto.isMember());
        }

        // 2) 조회 결과값을 main.jsp로 전달
        req.setAttribute("occupiedList", occupiedList);

        // 3) main.jsp로 이동
        req.getRequestDispatcher("/WEB-INF/main/main.jsp").forward(req, resp);
    }
}