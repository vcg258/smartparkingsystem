package com.example.parkingsystem.dao.setting;


import com.example.parkingsystem.util.ConnectionUtil;
import com.example.parkingsystem.vo.setting.PaymentInfoVO;
import lombok.Cleanup;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class PaymentInfoDAO {

    public List<PaymentInfoVO> selectAllInfo() {
        List<PaymentInfoVO> paymentInfoVOList = new ArrayList<>();
        String sql = "SELECT * from payment_info";

        try {
            @Cleanup Connection connection = ConnectionUtil.INSTANCE.getConnection();
            @Cleanup PreparedStatement preparedStatement = connection.prepareStatement(sql);
            @Cleanup ResultSet resultSet = preparedStatement.executeQuery();

            if (resultSet.next()) {
                PaymentInfoVO paymentInfoVO = PaymentInfoVO.builder()
                        .pno(resultSet.getInt("pno"))
                        .freeTime(resultSet.getInt("free_time"))
                        .basicTime(resultSet.getInt("basic_time"))
                        .extraTime(resultSet.getInt("extra_time"))
                        .basicCharge(resultSet.getInt("basic_charge"))
                        .extraCharge(resultSet.getInt("extra_charge"))
                        .maxCharge(resultSet.getInt("max_charge"))
                        .memberCharge(resultSet.getInt("member_charge"))
                        .smallCarDiscount(resultSet.getDouble("small_car_discount"))
                        .disabledDiscount(resultSet.getDouble("disabled_discount"))
                        .adminId(resultSet.getString("admin_id"))
                        .updatedAt(resultSet.getObject("updated_at", LocalDateTime.class))
                        .build();
                paymentInfoVOList.add(paymentInfoVO);
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return paymentInfoVOList;
    }

    public PaymentInfoVO selectInfo() {
        String sql = "SELECT * from payment_info ORDER BY pno desc limit 1";
        PaymentInfoVO paymentInfoVO = null;

        try {
            @Cleanup Connection connection = ConnectionUtil.INSTANCE.getConnection();
            @Cleanup PreparedStatement preparedStatement = connection.prepareStatement(sql);
            @Cleanup ResultSet resultSet = preparedStatement.executeQuery();

            if (resultSet.next()) {
                paymentInfoVO = PaymentInfoVO.builder()
                        .pno(resultSet.getInt("pno"))
                        .freeTime(resultSet.getInt("free_time"))
                        .basicTime(resultSet.getInt("basic_time"))
                        .extraTime(resultSet.getInt("extra_time"))
                        .basicCharge(resultSet.getInt("basic_charge"))
                        .extraCharge(resultSet.getInt("extra_charge"))
                        .maxCharge(resultSet.getInt("max_charge"))
                        .memberCharge(resultSet.getInt("member_charge"))
                        .smallCarDiscount(resultSet.getDouble("small_car_discount"))
                        .disabledDiscount(resultSet.getDouble("disabled_discount"))
                        .adminId(resultSet.getString("admin_id"))
                        .updatedAt(resultSet.getObject("updated_at", LocalDateTime.class))
                        .build();
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return paymentInfoVO;
    }

    public void insertInfo(PaymentInfoVO paymentInfoVO) {
        String sql = "insert into payment_info (free_time, basic_time, extra_time, basic_charge, extra_charge, max_charge, member_charge, small_car_discount, disabled_discount, admin_id, updated_at) values (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, now())";

        try {
            @Cleanup Connection connection = ConnectionUtil.INSTANCE.getConnection();
            @Cleanup PreparedStatement preparedStatement = connection.prepareStatement(sql);
            preparedStatement.setInt(1, paymentInfoVO.getFreeTime());
            preparedStatement.setInt(2, paymentInfoVO.getBasicTime());
            preparedStatement.setInt(3, paymentInfoVO.getExtraTime());
            preparedStatement.setInt(4, paymentInfoVO.getBasicCharge());
            preparedStatement.setInt(5, paymentInfoVO.getExtraCharge());
            preparedStatement.setInt(6, paymentInfoVO.getMaxCharge());
            preparedStatement.setInt(7, paymentInfoVO.getMemberCharge());
            preparedStatement.setDouble(8, paymentInfoVO.getSmallCarDiscount());
            preparedStatement.setDouble(9, paymentInfoVO.getDisabledDiscount());
            preparedStatement.setString(10, paymentInfoVO.getAdminId());
            preparedStatement.executeUpdate();

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public PaymentInfoVO selectInfoByEntryTime(LocalDateTime entryTime) {
        String sql = "SELECT * from payment_info WHERE updated_at <= ? ORDER BY updated_at DESC LIMIT 1";
        PaymentInfoVO paymentInfoVO = null;

        try {
            @Cleanup Connection connection = ConnectionUtil.INSTANCE.getConnection();
            @Cleanup PreparedStatement preparedStatement = connection.prepareStatement(sql);
            preparedStatement.setTimestamp(1, Timestamp.valueOf(entryTime));
            @Cleanup ResultSet resultSet = preparedStatement.executeQuery();

            if (resultSet.next()) {
                paymentInfoVO = PaymentInfoVO.builder()
                        .pno(resultSet.getInt("pno"))
                        .freeTime(resultSet.getInt("free_time"))
                        .basicTime(resultSet.getInt("basic_time"))
                        .extraTime(resultSet.getInt("extra_time"))
                        .basicCharge(resultSet.getInt("basic_charge"))
                        .extraCharge(resultSet.getInt("extra_charge"))
                        .maxCharge(resultSet.getInt("max_charge"))
                        .memberCharge(resultSet.getInt("member_charge"))
                        .smallCarDiscount(resultSet.getDouble("small_car_discount"))
                        .disabledDiscount(resultSet.getDouble("disabled_discount"))
                        .adminId(resultSet.getString("admin_id"))
                        .updatedAt(resultSet.getObject("updated_at", LocalDateTime.class))
                        .build();
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return paymentInfoVO;
    }
}