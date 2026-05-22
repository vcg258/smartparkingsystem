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
                document.getElementById('section-exit').style.display = 'block';
                btnMembershipPay.style.display = 'block';

                const parkNo = card.dataset.parkNo;
                // Java 서비스에서 계산한 미리보기 요금 조회
                const info = await axios.get(`${CONTEXT_PATH}/parking/getPaymentInfo?parkNo=${parkNo}`);
                const result = info.data;

                // isMember는 서버 응답 기준으로 판단 (새로고침 없이도 정확하게 표시)
                const memberLabel = result.isMember ? "(회원 할인 적용)" : "";

                document.getElementById('info-car').innerText = result.carNum;
                document.getElementById('info-type').innerText = type;
                document.getElementById('info-inTime').innerText = formatDateTime(result.inTime);
                document.getElementById('info-outTime').innerText = formatDateTime(result.outTime);
                document.getElementById('info-totalPrice').innerText = result.finalCharge.toLocaleString() + "원";
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

// 결제 진행 함수 — Java에서 계산 후 DB insert, 응답값으로 영수증 표시
    async function handlePayment() {
        const parkNo = window.currentCard.dataset.parkNo;

        try {
            const paymentResponse = await axios.post(
                CONTEXT_PATH + '/parking/payment',
                `parkNo=${parkNo}`,
                { headers: { 'Content-Type': 'application/x-www-form-urlencoded' } }
            );
            const data = paymentResponse.data;

            if (!data.success) {
                alert('결제 실패! ' + data.message);
                return;
            }

            // 영수증 데이터 매핑 (Java 계산값)
            document.getElementById('rec-car').innerText       = data.carNum;
            document.getElementById('rec-in').innerText        = formatDateTime(data.entryTime);
            document.getElementById('rec-out').innerText       = formatDateTime(data.exitTime);
            document.getElementById('rec-totalTime').innerText = data.totalMinutes + "분";
            document.getElementById('rec-basePrice').innerText = data.baseCharge.toLocaleString() + "원";
            document.getElementById('rec-extraPrice').innerText = data.extraCharge.toLocaleString() + "원";

            const discountText = data.discountAmount > 0
                ? `-${data.discountAmount.toLocaleString()}원 (${data.discountName})`
                : "0원";
            document.getElementById('rec-discount').innerText   = discountText;
            document.getElementById('rec-totalPrice').innerText = data.finalCharge.toLocaleString() + "원";

            // UI 전환
            document.getElementById('section-exit').style.display    = 'none';
            document.getElementById('section-receipt').style.display = 'block';
            document.querySelector('#section-receipt h5').tabIndex = -1;
            document.querySelector('#section-receipt h5').focus();

            btnMembershipPay.style.display = 'none';
            modalAction.style.display      = 'none';
            footerCloseBtn.style.display   = 'none';
        } catch (err) {
            console.error('결제 처리 오류', err);
            alert('오류 발생! 관리자에게 문의하세요.');
        }
    }

// 출차 후 영수증 화면 -> '정산 완료' 버튼 클릭 — 출차 처리만 (결제는 결제하기 클릭 시 완료)
    document.getElementById('btn-close-final').addEventListener('click', async () => {
        if (!window.currentCard) return;

        const parkNo = window.currentCard.dataset.parkNo;
        const btnFinal = document.getElementById('btn-close-final');
        btnFinal.disabled = true;
        btnFinal.innerText = '처리 중';

        try {
            const exitResponse = await axios.post(CONTEXT_PATH + '/parking/exit', `parkNo=${parkNo}`, {
                headers: { 'Content-Type': 'application/x-www-form-urlencoded' }
            });
            if (!exitResponse.data.success) {
                alert('출차 처리 실패!' + exitResponse.data.message);
                return;
            }

            // 1. 데이터 초기화
            window.currentCard.dataset.status    = 'available';
            window.currentCard.dataset.carNum     = "";
            window.currentCard.dataset.inFullTime = "";
            window.currentCard.dataset.carType    = "";
            window.currentCard.dataset.parkNo     = "";

            // 2. UI 초기화
            window.currentCard.classList.replace('occupied', 'available');
            window.currentCard.querySelector('.box-car').innerText  = "사용 가능";
            window.currentCard.querySelector('.box-time').innerText = "";

            // 3. 모달 닫기
            document.getElementById('parkingModal').querySelector('.btn-close').blur();
            document.body.focus();
            modal.hide();
            updateParkingCount();
            alert("정산이 완료되어 출차 처리되었습니다.");
        } catch (err) {
            console.log('오류 발생! ' + err);
            alert('오류 발생! 관리자에게 문의하세요.');
        } finally {
            btnFinal.disabled  = false;
            btnFinal.innerText = '정산 완료';
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
