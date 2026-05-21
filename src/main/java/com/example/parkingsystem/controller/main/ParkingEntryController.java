package com.example.parkingsystem.controller.main;

import com.example.parkingsystem.dto.main.ParkingHistoryDTO;
import com.example.parkingsystem.dto.member.MembersDTO;
import com.example.parkingsystem.service.main.ParkingHistoryService;
import com.example.parkingsystem.service.member.MembersService;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.log4j.Log4j2;

import java.io.IOException;
import java.time.LocalDate;

@Log4j2
@WebServlet("/parking/entry")
public class ParkingEntryController extends HttpServlet {
    private final ParkingHistoryService parkingService = ParkingHistoryService.INSTANCE;
    private final MembersService memberService = MembersService.INSTANCE;

    // 차량 입차를 등록하고 주차카드에 필요한 응답 데이터를 반환
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        resp.setContentType("application/json;charset=UTF-8");

        String parkingArea = req.getParameter("parkingArea");
        String carNum = req.getParameter("carNum");
        String carType = req.getParameter("carType");

        try {
            ParkingHistoryDTO existing = parkingService.getRecentParking(carNum);
            if (existing != null) {
                resp.getWriter().write("{\"success\": false, \"message\": \"이미 주차 중인 차량입니다.\"}");
                return;
            }

            MembersDTO member = memberService.getMember(carNum);
            boolean isMember = member != null && !member.getEndDate().isBefore(LocalDate.now())
                    && !member.getStartDate().isAfter(LocalDate.now());

            ParkingHistoryDTO parkingHistoryDTO = ParkingHistoryDTO.builder()
                    .parkingArea(parkingArea)
                    .carNum(carNum)
                    .carType(carType)
                    .isMember(isMember)
                    .build();
            parkingService.registerEntry(parkingHistoryDTO);

            ParkingHistoryDTO saved = parkingService.getRecentParking(carNum);
            if (saved == null) {
                resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                resp.getWriter().write("{\"success\": false, \"message\": \"DB 조회 실패\"}");
                return;
            }

            String entryTimeStr = String.valueOf(saved.getEntryTime()).replace(" ", "T");
            resp.getWriter().write(
                    "{\"success\": true" +
                            ", \"entryTime\": \"" + entryTimeStr + "\"" +
                            ", \"parkNo\": " + saved.getParkNo() +
                            ", \"isMember\": " + isMember + "}"
            );
        } catch (Exception e) {
            log.error("입차 처리 중 에러", e);
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            resp.getWriter().write("{\"success\": false, \"message\": \"서버 내부 오류\"}");
        }
    }
}

