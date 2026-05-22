package com.example.parkingsystem.vo.member;

import lombok.*;

import java.time.LocalDate;

@Getter
@ToString
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class MembersVO {
    private long mno;               // 회원 번호
    private String carNum;          // 차량 번호
    private String memberName;      // 회원 이름
    private String memberPhone;     // 회원 전화번호
    private LocalDate startDate;    // 이용 시작일
    private LocalDate endDate;      // 이용 만료일
    private int memberCharge;       // 회원권 결제 요금

    // 회원 여부 확인 메서드
//    public boolean isMember() {
//        if (this.endDate == null) return false;
//        return !this.endDate.isBefore(LocalDate.now());
//    }
}
