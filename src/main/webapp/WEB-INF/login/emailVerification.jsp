<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<!DOCTYPE html>
<html lang="ko">
<%
    String email = request.getParameter("email");
%>
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>이메일 인증</title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.2.3/dist/css/bootstrap.min.css" rel="stylesheet"/>
    <script src="<%=request.getContextPath()%>/web/static/js/auth/f12Ban.js"></script>
    <script src="<%=request.getContextPath()%>/web/static/js/auth/loading.js"></script>
    <script src="<%=request.getContextPath()%>/web/static/js/auth/myPage.js"></script>
    <script src="<%=request.getContextPath()%>/web/static/js/auth/emailVerification.js"></script>
    <script>
        const CONTEXT_PATH = '<%= request.getContextPath() %>';
    </script>
    <style>
        body {
            padding: 20px;
            background-color: #f8f9fa;
        }
    </style>
</head>
<body>
<div class="container">
    <div class="card">
        <div class="card-header bg-primary text-white text-center">
            <h5 class="mb-0">이메일 인증</h5>
        </div>
        <div class="card-body">
            <p class="text-center mb-3" id="emailText">
                <strong><%=email%>
                </strong><br>
                으로 인증번호를 전송했습니다.
            </p>
            <div class="text-center mb-4">
                <span class="badge bg-info" id="timer">남은 시간: 4:00</span>
            </div>

            <form id="verifyForm" onsubmit="verifyOTP(event)">
                <div class="mb-3">
                    <label class="form-label">인증번호 (6자리)</label>
                    <input type="text"
                           class="form-control"
                           id="otpCode"
                           maxlength="6"
                           pattern="[0-9]{6}"
                           placeholder="123456"
                    <%--required--%>>
                </div>

                <div class="d-grid gap-2">
                    <button type="submit" class="btn btn-primary" id="verifyBtn">인증 확인</button>
                    <button type="button" class="btn btn-outline-secondary" id="rtOTP" onclick="returnOTP()">
                        인증번호 재전송
                    </button>
                </div>
            </form>
        </div>
    </div>
</div>
</body>
</html>
