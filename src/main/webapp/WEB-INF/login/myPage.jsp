<%@ page import="com.example.parkingsystem.service.auth.AdminService" %>
<%@ page import="java.time.format.DateTimeFormatter" %>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<!DOCTYPE html>
<html lang="ko">
<%
    AdminService adminService = AdminService.INSTANCE;
    String adminId = (String) session.getAttribute("adminId");
    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

%>
<head>
    <meta charset="UTF-8">
    <meta http-equiv="X-UA-Compatible" content="IE=edge"/>
    <meta name="viewport" content="width=device-width, initial-scale=1, shrink-to-fit=no"/>
    <title>반월당 스마트 주차 관리 시스템 - 마이페이지</title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.2.3/dist/css/bootstrap.min.css" rel="stylesheet"/>
    <link href="${pageContext.request.contextPath}/web/static/css/styles.css" rel="stylesheet"/>
    <script src="https://use.fontawesome.com/releases/v6.3.0/js/all.js" crossorigin="anonymous"></script>
    <script src="${pageContext.request.contextPath}/web/static/js/auth/f12Ban.js"></script>
    <script src="${pageContext.request.contextPath}/web/static/js/auth/loading.js"></script>
    <script src="${pageContext.request.contextPath}/web/static/js/auth/myPage.js"></script>
    <script>
        const CONTEXT_PATH = '${pageContext.request.contextPath}';
    </script>

    <style>
        #margin {
            margin-top: 30px;
        }

        .row {
            margin-top: 30px;
        }
        h4 {
            margin-top: 25px;
        }
    </style>
</head>
<link rel="icon" href="data:,">
<body>
<%@include file="/WEB-INF/common/header_main.jsp"%>
<div class="container-fluid px-4">
    <h4><b>마이페이지</b></h4>
    <%
        if (adminService.getAdminById(adminId).isPasswordReset()) {
    %>
    <div class="alert alert-warning" id="margin">
        🔐 최초 로그인입니다. 보안을 위해 비밀번호를 변경해주세요.
    </div>
    <%
        }
    %>
    <div class="row">
        <!-- 기본 정보 -->
        <div class="col-lg-6">
            <div class="card mb-4">
                <div class="card-header">
                    <i class="fas fa-user me-1"></i>
                    기본 정보
                </div>
                <form class="card-body" onsubmit="submitUpdateEmail(event); return false;">
                    <div>
                        <div class="mb-3">
                            <label class="form-label">아이디</label>
                            <input type="text" class="form-control" value="<%=adminId%>" disabled>
                        </div>
                        <div class="mb-3">
                            <label class="form-label">이름</label>
                            <input type="text" class="form-control"
                                   value="<%=adminService.getAdminById(adminId).getAdminName()%>" disabled>
                        </div>
                        <div class="mb-3">
                            <label class="form-label">이메일</label>
                            <div class="input-group">
                            <input type="email" class="form-control" id="email"
                                   value="<%=adminService.getAdminById(adminId).getAdminEmail()%>">
                            <button type="button" class="btn btn-outline-primary" id="emailCertified" onclick="openEmailVerification()"> 인증하기 </button>
                            </div>
                        </div>
                        <button type="submit" id="updateEmail" class="btn btn-primary" disabled>정보 수정</button>
                    </div>
                </form>
            </div>
        </div>

        <!-- 비밀번호 변경 -->
        <form class="col-lg-6" onsubmit="updatePassword(event);return false;">
            <div>
                <div class="card mb-4 border-warning">
                    <div class="card-header bg-warning text-dark">
                        <i class="fas fa-key me-1"></i>
                        비밀번호 변경
                    </div>
                    <div class="card-body">
                        <div class="mb-3">
                            <label class="form-label">현재 비밀번호</label>
                            <input type="password" class="form-control" id="password">
                        </div>
                        <div class="mb-3">
                            <label class="form-label">새 비밀번호</label>
                            <input type="password" class="form-control" id="newPassword">
                        </div>
                        <div class="mb-3">
                            <label class="form-label">새 비밀번호 확인</label>
                            <input type="password" class="form-control" id="newPasswordCheck">
                        </div>
                        <button type="submit" class="btn btn-warning w-100">비밀번호 변경</button>
                    </div>
                </div>
            </div>
        </form>
    </div>

    <!-- 로그인 정보 -->
    <div class="card mb-4">
        <div class="card-header">
            <i class="fas fa-clock me-1"></i>
            로그인 정보
        </div>
        <div class="card-body">
            <table class="table table-bordered">
                <tr>
                    <th style="width:30%">최근 로그인</th>
                    <td><%=adminService.getAdminById(adminId).getLastLogin().format(formatter)%>
                    </td>
                </tr>
                <tr>
                    <th>최근 로그인 IP</th>
                    <td><%=adminService.getAdminById(adminId).getLastLoginIp()%>
                    </td>
                </tr>
            </table>
        </div>
    </div>
</div>
<footer>
    <%@ include file="/WEB-INF/common/footer.jsp" %>
</footer>
</body>
</html>
