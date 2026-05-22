// 콤마를 적용할 대상 ID들 리스트
const moneyIds = ['in-base-fee', 'in-day-max-fee', 'in-add-fee', 'in-monthly-fee'];

// 정책 미설정 안내 모달 표시 (로그인 후 정책이 없을 때 호출)
function showNoPolicyModal() {
    const modalEl = document.getElementById('noPolicyModal');
    if (modalEl) {
        const modal = new bootstrap.Modal(modalEl);
        modal.show();
    }
}

// 1. 숫자에 콤마를 추가하는 함수
function formatNumber(e) {
    let value = e.target.value.replace(/[^\d]/g, ""); // 숫자 외 제거
    if (value) {
        e.target.value = Number(value).toLocaleString('ko-KR');
    } else {
        e.target.value = "";
    }
}

// 2. 페이지 로드 시 초기값 설정 및 이벤트 바인딩
window.addEventListener('DOMContentLoaded', function() {
    moneyIds.forEach(id => {
        const input = document.getElementById(id);
        if (input) {
            // 초기값 콤마 처리
            if (input.value) {
                input.value = Number(input.value.replace(/[^\d]/g, "")).toLocaleString('ko-KR');
            }
            // 입력 시 실시간 처리
            input.addEventListener('input', formatNumber);
        }
    });
});

// 3. 전송 전 콤마 제거 함수 (서버 에러 방지)
function prepareSubmit() {
    moneyIds.forEach(id => {
        const input = document.getElementById(id);
        if (input) {
            input.value = input.value.replace(/,/g, ""); // 콤마 제거 후 전송
        }
    });
    return true; // form 제출 진행
}

function clickSubmit() {
    const form = document.forms['setting'];

    moneyIds.forEach(id => {
        const input = document.getElementById(id);
        if (input) {
            input.value = input.value.replace(/,/g, "");
        }
    });

    const formData = new FormData(form);
    const params = new URLSearchParams(formData);

    fetch(CONTEXT_PATH + "/setting", {
        method: 'POST',
        body: params
    })
        .then(res => {
            if (res.ok) {
                alert('설정이 성공적으로 변경되었습니다.')
                location.reload();
            } else {
                alert('설정이 형식에 맞지않습니다.')
            }
        });
}
