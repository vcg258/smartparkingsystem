package com.example.parkingsystem.dao.auth;


import com.example.parkingsystem.util.ConnectionUtil;
import com.example.parkingsystem.vo.auth.ValidationVO;
import lombok.Cleanup;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;

public class ValidationDAO {

    // OTP 기록 추가
    public void logOTP(ValidationVO validationVO) {
        String sql = "INSERT INTO validation (admin_id, otp_code, admin_email, expired_time) VALUE (?, ?, ?, ?)";
        try {
            @Cleanup Connection connection = ConnectionUtil.INSTANCE.getConnection();
            @Cleanup PreparedStatement preparedStatement = connection.prepareStatement(sql);
            preparedStatement.setString(1, validationVO.getAdminId());
            preparedStatement.setString(2, validationVO.getOtpCode());
            preparedStatement.setString(3, validationVO.getAdminEmail());
            preparedStatement.setObject(4, validationVO.getExpiredTime().plusMinutes(4));
            preparedStatement.executeUpdate();

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    // 관리자 아이디로 가장 최근에 추가한 OTP조회
    public ValidationVO selectOTPOne(String adminId) {
        String sql = "SELECT * FROM validation WHERE admin_id = ? ORDER BY no DESC LIMIT 1";

        try {
            @Cleanup Connection connection = ConnectionUtil.INSTANCE.getConnection();
            @Cleanup PreparedStatement preparedStatement = connection.prepareStatement(sql);
            preparedStatement.setString(1, adminId);
            @Cleanup ResultSet resultSet = preparedStatement.executeQuery();
            if (resultSet.next()) {
                return ValidationVO.builder()
                        .no(resultSet.getString("no"))
                        .adminId(resultSet.getString("admin_id"))
                        .otpCode(resultSet.getString("otp_code"))
                        .adminEmail(resultSet.getString("admin_email"))
                        .expiredTime(resultSet.getObject("expired_time", LocalDateTime.class))
                        .build();
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        } return null;
    }
}
