// Step 2로 이동
function goStep2() {
    console.log("Step 2로 이동");
    document.getElementById("step1").classList.add("d-none");
    document.getElementById("step2").classList.remove("d-none");
    document.getElementById("inputEmail").focus();
}

// Step 3로 이동
function goStep3() {
    console.log("Step 3로 이동");
    document.getElementById("step2").classList.add("d-none");
    document.getElementById("step3").classList.remove("d-none");
    document.getElementById("inputVerificationCode").focus();
}

// ClearStep 로 이동
function ClearStep() {
    console.log("Clear Step");
    document.getElementById("step3").classList.add("d-none");
    document.getElementById("clearStep").classList.remove("d-none");
}

// Step 1로 돌아가기
function goBackToStep1() {
    document.getElementById("step2").classList.add("d-none");
    document.getElementById("step1").classList.remove("d-none");
    document.getElementById("inputEmail").value = '';
}

// Step 2로 돌아가기
function goBackToStep2() {
    if (timerInterval) {
        clearInterval(timerInterval);
    }

    document.getElementById("step3").classList.add("d-none");
    document.getElementById("step2").classList.remove("d-none");
    document.getElementById("inputVerificationCode").value = '';
    document.getElementById('inputVerificationCode').disabled = false;
    document.getElementById('goStep').disabled = false;
    document.getElementById('timer').classList.remove('bg-danger');
    document.getElementById('timer').classList.add('bg-info');
}

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
            showAlert("인증 시간이 만료되었습니다.");
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

// Step 1 제출 (아이디 확인)
function submitStep1(event) {
    event.preventDefault();

    const adminId = document.getElementById('inputAdminId').value;

    if (!adminId) {
        alert("관리자 아이디를 입력해주세요.");
        return;
    }

    fetch(CONTEXT_PATH + "/password/id", {
        method: "POST",
        headers: {
            "Content-Type" : "application/x-www-form-urlencoded"
        },
        body: "adminId=" + adminId
    })
    goStep2();
}

// Step 2 제출 (이메일 입력 및 인증번호 발송)
function submitStep2(event) {
    event.preventDefault();

    const email = document.getElementById('inputEmail').value;
    const adminId = document.getElementById('inputAdminId').value;

    if (!email) {
        alert("이메일을 입력해주세요.");
        return;
    }

    showLoading()

    fetch(CONTEXT_PATH + "/password/email", {
        method: "POST",
        headers: {
            "Content-Type" : "application/x-www-form-urlencoded"
        },
        body: "email=" + email
    })
        .then(res => {
            if (res.status === 200) {
                document.getElementById("emailText").innerText = email + "로 인증번호를 발송했습니다.";
                goStep3();
                startTimer();
            } else {
                alert('입력하신 정보가 일치하지 않습니다.')
            }
        })
        .finally(() => {
            // 요청, 응답 끝나면 무조건 로딩 종료
            hideLoading()
        })
}

// Step 3 제출 (인증번호 확인 및 비밀번호 재설정)
function submitStep3(event) {
    event.preventDefault();

    const otpCode = document.getElementById('inputVerificationCode').value;

    if (otpCode.length !== 6) {
        alert("6자리 인증번호를 입력해주세요.");
        return;
    }

    showLoading()

    fetch(CONTEXT_PATH + "/password/otp", {
        method: "POST",
        headers: {
            "Content-Type" : "application/x-www-form-urlencoded"
        },
        body: "otpCode=" + otpCode
    })
        .then(res => {
            if (res.status === 200) {
                clearInterval(timerInterval);
                alert("[OTP Success] 인증 완료")
                ClearStep()
            } else if (res.status === 401) {
                alert("[OTP Fail] 인증번호가 일치 하지 않습니다.")
            } else if (res.status === 403) {
                clearInterval(timerInterval);
                alert("[OTP Expired] 이전페이지로 돌아가 재발송해주세요.")
            } else {
                alert("[ERROR] 알 수 없는 오류")
            }
        })
        .finally(() => {
            hideLoading()
        })
}

// 숫자만 입력 (필터링)
document.addEventListener('DOMContentLoaded', function() {
    const codeInput = document.getElementById('inputVerificationCode');
    if (codeInput) {
        codeInput.addEventListener('input', function(e) {
            this.value = this.value.replace(/[^0-9]/g, '');
        });
    }
});
