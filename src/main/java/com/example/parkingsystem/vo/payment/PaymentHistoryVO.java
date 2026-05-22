package com.example.parkingsystem.vo.payment;

import lombok.*;

import java.time.LocalDateTime;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
@Getter
public class PaymentHistoryVO {
    private long payNo; // 요금 기록 인덱스
    private String parkingArea; // 주차 구역 (A1 ~ A20)
    private String carNum; // 차량번호
    private LocalDateTime entryTime; // 입차 시간
    private LocalDateTime exitTime; // 출차 시간
    private long totalMinutes; // 총 주차 시간
    private int totalCharge; // 총 요금
    private Long mno; // 회원 번호
    private long pno; // 요금 정책 번호
    private long parkNo; // 주차 기록 번호
    private int discountAmount; // 할인 금액
    private int finalCharge; // 결제 요금
    private boolean isPaid; // 결제 여부
    private LocalDateTime paymentTime; // 결제 시간
}
