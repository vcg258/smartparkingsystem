// 비밀번호 변경칸
function updatePassword(event) {
    event.preventDefault(); // 새로고침 방지

    const password = document.getElementById("password").value;
    const newPassword = document.getElementById("newPassword").value;
    const newPasswordCheck = document.getElementById("newPasswordCheck").value;

    if (!password) {
        alert('현재 비밀번호를 입력해주세요.')
        return;
    }
    if (!newPassword || !newPasswordCheck) {
        alert('변경할 비밀번호를 입력해주세요.')
        return;
    }
    if (newPassword !== newPasswordCheck) {
        alert('새 비밀번호가 일치하지 않습니다.')
        return;
    }
    if (password === newPassword) {
        alert('현재 비밀번호와 동일합니다. 다른 비밀번호를 입력해주세요.')
        return;
    }
    fetch(CONTEXT_PATH + "/main/mypage/password", {

        method: "POST",
        headers: {
            "Content-Type": "application/x-www-form-urlencoded"
        },
        body: "password=" + password + "&newPassword=" + newPassword + "&newPasswordCheck=" + newPasswordCheck
    })
        .then(res => {
            if (res.status === 200) {
                alert("정보 수정 완료")
                window.location.href = CONTEXT_PATH + "/main"
            } else {
                alert("정보가 일치 하지 않습니다.")
            }
        })
}

// 이메일 변경칸 (OTP 인증)
function openEmailVerification() {
    const newEmail = document.getElementById("email").value;

    // TODO 변경시 경로 수정
    const url = CONTEXT_PATH + "/main/mypage/email?email=" + encodeURIComponent(newEmail);

    // 유효성 검사 (이메일 입력시 @가 있는지 없는지 검사)
    if (!newEmail || !newEmail.includes('@')) {
        alert('올바른 형식으로 입력해주세요.')
        return;
    }

    showLoading()

    fetch(CONTEXT_PATH + "/main/mypage/email/send", {
        method: "POST",
        headers: {
            "Content-Type": "application/x-www-form-urlencoded"
        },
        body: ""
    })
        .then(res => {
            if (res.status === 200) {
                console.log('인증번호 발송');
                // 팝업창 띄우기
                window.open(url, "emailPopup", "width=450,height=380");
            } else {
                alert('Error 알 수 없는 에러 발생')
            }
        })
        .finally(() => {
            // 요청, 응답 끝나면 무조건 로딩 종료
            hideLoading()
        })
}

// 인증 성공시 정보 수정 버튼 활성화
function onEmailVerified() {
    const btn = document.getElementById("updateEmail")

    alert('이메일 인증이 완료되었습니다.')
    document.getElementById('email').disabled = true;
    document.getElementById('emailCertified').disabled = true;
    btn.disabled = false;
    btn.classList.remove("btn-primary");
    btn.classList.add("btn-success");
}

// 이메일 수정
function submitUpdateEmail(event) {
    console.log("진입")
    event.preventDefault(); // 새로고침 방지

    const newEmail = document.getElementById("email").value;

    fetch(CONTEXT_PATH + "/main/mypage/email/update", {
        method: "POST",
        headers: {
            "Content-Type": "application/x-www-form-urlencoded"
        },
        body: "newEmail=" + encodeURIComponent(newEmail)
    })
        .then(res => {
            if (res.status === 200) {
                alert('이메일 변경완료')
                document.getElementById('updateEmail').disabled = true; // 변경후 다시 비활성화
                document.getElementById('email').disabled = false;
                document.getElementById('emailCertified').disabled = false;
                document.getElementById("updateEmail").classList.remove("btn-success");
                document.getElementById("updateEmail").classList.add("btn-primary");

            } else {
                alert('Error 알 수 없는 에러')
            }
        })
}
