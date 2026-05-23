<%@ page language="java" contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<!DOCTYPE html>
<html lang="ko">
<head>
    <meta charset="UTF-8">
    <meta http-equiv="X-UA-Compatible" content="IE=edge"/>
    <meta name="viewport" content="width=1280">
    <title>비밀번호 재설정</title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.2.3/dist/css/bootstrap.min.css" rel="stylesheet"/>
    <script src="https://use.fontawesome.com/releases/v6.3.0/js/all.js" crossorigin="anonymous"></script>
    <link href="${pageContext.request.contextPath}/web/static/css/styles.css" rel="stylesheet"/>
    <link rel="stylesheet" href="<%=request.getContextPath()%>/web/static/css/login.css">
    <script src="${pageContext.request.contextPath}/web/static/js/auth/f12Ban.js"></script>
    <script src="${pageContext.request.contextPath}/web/static/js/auth/loading.js"></script>
    <script src="${pageContext.request.contextPath}/web/static/js/auth/password.js"></script>
    <script>
        const CONTEXT_PATH = '${pageContext.request.contextPath}';
    </script>
</head>
<link rel="icon" href="data:,">

<body class="bg-light">

<div id="layoutAuthentication">
    <div id="layoutAuthentication_content">
        <main class="d-flex justify-content-center align-items-center vh-100">
            <div class="container">
                <div class="row justify-content-center">
                    <div class="col-xl-4 col-lg-5 col-md-7">
                        <div class="card shadow-lg border-0 rounded-lg mt-5">

                            <!-- Header -->
                            <div class="card-header text-center py-4">
                                <h4 class="mb-1">비밀번호 재설정</h4>
                            </div>

                            <!-- Body -->
                            <div class="card-body px-4">

                                <!-- Step 1: 아이디 입력 -->
                                <div id="step1">
                                    <div class="text-center mb-3">
                                        <p class="small text-muted">
                                            비밀번호를 재설정할 관리자 아이디를 입력해주세요.
                                        </p>
                                    </div>
                                    <form id="adminIdForm" onsubmit="submitStep1(event); return false;">
                                        <div class="form-floating mb-3">
                                            <input class="form-control"
                                                   type="text"
                                                   id="inputAdminId"
                                                   placeholder="admin"
                                            <%--required--%>>
                                            <label>관리자 아이디</label>
                                        </div>

                                        <div class="d-flex justify-content-between align-items-center">
                                            <a class="small" href="${pageContext.request.contextPath}/login">로그인 화면</a>
                                            <button type="submit" class="btn btn-primary px-4">
                                                다음
                                            </button>
                                        </div>
                                    </form>
                                </div>

                                <!-- Step 2: 이메일 입력 및 인증번호 요청 -->
                                <div id="step2" class="d-none">
                                    <div class="text-center mb-3">
                                        <p class="small text-muted">
                                            등록된 이메일 주소를 입력하면 인증번호를 보내드립니다.
                                        </p>
                                    </div>
                                    <form id="emailForm" onsubmit="submitStep2(event); return false;">
                                        <div class="input-group mb-3">
                                            <input class="form-control"
                                                   type="email"
                                                   name="email"
                                                   id="inputEmail"
                                                   placeholder="이메일"
                                            <%--required--%>>
                                            <button type="submit" class="btn btn-primary px-3">
                                                인증하기
                                            </button>
                                        </div>

                                        <div class="d-flex gap-2">
                                            <button type="button"
                                                    class="btn btn-secondary flex-fill"
                                                    onclick="goBackToStep1()">
                                                이전
                                            </button>
                                        </div>
                                    </form>
                                </div>

                                <!-- Step 3: 인증번호 입력 -->
                                <div id="step3" class="d-none">
                                    <div class="text-center mb-3">
                                        <i class="fas fa-envelope fa-3x text-primary mb-2"></i>
                                        <p class="small text-muted" id="emailText">
                                        </p>
                                    </div>

                                    <form id="verificationForm" onsubmit="submitStep3(event); return false;">

                                        <%-- 타이머 추가 --%>
                                        <div class="text-center mb-4">
                                            <span class="badge bg-info" id="timer">남은 시간: 4:00</span>
                                        </div>

                                        <div class="form-floating mb-3">
                                            <input class="form-control"
                                                   type="text"
                                                   id="inputVerificationCode"
                                                   maxlength="6"
                                                   pattern="[0-9]{6}"
                                                   placeholder="123456"
                                            <%--required--%>>
                                            <label>인증번호 (6자리)</label>
                                        </div>

                                        <div class="d-flex gap-2">
                                            <button type="button"
                                                    class="btn btn-secondary flex-fill"
                                                    onclick="goBackToStep2()">
                                                이전
                                            </button>
                                            <button type="submit" class="btn btn-primary flex-fill" id="goStep">
                                                다음
                                            </button>
                                        </div>
                                    </form>
                                </div>

                                <!-- Clear Step: 완료 -->
                                <div id="clearStep" class="d-none">
                                    <div class="text-center mb-3">
                                        <i class="fas fa-check-circle fa-3x text-success mb-3"></i>
                                        <h5 class="text-success mb-2">비밀번호가 재설정되었습니다!</h5>
                                        <p class="small text-muted">
                                            무작위로 생성된 비밀번호를 등록된 이메일로 발송했습니다.<br>
                                            이메일을 확인하신 후 로그인해주세요.
                                        </p>
                                    </div>

                                    <div class="d-grid">
                                        <a href="${pageContext.request.contextPath}/login" class="btn btn-primary">
                                            로그인 화면으로
                                        </a>
                                    </div>
                                </div>

                            </div>

                            <!-- Footer -->
                            <div class="card-footer text-center py-3 small text-muted">
                                🔒 보안을 위해 단계별 인증이 활성화되어 있습니다
                            </div>

                            <!-- 데모 관리자 계정 (실제 사용시 주석) -->
                            <div class="alert alert-warning">
                                <strong>주의!</strong><br>
                                데모 계정은 실제 메일 발송 대신<br>
                                서버 콘솔에 임시 비밀번호가 출력됩니다.
                            </div>
                            <div class="alert alert-info mt-3 mb-0">
                                <strong>포트폴리오 데모 계정</strong><br>
                                아이디: <code>demo</code> / 비밀번호: <code>demo1234</code><br>
                                이메일: <code>demo@example.com</code><br>
                                OTP: <code>123456</code>
                            </div>

                        </div>
                    </div>
                </div>
            </div>
        </main>
    </div>
</div>

<script src="https://cdn.jsdelivr.net/npm/bootstrap@5.2.3/dist/js/bootstrap.bundle.min.js"></script>
</body>
</html>
