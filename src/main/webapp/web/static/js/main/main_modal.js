let modal;

document.addEventListener('DOMContentLoaded', () => {
    /* 모달 관련 객체 선언
    모달 객체 불러와 정보 가져오기 (주차 구역, 차량 번호, 주차 시간, 주차 구역 상태)
    */
    modal = new bootstrap.Modal(document.getElementById('parkingModal'));
    const modalTitle = document.getElementById('modal-id');
    const modalAction = document.getElementById('modal-action');
    const btnMembershipPay = document.getElementById('btnMembershipPay');
    const sectionEntry = document.getElementById('section-entry');
    const sectionExit = document.getElementById('section-exit');
    const sectionReceipt = document.getElementById('section-receipt');
    const footerCloseBtn = document.querySelector('#parkingModal .modal-footer button[data-bs-dismiss="modal"]');

    window.currentCard = null;

// 모달 요소 초기화
    function resetModal() {
        sectionEntry.style.display = 'none';
        sectionExit.style.display = 'none';
        sectionReceipt.style.display = 'none';
        btnMembershipPay.style.display = 'none';
        modalAction.style.display = 'block';
        footerCloseBtn.style.display = 'block';
    }

// 주차 구역 클릭 이벤트 (클릭 시 정보 모달 팝업)
    document.querySelectorAll('.parking-card').forEach(card => {
        card.addEventListener('click', async () => {
            // 클릭된 주차 구역 정보 변수 저장
            window.currentCard = card;

            resetModal();

            const status = card.dataset.status;
            const id = card.dataset.id;
            const car = card.dataset.carNum;
            const inFullTime = currentCard.dataset.inFullTime;
            const type = card.dataset.carType;
            const memberLabel = (card.dataset.isMember === "true") ? "(회원 할인 적용)" : "";

            if (status === 'available') {
                modalTitle.innerText = id + " 입차 관리";
                // 입차 처리 세팅
                document.getElementById('section-entry').style.display = 'block';
                modalAction.innerText = "입차 등록";
            } else {
                modalTitle.innerText = id + " 출차 관리";
                // 출차 처리 세팅
                document.getElementById('section-exit').style.display = 'block';
                btnMembershipPay.style.display = 'block';

                const now = new Date();
                const outFullTime = now.toISOString();
                const effectiveType = (card.dataset.isMember === "true") ? "월정액" : type;
                const parkNo = card.dataset.parkNo;
                const info = await axios.get(`${CONTEXT_PATH}/parking/getPaymentInfo?parkNo=${parkNo}`);
                const paymentInfo = info.data;
                const chargeResult = calculateParkingCharge(inFullTime, outFullTime, effectiveType, paymentInfo);

                // 데이터 매핑
                document.getElementById('info-car').innerText = car; // 차량 번호
                document.getElementById('info-type').innerText = type; // 차종
                document.getElementById('info-inTime').innerText = formatDateTime(inFullTime); // 주차 시간
                document.getElementById('info-outTime').innerText = formatDateTime(outFullTime); // 출차 시간
                document.getElementById('info-totalPrice').innerText = chargeResult.total.toLocaleString() + "원";
                document.getElementById('info-isMember').innerText = memberLabel;

                modalAction.innerText = "결제하기";
                modalAction.className = "btn btn-danger";
            }
            modal.show();
        });
    });

// '입차 등록', '결제하기' 버튼
    modalAction.addEventListener('click', () => {
        if (modalAction.innerText === "입차 등록") {
            handleEntry();
        } else if (modalAction.innerText === "결제하기") {
            handlePayment();
        }
    })

// 입차 등록 함수
    function handleEntry() {
        const inputCarNum = document.getElementById('input-carNum'); // input-carNum : 사용자 입력 차량번호
        const parkingArea = window.currentCard.dataset.id;
        const selectedCarType = document.querySelector('input[name="carType"]:checked');
        const carType = selectedCarType.value;
        const carNum = inputCarNum.value.replace(/\s+/g, '');

        if (!carNum) {
            alert("차량 번호를 입력해 주세요");
            alert("차량 번호를 입력해 주세요");
            return;
        }
        if (!validateCarFullNumber(carNum)) {
            inputCarNum.focus();
            return;
        }
        if (!selectedCarType) {
            alert('차종을 선택해 주세요.')
            return;
        }

        // 서버 즉시 업데이트 (DB 전)
        // const now = new Date();
        // const fullTime = now.toISOString();
        // currentCard.dataset.status = 'occupied'; // 데이터 바꾸기
        // currentCard.dataset.carNum = carNum; // 차량 번호
        // currentCard.dataset.inFullTime = fullTime; // 주차 시간
        // currentCard.dataset.carType = carType; // 차종

        fetch(CONTEXT_PATH + '/parking/entry', {
            method: 'POST',
            headers: {'Content-Type': 'application/x-www-form-urlencoded'}, // 데이터 형식 지정
            body: `parkingArea=${parkingArea}&carNum=${carNum}&carType=${carType}` // 보낼 데이터
        })
            .then(res => res.json()) // 서버의 응답을 JSON으로 변환
            .then(data => { // 서버가 보낸 응답 객체

                if (!data.success) {
                    alert('입차 등록 실패! ' + data.message);
                    return;
                }
                if (data.exists) {
                    alert('이미 주차 중인 차량입니다.');
                    return;
                }

                // DB 저장 성공 시
                window.currentCard.dataset.status = 'occupied';
                window.currentCard.dataset.parkNo = data.parkNo;
                window.currentCard.dataset.carNum = carNum;
                window.currentCard.dataset.carType = carType;
                window.currentCard.dataset.inFullTime = data.entryTime;
                window.currentCard.dataset.isMember = data.isMember;

                console.log('set 후 dataset.isMember:', window.currentCard.dataset.isMember);

                // UI 업데이트
                const isCenter = window.currentCard.closest('.center-row') !== null;
                window.currentCard.classList.replace('available', 'occupied'); // 배경색 변경
                window.currentCard.querySelector('.box-car').innerText =
                    isCenter ? carNum.replace(/([가-힣])(\d)/, '$1\n$2') : carNum;
                window.currentCard.querySelector('.box-time').innerText = "00:00";

                alert(`${carNum} 차량 입차 완료!`);
                // console.log('dataset isMember after set:', window.currentCard.dataset.isMember);

                document.getElementById('parkingModal').querySelector('.btn-close').blur();
                document.body.focus(); //**
                modal.hide();
                updateParkingCount();
                inputCarNum.value = ""; // 입력창 초기화
            })
            .catch(err => alert('오류 발생! 관리자에게 문의하세요.' + err));
    }

// 결제 진행 함수
    async function handlePayment() {

     // 출차 -> 영수증
        const inFullTime = window.currentCard.dataset.inFullTime;
        const outFullTime = new Date().toISOString();
        const carType = window.currentCard.dataset.carType;
        const carNum = window.currentCard.dataset.carNum;
        const effectiveType = (window.currentCard.dataset.isMember === "true") ? "월정액" : carType;

        const parkNo = window.currentCard.dataset.parkNo;
        const info = await axios.get(`${CONTEXT_PATH}/parking/getPaymentInfo?parkNo=${parkNo}`);
        const paymentInfo = info.data;
        console.log('policy: ', paymentInfo);
        const chargeResult = calculateParkingCharge(inFullTime, outFullTime, effectiveType, paymentInfo);
        console.log('chargeResult: ', chargeResult);
        // console.log("effectiveType:", effectiveType);
        // console.log("isMember:", currentCard.dataset.isMember);
        // console.log("chargeResult:", chargeResult);


        // 영수증 데이터 매핑
        document.getElementById('rec-car').innerText = carNum;
        document.getElementById('rec-in').innerText = formatDateTime(inFullTime);
        document.getElementById('rec-out').innerText = formatDateTime(outFullTime);

        document.getElementById('rec-totalTime').innerText = chargeResult.duration + "분";
        document.getElementById('rec-basePrice').innerText = chargeResult.base.toLocaleString() + "원";
        document.getElementById('rec-extraPrice').innerText = chargeResult.extra.toLocaleString() + "원";

        // 할인 표시
        const discountText = chargeResult.discount > 0 ?
            `-${chargeResult.discount.toLocaleString()}원 (${chargeResult.discountName})`
            : "0원";
        document.getElementById('rec-discount').innerText = discountText;

        // 최종 금액
        document.getElementById('rec-totalPrice').innerText = chargeResult.total.toLocaleString() + "원";

        // UI 전환
        document.getElementById('section-exit').style.display = 'none';
        document.getElementById('section-receipt').style.display = 'block';

        document.querySelector('#section-receipt h5').tabIndex = -1;
        document.querySelector('#section-receipt h5').focus();

        // 하단 버튼 숨기기
        btnMembershipPay.style.display = 'none';
        modalAction.style.display = 'none';
        footerCloseBtn.style.display = 'none';
    }

// 출차 후 영수증 화면 -> '정산 완료' 버튼 클릭
    document.getElementById('btn-close-final').addEventListener('click', async () => {
        if (!window.currentCard) return;

        const parkNo = window.currentCard.dataset.parkNo;
        const btnFinal = document.getElementById('btn-close-final');
        btnFinal.disabled = true;
        btnFinal.innerText = '처리 중';

        try {
            console.log('결제 등록 시작')
            const paymentResponse = await axios.post(CONTEXT_PATH + '/parking/payment', `parkNo=${parkNo}`, {
                headers: {'Content-Type': 'application/x-www-form-urlencoded'}
            });
            if (!paymentResponse.data.success) {
                alert('결제 실패!' + paymentResponse.data.message);
                return;
            }

            console.log('결제 등록 완료');
            console.log('출차 처리 시작');
            const exitResponse = await axios.post(CONTEXT_PATH + '/parking/exit', `parkNo=${parkNo}`, {
                headers: {'Content-Type': 'application/x-www-form-urlencoded'}
            });
            if (!exitResponse.data.success) {
                alert('출차 처리 실패!' + exitResponse.data.message);
                return;
            }
            console.log('출차 처리 완료')

            // 1. 데이터 초기화
            window.currentCard.dataset.status = 'available';
            window.currentCard.dataset.carNum = "";
            window.currentCard.dataset.inFullTime = "";
            window.currentCard.dataset.carType = "";
            window.currentCard.dataset.parkNo = "";

            // 2. UI 초기화
            window.currentCard.classList.replace('occupied', 'available');
            // console.log(window.currentCard.classList);
            window.currentCard.querySelector('.box-car').innerText = "사용 가능";
            window.currentCard.querySelector('.box-time').innerText = "";

            // 3. 모달 닫기
            document.getElementById('parkingModal').querySelector('.btn-close').blur();
            document.body.focus();
            modal.hide();
            updateParkingCount();
            alert("정산이 완료되어 출차 처리되었습니다.");


            // fetch('/parking/exit', {
            //     method: 'POST',
            //     headers: {'Content-Type': 'application/x-www-form-urlencoded'}, // 데이터 형식 지정
            //     body: `parkNo=${parkNo}`
            // })
            //     .then(res => res.json()) // 서버의 응답을 JSON으로 변환
            //     .then(data => { // 서버가 보낸 응답 객체
        } catch (err) {
            console.log('오류 발생! ' + err);
            alert('오류 발생! 관리자에게 문의하세요.')
        } finally {
            btnFinal.disabled = false;
            btnFinal.disabled = false;
            btnFinal.innerText = '정산 완료'
        }


    });

    function updateElapsedTime() {
        const now = new Date();
        document.querySelectorAll('.parking-card.occupied').forEach(card => {
            const timeDisplay = card.querySelector('.box-time');
            if (!timeDisplay) return;

            // 1. 비어있는 구역이면 시간 글자를 지우고 다음 카드로 넘어감
            if (card.classList.contains('available')) {
                timeDisplay.innerText = "";
                return;
            }

            // 2. 중앙구역 차량번호 줄바꿈 유지
            const centerOccupied = document.querySelectorAll('.center-row .parking-card.occupied');
            centerOccupied.forEach(card => {
                const boxCar = card.querySelector('.box-car');
                const carNum = card.dataset.carNum;

                if (carNum && boxCar && !boxCar.innerText.includes('\n')) {
                    boxCar.innerText = carNum.replace(/([가-힣])(\d)/, '$1\n$2');
                }
            })

            const inFullTime = card.dataset.inFullTime;
            if (!inFullTime || isNaN(new Date(inFullTime).getTime())) {
                return;
            }

            const entryTime = new Date(inFullTime);
            const diffMins = Math.floor((now - entryTime) / 60000);

            // 서버-클라이언트 시차 - 방지
            // ex. 서버 시간 2시 00분 05초 -> 내 컴퓨터 1시 59분 57초
            const totalMins = diffMins < 0 ? 0 : diffMins;

            const hours = Math.floor(totalMins / 60);
            const mins = totalMins % 60;
            const timeStr = String(hours).padStart(2, '0') + ":" + String(mins).padStart(2, '0');

            timeDisplay.innerText = timeStr;
        });

        if (typeof updateParkingCount === 'function') {
            updateParkingCount();
        }
    }

    /* 창 닫힐 때 섹션 리셋 */
    const parkingModal = document.getElementById('parkingModal');
    parkingModal.addEventListener('hidden.bs.modal', () => {
        sectionReceipt.style.display = 'none';
        modalAction.style.display = 'block';
        footerCloseBtn.style.display = 'block';
        document.getElementById('input-carNum').value = "";
    })

    updateElapsedTime();

    setInterval(updateElapsedTime, 60000);
})

// 회원권 결제 클릭 시
window.moveMembershipPage = function () {
    console.log("회원권 결제 클릭됨");
    if (!window.currentCard) {
        alert("선택된 차량 정보가 없습니다.");
        return;
    }
    const carNum = window.currentCard.dataset.carNum;
    alert("신규 회원 등록 페이지로 이동합니다.");

    modal.hide();
    window.location.href = CONTEXT_PATH + '/member_list?pageNum=1&openNewMemberModal=true&carNum=' + encodeURIComponent(carNum);
}
