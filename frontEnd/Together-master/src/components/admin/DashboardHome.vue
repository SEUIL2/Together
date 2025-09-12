<template>
  <div>
    <h2>대시보드 요약</h2>
    <div class="stats-cards">
      <div class="card"><h3>총 사용자 수</h3><p>35 명</p></div>
      <div class="card"><h3>총 프로젝트 수</h3><p>7 개</p></div>
      <div class="card"><h3>오늘 가입자 수</h3><p>5 명</p></div>
      <div class="card"><h3>활성 프로젝트</h3><p>1 개</p></div>
    </div>

    <div class="dashboard-grid-new">
      <div class="widget">
        <h3>서버 상태</h3>
        <div class="server-status">
          <span class="status-indicator-stable"></span>
          <p><strong>상태:</strong> 정상</p>
        </div>
        <p><strong>CPU 사용량:</strong> 35%</p>
        <div class="progress-bar"><div style="width: 35%;"></div></div>
        <p><strong>Memory 사용량:</strong> 58%</p>
        <div class="progress-bar"><div style="width: 58%;"></div></div>
      </div>

      <div class="widget">
        <h3>AI 기능 사용량 (오늘)</h3>
        <ul class="ai-usage-list">
          <li>주제 추천: <strong>5회</strong></li>
          <li>키워드 생성: <strong>10회</strong></li>
          <li>코드 생성: <strong>58회</strong></li>
        </ul>
      </div>

      <div class="widget">
        <h3>주간 가입자 수</h3>
        <div class="chart-container">
          <div class="chart-bar" style="height: 10%;"><span class="label">월</span></div>
          <div class="chart-bar" style="height: 20%;"><span class="label">화</span></div>
          <div class="chart-bar" style="height: 30%;"><span class="label">수</span></div>
          <div class="chart-bar" style="height: 10%;"><span class="label">목</span></div>
          <div class="chart-bar" style="height: 50%;"><span class="label">금</span></div>
          <div class="chart-bar" style="height: 30%;"><span class="label">토</span></div>
          <div class="chart-bar" style="height: 10%;"><span class="label">일</span></div>
        </div>
      </div>
    </div>

    <div class="widget full-width">
      <h3>최근 프로젝트 활동</h3>
      <table class="data-table">
        <thead>
        <tr>
          <th>프로젝트명</th>
          <th>팀장</th>
          <th>최근 활동</th>
          <th>날짜</th>
        </tr>
        </thead>
        <tbody>
        <tr>
          <td>AI 기반 스마트 캠퍼스 안내 시스템</td>
          <td>박서준</td>
          <td>새로운 회의록이 추가되었습니다.</td>
          <td>2025-09-12 10:30</td>
        </tr>
        <tr>
          <td>실시간 온라인 협업 화이트보드</td>
          <td>김지민</td>
          <td>'개발' 업무가 '완료' 상태로 변경되었습니다.</td>
          <td>2025-09-12 09:15</td>
        </tr>
        <tr>
          <td>메타버스 기반 가상 도서관</td>
          <td>최정훈</td>
          <td>새로운 팀원 '이수빈'이 합류했습니다.</td>
          <td>2025-09-11 18:45</td>
        </tr>
        <tr>
          <td>반려동물 산책 경로 추천 앱</td>
          <td>송혜교</td>
          <td>새로운 투표가 생성되었습니다: '앱 아이콘 선정'</td>
          <td>2025-09-11 14:20</td>
        </tr>
        </tbody>
      </table>
    </div>
  </div>
</template>

<script>
export default { name: 'DashboardHome' };
</script>

<style scoped>
/* 기존 스타일 유지 */
.stats-cards {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
  gap: 20px;
  margin-bottom: 30px;
}
.card {
  background: #fff;
  padding: 20px;
  border-radius: 8px;
  box-shadow: 0 2px 4px rgba(0,0,0,0.1);
  text-align: center;
}
.card h3 { margin: 0 0 10px; font-size: 16px; color: #555; }
.card p { margin: 0; font-size: 28px; font-weight: bold; color: #333; }

/* [수정] 2단에서 3단 그리드로 변경 */
.dashboard-grid-new {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 30px;
  margin-bottom: 30px;
}
.widget {
  background: #fff;
  padding: 20px;
  border-radius: 8px;
  box-shadow: 0 2px 4px rgba(0,0,0,0.1);
}
.widget h3 { margin-top: 0; }
.server-status { display: flex; align-items: center; margin-bottom: 15px; }
.status-indicator-stable {
  width: 15px; height: 15px;
  background-color: #28a745;
  border-radius: 50%;
  margin-right: 10px;
}
.progress-bar {
  height: 10px;
  background-color: #e9ecef;
  border-radius: 5px;
  margin: 5px 0 15px;
}
.progress-bar div { height: 100%; background-color: #007bff; border-radius: 5px; }
.ai-usage-list { list-style: none; padding: 0; }
.ai-usage-list li { padding: 8px 0; border-bottom: 1px solid #eee; }
.ai-usage-list li:last-child { border-bottom: none; }

/* [추가] 차트 스타일 */
.chart-container {
  display: flex;
  justify-content: space-around;
  align-items: flex-end;
  height: 150px;
  padding: 10px 0;
}
.chart-bar {
  width: 10%;
  background-color: #007bff;
  border-radius: 4px 4px 0 0;
  position: relative;
  display: flex;
  justify-content: center;
}
.chart-bar .label {
  position: absolute;
  bottom: -20px;
  font-size: 12px;
  color: #555;
}

/* [추가] 전체 너비 위젯 및 테이블 스타일 */
.widget.full-width {
  grid-column: 1 / -1; /* 그리드의 처음부터 끝까지 차지 */
}
.data-table { width: 100%; border-collapse: collapse; background: #fff; }
.data-table th, .data-table td { border: 1px solid #ddd; padding: 12px; text-align: left; }
.data-table th { background-color: #f8f9fa; }
</style>