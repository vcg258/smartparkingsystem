package com.example.parkingsystem.dto.member;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PageResponseDTO {
    List<MembersDTO> membersDTOList;
    int pageNum;
    int totalCount;
    int totalPage;
}
