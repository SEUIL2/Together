<template>
    <div class="dashboard-container">
      <!-- 상단 정보: 하나의 박스로 통합 -->
      <div class="dashboard-top-card">
        <div class="info-section">
          <div class="info-content">
            <span class="highlight">55%</span>
            <span class="label">작업 진행도</span>
            <div class="progress-bar">
              <div class="progress-fill" :style="{ width: '55%' }"></div>
            </div>
          </div>
        </div>
  
        <div class="info-section">
          <img src="@/assets/bellicon.png" alt="bell" />
          <div class="info-content">
            <span class="highlight">3개</span>
            <span class="label">새로운 공지사항</span>
          </div>
        </div>
  
        <div class="info-section no-border">
          <img src="@/assets/todo.png" alt="todo" />
          <div class="info-content">
            <span class="highlight">13개</span>
            <span class="label">남은 작업</span>
          </div>
        </div>
      </div>
  
      <!-- 중간 작업 정보 -->
      <div class="dashboard-mid">
        <div class="card">모든 작업 카드 넣을 자리</div>
        <div class="card">내 작업 카드 넣을 자리</div>
      </div>
  
      <!-- 하단 공지사항, 투표, 활동 -->
      <div class="dashboard-bottom">
        <div class="card wide">
          <DashboardNotice />
        </div>
        <div class="card">
          <h3>투표</h3>
          <p>투표 카드 예시</p>
        </div>
        <div class="card">
          <h3>최근 활동</h3>
          <p>최근 활동 예시</p>
        </div>
      </div>
    </div>
  </template>
  
  <script setup>
  import DashboardNotice from '@/components/dashboard/DashboardNotice.vue'
  </script>
  
  <style scoped>
.dashboard-container {
  padding: 24px;
  display: flex;
  flex-direction: column;
  gap: 24px;
  background-color: #f5f6f8; /* 전체 배경 연회색 */
  min-height: 100vh;
}

/* 모든 카드 스타일 공통 */
.card, .dashboard-top-card {
  background: #ffffff;
  border-radius: 12px;
  padding: 20px;
  box-shadow: 0 2px 6px rgba(0, 0, 0, 0.05);
  border: none; /* 테두리 제거로 더 깔끔 */
}

/* 상단 박스 */
.dashboard-top-card {
  display: flex;
  overflow: hidden;
}

/* 작업 중간/하단 박스 간격 */
.dashboard-mid,
.dashboard-bottom {
  display: flex;
  gap: 20px;
}

/* 카드 너비 */
.card.wide {
  flex: 2;
}
.card:not(.wide) {
  flex: 1;
}

  
  .info-section {
  flex: 1;
  padding: 0px 24px;
  border-right: 1px solid #eee;
  display: flex;
  flex-direction: row;
  align-items: center;
  justify-content: flex-start; /* 👉 왼쪽 정렬로 변경 */
  gap: 12px;
}

  
  .info-section.no-border {
    border-right: none;
  }
  
  .info-section img {
    width: 36px;
  }
  
  .info-content {
  display: flex;
  flex-direction: column;
  align-items: flex-start;
  gap: 1px; /* 간격 추가 */
  min-width: 240px; /* ✅ progress-bar를 넣을 공간 확보 */
}

  
  .highlight {
    font-size: 28px;
    color: #3f8efc;
    font-weight: bold;
    line-height: 1.2;
  }
  
  .label {
    font-size: 14px;
    color: #777;
    line-height: 1.2;
  }
  
  .progress-bar {
  width: 100%; /* 예시로 200~300px */
  height: 10px;
  background: #ddd;
  border-radius: 5px;
  overflow: hidden;
}

  
  .progress-fill {
    height: 100%;
    background: #3f8efc;
    border-radius: 10px;
  }
  
  /* 중간/하단 카드 동일 */
  .dashboard-mid {
    display: flex;
    gap: 20px;
  }
  .card {
    background: white;
    border: 1px solid #ddd;
    border-radius: 10px;
    padding: 16px;
    flex: 1;
  }
  .dashboard-bottom {
    display: flex;
    gap: 20px;
  }
  .card.wide {
    flex: 2;
  }
  .card:not(.wide) {
    flex: 1;
  }
  </style>