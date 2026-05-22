// 날짜 객체를 "YYYY-MM-DD HH:mm" 형식으로 변환
function formatDateTime(dateStr) {
    if (!dateStr) return "-";
    const d = new Date(dateStr);
    const z = (n) => n.toString().padStart(2, '0');

    return d.getFullYear() + "-" + z(d.getMonth() + 1) + "-" + z(d.getDate()) +
        " " + z(d.getHours()) + ":" + z(d.getMinutes());
}

// 주차 현황 숫자 업데이트 함수
function updateParkingCount() {
    const occupiedCount = document.querySelectorAll('.parking-card.occupied').length;
    const displayCount = document.getElementById('occupiedCountText');
    if (displayCount) {
        displayCount.innerText = occupiedCount + "대";
    }
}

// 차량 번호 형식 함수
function validateCarFullNumber(inputCarNum) {
    const carNum = inputCarNum.trim();
    /*
    대한민국 차량 번호 공식
    1) 차종 기호 숫자 2~3자리로 시작 ^\d{2,3}
    2) 용도 기호 한글 1자리 [가-힣]
    3) 고유번호 숫자 4자리 \d{4}
     */
    const carRegex = /^\d{2,3}[가-힣]\d{4}$/;
    if (!carRegex.test(carNum)) {
        alert('올바른 형식의 차량번호를 입력하세요.')
        return false;
    }
    return true;
}