package com.example.parkingsystem.dto.member;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class MembersDTO {
    private long mno;               // 회원 번호
    private String carNum;          // 차량 번호
    private String memberName;      // 회원 이름
    private String memberPhone;     // 회원 전화번호
    private LocalDate startDate;    // 이용 시작일
    private LocalDate endDate;      // 이용 만료일
    private int memberCharge;       // 회원권 결제 요금
}
