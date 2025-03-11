<template>
  <header class="header-bar">
    <!-- 로고 영역 -->
    <div class="logo">
      <img src="@/assets/togetherlogo.png" alt="Together Logo" class="logo-img" />
      <span class="logo-text">TOGETHER</span>
    </div>

    <!-- 메뉴 영역 -->
    <nav>
      <ul>
        <li>
          <button 
            :class="{ active: $route.path === '/MyProject' }" 
            @click="goMyProject"
          >
            내 프로젝트
          </button>
        </li>
        <li>
          <button 
            :class="{ active: $route.path === '/DashBoard' }"
            @click="goMyDashBoard"
          >
            대시보드
          </button>
        </li>
        <li><button><span>작업</span></button></li>
        <li><button><span>일정 관리</span></button></li>
        <li><button><span>팀원 관리</span></button></li>
        <li><button><span>회의</span></button></li>
      </ul>
    </nav>

    <!-- 설정 아이콘 -->
    <div class="settings-icon">
      <i class="fas fa-cog"></i>
    </div>
  </header>
</template>

<script setup>
import { useRouter } from 'vue-router'
const router = useRouter()

function goMyProject() {
  router.push('/MyProject')
}

function goMyDashBoard() {
  router.push('/DashBoard')
}
</script>

<style scoped>
.header-bar {
  position: fixed;     
  top: 0;
  left: 0;
  width: 100%;
  z-index: 1000;
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
  display: flex;
  align-items: center;
}

.logo-img {
  height: 30px;
  margin-right: 0.8rem;
}

.logo-text {
  font-weight: bold;
  color: #3f8efc;
  font-size: 22px;
}

/* 메뉴 리스트 */
nav ul {
  display: flex;
  list-style: none;
  margin: 0;
  padding: 0;
  gap: 4rem;
  margin-left: -20rem; /* 여기서 원하는 값(-1rem, -2rem 등)으로 조정 */
}

/* 메뉴 버튼 기본 스타일 */
nav ul li button {
  background: none;
  border: none;
  font-size: 1rem;
  cursor: pointer;
  color: #000;
  padding: 0;
  transition: color 0.3s;
  position: relative;
  height: 60px;             /* 헤더바와 동일한 높이 */
  display: flex;
  align-items: center;
  justify-content: center;
}

/* 호버 효과 */
nav ul li button:hover {
  color: #666;
}

/* 활성화 상태: 글씨 색상 변경 */
nav ul li button.active {
  color: #3f8efc;
}

/* 활성화된 버튼의 밑줄: 버튼의 맨 아래에 붙임 */
nav ul li button.active::after {
  content: "";
  position: absolute;
  bottom: 0;
  left: 50%;
  transform: translateX(-50%);
  width: 130%;      /* 텍스트보다 20% 더 넓게 */
  height: 5px;
  background-color: #3f8efc;
  border-radius: 5px;
}

/* 설정 아이콘 */
.settings-icon {
  font-size: 1.2rem;
  cursor: pointer;
}
</style>
