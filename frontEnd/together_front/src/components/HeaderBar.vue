<template>
  <header class="header-bar">
    <div class="logo">
      <!-- 로고 이미지 -->
      <img src="@/assets/togetherlogo.png" alt="Together Logo" class="logo-img" />
      <span>TOGETHER</span>
    </div>
    <nav>
      <ul>
        <li>내 프로젝트</li>
        <li>대시보드</li>
        <li>작업</li>
        <li>일정 관리</li>
        <li>팀원 관리</li>
        <li>회의</li>
      </ul>
    </nav>
    <div class="settings-icon">
      <i class="fas fa-cog"></i>
    </div>
  </header>
</template>

<script setup>
// 별도의 로직이 필요 없다면 비워둬도 됩니다.
</script>

<style scoped>
.header-bar {
  width: 100%; /* 전체 너비 설정 */
  min-width: 100vw; /* 가로 너비를 화면 전체로 확장 */
  max-width: none; /* 혹시라도 max-width가 설정되어 있다면 제거 */
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 2rem;
  height: 60px;
  background-color: #ffffff;
  border-bottom: 1px solid #eee;
}

/* 로고 영역 */
.logo {
  display: flex; /* 텍스트와 이미지를 가로로 나란히 배치 */
  align-items: center; /* 수직 정렬 */
  font-weight: bold;
  color: #3f8efc;
}

/* 로고 이미지 스타일 */
.logo-img {
  height: 20px; /* 원하는 크기로 조정 */
  margin-right: 0.5rem; /* 텍스트와 간격 */
}

/* 네비게이션 */
nav ul {
  display: flex;
  list-style: none;
  margin: 0; /* 추가 */
  padding: 0; /* 추가 */
  gap: 1.5rem;
}
nav ul li {
  color: #000000;
}

/* 설정 아이콘 */
.settings-icon {
  font-size: 1.2rem;
  cursor: pointer;
}
</style>
