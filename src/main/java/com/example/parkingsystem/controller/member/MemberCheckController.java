package com.example.parkingsystem.controller.member;

import com.example.parkingsystem.dto.member.MembersDTO;
import com.example.parkingsystem.service.member.MembersService;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import lombok.extern.log4j.Log4j2;

import java.io.IOException;

@Log4j2
@WebServlet("/member_check")
public class MemberCheckController extends HttpServlet {
    private final MembersService membersService = MembersService.INSTANCE;

    // 차량번호로 기존 회원 여부를 확인하고 목록 화면으로 이동
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String carNum = req.getParameter("carNum");
        log.info("회원 존재 여부 조회 요청 carNum={}", carNum);

        MembersDTO membersDTO = membersService.getMemberOne(carNum);

        HttpSession session = req.getSession();
        if (membersDTO != null) {
            log.info("기존 회원 조회 성공 carNum={}", carNum);
            session.setAttribute("checkResult", "found");
            session.setAttribute("memberDTO", membersDTO);
        } else {
            log.info("기존 회원 없음 carNum={}", carNum);
            session.setAttribute("checkResult", "notFound");
            session.setAttribute("searchCarNum", carNum);
        }

        resp.sendRedirect(req.getContextPath() + "/member_list?pageNum=1");
    }
}

