<%@ page import="com.example.parkingsystem.dto.setting.PaymentInfoDTO" %>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%
    PaymentInfoDTO paymentInfoDTO = (PaymentInfoDTO) request.getAttribute("paymentInfoDTO");
%>
<!DOCTYPE html>
<html lang="ko">
<head>
    <meta charset="UTF-8">
    <link rel="stylesheet" as="style" crossorigin
          href="https://cdn.jsdelivr.net/gh/orioncactus/pretendard@v1.3.9/dist/web/static/pretendard.css"/>
    <title>스마트 주차 관리 시스템 - 설정</title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.2/dist/css/bootstrap.min.css" rel="stylesheet">
    <link rel="stylesheet" href="<%=request.getContextPath()%>/web/static/css/setting.css">
    <script src="<%=request.getContextPath()%>/web/static/js/setting/setting.js"></script>
</head>
<body>
<%@include file="/WEB-INF/common/header_main.jsp"%>
<div class="container" id="container">
    <form name="setting" action="/setting" method="post" class="setup-area" onsubmit="return prepareSubmit()">
        <div class="title-bar">
            설정 관리 - 요금 및 할인 정책
            <button class="save-btn" type="button" onclick="clickSubmit()">저장하기</button>
        </div>

        <div class="card">
            <span class="card-label">기본 요금 및 일일 최대 요금 설정</span>
            <div class="input-row">
                <div class="input-item">
                    <label>기본 주차 요금(원)</label>
                    <input type="text" id="in-base-fee" value="<%=paymentInfoDTO == null ? 0 : paymentInfoDTO.getBasicCharge()%>"
                           name="basicCharge">
                </div>
                <div class="input-item">
                    <label>기본 주차 시간(분)</label>
                    <input type="text" id="in-base-time" value="<%=paymentInfoDTO == null ? 0 : paymentInfoDTO.getBasicTime()%>" name="basicTime">
                </div>
                <div class="input-item">
                    <label>일일 최대 요금(원)</label>
                    <input type="text" id="in-day-max-fee" value="<%=paymentInfoDTO == null ? 0 : paymentInfoDTO.getMaxCharge()%>"
                           name="maxCharge">
                </div>
            </div>
        </div>

        <div class="card">
            <span class="card-label"> 무료 회차 시간 및 추가 요금 설정</span>
            <div class="input-row">
                <div class="input-item">
                    <label>무료 회차 시간(분)</label>
                    <input type="text" id="in-free-time" value="<%=paymentInfoDTO == null ? 0 : paymentInfoDTO.getFreeTime()%>" name="freeTime">
                </div>
                <div class="input-item">
                    <label>추가 요금(원)</label>
                    <input type="text" id="in-add-fee" value="<%=paymentInfoDTO == null ? 0 : paymentInfoDTO.getExtraCharge()%>"
                           name="extraCharge">
                </div>
                <div class="input-item">
                    <label>추가 요금 기준 시간(분)</label>
                    <input type="text" id="in-add-time" value="<%=paymentInfoDTO == null ? 0 : paymentInfoDTO.getExtraTime()%>" name="extraTime">
                </div>
            </div>
        </div>

        <div class="card">
            <span class="card-label">할인율 및 월 주차 요금 설정</span>
            <div class="input-row">
                <div class="input-item">
                    <label>경차 할인율</label>
                    <input type="text" id="in-light-dis" value="<%=paymentInfoDTO == null ? 0 : paymentInfoDTO.getSmallCarDiscount()%>"
                           name="smallCarDiscount">
                </div>
                <div class="input-item">
                    <label>장애인 할인율</label>
                    <input type="text" id="in-disabled-dis" value="<%=paymentInfoDTO == null ? 0 : paymentInfoDTO.getDisabledDiscount()%>"
                           name="disabledDiscount">
                </div>
                <div class="input-item">
                    <label>월 정액권(원)</label>
                    <input type="text" id="in-monthly-fee" value="<%=paymentInfoDTO == null ? 0 : paymentInfoDTO.getMemberCharge()%>"
                           name="memberCharge">
                </div>
            </div>
        </div>
    </form>
</div>
<footer>
    <%@ include file="/WEB-INF/common/footer.jsp" %>
</footer>
</body>
</html>