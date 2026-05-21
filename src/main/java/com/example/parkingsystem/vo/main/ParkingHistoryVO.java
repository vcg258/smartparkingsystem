package com.example.parkingsystem.vo.main;

import lombok.*;

import java.time.LocalDateTime;

@Getter
@ToString
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ParkingHistoryVO {
    private Long parkNo;
    private String parkingArea;
    private String carNum;
    private String carType;
    private boolean isMember;
    private LocalDateTime entryTime;
    private LocalDateTime exitTime;
    private int totalMinutes;
}
