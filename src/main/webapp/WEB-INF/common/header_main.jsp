<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<!-- Bootstrap JS (햄버거 메뉴 동작에 필요) -->
<script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.2/dist/js/bootstrap.bundle.min.js"></script>
<nav class="navbar navbar-expand-lg navbar-dark bg-dark py-3" style="font-family: 'Pretendard', sans-serif;">
    <div class="container-fluid">
        <a class="navbar-brand" href="${pageContext.request.contextPath}/main"><b>스마트 주차 관리 시스템</b></a>

        <!-- 모바일 햄버거 버튼 -->
        <button class="navbar-toggler" type="button" data-bs-toggle="collapse" data-bs-target="#mainNavbar"
                aria-controls="mainNavbar" aria-expanded="false" aria-label="Toggle navigation">
            <span class="navbar-toggler-icon"></span>
        </button>

        <div class="collapse navbar-collapse" id="mainNavbar">
            <!-- 왼쪽 메뉴 -->
            <ul class="navbar-nav me-auto mb-2 mb-lg-0">
                <li class="nav-item"><a class="nav-link" href="${pageContext.request.contextPath}/main">대시보드</a></li>
                <li class="nav-item"><a class="nav-link" href="${pageContext.request.contextPath}/member_list">회원 관리</a></li>
                <li class="nav-item"><a class="nav-link" href="${pageContext.request.contextPath}/setting">설정 관리</a></li>
                <li class="nav-item"><a class="nav-link" href="${pageContext.request.contextPath}/statistic">통계</a></li>
            </ul>

            <!-- 오른쪽 메뉴 (collapse 안에 포함) -->
            <ul class="navbar-nav mb-2 mb-lg-0">
                <li class="nav-item">
                    <a href="${pageContext.request.contextPath}/main/mypage" class="nav-link text-white">MyPage</a>
                </li>
                <li class="nav-item">
                    <a href="${pageContext.request.contextPath}/logout" class="nav-link text-white" onclick="return logout()">로그아웃</a>
                </li>
            </ul>
        </div>
    </div>
</nav>

<script>
    const CONTEXT_PATH = '${pageContext.request.contextPath}';

    document.addEventListener("DOMContentLoaded", function () {
        const currentPath = window.location.pathname;
        const navLinks = document.querySelectorAll('.navbar-nav .nav-link');

        navLinks.forEach(link => {
            if (currentPath.includes(link.getAttribute('href').split('/').pop())) {
                navLinks.forEach(item => item.classList.remove('active'));
                link.classList.add('active');
            }
        });
    });

    function logout() {
        return confirm('로그아웃 하시겠습니까?')
    }
</script>
