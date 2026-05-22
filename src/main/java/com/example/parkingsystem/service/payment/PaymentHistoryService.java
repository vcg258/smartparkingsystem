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
import java.time.temporal.ChronoUnit;

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

    /**
     * JS의 calculateBaseFeeOnly() 이식
     * 초 단위 계산으로 freeTime=0 설정 시 분 단위와의 차이 문제 해결
     */
    private int calcBaseFeeOnly(long seconds, int freeTimeSec, int basicTimeSec,
                                int basicCharge, int extraTimeSec, int extraCharge) {
        if (seconds < freeTimeSec) return 0;
        if (seconds <= basicTimeSec) return basicCharge;
        long extraSeconds = seconds - basicTimeSec;
        int extraUnits = (int) Math.ceil((double) extraSeconds / extraTimeSec);
        return basicCharge + extraUnits * extraCharge;
    }

    /**
     * JS의 calculateParkingCharge() 이식 — 날짜별 maxCharge cap 처리
     * DB insert 없이 계산 결과만 반환 (미리보기 및 최종 계산 공통 사용)
     */
    public PaymentHistoryDTO calcCharge(String carNum) {
        PaymentInfoVO p = paymentInfoDAO.selectInfo();
        ParkingHistoryVO parking = parkingHistoryDAO.selectRecentParking(carNum);
        if (parking == null || p == null) return null;

        LocalDateTime entryTime = parking.getEntryTime();
        LocalDateTime exitTime  = LocalDateTime.now();
        String carType = parking.getCarType();

        // 정책값 초 단위 변환
        int freeTimeSec  = p.getFreeTime()  * 60;
        int basicTimeSec = p.getBasicTime() * 60;
        int extraTimeSec = p.getExtraTime() * 60;
        int basicCharge  = p.getBasicCharge();
        int extraCharge  = p.getExtraCharge();
        int maxCharge    = p.getMaxCharge();
        double smallCarDiscount = p.getSmallCarDiscount();
        double disabledDiscount = p.getDisabledDiscount();

        // 날짜 기준 diff
        LocalDate startDate = entryTime.toLocalDate();
        LocalDate endDate   = exitTime.toLocalDate();
        long diffDays = startDate.until(endDate, ChronoUnit.DAYS);

        int preTotal;
        if (diffDays == 0) {
            long totalSeconds = Duration.between(entryTime, exitTime).toSeconds();
            preTotal = calcBaseFeeOnly(totalSeconds, freeTimeSec, basicTimeSec, basicCharge, extraTimeSec, extraCharge);
            if (preTotal > maxCharge) preTotal = maxCharge;
        } else {
            // 첫날: 자정까지 남은 시간(초)
            long day1Seconds = (long)(1440 - (entryTime.getHour() * 60 + entryTime.getMinute())) * 60;
            int day1Charge = Math.min(
                    calcBaseFeeOnly(day1Seconds, freeTimeSec, basicTimeSec, basicCharge, extraTimeSec, extraCharge),
                    maxCharge);
            // 중간 날짜
            int middleCharge = (int)(diffDays - 1) * maxCharge;
            // 마지막날: 자정부터 출차까지(초)
            long lastDaySeconds = (long)(exitTime.getHour() * 60 + exitTime.getMinute()) * 60;
            int lastDayCharge = Math.min(
                    calcBaseFeeOnly(lastDaySeconds, freeTimeSec, basicTimeSec, basicCharge, extraTimeSec, extraCharge),
                    maxCharge);
            preTotal = day1Charge + middleCharge + lastDayCharge;
        }

        // 영수증 표시용 base/extra 분리
        int base  = preTotal == 0 ? 0 : basicCharge;
        int extra = Math.max(0, preTotal - base);

        // 할인 계산
        int discountAmount = 0;
        String discountName = "";
        MembersVO member = membersDAO.selectValidMember(carNum);
        boolean isMember = member != null;

        if (isMember) {
            discountAmount = preTotal;
            discountName   = "월정액 회원 할인 (100%)";
            base  = 0;
            extra = 0;
        } else if ("장애인".equals(carType)) {
            discountAmount = (int)(preTotal * disabledDiscount);
            discountName   = "장애인 할인 (" + (int)(disabledDiscount * 100) + "%)";
        } else if ("경차".equals(carType)) {
            discountAmount = (int)(preTotal * smallCarDiscount);
            discountName   = "경차 할인 (" + (int)(smallCarDiscount * 100) + "%)";
        }

        int finalCharge = preTotal - discountAmount;
        long totalMinutes = Duration.between(entryTime, exitTime).toMinutes();

        return PaymentHistoryDTO.builder()
                .carNum(carNum)
                .entryTime(entryTime)
                .exitTime(exitTime)
                .totalMinutes(totalMinutes)
                .totalCharge(preTotal)
                .baseCharge(base)
                .extraCharge(extra)
                .discountAmount(discountAmount)
                .discountName(discountName)
                .finalCharge(finalCharge)
                .build();
    }

    // 최종 결제 금액 계산 후 DB insert, 결과 반환 (이중 호출 방지)
    public PaymentHistoryDTO calculateFinalCharge(String carNum) {
        PaymentInfoVO paymentInfoVO = paymentInfoDAO.selectInfo();
        ParkingHistoryVO recent = parkingHistoryDAO.selectRecentParking(carNum);

        if (recent == null || recent.getCarNum() == null) {
            log.info("차량 없음으로 return");
            return null;
        }
        log.info("calculateFinalCharge 시작 - carNum: {}", carNum);

        PaymentHistoryDTO result = calcCharge(carNum);
        if (result == null) {
            log.error("요금 계산 실패 carNum={}", carNum);
            return null;
        }

        MembersVO membersVO = membersDAO.selectOneMember(carNum);
        Long mno = membersVO == null ? null : membersVO.getMno();

        PaymentHistoryVO paymentHistoryVO = PaymentHistoryVO.builder()
                .parkingArea(recent.getParkingArea())
                .carNum(carNum)
                .entryTime(result.getEntryTime())
                .exitTime(result.getExitTime())
                .totalMinutes(result.getTotalMinutes())
                .totalCharge(result.getTotalCharge())
                .mno(mno)
                .pno(paymentInfoVO.getPno())
                .parkNo(recent.getParkNo())
                .discountAmount(result.getDiscountAmount())
                .finalCharge(result.getFinalCharge())
                .isPaid(true)
                .build();
        paymentHistoryDAO.insertPaymentHistory(paymentHistoryVO);
        log.info("insert 완료 carNum={}", carNum);

        return result;
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
