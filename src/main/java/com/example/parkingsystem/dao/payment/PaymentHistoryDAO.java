package com.example.parkingsystem.dao.payment;


import com.example.parkingsystem.util.ConnectionUtil;
import com.example.parkingsystem.vo.payment.PaymentHistoryVO;
import lombok.Cleanup;
import lombok.extern.log4j.Log4j2;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

@Log4j2
public class PaymentHistoryDAO {

    public void insertPaymentHistory(PaymentHistoryVO paymentHistoryVO) {
        log.info("insertPaymentHistory: ");
        log.info(paymentHistoryVO.toString());
        String sql = "insert into payment_history (parking_area, car_num, entry_time, exit_time, total_minutes, " +
                "total_charge, mno, pno, park_no, discount_amount, " +
                "final_charge, is_paid, payment_time) values (?, ?, ?, ?, ?, " +
                "?, ?, ?, ?, ?, " +
                "?, ?, now())";

        try {
            @Cleanup Connection connection = ConnectionUtil.INSTANCE.getConnection();
            @Cleanup PreparedStatement preparedStatement = connection.prepareStatement(sql);
            preparedStatement.setString(1, paymentHistoryVO.getParkingArea());
            preparedStatement.setString(2, paymentHistoryVO.getCarNum());
            preparedStatement.setObject(3, paymentHistoryVO.getEntryTime());
            preparedStatement.setObject(4, paymentHistoryVO.getExitTime());
            preparedStatement.setLong(5, paymentHistoryVO.getTotalMinutes());
            preparedStatement.setInt(6, paymentHistoryVO.getTotalCharge());
            preparedStatement.setObject(7, paymentHistoryVO.getMno());
            preparedStatement.setLong(8, paymentHistoryVO.getPno());
            preparedStatement.setLong(9, paymentHistoryVO.getParkNo());
            preparedStatement.setInt(10, paymentHistoryVO.getDiscountAmount());
            preparedStatement.setInt(11, paymentHistoryVO.getFinalCharge());
            preparedStatement.setBoolean(12, paymentHistoryVO.isPaid());
            preparedStatement.executeUpdate();

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public PaymentHistoryVO selectRecentPayment(String carNum) {
        PaymentHistoryVO paymentHistoryVO = null;
        String sql = "SELECT * FROM payment_history WHERE car_num = ?" +
                "ORDER BY entry_time DESC LIMIT 1";

        try {
            @Cleanup Connection connection = ConnectionUtil.INSTANCE.getConnection();
            @Cleanup PreparedStatement preparedStatement = connection.prepareStatement(sql);
            preparedStatement.setString(1, carNum);
            @Cleanup ResultSet resultSet = preparedStatement.executeQuery();

            if (resultSet.next()) {
                paymentHistoryVO = PaymentHistoryVO.builder()
                        .payNo(resultSet.getLong("pay_no"))
                        .parkingArea(resultSet.getString("parking_area"))
                        .carNum(resultSet.getString("car_num"))
                        .entryTime(resultSet.getObject("entry_time", LocalDateTime.class))
                        .exitTime(resultSet.getObject("exit_time", LocalDateTime.class))
                        .totalMinutes(resultSet.getInt("total_minutes"))
                        .totalCharge(resultSet.getInt("total_charge"))
                        .mno(resultSet.getLong("mno"))
                        .pno(resultSet.getLong("pno"))
                        .parkNo(resultSet.getLong("park_no"))
                        .discountAmount(resultSet.getInt("discount_amount"))
                        .finalCharge(resultSet.getInt("final_charge"))
                        .isPaid(resultSet.getBoolean("is_paid"))
                        .paymentTime(resultSet.getObject("payment_time", LocalDateTime.class))
                        .build();
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return paymentHistoryVO;
    }

    public Map<Integer, Map<Integer, List<PaymentHistoryVO>>> selectPaidOrderByPaymentYearMonth() {
        // 통계 매출 차트용 결제 완료 이력 조회
        // payment_time이 없으면 entry_time을 보조 기준으로 사용
        Map<Integer, Map<Integer, List<PaymentHistoryVO>>> result = new TreeMap<>(Collections.reverseOrder());

        String sql = "SELECT YEAR(COALESCE(payment_time, entry_time)) as year, " +
                "MONTH(COALESCE(payment_time, entry_time)) as month, " +
                "pay_no, parking_area, car_num, entry_time, exit_time, payment_time, " +
                "total_minutes, final_charge, mno " +
                "FROM payment_history " +
                "WHERE is_paid = TRUE " +
                "ORDER BY year DESC, month DESC";

        try {
            @Cleanup Connection connection = ConnectionUtil.INSTANCE.getConnection();
            @Cleanup PreparedStatement preparedStatement = connection.prepareStatement(sql);
            @Cleanup ResultSet resultSet = preparedStatement.executeQuery();

            while (resultSet.next()) {
                int year = resultSet.getInt("year");
                int month = resultSet.getInt("month");

                result.computeIfAbsent(year, k -> new TreeMap<>(Collections.reverseOrder()))
                        .computeIfAbsent(month, k -> new ArrayList<>())
                        .add(mapStatisticPayment(resultSet));
            }
            return result;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public List<PaymentHistoryVO> selectByDate(LocalDate date) {
        // 일일 현황 매출 계산용 특정 날짜 결제 완료 이력 조회
        // 차트와 같은 기준을 유지하기 위해 payment_time 기준으로 날짜 판단
        List<PaymentHistoryVO> result = new ArrayList<>();
        String sql = "SELECT pay_no, parking_area, car_num, entry_time, exit_time, " +
                "payment_time, total_minutes, final_charge, mno " +
                "FROM payment_history " +
                "WHERE is_paid = TRUE AND DATE(COALESCE(payment_time, entry_time)) = ? " +
                "ORDER BY COALESCE(payment_time, entry_time) DESC";
        try {
            @Cleanup Connection connection = ConnectionUtil.INSTANCE.getConnection();
            @Cleanup PreparedStatement ps = connection.prepareStatement(sql);
            ps.setObject(1, date);
            @Cleanup ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                result.add(mapStatisticPayment(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return result;
    }

    private PaymentHistoryVO mapStatisticPayment(ResultSet rs) throws SQLException {
        // 통계 화면에서 필요한 결제 필드만 PaymentHistoryVO로 매핑
        // 결제 상세 화면의 전체 필드 매핑과 분리해서 통계용 조회 단순화
        return PaymentHistoryVO.builder()
                .payNo(rs.getLong("pay_no"))
                .parkingArea(rs.getString("parking_area"))
                .carNum(rs.getString("car_num"))
                .entryTime(rs.getObject("entry_time", LocalDateTime.class))
                .exitTime(rs.getObject("exit_time", LocalDateTime.class))
                .paymentTime(rs.getObject("payment_time", LocalDateTime.class))
                .totalMinutes(rs.getInt("total_minutes"))
                .finalCharge(rs.getInt("final_charge"))
                .mno(rs.getObject("mno", Long.class))
                .build();
    }

}
