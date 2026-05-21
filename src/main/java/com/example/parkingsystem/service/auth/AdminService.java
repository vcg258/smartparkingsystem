package com.example.parkingsystem.service.auth;


import com.example.parkingsystem.dao.auth.AdminDAO;
import com.example.parkingsystem.dto.auth.AdminDTO;
import com.example.parkingsystem.util.MapperUtil;
import com.example.parkingsystem.vo.auth.AdminVO;
import org.mindrot.jbcrypt.BCrypt;
import org.modelmapper.ModelMapper;

public enum AdminService {
    INSTANCE;

    private final AdminDAO adminDAO;
    private final ModelMapper modelMapper;

    AdminService() {
        adminDAO = new AdminDAO();
        modelMapper = MapperUtil.INSTANCE.getInstance();
    }

    // 아이디로 하나 조회
    public AdminDTO getAdminById(String adminId) {
        AdminVO adminVO = adminDAO.selectAdminById(adminId);
        if (adminVO == null) {
            return null;
        }
        return modelMapper.map(adminVO, AdminDTO.class);
    }

    // 생성된 쿠키의 uuid와 동일한 uuid 있는지 체크
    public AdminDTO getAdminByUuid(String uuid) {
        AdminVO adminVO = adminDAO.selectUuid(uuid);
        if (adminVO == null) {
            return null;
        }
        return modelMapper.map(adminVO, AdminDTO.class);
    }

    // 로그인 인증 (활동여부 사용중 True, 사용중지 False)
    public boolean AuthenticateAdmin(String adminId, String password) {
        AdminDTO admin = getAdminById(adminId);
        return admin != null && BCrypt.checkpw(password, admin.getPassword());
    }

    // 로그
    public void renewalLog(String adminId, String lastLoginIp) {
        adminDAO.updateLog(adminId, lastLoginIp);
    }

    // 계정 정보 수정
    public void modifyAdmin(AdminDTO adminDTO) {
        AdminVO adminVO = modelMapper.map(adminDTO, AdminVO.class);
        adminDAO.updateAdmin(adminVO);
    }

    // uuid 업데이트
    public void modifyUUID (AdminDTO adminDTO) {
        AdminVO adminVO = modelMapper.map(adminDTO, AdminVO.class);
        adminDAO.updateUUID(adminVO);
    }
}
