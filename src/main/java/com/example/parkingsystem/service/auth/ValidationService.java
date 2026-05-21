package com.example.parkingsystem.service.auth;


import com.example.parkingsystem.dao.auth.ValidationDAO;
import com.example.parkingsystem.dto.auth.AdminDTO;
import com.example.parkingsystem.dto.auth.ValidationDTO;
import com.example.parkingsystem.util.MapperUtil;
import com.example.parkingsystem.vo.auth.ValidationVO;
import lombok.extern.log4j.Log4j2;
import org.mindrot.jbcrypt.BCrypt;
import org.modelmapper.ModelMapper;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.UUID;

@Log4j2
public enum ValidationService {
    INSTANCE;

    private final ValidationDAO validationDAO;
    private final AdminService adminService;
    private final MailService mailService;
    private final ModelMapper modelMapper;
    private final SecureRandom random; // 랜덤값 생성 변경(시큐리티)

    ValidationService() {
        validationDAO = new ValidationDAO();
        adminService = AdminService.INSTANCE;
        modelMapper = MapperUtil.INSTANCE.getInstance();
        mailService = MailService.INSTANCE;
        random = new SecureRandom();
    }

    // 시큐리티 랜덤값 사용 (앞자리 0 포함 6자리)
    public String randomOTP() {
        int otpCode = random.nextInt(1000000);
        return String.format("%06d", otpCode);
    }

    // OTP 발송
    public void otpShipment(String adminId) {
        // 발송할 이메일
        adminId = adminId.trim(); // 공백 제거
        String adminEmail = adminService.getAdminById(adminId).getAdminEmail();
        String otpCode = randomOTP();
        log.info("OTP : {}", otpCode);

        if (adminId.equals("demo")) { // 포트폴리오용
            log.info("[ 테스트 ] 데모계정 OTP: 123456");
            otpCode = "123456";
            ValidationVO validationVO = ValidationVO.builder()
                    .adminId(adminId)
                    .otpCode(otpCode)
                    .adminEmail(adminEmail)
                    .expiredTime(LocalDateTime.now())
                    .build();
            validationDAO.logOTP(validationVO);
            return;
        }

        mailService.sendAuthEmail(adminEmail, otpCode);
        ValidationVO validationVO = ValidationVO.builder()
                .adminId(adminId)
                .otpCode(otpCode)
                .adminEmail(adminEmail)
                .expiredTime(LocalDateTime.now())
                .build();
        validationDAO.logOTP(validationVO);
    }

    // 재설정 비밀번호 발송
    public void uuidPassword(String adminId) {
        // 발송할 이메일
        String adminEmail = adminService.getAdminById(adminId).getAdminEmail();
        String uuid = UUID.randomUUID().toString().replace("-", "").substring(0, 12);
        String BCryptUuid = BCrypt.hashpw(uuid, BCrypt.gensalt(12));
        log.info("UUID : {}", uuid);

        // TODO 테스트할때는 수정
        mailService.sendAuthPw(adminEmail, uuid);
        AdminDTO adminDTO = AdminDTO.builder()
                .adminId(adminId)
                .adminEmail(adminEmail)
                .password(BCryptUuid) // 랜덤키도 암호화 하여 DB저장
                .isPasswordReset(true)
                .build();
        adminService.modifyAdmin(adminDTO);

    }

    // 관리자 아이디로 OTP검색
    public ValidationDTO getOTP (String adminId) {
        return modelMapper.map(validationDAO.selectOTPOne(adminId), ValidationDTO.class);
    }
}
