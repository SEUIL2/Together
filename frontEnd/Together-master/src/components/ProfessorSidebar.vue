<template>
  <aside class="sidebar" :class="{ collapsed: isCollapsed }">
    <!-- 사이드바 헤더 -->
    <div class="sidebar-header">
      <span v-if="!isCollapsed" class="logo-text">교수님 환영합니다</span>
      <button class="toggle-btn" @click="$emit('toggle')" title="사이드바 접기/펼치기">
        <svg xmlns="http://www.w3.org/2000/svg" width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="15 18 9 12 15 6"></polyline></svg>
      </button>
    </div>

    <!-- 메뉴 영역 -->
    <nav class="sidebar-nav">
      <ul>
        <!-- 대시보드 -->
        <li>
          <button :class="{ active: $route.path.startsWith('/professor/mainpage') }" @click="goTo('/professor/mainpage')" :title="isCollapsed ? '대시보드' : null">
            <svg xmlns="http://www.w3.org/2000/svg" width="24" height="24" viewBox="0 0 24 24"><rect x="3" y="3" width="7" height="7"></rect><rect x="14" y="3" width="7" height="7"></rect><rect x="14" y="14" width="7" height="7"></rect><rect x="3" y="14" width="7" height="7"></rect></svg>
            <span v-if="!isCollapsed">대시보드</span>
          </button>
        </li>
        <!-- 공지사항 -->
        <li>
          <button :class="{ active: $route.path.startsWith('/professor/announcements') }" @click="goTo('/professor/announcements')" :title="isCollapsed ? '공지사항' : null">
            <svg xmlns="http://www.w3.org/2000/svg" width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M18 8A6 6 0 0 0 6 8c0 7-3 9-3 9h18s-3-2-3-9"></path><path d="M13.73 21a2 2 0 0 1-3.46 0"></path></svg>
            <span v-if="!isCollapsed">공지사항</span>
          </button>
        </li>
        <!-- 투표 -->
        <li>
          <button :class="{ active: $route.path.startsWith('/professor/votes') }" @click="goTo('/professor/votes')" :title="isCollapsed ? '투표' : null">
            <svg xmlns="http://www.w3.org/2000/svg" width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="22 12 18 12 15 21 9 3 6 12 2 12"></polyline></svg>
            <span v-if="!isCollapsed">투표</span>
          </button>
        </li>
        <!-- 피드백 -->
        <li>
          <button :class="{ active: $route.path.startsWith('/professor/feedback') }" @click="goTo('/professor/feedback')" :title="isCollapsed ? '피드백' : null">
            <svg xmlns="http://www.w3.org/2000/svg" width="24" height="24" viewBox="0 0 24 24"><path d="M21 15a2 2 0 0 1-2 2H7l-4 4V5a2 2 0 0 1 2-2h14a2 2 0 0 1 2 2z"></path></svg>
            <span v-if="!isCollapsed">피드백</span>
          </button>
        </li>
        <!-- 보고서 -->
        <li>
          <button :class="{ active: $route.path.startsWith('/professor/report') }" @click="goTo('/professor/report')" :title="isCollapsed ? '보고서' : null">
            <svg xmlns="http://www.w3.org/2000/svg" width="24" height="24" viewBox="0 0 24 24"><path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"></path><polyline points="14 2 14 8 20 8"></polyline><line x1="16" y1="13" x2="8" y2="13"></line><line x1="16" y1="17" x2="8" y2="17"></line><polyline points="10 9 9 9 8 9"></polyline></svg>
            <span v-if="!isCollapsed">보고서</span>
          </button>
        </li>
        <!-- 유사성 검사 -->
        <li>
          <button :class="{ active: $route.path.startsWith('/professor/plagiarism-check') }" @click="goTo('/professor/plagiarism-check')" :title="isCollapsed ? '유사성 검사' : null">
            <svg xmlns="http://www.w3.org/2000/svg" width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M4 12.55a6 6 0 1 1 11.9 0"/><path d="M12 3v9"/><path d="M18.13 7.87a6 6 0 1 1-11.9 0"/><path d="M12 12v9"/></svg>
            <span v-if="!isCollapsed">유사성 검사</span>
          </button>
        </li>
      </ul>
    </nav>
  </aside>
</template>

<script setup>
import { useRouter } from 'vue-router'

defineProps({
  isCollapsed: Boolean
})
defineEmits(['toggle'])

const router = useRouter()

const goTo = (path) => {
  router.push(path)
}
</script>

<style scoped>
/* 기존 Sidebar.vue의 스타일과 거의 동일하게 사용합니다. */
.sidebar {
  --sidebar-bg: #fff;
  --sidebar-width: 240px;
  --logo-text-color: #000;
  --menu-text-color: #4a5568;
  --menu-text-hover-bg: #f7fafc;
  --menu-active-bg: #eef6ff;
  --menu-active-text: #3f8efc;
  transition: width 0.3s ease-in-out;
  width: 16.2%;
  height: 100vh;
  position: fixed;
  top: 0;
  left: 0;
  background-color: var(--sidebar-bg);
  display: flex;
  flex-direction: column;
  z-index: 1000;
  border-right: 1px solid #e2e8f0;
}
.sidebar.collapsed {
  width: 70px;
}
.sidebar-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 16px;
  height: 60px;
  border-bottom: 1px solid #e2e8f0;
  flex-shrink: 0;
}
.sidebar.collapsed .sidebar-header {
  justify-content: center;
}
.logo-text {
  font-weight: 700;
  font-size: 1.1rem;
  color: #333;
}
.toggle-btn {
  background: none;
  border: none;
  cursor: pointer;
  padding: 4px;
  color: #64748b;
}
.sidebar.collapsed .toggle-btn {
  transform: rotate(180deg);
}
.sidebar-nav {
  flex-grow: 1;
  overflow-y: auto;
  padding: 16px 0;
}
.sidebar-nav ul { list-style: none; padding: 0; margin: 0; }
.sidebar-nav ul li { padding: 2px 12px; }
.sidebar-nav ul li button {
  display: flex;
  align-items: center;
  gap: 12px;
  width: 100%;
  padding: 12px 16px;
  background: none;
  border: none;
  color: var(--menu-text-color);
  font-size: 1rem;
  text-align: left;
  cursor: pointer;
  border-radius: 8px;
  transition: background-color 0.2s, color 0.2s;
}
.sidebar.collapsed .sidebar-nav ul li button {
  justify-content: center;
  padding: 12px;
}
.sidebar-nav ul li button:hover {
  background-color: var(--menu-text-hover-bg);
  color: var(--menu-active-text);
}
.sidebar-nav ul li button.active {
  background-color: var(--menu-active-bg);
  color: var(--menu-active-text);
  font-weight: 700;
}
.sidebar-nav ul li button svg {
  width: 20px;
  height: 20px;
  stroke: currentColor;
  fill: none;
  stroke-width: 2;
  stroke-linecap: round;
  stroke-linejoin: round;
}
</style>