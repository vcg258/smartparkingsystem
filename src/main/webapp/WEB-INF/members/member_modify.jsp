<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="com.example.parkingsystem.*" %>
<%@ page import="java.time.LocalDate" %>
<%@ page import="com.example.parkingsystem.dao.member.MembersDAO" %>
<%@ page import="com.example.parkingsystem.dto.member.MembersDTO" %>
<%@ page import="com.example.parkingsystem.vo.member.MembersVO" %>
<%@ page import="com.example.parkingsystem.service.member.MembersService" %>
<%
    MembersDTO membersDTO = MembersDTO.builder()
            .mno(Long.parseLong(request.getParameter("mno")))
            .carNum(request.getParameter("carNum"))
            .memberName(request.getParameter("memberName"))
            .memberPhone(request.getParameter("memberPhone"))
            .build();

    MembersService membersService = MembersService.INSTANCE;
    membersService.modifyMember(membersDTO);
%>