package com.example.parkingsystem.dao.auth;


import com.example.parkingsystem.util.ConnectionUtil;
import com.example.parkingsystem.vo.auth.AdminVO;
import lombok.Cleanup;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;

public class AdminDAO {

    // 아이디로 조회
    public AdminVO selectAdminById(String adminId) {
        String sql = "SELECT * FROM admin WHERE admin_id = ?";
        try {
            @Cleanup Connection connection = ConnectionUtil.INSTANCE.getConnection();
            @Cleanup PreparedStatement preparedStatement = connection.prepareStatement(sql);
            preparedStatement.setString(1, adminId);
            @Cleanup ResultSet resultSet = preparedStatement.executeQuery();
            if (resultSet.next()) {
                return AdminVO.builder()
                        .adminId(resultSet.getString("admin_id"))
                        .password(resultSet.getString("password"))
                        .adminName(resultSet.getString("admin_name"))
                        .adminEmail(resultSet.getString("admin_email"))
                        .isActive(resultSet.getBoolean("is_active"))
                        .lastLogin(resultSet.getObject("last_login", LocalDateTime.class))
                        .lastLoginIp(resultSet.getString("last_login_ip"))
                        .isPasswordReset(resultSet.getBoolean("is_password_reset"))
                        .uuid(resultSet.getString("uuid"))
                        .createdAt(resultSet.getObject("created_at", LocalDateTime.class))
                        .build();
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return null;
    }

    // 정보 수정
    public void updateAdmin(AdminVO adminVO) {
        String sql = "UPDATE admin SET password = ?, admin_email = ?, is_password_reset = ? WHERE admin_id = ?";
        try {
            @Cleanup Connection connection = ConnectionUtil.INSTANCE.getConnection();
            @Cleanup PreparedStatement preparedStatement = connection.prepareStatement(sql);
            preparedStatement.setString(1, adminVO.getPassword());
            preparedStatement.setString(2, adminVO.getAdminEmail());
            preparedStatement.setBoolean(3, adminVO.isPasswordReset());
            preparedStatement.setString(4, adminVO.getAdminId());
            preparedStatement.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    // TODO 로그인 상태유지 UUID 추가
    public void updateUUID(AdminVO adminVO) {
        String sql = "UPDATE admin SET uuid = ? WHERE admin_id = ?";
        try {
            @Cleanup Connection connection = ConnectionUtil.INSTANCE.getConnection();
            @Cleanup PreparedStatement preparedStatement = connection.prepareStatement(sql);
            preparedStatement.setString(1, adminVO.getUuid());
            preparedStatement.setString(2, adminVO.getAdminId());
            preparedStatement.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public AdminVO selectUuid(String uuid) {
        String sql = "SELECT admin_id, uuid FROM admin WHERE uuid = ?";
        try {
            @Cleanup Connection connection = ConnectionUtil.INSTANCE.getConnection();
            @Cleanup PreparedStatement preparedStatement = connection.prepareStatement(sql);
            preparedStatement.setString(1, uuid);
            @Cleanup ResultSet resultSet = preparedStatement.executeQuery();
            if (resultSet.next()) {
                return AdminVO.builder()
                        .adminId(resultSet.getString("admin_id"))
                        .uuid(resultSet.getString("uuid"))
                        .build();
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return null;
    }

    // 로그 추가
    public void updateLog(String adminId, String lastLoginIp) {
        String sql = "UPDATE admin SET last_login = ?, last_login_ip = ? WHERE admin_id = ?";
        try {
            @Cleanup Connection connection = ConnectionUtil.INSTANCE.getConnection();
            @Cleanup PreparedStatement preparedStatement = connection.prepareStatement(sql);
            preparedStatement.setObject(1, LocalDateTime.now());
            preparedStatement.setString(2, lastLoginIp);
            preparedStatement.setString(3, adminId);
            preparedStatement.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

}
