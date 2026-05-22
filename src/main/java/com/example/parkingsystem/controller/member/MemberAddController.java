package com.example.parkingsystem.controller.member;

import com.example.parkingsystem.dto.member.MembersDTO;
import com.example.parkingsystem.service.main.ParkingHistoryService;
import com.example.parkingsystem.service.member.MembersService;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import lombok.extern.log4j.Log4j2;

import java.io.IOException;
import java.time.LocalDate;

@Log4j2
@WebServlet("/member_add")
public class MemberAddController extends HttpServlet {
    private final MembersService membersService = MembersService.INSTANCE;
    private final ParkingHistoryService parkingService = ParkingHistoryService.INSTANCE;

    // 신규 회원 등록 또는 기존 회원권 연장 처리
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String carNum = req.getParameter("carNum");
        String memberName = req.getParameter("memberName");
        String memberPhone = req.getParameter("memberPhone");
        LocalDate startDate = LocalDate.parse(req.getParameter("startDate"));
        LocalDate endDate = LocalDate.parse(req.getParameter("endDate"));
        log.info("회원 등록 요청 carNum={}, startDate={}, endDate={}", carNum, startDate, endDate);

        MembersDTO membersDTO = MembersDTO.builder()
                .carNum(carNum)
                .memberName(memberName)
                .memberPhone(memberPhone)
                .startDate(startDate)
                .endDate(endDate)
                .build();

        membersService.addMember(membersDTO);
        parkingService.changeIsMemberState(carNum);
        log.info("회원 등록 완료 carNum={}", carNum);

        HttpSession session = req.getSession();
        session.removeAttribute("searchCarNum");

        String message = "true".equals(req.getParameter("isExistingMember"))
                ? "회원권이 연장되었습니다."
                : "회원이 등록되었습니다.";
        session.setAttribute("flashMsg", message);

        resp.sendRedirect(req.getContextPath() + "/member_list");
    }
}

