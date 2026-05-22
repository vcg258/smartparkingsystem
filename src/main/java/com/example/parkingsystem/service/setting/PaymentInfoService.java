package com.example.parkingsystem.service.setting;


import com.example.parkingsystem.dao.setting.PaymentInfoDAO;
import com.example.parkingsystem.dto.setting.PaymentInfoDTO;
import com.example.parkingsystem.util.MapperUtil;
import com.example.parkingsystem.vo.setting.PaymentInfoVO;
import lombok.extern.log4j.Log4j2;
import org.modelmapper.ModelMapper;

import java.time.LocalDateTime;

@Log4j2
public enum PaymentInfoService {
    INSTANCE;

    private final PaymentInfoDAO paymentInfoDAO;
    private final ModelMapper modelMapper = MapperUtil.INSTANCE.getInstance();

    PaymentInfoService () {
        paymentInfoDAO = new PaymentInfoDAO();
    }

    // setting 등록
    public void addInfo(PaymentInfoDTO paymentInfoDTO) {
        log.info(paymentInfoDTO);
        PaymentInfoVO paymentInfoVO = modelMapper.map(paymentInfoDTO, PaymentInfoVO.class);
        paymentInfoDAO.insertInfo(paymentInfoVO);
    }

    // setting 조회
    public PaymentInfoDTO getInfo() {
        PaymentInfoVO paymentInfoVO = paymentInfoDAO.selectInfo();
        if (paymentInfoVO == null) {
            log.warn("정책 데이터 없음");
            return null;
        }
        return modelMapper.map(paymentInfoVO, PaymentInfoDTO.class);
    }

    // mainboard 요금 계산용 입차 시간 기준 정책 조회
    public PaymentInfoDTO getInfoByEntryTime(LocalDateTime entryTime) {
        PaymentInfoVO paymentInfoVO = paymentInfoDAO.selectInfoByEntryTime(entryTime);
        if (paymentInfoVO == null) {
            log.warn("입차 시간 기준 정책 없음 entryTime={}", entryTime);
            return null;
        }
        return modelMapper.map(paymentInfoVO, PaymentInfoDTO.class);
    }
}
