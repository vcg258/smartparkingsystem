package com.example.parkingsystem.dto.main;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ParkingHistoryDTO {
    private Long parkNo;
    private String parkingArea;
    private String carNum;
    private String carType;
    private boolean isMember;
    private LocalDateTime entryTime;
    private LocalDateTime exitTime;
    private int totalMinutes;


//    // 통계용 추가
//    private Integer finalCharge;
}
