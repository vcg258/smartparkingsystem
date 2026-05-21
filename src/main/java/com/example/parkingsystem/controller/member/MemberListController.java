package com.example.parkingsystem.controller.member;

import com.example.parkingsystem.dao.setting.PaymentInfoDAO;
import com.example.parkingsystem.dto.member.PageRequestDTO;
import com.example.parkingsystem.dto.member.PageResponseDTO;
import com.example.parkingsystem.service.member.MembersService;
import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.log4j.Log4j2;

import java.io.IOException;

@Log4j2
@WebServlet("/member_list")
public class MemberListController extends HttpServlet {
    private final MembersService membersService = MembersService.INSTANCE;
    private final PaymentInfoDAO paymentInfoDAO = new PaymentInfoDAO();

    // 회원 목록 화면과 검색 조건을 조회해서 JSP에 전달
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        handleList(req, resp);
    }

    // 목록 조회는 GET/POST 모두 동일하게 처리
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        handleList(req, resp);
    }

    private void handleList(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String pageNumStr = req.getParameter("pageNum");
        if (pageNumStr == null) {
            redirectFirstPage(req, resp);
            return;
        }

        String searchType = req.getParameter("searchType");
        String keyword = req.getParameter("keyword");
        String status = req.getParameter("status");
        int pageNum = Integer.parseInt(pageNumStr);

        PageRequestDTO pageRequestDTO = PageRequestDTO.builder()
                .searchType(searchType)
                .keyword(keyword)
                .status(status)
                .pageNum(pageNum)
                .build();

        PageResponseDTO pageResponseDTO = membersService.getMemberList(pageRequestDTO);
        req.setAttribute("pageResponseDTO", pageResponseDTO);

        req.setAttribute("openNewMemberModal", req.getParameter("openNewMemberModal"));
        req.setAttribute("prefillCarNum", req.getParameter("carNum"));

        var paymentInfo = paymentInfoDAO.selectInfo();
        if (paymentInfo != null) {
            req.setAttribute("memberCharge", paymentInfo.getMemberCharge());
        } else {
            req.setAttribute("memberCharge", 0);
        }

        RequestDispatcher requestDispatcher = req.getRequestDispatcher("/WEB-INF/members/member_list.jsp");
        requestDispatcher.forward(req, resp);
    }

    // pageNum이 없는 목록 요청은 1페이지 기준 URL로 정규화
    private void redirectFirstPage(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String status = req.getParameter("status");
        String searchType = req.getParameter("searchType");
        String keyword = req.getParameter("keyword");
        String from = req.getParameter("from");
        String carNum = req.getParameter("carNum");

        StringBuilder redirectURL = new StringBuilder("/member_list?pageNum=1");
        if (status != null) redirectURL.append("&status=").append(status);
        if (searchType != null) redirectURL.append("&searchType=").append(searchType);
        if (keyword != null) redirectURL.append("&keyword=").append(keyword);
        if (from != null) redirectURL.append("&from=").append(from);
        if (carNum != null) redirectURL.append("&carNum=").append(carNum);

        resp.sendRedirect(req.getContextPath() + redirectURL);
    }
}

