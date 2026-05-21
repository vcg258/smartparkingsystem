package com.example.parkingsystem.service.statistic;

import com.example.parkingsystem.dao.main.ParkingHistoryDAO;
import com.example.parkingsystem.dao.member.MembersDAO;
import com.example.parkingsystem.dao.payment.PaymentHistoryDAO;
import com.example.parkingsystem.vo.main.ParkingHistoryVO;
import com.example.parkingsystem.vo.member.MembersVO;
import com.example.parkingsystem.vo.payment.PaymentHistoryVO;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public enum StatisticService {
    INSTANCE;

    private final PaymentHistoryDAO paymentHistoryDAO;
    private final ParkingHistoryDAO parkingHistoryDAO;
    private final MembersDAO membersDAO;

    StatisticService() {
        paymentHistoryDAO = new PaymentHistoryDAO();
        parkingHistoryDAO = new ParkingHistoryDAO();
        membersDAO = new MembersDAO();
    }

    // 일일 현황 카드에 뿌릴 오늘 데이터 조회
    // 매출은 결제 이력 기준, 입차 수와 누적은 주차 이력 기준
    public Map<String, Object> getTodaySummary() {
        LocalDate today = LocalDate.now();
        Map<String, Object> summary = new HashMap<>();
        summary.put("dailySales", sumFinalCharge(paymentHistoryDAO.selectByDate(today)));
        summary.put("dailyCount", parkingHistoryDAO.selectByDate(today).size());
        summary.put("totalCount", parkingHistoryDAO.getTotalCount());
        return summary;
    }

    // 회원 통계 차트용 데이터 계산
    // 활성 회원은 조회 기간과 회원권 기간이 하루라도 겹치면 포함
    public Map<String, Object> getMemberStats(int year, Integer month) {
        LocalDate startDate = month == null ? LocalDate.of(year, 1, 1) : LocalDate.of(year, month, 1);
        LocalDate endDate = month == null ? LocalDate.of(year, 12, 31) : startDate.withDayOfMonth(startDate.lengthOfMonth());

        List<MembersVO> members = membersDAO.selectAllMembers();
        int activeCount = 0;
        for (MembersVO member : members) {
            if (isActiveInPeriod(member, startDate, endDate)) {
                activeCount++;
            }
        }

        Map<String, Object> result = new HashMap<>();
        result.put("totalCount", members.size());
        result.put("activeCount", activeCount);
        result.put("inactiveCount", members.size() - activeCount);
        result.put("nonMemberUsageCount", parkingHistoryDAO.getNonMemberCountByPeriod(startDate, endDate));
        return result;
    }

    // 매출 차트용 데이터 조회
    // month가 없으면 월별 매출, 있으면 해당 월의 일별 매출 반환
    public Map<String, Object> getMonthlySales(int year, Integer month, boolean includeMembership) {
        Map<Integer, List<PaymentHistoryVO>> yearPayments = getPaymentYearMap(year);
        if (yearPayments == null) {
            return new HashMap<>();
        }

        return month == null
                ? buildMonthlySales(year, yearPayments, includeMembership)
                : buildDailySales(year, month, yearPayments, includeMembership);
    }

    // 누적 매출 차트용 데이터 조회
    // 일반 매출과 회원권 매출을 따로 누적해서 차트에 표시
    public Map<String, Object> getCumulativeSales(int year, Integer month, boolean includeMembership) {
        Map<Integer, List<PaymentHistoryVO>> yearPayments = getPaymentYearMap(year);
        if (yearPayments == null) {
            return new HashMap<>();
        }

        Map<String, Object> result = month == null
                ? buildMonthlyCumulative(year, yearPayments, includeMembership)
                : buildDailyCumulative(year, month, yearPayments, includeMembership);
        result.put("title", month == null ? year + "년 누적 매출 현황" : month + "월 일별 누적 매출 현황");
        return result;
    }

    // 차종별 파이 차트용 데이터 계산
    // 주차 이력의 carType 값을 기준으로 차종별 건수 집계
    public Map<String, Object> getCarTypeStats(int year, Integer month) {
        Map<String, Integer> countMap = new HashMap<>();
        Map<Integer, List<ParkingHistoryVO>> yearParkings = getParkingYearMap(year);

        if (yearParkings != null) {
            for (Map.Entry<Integer, List<ParkingHistoryVO>> entry : yearParkings.entrySet()) {
                if (month != null && !entry.getKey().equals(month)) {
                    continue;
                }
                for (ParkingHistoryVO parking : entry.getValue()) {
                    countMap.put(parking.getCarType(), countMap.getOrDefault(parking.getCarType(), 0) + 1);
                }
            }
        }

        int total = countMap.values().stream().mapToInt(Integer::intValue).sum();
        List<Map<String, Object>> data = new ArrayList<>();
        for (Map.Entry<String, Integer> entry : countMap.entrySet()) {
            Map<String, Object> item = new HashMap<>();
            item.put("name", entry.getKey());
            item.put("y", entry.getValue());
            item.put("percentage", total == 0 ? "0.0" : String.format("%.1f", entry.getValue() * 100.0 / total));
            data.add(item);
        }

        Map<String, Object> result = new HashMap<>();
        result.put("data", data);
        result.put("total", total);
        return result;
    }

    // 피크 시간대 차트용 데이터 계산
    // 입차 시간을 기준으로 0시부터 23시까지 입차 수 집계
    public Map<String, Object> getPeakTimeStats(int year, Integer month) {
        int[] hourlyCount = new int[24];
        Map<Integer, List<ParkingHistoryVO>> yearParkings = getParkingYearMap(year);

        if (yearParkings != null) {
            for (Map.Entry<Integer, List<ParkingHistoryVO>> entry : yearParkings.entrySet()) {
                if (month != null && !entry.getKey().equals(month)) {
                    continue;
                }
                for (ParkingHistoryVO parking : entry.getValue()) {
                    hourlyCount[parking.getEntryTime().getHour()]++;
                }
            }
        }

        Map<String, Object> result = new HashMap<>();
        result.put("categories", hourCategories());
        result.put("hourlyCount", toList(hourlyCount));
        return result;
    }

    // 결제 완료 이력을 결제일 기준 연도와 월로 조회
    // 통계 매출은 입차일이 아니라 결제일 기준으로 계산
    private Map<Integer, List<PaymentHistoryVO>> getPaymentYearMap(int year) {
        return paymentHistoryDAO.selectPaidOrderByPaymentYearMonth().get(year);
    }

    // 주차 이력을 입차일 기준 연도와 월로 조회
    // 차종 통계와 피크 시간대는 주차 이력 기준으로 계산
    private Map<Integer, List<ParkingHistoryVO>> getParkingYearMap(int year) {
        return parkingHistoryDAO.selectAllByYearMonth().get(year);
    }

    // 선택 연도의 월별 매출 응답 생성
    // 회원권 포함 시 회원권 시작일 기준 매출도 같이 계산
    private Map<String, Object> buildMonthlySales(int year, Map<Integer, List<PaymentHistoryVO>> yearPayments,
                                                  boolean includeMembership) {
        List<Integer> months = sortedKeys(yearPayments);
        Map<Integer, Integer> memberRevenue = includeMembership ? membershipRevenueByMonth(year) : new HashMap<>();

        List<String> categories = new ArrayList<>();
        List<Integer> normalSales = new ArrayList<>();
        List<Integer> memberSales = new ArrayList<>();

        for (int month : months) {
            categories.add(month + "월");
            normalSales.add(sumFinalCharge(yearPayments.get(month)));
            memberSales.add(memberRevenue.getOrDefault(month, 0));
        }

        return salesResponse(categories, normalSales, memberSales, includeMembership);
    }

    // 선택 월의 일별 매출 응답 생성
    // payment_time이 있으면 결제일 기준으로 일자 계산
    private Map<String, Object> buildDailySales(int year, int month, Map<Integer, List<PaymentHistoryVO>> yearPayments,
                                                boolean includeMembership) {
        int daysInMonth = LocalDate.of(year, month, 1).lengthOfMonth();
        int[] normalSales = new int[daysInMonth];
        int[] memberSales = new int[daysInMonth];
        List<PaymentHistoryVO> payments = yearPayments.get(month);
        if (payments == null) {
            return new HashMap<>();
        }

        fillDailySales(year, month, payments, includeMembership, normalSales, memberSales);

        return salesResponse(dayCategories(daysInMonth), toList(normalSales), toList(memberSales), includeMembership);
    }

    // 연간 누적 매출 응답 생성
    // 월별 매출을 월 순서대로 누적
    private Map<String, Object> buildMonthlyCumulative(int year, Map<Integer, List<PaymentHistoryVO>> yearPayments,
                                                       boolean includeMembership) {
        List<Integer> months = sortedKeys(yearPayments);
        Map<Integer, Integer> memberRevenue = includeMembership ? membershipRevenueByMonth(year) : new HashMap<>();
        List<String> categories = new ArrayList<>();
        List<Integer> cumulativeNormal = new ArrayList<>();
        List<Integer> cumulativeMember = new ArrayList<>();
        int normalTotal = 0;
        int memberTotal = 0;

        for (int month : months) {
            categories.add(month + "월");
            normalTotal += sumFinalCharge(yearPayments.get(month));
            memberTotal += memberRevenue.getOrDefault(month, 0);
            cumulativeNormal.add(normalTotal);
            cumulativeMember.add(memberTotal);
        }

        return cumulativeResponse(categories, cumulativeNormal, cumulativeMember, includeMembership);
    }

    // 특정 월 누적 매출 응답 생성
    // 일별 매출을 일자 순서대로 누적
    private Map<String, Object> buildDailyCumulative(int year, int month, Map<Integer, List<PaymentHistoryVO>> yearPayments,
                                                     boolean includeMembership) {
        List<PaymentHistoryVO> payments = yearPayments.get(month);
        if (payments == null) {
            return new HashMap<>();
        }

        int daysInMonth = LocalDate.of(year, month, 1).lengthOfMonth();
        int[] normalSales = new int[daysInMonth];
        int[] memberSales = new int[daysInMonth];
        fillDailySales(year, month, payments, includeMembership, normalSales, memberSales);

        return cumulativeResponse(dayCategories(daysInMonth),
                cumulative(toList(normalSales)), cumulative(toList(memberSales)), includeMembership);
    }

    // 회원권 매출을 회원권 시작 월 기준으로 합산
    // 결제 이력이 아니라 members 테이블의 memberCharge 사용
    private Map<Integer, Integer> membershipRevenueByMonth(int year) {
        Map<Integer, Integer> result = new HashMap<>();
        for (MembersVO member : membersDAO.selectAllMembers()) {
            if (member.getStartDate().getYear() == year) {
                int month = member.getStartDate().getMonthValue();
                result.put(month, result.getOrDefault(month, 0) + calculateMembershipRevenue(member));
            }
        }
        return result;
    }

    // 특정 월의 회원권 매출을 시작일 기준 일자 배열에 더함
    private void fillDailyMembershipRevenue(int year, int month, int[] dailyMember) {
        for (MembersVO member : membersDAO.selectAllMembers()) {
            LocalDate startDate = member.getStartDate();
            if (startDate.getYear() == year && startDate.getMonthValue() == month) {
                dailyMember[startDate.getDayOfMonth() - 1] += calculateMembershipRevenue(member);
            }
        }
    }

    // 결제 매출과 회원권 매출을 일별 배열에 채움
    private void fillDailySales(int year, int month, List<PaymentHistoryVO> payments, boolean includeMembership,
                                int[] normalSales, int[] memberSales) {
        if (includeMembership) {
            fillDailyMembershipRevenue(year, month, memberSales);
        }

        for (PaymentHistoryVO payment : payments) {
            int day = paymentDateTime(payment).getDayOfMonth();
            normalSales[day - 1] += payment.getFinalCharge();
        }
    }

    // 회원권 기간을 30일 단위로 계산해서 회원권 매출 산출
    private int calculateMembershipRevenue(MembersVO member) {
        long days = ChronoUnit.DAYS.between(member.getStartDate(), member.getEndDate());
        int periods = (int) Math.ceil(days / 30.0);
        return periods * member.getMemberCharge();
    }

    // 회원권 기간이 조회 기간과 겹치면 활성 회원으로 판단
    private boolean isActiveInPeriod(MembersVO member, LocalDate startDate, LocalDate endDate) {
        return !member.getStartDate().isAfter(endDate) && !member.getEndDate().isBefore(startDate);
    }

    // 결제 이력의 최종 결제 금액 합산
    private int sumFinalCharge(List<PaymentHistoryVO> payments) {
        int sum = 0;
        for (PaymentHistoryVO payment : payments) {
            sum += payment.getFinalCharge();
        }
        return sum;
    }

    // 매출 통계 기준일 결정
    // 결제 시간이 없으면 입차 시간을 보조 기준으로 사용
    private LocalDateTime paymentDateTime(PaymentHistoryVO payment) {
        return payment.getPaymentTime() == null ? payment.getEntryTime() : payment.getPaymentTime();
    }

    // 월별 데이터가 시간 순서대로 보이도록 월 키 정렬
    private List<Integer> sortedKeys(Map<Integer, ?> map) {
        List<Integer> keys = new ArrayList<>(map.keySet());
        Collections.sort(keys);
        return keys;
    }

    // 일별 차트 x축 라벨 생성
    private List<String> dayCategories(int daysInMonth) {
        List<String> categories = new ArrayList<>();
        for (int day = 1; day <= daysInMonth; day++) {
            categories.add(day + "일");
        }
        return categories;
    }

    // 시간대별 차트 x축 라벨 생성
    private List<String> hourCategories() {
        List<String> categories = new ArrayList<>();
        for (int hour = 0; hour < 24; hour++) {
            categories.add(hour + "시");
        }
        return categories;
    }

    // 매출 API 공통 응답 형식 생성
    private Map<String, Object> salesResponse(List<String> categories, List<Integer> normalSales,
                                              List<Integer> memberSales, boolean includeMembership) {
        Map<String, Object> result = new HashMap<>();
        result.put("categories", categories);
        result.put("normalSales", normalSales);
        result.put("memberSales", memberSales);
        result.put("includeMembership", includeMembership);
        return result;
    }

    // 누적 매출 API 응답 형식 생성
    private Map<String, Object> cumulativeResponse(List<String> categories, List<Integer> cumulativeNormal,
                                                   List<Integer> cumulativeMember, boolean includeMembership) {
        Map<String, Object> result = new HashMap<>();
        result.put("categories", categories);
        result.put("cumulativeNormal", cumulativeNormal);
        result.put("cumulativeMember", cumulativeMember);
        result.put("includeMembership", includeMembership);
        return result;
    }

    // 일반 리스트 값을 누적 합계 리스트로 변환
    private List<Integer> cumulative(List<Integer> values) {
        List<Integer> result = new ArrayList<>();
        int runningTotal = 0;
        for (int value : values) {
            runningTotal += value;
            result.add(runningTotal);
        }
        return result;
    }

    // 배열 집계 결과를 JSON 응답용 List로 변환
    private List<Integer> toList(int[] values) {
        List<Integer> result = new ArrayList<>();
        for (int value : values) {
            result.add(value);
        }
        return result;
    }

}
