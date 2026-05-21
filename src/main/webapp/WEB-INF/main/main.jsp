<%@ page import="com.example.parkingsystem.dto.main.ParkingHistoryDTO" %>
<%@ page import="java.util.List" %>
<%@ page import="com.example.parkingsystem.service.main.ParkingHistoryService" %>
<%@ page import="java.util.Map" %>
<%@ page import="java.util.HashMap" %>
<%@ page import="com.example.parkingsystem.service.member.MembersService" %>
<%@ page import="com.example.parkingsystem.dao.member.MembersDAO" %>
<%@ page import="com.example.parkingsystem.dto.member.MembersDTO" %>
<%@ page import="com.example.parkingsystem.service.setting.PaymentInfoService" %>
<%@ page import="com.example.parkingsystem.dto.setting.PaymentInfoDTO" %>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%
    // parking_history 불러오기
    ParkingHistoryService parkingHistoryService = ParkingHistoryService.INSTANCE;
    List<ParkingHistoryDTO> occupiedList = (List<ParkingHistoryDTO>) request.getAttribute("occupiedList");

    Map<String, ParkingHistoryDTO> occupiedMap = new HashMap<>();
    if (occupiedList != null) {
        for (ParkingHistoryDTO dto : occupiedList) {
            occupiedMap.put(dto.getParkingArea(), dto);
        }
    }

    // 입차 시간 기준 요금 정책 불러오기
    PaymentInfoDTO paymentInfo = (PaymentInfoDTO) request.getAttribute("paymentInfoDTO");

    // 회원 목록 불러오기
//    List<MembersDTO> members = request.getAttribute("")
%>
<!DOCTYPE html>
<html lang="ko">
<head>
    <%-- 아이콘 --%>
    <link rel="icon" href="data:,">
    <%--    <%@include file="/web/main/main_process.jsp"%>--%>

    <meta charset="UTF-8">
    <link rel="stylesheet" as="style" crossorigin
          href="https://cdn.jsdelivr.net/gh/orioncactus/pretendard@v1.3.9/dist/web/static/pretendard.css"/>
    <title>반월당 스마트 주차 관리 시스템</title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.2/dist/css/bootstrap.min.css" rel="stylesheet">

    <!-- 메인보드 css -->
    <link rel="stylesheet" href="${pageContext.request.contextPath}/web/static/css/mainboard.css">

    <!-- 영수증 css -->
    <link rel="stylesheet" href="${pageContext.request.contextPath}/web/static/css/receipt.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/web/static/css/mainModal.css">
</head>
<body>
<!-- 공통 header 구역 -->
<%@include file="/WEB-INF/common/header_main.jsp" %>

<!-- 메인 페이지 -->
<div class="container-fluid mt-4">
    <!-- 주차 현황 요약 -->
    <div class="section-header d-flex justify-content-between align-items-end">
        <div>
            <h3 class="section-title">주차 현황</h3>
            <p class="mb-0">현재 <b>6대</b>의 차량이 주차되어 있습니다.</p>
        </div>

        <!-- 차량 검색 창 -->
        <div class="d-flex" style="gap: 10px; margin-bottom: 5px;">
            <input type="text" class="form-control" id="inputCarNum"
                   placeholder="검색할 차량번호 4자리를 입력하세요."
                   style="width: 450px; height: 38px;">
            <button class="btn btn-secondary px-4 text-nowrap" id="btnCarSearch" style="height: 38px;">검색</button>
        </div>
    </div>

    <!-- 주차 구역 메인 보드 -->
    <div id="parking-board">
        <div class="layout-wrapper">
            <!-- 좌측 주차 구역 A1 ~ A5 (세로 정렬) -->
            <div class="column">
                <% for (int i = 1; i <= 5; i++) {
                    String currentId = "A-" + i;
                    ParkingHistoryDTO occupiedInfo = occupiedMap.get(currentId);
                    request.setAttribute("id", currentId);

                    if (occupiedInfo != null) {
                        request.setAttribute("status", "occupied");
                        request.setAttribute("car", occupiedInfo.getCarNum());
                        request.setAttribute("time", occupiedInfo.getEntryTime());
                        request.setAttribute("type", occupiedInfo.getCarType());
                        request.setAttribute("parkNo", occupiedInfo.getParkNo());
                        request.setAttribute("isMember", occupiedInfo.isMember());
                    } else {
                        request.setAttribute("status", "available");
                        request.setAttribute("car", null);
                        request.setAttribute("time", null);
                        request.setAttribute("type", null);
                        request.setAttribute("parkNo", null);
                        request.setAttribute("isMember", null);
                    }
                %>
                <%@ include file="/WEB-INF/main/parking_card.jsp" %>
                <% } %>
            </div>

            <!-- 중앙 주차 구역 -->
            <div class="center-wrapper">
                <div class="central-column">
                    <!-- 중앙 상단 주차 구역 A6 ~ A10 -->
                    <div class="center-row">
                        <% for (int i = 6; i <= 10; i++) {
                            String currentId = "A-" + i;
                            ParkingHistoryDTO occupiedInfo = occupiedMap.get(currentId);
                            request.setAttribute("id", currentId);

                            if (occupiedInfo != null) {
                                request.setAttribute("status", "occupied");
                                request.setAttribute("car", occupiedInfo.getCarNum());
                                request.setAttribute("time", occupiedInfo.getEntryTime());
                                request.setAttribute("type", occupiedInfo.getCarType());
                                request.setAttribute("parkNo", occupiedInfo.getParkNo());
                                request.setAttribute("isMember", occupiedInfo.isMember());
                            } else {
                                request.setAttribute("status", "available");
                                request.setAttribute("car", null);
                                request.setAttribute("time", null);
                                request.setAttribute("type", null);
                                request.setAttribute("parkNo", null);
                                request.setAttribute("isMember", null);
                            }
                        %>
                        <%@ include file="/WEB-INF/main/parking_card.jsp" %>
                        <% } %>
                    </div>
                    <!-- 중앙 주차 하단 구역 A11 ~ A15 -->
                    <div class="center-row">
                        <% for (int i = 11; i <= 15; i++) {
                            String currentId = "A-" + i;
                            ParkingHistoryDTO occupiedInfo = occupiedMap.get(currentId);
                            request.setAttribute("id", currentId);

                            if (occupiedInfo != null) {
                                request.setAttribute("status", "occupied");
                                request.setAttribute("car", occupiedInfo.getCarNum());
                                request.setAttribute("time", occupiedInfo.getEntryTime());
                                request.setAttribute("type", occupiedInfo.getCarType());
                                request.setAttribute("parkNo", occupiedInfo.getParkNo());
                                request.setAttribute("isMember", occupiedInfo.isMember());
                            } else {
                                request.setAttribute("status", "available");
                                request.setAttribute("car", null);
                                request.setAttribute("time", null);
                                request.setAttribute("type", null);
                                request.setAttribute("parkNo", null);
                                request.setAttribute("isMember", null);
                            }
                        %>
                        <%@ include file="/WEB-INF/main/parking_card.jsp" %>
                        <% } %>
                    </div>
                </div>
            </div>

            <!-- 우측 주차 구역 A16 ~ A20 -->
            <div class="column">
                <% for (int i = 16; i <= 20; i++) {
                    String currentId = "A-" + i;
                    ParkingHistoryDTO occupiedInfo = occupiedMap.get(currentId);
                    request.setAttribute("id", currentId);

                    if (occupiedInfo != null) {
                        request.setAttribute("status", "occupied");
                        request.setAttribute("car", occupiedInfo.getCarNum());
                        request.setAttribute("time", occupiedInfo.getEntryTime());
                        request.setAttribute("type", occupiedInfo.getCarType());
                        request.setAttribute("parkNo", occupiedInfo.getParkNo());
                        request.setAttribute("isMember", occupiedInfo.isMember());
                    } else {
                        request.setAttribute("status", "available");
                        request.setAttribute("car", null);
                        request.setAttribute("time", null);
                        request.setAttribute("type", null);
                        request.setAttribute("parkNo", null);
                        request.setAttribute("isMember", null);
                    }
                %>
                <%@ include file="/WEB-INF/main/parking_card.jsp" %>
                <% } %>
            </div>
        </div>
    </div>
</div>

<!-- 주차 상태 처리 모달 -->
<%@ include file="/WEB-INF/main/parking_modal.jsp" %>
<%--<%@include file="/web/main/@membershipPayModal.jsp"%>--%>

<!-- 공통 footer 구역 -->
<footer>
    <%@ include file="/WEB-INF/common/footer.jsp" %>
</footer>

<!-- bootstrap JS (모달 동작용) -->
<script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.2/dist/js/bootstrap.bundle.min.js"></script>

<!-- Axios 라이브러리 -->
<script src="https://cdn.jsdelivr.net/npm/axios/dist/axios.min.js"></script>

<!-- 함수(날짜 형식, 주차 현황 숫자) 로직 JS -->
<script src="${pageContext.request.contextPath}/web/static/js/main/main_function.js"></script>

<!-- 검색 모달 JS -->
<script src="${pageContext.request.contextPath}/web/static/js/main/main_search.js"></script>

<!-- 요금 계산 로직 JS -->
<script>
    const policy = {
        freeTime: <%=paymentInfo != null ? paymentInfo.getFreeTime() : 0%>,
        basicTime: <%=paymentInfo != null ? paymentInfo.getBasicTime() : 0%>,
        extraTime: <%=paymentInfo != null ? paymentInfo.getExtraTime() : 0%>,
        basicCharge: <%=paymentInfo != null ? paymentInfo.getBasicCharge() : 0%>,
        extraCharge: <%=paymentInfo != null ? paymentInfo.getExtraCharge() : 0%>,
        smallCarDiscount: <%=paymentInfo != null ? paymentInfo.getSmallCarDiscount() : 0%>,
        disabledDiscount: <%=paymentInfo != null ? paymentInfo.getDisabledDiscount() : 0%>,
        maxCharge: <%=paymentInfo != null ? paymentInfo.getMaxCharge() : 0%>
    }
</script>
<script src="${pageContext.request.contextPath}/web/static/js/main/main_parking_charge_logic.js"></script>

<!-- 메인 모달 JS -->
<script src="${pageContext.request.contextPath}/web/static/js/main/main_modal.js"></script>

<!-- 회원권 결제 모달 JS -->
<%--<script src="${pageContext.request.contextPath}/web/main/@@main_membershipPay.js"></script>--%>
</body>
</html>