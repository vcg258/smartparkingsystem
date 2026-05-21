package com.example.parkingsystem.vo.setting;

import lombok.*;

import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
@NoArgsConstructor
@ToString
@Builder
public class PaymentInfoVO {
    private int pno; // 인덱스
    private int freeTime; // 무료 회차 시간
    private int basicTime; // 기본 시간
    private int extraTime; // 초과 시간
    private int basicCharge; // 기본 요금
    private int extraCharge; // 초과 시간 당 추가 요금
    private int maxCharge; // 일일 최대 요금
    private int memberCharge; // 회원권 결제 요금
    private double smallCarDiscount; // 경차 할인율
    private double disabledDiscount; // 장애인 할인율
    private String adminId; //정책 수정한 관리자 아이디
    private LocalDateTime updatedAt; // 관리자 정책 수정일
}
