package com.example.parkingsystem.service.payment;


import com.example.parkingsystem.dao.main.ParkingHistoryDAO;
import com.example.parkingsystem.dao.member.MembersDAO;
import com.example.parkingsystem.dao.payment.PaymentHistoryDAO;
import com.example.parkingsystem.dao.setting.PaymentInfoDAO;
import com.example.parkingsystem.dto.payment.PaymentHistoryDTO;
import com.example.parkingsystem.util.MapperUtil;
import com.example.parkingsystem.vo.main.ParkingHistoryVO;
import com.example.parkingsystem.vo.member.MembersVO;
import com.example.parkingsystem.vo.payment.PaymentHistoryVO;
import com.example.parkingsystem.vo.setting.PaymentInfoVO;
import lombok.extern.log4j.Log4j2;
import org.modelmapper.ModelMapper;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Log4j2
public enum PaymentHistoryService {
    INSTANCE;

    // 객체 선언
    private final PaymentHistoryDAO paymentHistoryDAO;
    private final ParkingHistoryDAO parkingHistoryDAO;
    private final MembersDAO membersDAO;
    private final PaymentInfoDAO paymentInfoDAO;
    // 싱글턴 작업으로 한번만 불러와서 사용하다보니 한번 정책 변경후에는 서버재실행 전까진 적용안됨 수정완료
    //    private final PaymentInfoVO paymentInfoVO;
    private final ModelMapper modelMapper;

    PaymentHistoryService() {
        paymentHistoryDAO = new PaymentHistoryDAO();
        parkingHistoryDAO = new ParkingHistoryDAO();
        membersDAO = new  MembersDAO();
        paymentInfoDAO = new PaymentInfoDAO();
        modelMapper = MapperUtil.INSTANCE.getInstance();
    }

    // 출차 시간(현재)
    // *** 상수로 선언해서 출차 시간이 고정돼 음수 나오는 현상 -> 출차 실패 오류 뜨는 원인
    // private final LocalDateTime exitTime = LocalDateTime.now();

    // 총 주차시간 계산 메서드
    public long getTotalTime(String carNum) {
        LocalDateTime entryTime = parkingHistoryDAO.selectRecentParking(carNum).getEntryTime(); // 입차 시간
        LocalDateTime exitTime = LocalDateTime.now(); //***
        return Duration.between(entryTime, exitTime).toSeconds(); // 입차 시간 - 출차 시간;
    }

    // 총 요금 계산 메서드 (total_charge)
    private int calculateTotalCharge(String carNum) {
        // 메서드 실행마다 불러와서 실시간 적용
        PaymentInfoVO paymentInfoVO = paymentInfoDAO.selectInfo();
        log.info("calculateTotalCharge");
        // 정책
        int freeTime = paymentInfoVO.getFreeTime() * 60; // 무료 회차 시간 (분을 초로 변환)
        int basicCharge = paymentInfoVO.getBasicCharge(); // 기본 요금
        int basicTime = paymentInfoVO.getBasicTime() * 60; // 기본 요금 시간 (분을 초로 변환)
        int extraTime = paymentInfoVO.getExtraTime() * 60; // 초과 시간 (분을 초로 변환)
        int extraCharge = paymentInfoVO.getExtraCharge(); // 초과 시간 당 추가 요금
        int maxCharge = paymentInfoVO.getMaxCharge(); // 일일 최대 요금
        long totalSeconds = getTotalTime(carNum); // 총 주차시간(초)
        int totalCharge; // 총 주차 요금

        // 주차일수 ((24 * 60) * 60) = 24시간
        int dayCount = (int) totalSeconds / ((24 * 60) * 60); // (분을 초로 변환)

        if (totalSeconds <= freeTime) {
            totalCharge = 0; // 무료 회차시간 적용 요금
        } else if (totalSeconds <= basicTime) {
            totalCharge = basicCharge;
        } else {
            // 24시간 이하 요금 & 24시간 초과시 (24시간 제외 후) 남은 시간에 대한 요금
            long remainSeconds = totalSeconds % ((24 * 60) * 60); // (분을 초로 변환)
            int restTimeCharge = remainSeconds <= basicTime // 음수 방지 추가
                    ? basicCharge : ((int)((remainSeconds - basicTime) / extraTime) * extraCharge) + basicCharge;
            // 일일 최대 요금 초과시 일일 최대 요금으로 변경
            restTimeCharge = Math.min(restTimeCharge, maxCharge);

            // 24시간 넘는 경우 총 요금
            totalCharge = dayCount > 0 ? restTimeCharge + (dayCount * maxCharge) : restTimeCharge;
        }

        return totalCharge;
    }

    // 할인 금액 계산 메서드(discount_amount)
    private int calculateDiscountAmount(String carNum) {
        // 메서드 실행마다 불러와서 실시간 적용
        PaymentInfoVO paymentInfoVO = paymentInfoDAO.selectInfo();
        log.info("calculateDiscountAmount");

        int totalCharge = calculateTotalCharge(carNum);
        int discountAmount = 0; // 할인 금액

        // 정책
        double smallCarDiscount = paymentInfoVO.getSmallCarDiscount();
        log.info(smallCarDiscount);
        double disabledDiscount = paymentInfoVO.getDisabledDiscount();

        // 자동차 타입 확인(일반, 경차, 장애인)
        String carType = parkingHistoryDAO.selectRecentParking(carNum).getCarType();

        // 타입 별 할인 금액
        if (carType.equals("경차")) {
            discountAmount = (int) (totalCharge * smallCarDiscount);
        } else if (carType.equals("장애인")) {
            discountAmount = (int) (totalCharge * disabledDiscount);
        }

        return discountAmount;
    }

    // 최종 결제 금액, VO에 입력 메서드
    // 잘못된 차량번호 조회시 return, 멤버이면 총요금, 할인금액, 최종금액 0원 처리 후 return
    public void calculateFinalCharge(String carNum) { // PaymentHistoryVO에 넣는 메서드
        // 메서드 실행마다 불러와서 실시간 적용
        PaymentInfoVO paymentInfoVO = paymentInfoDAO.selectInfo();
        // 잘못된 차량번호 조회
        if (parkingHistoryDAO.selectRecentParking(carNum).getCarNum() == null) {
            return;
        }
        log.info("calculateFinalCharge 시작 - carNum: {}", carNum);

        ParkingHistoryVO recent = parkingHistoryDAO.selectRecentParking(carNum);
        log.info("selectRecentParking 결과: {}", recent);

        if (recent == null || recent.getCarNum() == null) {
            log.info("차량 없음으로 return");
            return;
        }

        int totalCharge = calculateTotalCharge(carNum);
        int discountAmount = calculateDiscountAmount(carNum);
        long totalSeconde = getTotalTime(carNum);
        int finalCharge; // 최종 결제 요금

        // 멤버인지 아닌지 확인 후 멤버면 총 요금 0원
        // TODO 만료된 회원도 무료처리가 되던거 정확하게 수정
        MembersVO member = membersDAO.selectMemberByCarNum(carNum);
        boolean isMember = member != null
                && !member.getEndDate().isBefore(LocalDate.now())
                && !member.getStartDate().isAfter(LocalDate.now());
        if (isMember) {
            totalCharge = 0;
            discountAmount = 0;
            finalCharge = 0;
        } else {
            // 최종 결제 금액
            finalCharge = totalCharge - discountAmount;
        }

        ParkingHistoryVO parkingHistoryVO = parkingHistoryDAO.selectRecentParking(carNum);

        MembersVO membersVO = membersDAO.selectOneMember(carNum);
        Long mno = membersVO == null ? null : membersVO.getMno();

         PaymentHistoryVO paymentHistoryVO = PaymentHistoryVO.builder()
                .parkingArea(parkingHistoryVO.getParkingArea())
                .carNum(carNum)
                .entryTime(parkingHistoryVO.getEntryTime())
                .exitTime(LocalDateTime.now()) // ***
                .totalMinutes(totalSeconde / 60)
                .totalCharge(totalCharge)
                .mno(mno)
                .pno(paymentInfoVO.getPno())
                .parkNo(parkingHistoryVO.getParkNo())
                .discountAmount(discountAmount)
                .finalCharge(finalCharge)
                .isPaid(true)
                .build();
         paymentHistoryDAO.insertPaymentHistory(paymentHistoryVO);
        log.info("insert 완료, 조회 시작");

        PaymentHistoryVO check = paymentHistoryDAO.selectRecentPayment(carNum);
        log.info("selectRecentPayment 결과: {}", check);

    }

    // VO를 DTO로 변경 메서드
    public PaymentHistoryDTO getRecentPayment(String carNum) {
        PaymentHistoryVO paymentHistoryVO = paymentHistoryDAO.selectRecentPayment(carNum);
        if (paymentHistoryVO == null) {
            return null;
        }
        return modelMapper.map(paymentHistoryVO, PaymentHistoryDTO.class);
    }
}
