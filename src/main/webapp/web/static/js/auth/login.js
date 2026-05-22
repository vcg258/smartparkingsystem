// Step 2로 이동
function goStep2() {
    console.log("Step 2로 이동");
    document.getElementById("step1").classList.add("d-none");
    document.getElementById("step2").classList.remove("d-none");
    document.getElementById("step1Circle").classList.remove("active");
    document.getElementById("step2Circle").classList.add("active");
    document.getElementById("email").focus();
}

// Step 3로 이동
function goStep3() {
    console.log("Step 3로 이동");
    document.getElementById("step2").classList.add("d-none");
    document.getElementById("step3").classList.remove("d-none");
    document.getElementById("step2Circle").classList.remove("active");
    document.getElementById("step3Circle").classList.add("active");
    document.getElementById("otpCode").focus();
}

// Step 1로 돌아가기
function goBackToStep1() {
    console.log("Step 1로 돌아가기");
    document.getElementById("step2").classList.add("d-none");
    document.getElementById("step1").classList.remove("d-none");
    document.getElementById("step2Circle").classList.remove("active");
    document.getElementById("step1Circle").classList.add("active");
    document.getElementById("email").value = '';
}

// Step 2로 돌아가기
function goBackToStep2() {
    console.log("Step 2로 돌아가기");

    // 돌아갈때 타이머 제거, OTP 인증완료칸 다시 활성화
    if (timerInterval) {
        clearInterval(timerInterval);
        timerInterval = null;
    }

    document.getElementById("step3").classList.add("d-none");
    document.getElementById("step2").classList.remove("d-none");
    document.getElementById("step3Circle").classList.remove("active");
    document.getElementById("step2Circle").classList.add("active");
    // document.getElementById("otpCode").value = '';
    document.getElementById("otpCode").disabled = false;
    document.getElementById("loginOtp").disabled = false;
    document.getElementById('timer').classList.remove('bg-danger');
    document.getElementById('timer').classList.add('bg-info');
}

// // alert() 대신 이 함수 호출
// function showAlert(message, callback=null) {
//     document.getElementById('alertModalMessage').innerText = message;
//     const model = new bootstrap.Modal(document.getElementById('alertModal'));
//     model.show();
//
//     // 확인 버튼 클릭시 콜백 실행
//     const confirmBtn = document.getElementById('alertModal').querySelector('[data-bs-dismiss="modal"]');
//     confirmBtn.onclick = () => {
//         if (callback) callback();
//     };
// }

// 타이머
let timerInterval = null;
let endTime = null; // 만료된 시각 저장

function startTimer() {

    // 기존 타이머 존재시 제거
    if (timerInterval) {
        clearInterval(timerInterval);
    }
    endTime = Date.now() + 240 * 1000; // 현재 시각 + 4분으로 계산

    const timerEl = document.getElementById("timer");

    timerInterval = setInterval(() => {
        const remaining = Math.ceil((endTime - Date.now()) / 1000);

        if (remaining <= 0) {
            clearInterval(timerInterval);
            timerEl.innerText = "남은 시간: 0:00"
            alert("인증 시간이 만료되었습니다.");
            document.getElementById('otpCode').disabled = true;
            document.getElementById('loginOtp').disabled = true;
            return;
        }

        const minutes = Math.floor(remaining / 60);
        const seconds = remaining % 60;

        timerEl.innerText =
            "남은 시간: " + minutes + ":" +
            (seconds < 10 ? "0" : "") + seconds;

        // 1분 이하 빨간색으로 변경
        if (remaining <= 60) {
            document.getElementById('timer').classList.remove('bg-info');
            document.getElementById('timer').classList.add('bg-danger');
        }

    }, 500); // 0.5초 마다 체크 정확도 올리기 위함
    // window.onload = startTimer;
}

// Step 1 제출 (아이디/비밀번호)
function submitStep1(event) {
    event.preventDefault(); // 새로고침 방지

    const adminId = document.getElementById('adminId').value;
    const password = document.getElementById('password').value;
    const rememberMe = document.getElementById('rememberMe').checked;

    if (!adminId || !password) {
        alert("아이디와 비밀번호를 입력해주세요.");
        return;
    }

    // Servlet 연동
    // TODO 로그인 상태유지 여부 추가 (유지기간 일단 30분으로 지정 상의 필요)
    fetch(CONTEXT_PATH + "/login/authenticate", {
        method: "POST",
        credentials: "same-origin", // 같은 도메일일때 세션을 자동으로 같이 넘겨주기 위해 설정
        headers: { // form값이 전송하는 파라미터를 받기위해 설정
            "Content-Type": "application/x-www-form-urlencoded"
        },
        body: "adminId=" + adminId + "&password=" + password + "&rememberMe=" + rememberMe
    })
        .then(res => {
            if (res.status === 403) {
                alert("활동이 중지된 계정입니다.")
                return;
            } else if (res.status === 401) {
                alert("아이디 또는 비밀번호가 잘못 되었습니다. 아이디와 비밀번호를 정확히 입력해 주세요.")
                return;
            }
            goStep2();
        })
}

// Step 2 제출 (이메일 확인)
function submitEmailStep(event) {
    event.preventDefault();

    const email = document.getElementById('email').value;
    if (!email) {
        alert("이메일을 입력해주세요.");
        return;
    }

    // 로딩 표시
    showLoading()

    fetch(CONTEXT_PATH + "/login/email", {
        method: "POST",
        credentials: "same-origin",
        headers: {
            "Content-Type": "application/x-www-form-urlencoded"
        },
        body: "email=" + email
    })
        .then(res => {
            if (res.status === 200) {
                document.getElementById('emailText').innerText = email + '로 전송된 인증번호를 입력하세요.'
                goStep3();
                // 이동후 타이머 시작
                startTimer();
            } else {
                alert('등록된 이메일과 일치하지 않습니다.')
            }
        })
        .finally(() => {
            // 요청, 응답 끝나면 무조건 로딩 종료
            hideLoading()
        })
}

// Step 3 제출 (OTP 인증)
function submitStep3(event) {
    event.preventDefault();

    const otpCode = document.getElementById('otpCode').value;

    console.log("OTP 인증 요청");
    console.log("OTP 입력:", otpCode);

    if (otpCode.length !== 6) {
        alert("6자리 인증번호를 입력해주세요.");
        return;
    }

    fetch(CONTEXT_PATH + "/login/otp", {
        method: "POST",
        credentials: "same-origin",
        headers: {
            "Content-Type": "application/x-www-form-urlencoded"
        },
        body: "otpCode=" + otpCode
    })
        .then(res => {
            if (res.redirected) {
                clearInterval(timerInterval);
                alert("[OTP Success] 인증 완료")
                window.location.href = res.url;
            } else if (res.status === 200) {
                clearInterval(timerInterval);
                alert("[OTP Success] 인증 완료")
                window.location.href = CONTEXT_PATH + "/main";
            } else if (res.status === 202) {
                clearInterval(timerInterval);
                alert("[OTP Success] 인증 완료")
                window.location.href = CONTEXT_PATH + "/setting?noPolicy=true";
            } else if (res.status === 401) {
                alert("[OTP Fail] \n 인증번호가 일치 하지 않습니다.")
            } else if (res.status === 403) {
                clearInterval(timerInterval);
                alert("[OTP Expired] 이전페이지로 돌아가 재발송해주세요.")
            } else {
                alert("[ERROR] 알 수 없는 오류")
            }
        })
}

// 숫자만 입력 (필터링)
document.addEventListener('DOMContentLoaded', function () {
    const otpInput = document.getElementById('otpCode');
    if (otpInput) {
        otpInput.addEventListener('input', function (e) {  // 화살표 함수 대신 일반 function 사용
            this.value = this.value.replace(/[^0-9]/g, '');
        });
    }
});
