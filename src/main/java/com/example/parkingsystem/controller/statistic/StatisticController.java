package com.example.parkingsystem.controller.statistic;

import com.example.parkingsystem.service.statistic.StatisticService;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.log4j.Log4j2;

import java.io.IOException;
import java.time.LocalDate;
import java.util.Map;

@Log4j2
@WebServlet(name = "statisticController", value = "/statistic/*")
public class StatisticController extends HttpServlet {

    private final StatisticService statisticService = StatisticService.INSTANCE;
    private final ObjectMapper objectMapper = new ObjectMapper();

    // /api/ 요청이면 JSON 응답, 아니면 통계 화면 출력
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String pathInfo = request.getPathInfo();

        if (pathInfo != null && pathInfo.startsWith("/api/")) {
            handleApiRequest(request, response, pathInfo);
            return;
        }

        handlePageRequest(request, response);
    }

    // 통계 화면 처음 들어올 때 오늘 현황 데이터 넣어서 JSP로 넘김
    // 차트 데이터는 statistic.js에서 AJAX로 따로 가져옴
    private void handlePageRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        try {
            request.setAttribute("todaySummary", statisticService.getTodaySummary());
            request.getRequestDispatcher("/WEB-INF/statistic/statistic.jsp").forward(request, response);
        } catch (Exception e) {
            log.error("통계 페이지 로드 실패", e);
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "통계 페이지를 불러오는 중 오류가 발생했습니다.");
        }
    }

    // 차트/요약 카드에서 쓰는 JSON API 처리
    // 통계는 항상 최신 값을 봐야 하니까 캐시 못 쓰게 막아둠
    private void handleApiRequest(HttpServletRequest request, HttpServletResponse response, String pathInfo)
            throws IOException {
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        response.setHeader("Cache-Control", "no-cache, no-store, must-revalidate");
        response.setHeader("Pragma", "no-cache");
        response.setDateHeader("Expires", 0);

        try {
            Map<String, Object> result = switch (pathInfo) {
                case "/api/monthly-sales" -> handleMonthlySales(request);
                case "/api/cumulative-sales" -> handleCumulativeSales(request);
                case "/api/car-type-stats" -> handleCarTypeStats(request);
                case "/api/peak-time" -> handlePeakTime(request);
                case "/api/member-stats" -> handleMemberStats(request);
                case "/api/today-summary" -> statisticService.getTodaySummary();
                default -> null;
            };

            if (result == null) {
                response.sendError(HttpServletResponse.SC_NOT_FOUND, "API를 찾을 수 없습니다.");
                return;
            }

            objectMapper.writeValue(response.getWriter(), result);
        } catch (Exception e) {
            log.error("통계 API 처리 실패: {}", pathInfo, e);
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            objectMapper.writeValue(response.getWriter(), Map.of(
                    "error", true,
                    "message", e.getMessage() == null ? "알 수 없는 오류" : e.getMessage(),
                    "cause", e.getClass().getName()
            ));
        }
    }

    // month 파라미터 없으면 월별, 있으면 해당 월의 일별 매출 반환
    private Map<String, Object> handleMonthlySales(HttpServletRequest request) {
        return statisticService.getMonthlySales(getYear(request), getMonth(request), getIncludeMembership(request));
    }

    // 현재 기간과 이전 기간을 각각 호출해서 증감 비교할 때 사용
    private Map<String, Object> handleCumulativeSales(HttpServletRequest request) {
        return statisticService.getCumulativeSales(getYear(request), getMonth(request), getIncludeMembership(request));
    }

    // 차량 종류별 비율 데이터 반환
    private Map<String, Object> handleCarTypeStats(HttpServletRequest request) {
        return statisticService.getCarTypeStats(getYear(request), getMonth(request));
    }

    // 입차 시간 기준으로 시간대별 입차 수 반환
    private Map<String, Object> handlePeakTime(HttpServletRequest request) {
        return statisticService.getPeakTimeStats(getYear(request), getMonth(request));
    }

    // 활성 회원 수와 비회원 이용 수 조회
    // 기간 필터에 따라 연도 또는 월 기준으로 계산함
    private Map<String, Object> handleMemberStats(HttpServletRequest request) {
        return statisticService.getMemberStats(getYear(request), getMonth(request));
    }

    // year 파라미터가 없거나 이상하면 올해 연도로 대체
    private int getYear(HttpServletRequest request) {
        return getIntParameter(request, "year", LocalDate.now().getYear());
    }

    // month가 all이거나 비어있으면 전체 조회로 보고 null 반환
    private Integer getMonth(HttpServletRequest request) {
        String month = request.getParameter("month");
        if (month == null || month.isBlank() || "all".equals(month)) {
            return null;
        }
        try {
            return Integer.parseInt(month);
        } catch (NumberFormatException e) {
            log.warn("잘못된 month 파라미터: {}", month);
            return null;
        }
    }

    // 체크박스 값이 true나 on이면 회원권 매출 포함으로 처리
    private boolean getIncludeMembership(HttpServletRequest request) {
        String value = request.getParameter("includeMembership");
        return "true".equalsIgnoreCase(value) || "on".equalsIgnoreCase(value);
    }

    // 숫자 파싱 실패 시 화면이 깨지지 않도록 기본값으로 처리
    private int getIntParameter(HttpServletRequest request, String name, int defaultValue) {
        String value = request.getParameter(name);
        if (value == null || value.isBlank()) {
            return defaultValue;
        }
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            log.warn("잘못된 숫자 파라미터: {}={}", name, value);
            return defaultValue;
        }
    }
}
