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
@WebServlet("/member_modify")
public class MemberModifyController extends HttpServlet {
    private final MembersService membersService = MembersService.INSTANCE;

    // 회원 기본 정보 수정 후 목록 화면으로 복귀
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        Long mno = Long.parseLong(req.getParameter("mno"));
        String carNum = req.getParameter("carNum");
        String memberName = req.getParameter("memberName");
        String memberPhone = req.getParameter("memberPhone");
        log.info("회원 정보 수정 요청 mno={}, carNum={}", mno, carNum);

        MembersDTO membersDTO = MembersDTO.builder()
                .mno(mno)
                .carNum(carNum)
                .memberName(memberName)
                .memberPhone(memberPhone)
                .build();

        membersService.modifyMember(membersDTO);
        log.info("회원 정보 수정 완료 mno={}", mno);

        HttpSession session = req.getSession();
        session.setAttribute("flashMsg", "회원 정보가 수정되었습니다.");
        resp.sendRedirect(req.getContextPath() + "/member_list");
    }
}

