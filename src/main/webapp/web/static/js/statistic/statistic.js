// Highcharts 우측 하단 링크 제거
Highcharts.setOptions({ credits: { enabled: false } });

// 차트 섹션 제목 변경
function setSectionTitle(text) {
    document.querySelector('.section-title').innerHTML = '<b>' + text + '</b>';
}

// 필터 연속 변경 시 마지막 입력만 반영
let chartLoadTimer = null;
function debouncedLoadChart() {
    clearTimeout(chartLoadTimer);
    chartLoadTimer = setTimeout(loadChart, 300);
}

// 연/월 필터 초기화 및 이벤트 바인딩
function initFilters() {
    const now = new Date();
    const currentYear = now.getFullYear();
    const currentMonth = now.getMonth() + 1;

    const yearSelect = document.getElementById('year');
    const monthSelect = document.getElementById('month');

    yearSelect.innerHTML = '';
    for (let y = 2024; y <= currentYear; y++) {
        const opt = document.createElement('option');
        opt.value = y;
        opt.textContent = y + '년';
        if (y === currentYear) opt.selected = true;
        yearSelect.appendChild(opt);
    }

    buildMonthOptions(currentYear, currentMonth);

    yearSelect.addEventListener('change', function () {
        const selectedYear = parseInt(this.value);
        const limitMonth = selectedYear === currentYear ? currentMonth : 12;
        buildMonthOptions(selectedYear, limitMonth);
        debouncedLoadChart();
    });

    monthSelect.addEventListener('change', debouncedLoadChart);
    document.getElementById('includeMembership').addEventListener('change', debouncedLoadChart);
    document.getElementById('chartType').addEventListener('change', function () {
        updateFilterVisibility(this.value);
        loadChart();
    });

    updateFilterVisibility(document.getElementById('chartType').value);
}

// 연도 변경 시 월 선택 옵션 갱신
function buildMonthOptions(selectedYear, limitMonth) {
    const now = new Date();
    const currentYear = now.getFullYear();
    const currentMonth = now.getMonth() + 1;

    const monthSelect = document.getElementById('month');
    const prevMonth = parseInt(monthSelect.value) || currentMonth;

    monthSelect.innerHTML = '';

    const allOpt = document.createElement('option');
    allOpt.value = 'all';
    allOpt.textContent = '전체';
    if (selectedYear < currentYear) allOpt.selected = true;
    monthSelect.appendChild(allOpt);

    for (let m = 1; m <= limitMonth; m++) {
        const opt = document.createElement('option');
        opt.value = m;
        opt.textContent = m + '월';
        if (selectedYear === currentYear && m === (prevMonth <= limitMonth ? prevMonth : limitMonth)) {
            opt.selected = true;
        }
        monthSelect.appendChild(opt);
    }
}

// 통계 유형에 따라 회원권 필터 표시 여부 결정
function updateFilterVisibility(chartType) {
    const showMembership = chartType === 'monthly_sales' || chartType === 'cumulative_sales';
    document.getElementById('membershipLabel').style.display = showMembership ? '' : 'none';
}

// 페이지 진입 시 초기화
window.onload = function () {
    initFilters();
    loadChart();
};

// 선택된 필터 값을 읽어 해당 차트 API 호출
function loadChart() {
    const chartType = document.getElementById('chartType').value;
    const year = parseInt(document.getElementById('year').value);
    const month = document.getElementById('month').value;
    const includeMembership = document.getElementById('includeMembership').checked;

    showLoading();

    switch (chartType) {
        case 'monthly_sales':    loadMonthlySales(year, month, includeMembership); break;
        case 'cumulative_sales': loadCumulativeSales(year, month, includeMembership); break;
        case 'car_type_pie':     loadCarTypePie(year, month); break;
        case 'peak_time':        loadPeakTime(year, month); break;
        case 'member_stats':     loadMemberStats(year, month); break;
    }
}

function reloadStatistics() {
    refreshTodaySummary();
    loadChart();
}

// API 공통 fetch 처리
function fetchApi(url) {
    return fetch(url).then(r => {
        if (!r.ok) throw new Error('HTTP ' + r.status);
        return r.json();
    });
}

// month가 all이면 API 파라미터용 빈 문자열로 변환
function toMonthParam(month) {
    return month === 'all' ? '' : month;
}

// 월별 매출 차트 로드
function loadMonthlySales(year, month, includeMembership) {
    const url = CONTEXT_PATH + '/statistic/api/monthly-sales?year=' + year
        + '&month=' + toMonthParam(month) + '&includeMembership=' + includeMembership;

    fetchApi(url)
        .then(data => drawMonthlySalesChart(data))
        .catch(e => showError('월별 매출 데이터를 불러오는데 실패했습니다. ' + e.message));
}

// 월별 매출 차트 렌더링
function drawMonthlySalesChart(data) {
    if (data.error) { showError('서버 오류: ' + data.message); return; }
    if (!data.categories || !data.categories.length) { showError('해당 기간의 데이터가 없습니다.'); return; }
    if (!data.normalSales || !data.normalSales.length) { showError('매출 데이터가 없습니다.'); return; }

    const series = [{ name: '일반 매출', data: data.normalSales, color: '#4472C4' }];
    if (data.includeMembership) {
        series.push({ name: '회원권 매출', data: data.memberSales, color: '#70AD47' });
    }

    setSectionTitle(data.categories.length > 12 ? '일별 매출 현황' : '월별 매출 현황');
    Highcharts.chart('chart_container', {
        chart: { type: 'column' },
        title: { text: '' },
        xAxis: { categories: data.categories },
        yAxis: { min: 0, title: { text: '매출액 (원)' }, labels: { formatter: formatYAxis } },
        tooltip: { shared: true, formatter: formatSalesTooltip },
        plotOptions: { column: { stacking: 'normal' } },
        series: series
    });
    showMonthlySalesSummary(data);
}

// 누적 매출 차트 로드
// 현재 기간과 이전 기간을 동시에 요청해서 증감 비교
function loadCumulativeSales(year, month, includeMembership) {
    const url = CONTEXT_PATH + '/statistic/api/cumulative-sales?year=' + year
        + '&month=' + toMonthParam(month) + '&includeMembership=' + includeMembership;

    let prevYear = year, prevMonth = '';
    if (month === 'all' || !month) {
        prevYear = year - 1;
    } else {
        const m = parseInt(month);
        prevYear = m === 1 ? year - 1 : year;
        prevMonth = m === 1 ? 12 : m - 1;
    }

    const prevUrl = CONTEXT_PATH + '/statistic/api/cumulative-sales?year=' + prevYear
        + '&month=' + prevMonth + '&includeMembership=' + includeMembership;

    showLoading();
    Promise.all([
        fetchApi(url),
        fetch(prevUrl).then(r => r.ok ? r.json() : null).catch(() => null)
    ])
        .then(([currentData, prevData]) => drawCumulativeSalesChart(currentData, prevData, year, month))
        .catch(e => showError('누적 매출 데이터를 불러오는데 실패했습니다. ' + e.message));
}

// 누적 매출 차트 렌더링
function drawCumulativeSalesChart(data, prevData, year, month) {
    if (!data.categories || !data.categories.length) { showError('해당 기간의 데이터가 없습니다.'); return; }

    const title = (month && month !== 'all')
        ? year + '년 ' + month + '월 누적 매출 현황'
        : year + '년 누적 매출 현황';
    setSectionTitle(title);

    const series = [{ name: '일반 누적', data: data.cumulativeNormal, color: '#4472C4' }];
    if (data.includeMembership) {
        series.push({ name: '회원 누적', data: data.cumulativeMember, color: '#70AD47' });
    }

    Highcharts.chart('chart_container', {
        chart: { type: 'column' },
        title: { text: '' },
        xAxis: { categories: data.categories },
        yAxis: { min: 0, title: { text: '매출액 (원)' }, labels: { formatter: formatYAxis } },
        tooltip: { shared: true, formatter: formatCumulativeTooltip },
        plotOptions: { column: { stacking: 'normal' } },
        series: series
    });
    showCumulativeSalesSummary(data, prevData);
}

// 차종별 통계 파이 차트 로드
function loadCarTypePie(year, month) {
    const url = CONTEXT_PATH + '/statistic/api/car-type-stats?year=' + year + '&month=' + toMonthParam(month);
    fetchApi(url)
        .then(data => drawCarTypePie(data))
        .catch(e => showError('차종별 통계 데이터를 불러오는데 실패했습니다. ' + e.message));
}

// 차종별 통계 파이 차트 렌더링
function drawCarTypePie(data) {
    if (!data.data || !data.data.length) { showError('해당 기간의 데이터가 없습니다.'); return; }
    hideSummary();
    setSectionTitle('차종별 통계');
    Highcharts.chart('chart_container', {
        chart: { type: 'pie' },
        title: { text: '' },
        subtitle: { text: '총 ' + data.total + '대' },
        tooltip: { pointFormat: '<b>{point.y}대 ({point.percentage:.1f}%)</b>' },
        plotOptions: {
            pie: {
                allowPointSelect: true, cursor: 'pointer',
                dataLabels: { enabled: true, format: '<b>{point.name}</b><br>{point.y}대 ({point.percentage:.1f}%)' }
            }
        },
        series: [{ name: '차종', colorByPoint: true, data: data.data }]
    });
}

// 피크 시간대 차트 로드
function loadPeakTime(year, month) {
    const url = CONTEXT_PATH + '/statistic/api/peak-time?year=' + year + '&month=' + toMonthParam(month);
    fetchApi(url)
        .then(data => drawPeakTimeChart(data))
        .catch(e => showError('피크 시간대 데이터를 불러오는데 실패했습니다. ' + e.message));
}

// 피크 시간대 차트 렌더링
function drawPeakTimeChart(data) {
    setSectionTitle('시간대별 입차 현황');
    Highcharts.chart('chart_container', {
        chart: { type: 'column' },
        title: { text: '' },
        xAxis: { categories: data.categories },
        yAxis: { min: 0, title: { text: '입차 대수' } },
        tooltip: { pointFormat: '<b>{point.y}대</b>' },
        series: [{ name: '입차 대수', data: data.hourlyCount, color: '#ED7D31' }]
    });
    showPeakTimeSummary(data);
}

// 회원 통계 로드
function loadMemberStats(year, month) {
    const url = CONTEXT_PATH + '/statistic/api/member-stats?year=' + year + '&month=' + toMonthParam(month);
    fetchApi(url)
        .then(data => drawMemberStatsChart(data))
        .catch(e => showError('회원 통계 데이터를 불러오는데 실패했습니다. ' + e.message));
}

// 회원 이용 현황과 활성 현황 파이 차트 렌더링
function drawMemberStatsChart(data) {
    hideSummary();
    document.getElementById('chart_container').innerHTML = `
        <div style="display:flex; width:100%; height:100%;">
            <div id="chart_member_usage" style="flex:1.3; min-height:380px;"></div>
            <div id="chart_member_status" style="flex:1; min-height:380px;"></div>
        </div>`;
    setSectionTitle('회원 현황');

    Highcharts.chart('chart_member_usage', {
        credits: { enabled: false },
        chart: { type: 'pie' },
        title: { text: '회원 vs 비회원 이용 현황', style: { fontSize: '15px', fontWeight: 'bold' } },
        subtitle: { text: '총 이용 ' + (data.activeCount + data.nonMemberUsageCount).toLocaleString('ko-KR') + '건' },
        tooltip: { pointFormat: '<b>{point.y}건 ({point.percentage:.1f}%)</b>' },
        plotOptions: { pie: { allowPointSelect: true, cursor: 'pointer', size: '80%',
            dataLabels: { enabled: true, format: '<b>{point.name}</b><br>{point.y}건 ({point.percentage:.1f}%)', style: { fontSize: '13px' } } } },
        series: [{ name: '이용 현황', colorByPoint: true, data: [
            { name: '활성 회원', y: data.activeCount, color: '#4472C4' },
            { name: '비회원 이용', y: data.nonMemberUsageCount, color: '#ED7D31' }
        ]}]
    });

    Highcharts.chart('chart_member_status', {
        credits: { enabled: false },
        chart: { type: 'pie' },
        title: { text: '회원 활성 현황', style: { fontSize: '14px' } },
        subtitle: { text: '총 회원 ' + data.totalCount + '명' },
        tooltip: { pointFormat: '<b>{point.y}명 ({point.percentage:.1f}%)</b>' },
        plotOptions: { pie: { allowPointSelect: true, cursor: 'pointer', size: '70%',
            dataLabels: { enabled: true, format: '<b>{point.name}</b><br>{point.y}명 ({point.percentage:.1f}%)', style: { fontSize: '12px' } } } },
        series: [{ name: '회원 현황', colorByPoint: true, data: [
            { name: '활성 회원', y: data.activeCount, color: '#70AD47' },
            { name: '비활성 회원', y: data.inactiveCount, color: '#FFC000' }
        ]}]
    });
}

// 월별 매출 요약 카드 생성
// 최고 매출, 최저 매출, 평균 매출 표시
function showMonthlySalesSummary(data) {
    const isDaily = data.categories.length > 12;
    const unit = isDaily ? '일' : '월';
    const totals = data.normalSales.map((v, i) =>
        v + (data.memberSales && data.includeMembership ? (data.memberSales[i] || 0) : 0));
    const maxIdx = totals.indexOf(Math.max(...totals));
    const minNonZeroIdx = totals.reduce((best, v, i) => v > 0 && (best === -1 || v < totals[best]) ? i : best, -1);
    const avg = Math.round(totals.reduce((a, b) => a + b, 0) / totals.filter(v => v > 0).length) || 0;

    renderSummaryCards([
        { label: '최고 매출 ' + unit, value: data.categories[maxIdx], sub: numberFormat(totals[maxIdx]) + '원', color: '#4472C4' },
        { label: '최저 매출 ' + unit, value: minNonZeroIdx >= 0 ? data.categories[minNonZeroIdx] : '-', sub: minNonZeroIdx >= 0 ? numberFormat(totals[minNonZeroIdx]) + '원' : '-', color: '#ED7D31' },
        { label: '평균 매출액', value: numberFormat(avg) + '원', sub: '매출 발생 ' + unit + ' 기준', color: '#70AD47' }
    ]);
}

// 누적 매출 요약 카드 생성
// 최종 누적 매출과 이전 기간 대비 증감 표시
function showCumulativeSalesSummary(data, prevData) {
    const isMonthly = data.categories && data.categories[0] && data.categories[0].includes('월');
    const periodLabel = isMonthly ? '전년' : '전월';
    const currentTotal = ((data.cumulativeNormal || []).slice(-1)[0] || 0)
        + (data.cumulativeMember && data.includeMembership ? (data.cumulativeMember.slice(-1)[0] || 0) : 0);
    let prevTotal = 0;
    if (prevData && prevData.cumulativeNormal) {
        prevTotal = (prevData.cumulativeNormal.slice(-1)[0] || 0)
            + (prevData.cumulativeMember && prevData.includeMembership ? (prevData.cumulativeMember.slice(-1)[0] || 0) : 0);
    }
    const diff = currentTotal - prevTotal;
    const rate = prevTotal > 0 ? ((diff / prevTotal) * 100).toFixed(1) : null;

    renderSummaryCards([
        { label: '최종 누적 매출', value: numberFormat(currentTotal) + '원', sub: data.title || '', color: '#4472C4' },
        { label: periodLabel + ' 대비 증감',
            value: rate !== null ? (diff >= 0 ? '+' : '-') + numberFormat(diff) + '원' : '비교 데이터 없음',
            sub: rate !== null ? (diff >= 0 ? '+' : '') + rate + '%' : '',
            color: rate !== null ? (diff >= 0 ? '#70AD47' : '#ED7D31') : '#8a93aa' }
    ]);
}

// 피크 시간대 요약 카드 생성
// 혼잡 TOP3, 한산한 시간대, 전체 입차 대수 표시
function showPeakTimeSummary(data) {
    const counts = data.hourlyCount;
    const total = counts.reduce((a, b) => a + b, 0);
    const sorted = counts.map((v, i) => ({ hour: i, count: v })).sort((a, b) => b.count - a.count);
    const top3Text = sorted.slice(0, 3).map(h => h.hour + '시 (' + h.count + '대)').join(', ');
    const minCount = Math.min(...counts);
    const quietestList = counts.map((v, i) => ({ hour: i, count: v }))
        .filter(h => h.count === minCount).map(h => h.hour + '시').join(', ');

    renderSummaryCards([
        { label: '혼잡 TOP 3', value: top3Text, sub: '입차가 가장 많은 시간대', color: '#ED7D31' },
        { label: '가장 한산한 시간대', value: quietestList, sub: minCount + '대', color: '#4472C4' },
        { label: '전체 입차 대수', value: numberFormat(total) + '대', sub: '조회 기간 합계', color: '#70AD47' }
    ]);
}

// 요약 카드 HTML 생성 및 표시
function renderSummaryCards(cards) {
    const section = document.getElementById('summarySection');
    const container = document.getElementById('summaryCards');
    if (!section || !container) return;
    container.innerHTML = cards.map(card => `
        <div class="summary-card" style="border-top: 4px solid ${card.color}">
            <div class="summary-label">${card.label}</div>
            <div class="summary-value" style="color:${card.color}">${card.value}</div>
            <div class="summary-sub">${card.sub}</div>
        </div>`).join('');
    section.style.display = 'block';
}

function hideSummary() {
    const section = document.getElementById('summarySection');
    if (section) section.style.display = 'none';
}

// 일일 현황 영역 갱신
function refreshTodaySummary() {
    fetchApi(CONTEXT_PATH + '/statistic/api/today-summary')
        .then(data => {
            document.getElementById('dailySales').textContent = data.dailySales.toLocaleString();
            document.getElementById('dailyCount').textContent = data.dailyCount;
            document.getElementById('totalCount').textContent = data.totalCount;
        })
        .catch(() => {});
}

// 로딩 스피너 표시
function showLoading() {
    document.getElementById('chart_container').innerHTML = `
        <div class="text-center p-5">
            <div class="spinner-border" role="status"><span class="visually-hidden">로딩중...</span></div>
            <p class="mt-3">데이터를 불러오는 중입니다...</p>
        </div>`;
}

// 에러 메시지 표시
function showError(message) {
    document.getElementById('chart_container').innerHTML = `
        <div class="alert alert-warning text-center p-5" role="alert">${message}</div>`;
}

// Y축 레이블을 억/만 단위로 축약
function formatYAxis() {
    if (this.value >= 100000000) return (this.value / 100000000) + '억';
    if (this.value >= 10000) return (this.value / 10000) + '만';
    return this.value;
}

// 매출 tooltip에 항목별 금액과 합계 표시
function formatSalesTooltip() {
    let s = '<b>' + tooltipCategory(this) + '</b><br/>';
    let total = 0;
    this.points.forEach(p => { s += p.series.name + ': ' + p.y.toLocaleString('ko-KR') + '원<br/>'; total += p.y; });
    return s + '<b>총 매출: ' + total.toLocaleString('ko-KR') + '원</b>';
}

// 누적 매출 tooltip 포맷
function formatCumulativeTooltip() {
    let s = '<b>' + tooltipCategory(this) + '</b><br/>';
    let total = 0;
    this.points.forEach(p => { s += p.series.name + ': ' + p.y.toLocaleString('ko-KR') + '원<br/>'; total += p.y; });
    return s + '<b>총 누적: ' + total.toLocaleString('ko-KR') + '원</b>';
}

// Highcharts tooltip에서 내부 index 대신 x축 카테고리 라벨을 표시
function tooltipCategory(context) {
    if (context.points && context.points.length > 0) {
        return context.points[0].point.category;
    }
    return context.point && context.point.category ? context.point.category : context.x;
}

// 숫자 천단위 콤마 포맷
function numberFormat(n) {
    return Math.abs(n).toLocaleString('ko-KR');
}
