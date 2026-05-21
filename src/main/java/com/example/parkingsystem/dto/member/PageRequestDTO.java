package com.example.parkingsystem.dto.member;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PageRequestDTO {
    private String searchType;
    private String keyword;
    private String status;
    private int pageNum;
}
