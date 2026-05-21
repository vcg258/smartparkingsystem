package com.example.parkingsystem.service.member;


import com.example.parkingsystem.dao.member.MembersDAO;
import com.example.parkingsystem.dao.setting.PaymentInfoDAO;
import com.example.parkingsystem.dto.member.MembersDTO;
import com.example.parkingsystem.dto.member.PageRequestDTO;
import com.example.parkingsystem.dto.member.PageResponseDTO;
import com.example.parkingsystem.util.MapperUtil;
import com.example.parkingsystem.vo.member.MembersVO;
import lombok.extern.log4j.Log4j2;
import org.modelmapper.ModelMapper;

import java.time.LocalDate;
import java.util.List;

@Log4j2
public enum MembersService {
    INSTANCE;

    private final MembersDAO membersDAO;
    private final PaymentInfoDAO paymentInfoDAO;
    private final ModelMapper modelMapper = MapperUtil.INSTANCE.getInstance();
    private final int pagePerCount = 10;

    MembersService() {
        membersDAO = new MembersDAO();
        paymentInfoDAO = new PaymentInfoDAO();
    }

    // 회원 목록 페이징
    public PageResponseDTO getMemberList(PageRequestDTO pageRequestDTO) {
        int pageNum = pageRequestDTO.getPageNum();
        int limit = pagePerCount;

        String searchType = pageRequestDTO.getSearchType();
        String keyword = pageRequestDTO.getKeyword();
        String status = pageRequestDTO.getStatus();

        int totalCount = membersDAO.selectMemberCount(searchType, keyword, status);

        int totalPage = (totalCount == 0) ? 0 : (totalCount + limit - 1) / limit;

        if (totalPage > 0 && pageNum > totalPage) {
            pageNum = totalPage;
        }

        int offset = (pageNum - 1) * limit;

        List<MembersVO> membersVOList = membersDAO.selectMemberList(searchType, keyword, status, offset, limit);

        List<MembersDTO> membersDTOList = membersVOList.stream()
                .map(vo -> modelMapper.map(vo, MembersDTO.class))
                .toList();

        return PageResponseDTO.builder()
                .membersDTOList(membersDTOList)
                .pageNum(pageNum)
                .totalCount(totalCount)
                .totalPage(totalPage)
                .build();
    }

    // 차량 번호로 가장 최근 등록된 회원 정보 조회
    public MembersDTO getMemberOne(String carNum) {
        MembersVO membersVO = membersDAO.selectMemberByCarNum(carNum);

        if (membersVO == null) {
            return null;
        }

        MembersDTO membersDTO = modelMapper.map(membersVO, MembersDTO.class);

        return membersDTO;
    }

    // 차량 번호로 특정 회원 정보 조회
    public MembersDTO getMember(String carNum) {
        MembersVO membersVO = membersDAO.selectValidMember(carNum);

        if (membersVO == null) {
            return null;
        }

        LocalDate now = LocalDate.now();
        if (membersVO.getEndDate().isBefore(now) || membersVO.getStartDate().isAfter(now)) {
            return null;
        }

        return modelMapper.map(membersVO, MembersDTO.class);
    }

    // 신규 회원 등록
    public void addMember(MembersDTO membersDTO) {
        int memberCharge = paymentInfoDAO.selectInfo().getMemberCharge();
        membersDTO.setMemberCharge(memberCharge);
        MembersVO membersVO = modelMapper.map(membersDTO, MembersVO.class);
        membersDAO.insertMember(membersVO);
    }

    // 회원 정보 수정
    public void modifyMember(MembersDTO membersDTO) {
        membersDAO.updateMember(modelMapper.map(membersDTO, MembersVO.class));
    }
}
