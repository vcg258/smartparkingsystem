package com.example.parkingsystem.dao.main;


import com.example.parkingsystem.util.ConnectionUtil;
import com.example.parkingsystem.vo.main.ParkingHistoryVO;
import lombok.Cleanup;
import lombok.extern.log4j.Log4j2;

import java.sql.*;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

@Log4j2
public class ParkingHistoryDAO {
    /* 입차 등록 */
    public void insertEntry(ParkingHistoryVO parkingHistoryVO) {
        String sql = "INSERT INTO parking_history (parking_area, car_num, car_type, is_member, entry_time) " +
                "VALUES (?, ?, ?, EXISTS(SELECT 1 FROM members WHERE car_num = ?) , now())";

        try {
            @Cleanup Connection connection = ConnectionUtil.INSTANCE.getConnection();
            @Cleanup PreparedStatement preparedStatement = connection.prepareStatement(sql);
            preparedStatement.setString(1, parkingHistoryVO.getParkingArea());
            preparedStatement.setString(2, parkingHistoryVO.getCarNum());
            preparedStatement.setString(3, parkingHistoryVO.getCarType());
            preparedStatement.setString(4, parkingHistoryVO.getCarNum());
            preparedStatement.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    /* isMember 상태 변경
     * 출차하지 않은 isMember=0 차량이 members 테이블에 등록되었을 때 */
    public void updateIsMember(String carNum) {
//        ParkingHistoryVO dbVO = selectParkingHistory(parkingHistoryVO.getParkNo());
//        if (dbVO == null || dbVO.getEntryTime() == null) {
//            throw new IllegalStateException("입차 기록 없음");
//        }
//
//        if (parkingHistoryVO.getCarNum() == null || parkingHistoryVO.getCarNum().isBlank()) {
//            throw new IllegalArgumentException("차량 번호 없음");
//        }

        String sql = "UPDATE parking_history SET is_member = 1 " +
                "WHERE EXISTS(SELECT 1 FROM members WHERE car_num = ?) AND car_num = ? " +
                "AND is_member = 0 AND exit_time IS NULL";

        try {
            @Cleanup Connection connection = ConnectionUtil.INSTANCE.getConnection();
            @Cleanup PreparedStatement preparedStatement = connection.prepareStatement(sql);
            preparedStatement.setString(1, carNum);
            preparedStatement.setString(2, carNum);
            preparedStatement.executeUpdate();

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public ParkingHistoryVO selectParkingHistory(long parkNo) {
        ParkingHistoryVO parkingHistoryVO = null;
        String sql = "SELECT * FROM parking_history WHERE park_no = ?";

        try {
            @Cleanup Connection connection = ConnectionUtil.INSTANCE.getConnection();
            @Cleanup PreparedStatement preparedStatement = connection.prepareStatement(sql);
            preparedStatement.setLong(1, parkNo);
            @Cleanup ResultSet resultSet = preparedStatement.executeQuery();

            if (resultSet.next()) {
                parkingHistoryVO = ParkingHistoryVO.builder()
                        .parkNo(resultSet.getLong("park_no"))
                        .parkingArea(resultSet.getString("parking_area"))
                        .carNum(resultSet.getString("car_num"))
                        .carType(resultSet.getString("car_type"))
                        .isMember(resultSet.getBoolean("is_member"))
                        .entryTime(resultSet.getObject("entry_time", LocalDateTime.class))
                        .exitTime(resultSet.getObject("exit_time", LocalDateTime.class))
                        .totalMinutes(resultSet.getInt("total_minutes"))
                        .build();
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return parkingHistoryVO;
    }

    /* 현재 주차 중인 구역 조회 */
    public List<ParkingHistoryVO> selectOccupied() {
        List<ParkingHistoryVO> occupiedList = new ArrayList<>();
        String sql = "SELECT * FROM parking_history WHERE exit_time IS NULL";

        try {
            @Cleanup Connection connection = ConnectionUtil.INSTANCE.getConnection();
            @Cleanup PreparedStatement preparedStatement = connection.prepareStatement(sql);
            @Cleanup ResultSet resultSet = preparedStatement.executeQuery();

            while (resultSet.next()) {
                ParkingHistoryVO parkingHistoryVO = ParkingHistoryVO.builder()
                        .parkNo(resultSet.getLong("park_no"))
                        .parkingArea(resultSet.getString("parking_area"))
                        .carNum(resultSet.getString("car_num"))
                        .carType(resultSet.getString("car_type"))
                        .isMember(resultSet.getBoolean("is_member"))
                        .entryTime(resultSet.getObject("entry_time", LocalDateTime.class))
                        .exitTime(resultSet.getObject("exit_time", LocalDateTime.class))
                        .totalMinutes(resultSet.getInt("total_minutes"))
                        .build();
                occupiedList.add(parkingHistoryVO);
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return occupiedList;
    }

    /* 최근 입차 조회 */
    public ParkingHistoryVO selectRecentParking(String carNum) {
        ParkingHistoryVO parkingHistoryVO = null;
        String sql = "SELECT * FROM parking_history WHERE car_num = ? AND exit_time IS NULL " +
                "ORDER BY entry_time DESC LIMIT 1";

        try {
            @Cleanup Connection connection = ConnectionUtil.INSTANCE.getConnection();
            @Cleanup PreparedStatement preparedStatement = connection.prepareStatement(sql);
            preparedStatement.setString(1, carNum);
            @Cleanup ResultSet resultSet = preparedStatement.executeQuery();

            if (resultSet.next()) {
                parkingHistoryVO = ParkingHistoryVO.builder()
                        .parkNo(resultSet.getLong("park_no"))
                        .parkingArea(resultSet.getString("parking_area"))
                        .carNum(resultSet.getString("car_num"))
                        .carType(resultSet.getString("car_type"))
                        .isMember(resultSet.getBoolean("is_member"))
                        .entryTime(resultSet.getObject("entry_time", LocalDateTime.class))
                        .exitTime(resultSet.getObject("exit_time", LocalDateTime.class))
                        .totalMinutes(resultSet.getInt("total_minutes"))
                        .build();
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return parkingHistoryVO;
    }

    /* 출차 등록 */
    public void updateExit(ParkingHistoryVO parkingHistoryVO) {
        ParkingHistoryVO dbVO = selectParkingHistory(parkingHistoryVO.getParkNo());
        if (dbVO == null || dbVO.getEntryTime() == null) {
            throw new IllegalStateException("입차 기록 없음");
        }

        String sql = "UPDATE parking_history SET exit_time = now(), total_minutes = ? WHERE park_no = ?";
        LocalDateTime now = LocalDateTime.now();
        int totalMinutes = (int) Duration.between(dbVO.getEntryTime(), now).toMinutes();

        try {
            @Cleanup Connection connection = ConnectionUtil.INSTANCE.getConnection();
            @Cleanup PreparedStatement preparedStatement = connection.prepareStatement(sql);
            preparedStatement.setInt(1, totalMinutes);
            preparedStatement.setLong(2, parkingHistoryVO.getParkNo());
            preparedStatement.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    /***** 테스트 데이터 입력용 insert *****/
    public void insertTestData(ParkingHistoryVO parkingHistoryVO) {
        String sql = "INSERT INTO parking_history (parking_area, car_num, car_type, is_member, entry_time, exit_time, total_minutes) " +
                "VALUES (?, ?, ?, EXISTS(SELECT 1 FROM members WHERE car_num = ?) , ?, ?, ?)";

        try {
            @Cleanup Connection connection = ConnectionUtil.INSTANCE.getConnection();
            @Cleanup PreparedStatement preparedStatement = connection.prepareStatement(sql);
            preparedStatement.setString(1, parkingHistoryVO.getParkingArea());
            preparedStatement.setString(2, parkingHistoryVO.getCarNum());
            preparedStatement.setString(3, parkingHistoryVO.getCarType());
            preparedStatement.setString(4, parkingHistoryVO.getCarNum());
            preparedStatement.setTimestamp(5, Timestamp.valueOf(parkingHistoryVO.getEntryTime()));
            preparedStatement.setTimestamp(6, Timestamp.valueOf(parkingHistoryVO.getExitTime()));
            preparedStatement.setInt(7, parkingHistoryVO.getTotalMinutes());
            preparedStatement.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    /*
     * 통계용 전체 주차 기록 개수 조회
     */
    public int getTotalCount() {
        String sql = "SELECT COUNT(*) as total FROM parking_history";

        try {
            @Cleanup Connection connection = ConnectionUtil.INSTANCE.getConnection();
            @Cleanup PreparedStatement preparedStatement = connection.prepareStatement(sql);
            @Cleanup ResultSet resultSet = preparedStatement.executeQuery();

            if (resultSet.next()) {
                return resultSet.getInt("total");
            }

            return 0;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    /*
     * 통계용 날짜로 검색
     */
    public List<ParkingHistoryVO> selectByDate(LocalDate date) {
        List<ParkingHistoryVO> ParkingHistoryVOList = new ArrayList<>();
        String sql = "SELECT * FROM parking_history WHERE DATE(entry_time) = ?";

        try {
            @Cleanup Connection connection = ConnectionUtil.INSTANCE.getConnection();
            @Cleanup PreparedStatement preparedStatement = connection.prepareStatement(sql);
            preparedStatement.setDate(1, java.sql.Date.valueOf(date));
            @Cleanup ResultSet resultSet = preparedStatement.executeQuery();

            while (resultSet.next()) {
                ParkingHistoryVO parkingHistoryVO = ParkingHistoryVO.builder()
                        .parkNo(resultSet.getLong("park_no"))
                        .parkingArea(resultSet.getString("parking_area"))
                        .carNum(resultSet.getString("car_num"))
                        .carType(resultSet.getString("car_type"))
                        .isMember(resultSet.getBoolean("is_member"))
                        .entryTime(resultSet.getObject("entry_time", LocalDateTime.class))
                        .exitTime(resultSet.getObject("exit_time", LocalDateTime.class))
                        .totalMinutes(resultSet.getInt("total_minutes"))
                        .build();
                ParkingHistoryVOList.add(parkingHistoryVO);
            }
            return ParkingHistoryVOList;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    /*
     * 비회원 조회 메서드
     */
    public int getNonMemberCountByPeriod(LocalDate startDate, LocalDate endDate) {
        String sql = "SELECT COUNT(DISTINCT car_num) FROM parking_history " +
                "WHERE is_member = FALSE " +
                "AND entry_time >= ? AND entry_time < ?";
        try {
            @Cleanup Connection connection = ConnectionUtil.INSTANCE.getConnection();
            @Cleanup PreparedStatement ps = connection.prepareStatement(sql);
            ps.setDate(1, java.sql.Date.valueOf(startDate));
            ps.setDate(2, java.sql.Date.valueOf(endDate.plusDays(1)));
            @Cleanup ResultSet rs = ps.executeQuery();
            if (rs.next()) return rs.getInt(1);
            return 0;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    /*
     * 통계용 전체 입출차 데이터 연월별 조회
     */
    public Map<Integer, Map<Integer, List<ParkingHistoryVO>>> selectAllByYearMonth() {
        Map<Integer, Map<Integer, List<ParkingHistoryVO>>> result = new TreeMap<>(Collections.reverseOrder());
        String sql = "SELECT * FROM parking_history ORDER BY entry_time DESC";

        try {
            @Cleanup Connection connection = ConnectionUtil.INSTANCE.getConnection();
            @Cleanup PreparedStatement preparedStatement = connection.prepareStatement(sql);
            @Cleanup ResultSet resultSet = preparedStatement.executeQuery();

            while (resultSet.next()) {
                ParkingHistoryVO vo = ParkingHistoryVO.builder()
                        .parkNo(resultSet.getLong("park_no"))
                        .parkingArea(resultSet.getString("parking_area"))
                        .carNum(resultSet.getString("car_num"))
                        .carType(resultSet.getString("car_type"))
                        .isMember(resultSet.getBoolean("is_member"))
                        .entryTime(resultSet.getObject("entry_time", LocalDateTime.class))
                        .exitTime(resultSet.getObject("exit_time", LocalDateTime.class))
                        .totalMinutes(resultSet.getInt("total_minutes"))
                        .build();

                int year = vo.getEntryTime().getYear();
                int month = vo.getEntryTime().getMonthValue();

                result.computeIfAbsent(year, k -> new TreeMap<>(Collections.reverseOrder()))
                        .computeIfAbsent(month, k -> new ArrayList<>())
                        .add(vo);
            }
            return result;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

}
