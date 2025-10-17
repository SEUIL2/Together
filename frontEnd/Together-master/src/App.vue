<template>
  <div id="app" :class="{ 'has-sidebar': showLayout, 'sidebar-collapsed': isSidebarCollapsed }">
    <transition name="toast-slide">
      <div v-if="toastIsVisible" class="global-toast">{{ toastMessage }}</div>
    </transition>
    <ProfessorSidebar
        v-if="showLayout && isProfessorGlobalView"
        :is-collapsed="isSidebarCollapsed"
        @toggle="toggleSidebar"
    />
    <SideBar
        v-else-if="showLayout"
        :is-collapsed="isSidebarCollapsed"
        @toggle="toggleSidebar"
    />
    <HeaderBar v-if="showLayout" />

    <main class="content">
      <router-view />
    </main>
  </div>
</template>

<script setup>
import { ref, computed, readonly } from 'vue';
import { useRoute } from 'vue-router';
import HeaderBar from '@/components/HeaderBar.vue';
import SideBar from '@/components/SideBar.vue';
import ProfessorSidebar from '@/components/ProfessorSidebar.vue';
import { useGlobalToast } from '@/composables/useGlobalToast';

const { isVisible: toastIsVisible, message: toastMessage } = useGlobalToast();


const route = useRoute();
const isSidebarCollapsed = ref(false);

const toggleSidebar = () => {
  isSidebarCollapsed.value = !isSidebarCollapsed.value;
};

// 교수님 전용 페이지(프로젝트 상세 조회가 아닌)인지 확인
const isProfessorGlobalView = computed(() => {
  // /professor/ 로 시작하지만, projectId 파라미터가 없는 경우
  return route.path.startsWith('/professor/') && !route.params.projectId;
});

// 레이아웃(헤더, 사이드바)을 표시할지 여부를 결정합니다.
const showLayout = computed(() => {
  // 로그인, 회원가입, 프로젝트 생성, 랜딩 페이지에서는 숨깁니다.
  return !['/', '/login', '/Login', '/Signup', '/CreateProject', '/SignupDetails', '/EmailVerification', '/admin', '/MainPage2','/create-project/ai'].includes(route.path);
});


</script>

<style>
* {
  box-sizing: border-box;
}

html,
body,
#app {
  margin: 0;
  padding: 0;
  width: 100%;
  min-width: 100vw;
  overflow-x: hidden;
  background-color: #fff;
}

#app {
  transition: padding-left 0.3s ease-in-out;
}

/* 헤더는 항상 상단에 고정 */
.header-bar {
  position: fixed;
  top: 0;
  left: 0;
  width: 100%;
  z-index: 1001; /* 사이드바보다 위에 오도록 */
}

/* 사이드바가 있을 때, 사이드바와 콘텐츠 영역에 상단 여백을 주어 헤더와 겹치지 않게 함 */
.has-sidebar .sidebar {
  padding-top: 60px; /* 헤더 높이 */
}
.has-sidebar .content {
  padding-top: 60px; /* 헤더 높이 */
  padding-left: 16%; /* 사이드바 너비 (SideBar.vue의 .sidebar width와 동일하게) */
}

/* 사이드바가 접혔을 때의 콘텐츠 영역 */
.has-sidebar.sidebar-collapsed .content {
  padding-left: 70px; /* 접힌 사이드바 너비 (SideBar.vue와 일치) */
}

/* 사이드바가 없을 때의 콘텐츠 영역 */
.content {
  /* padding-top: 60px; */
  transition: padding-left 0.3s ease-in-out;
}

.global-toast {
  position: fixed;
  top: 80px; /* 헤더 높이(60px) + 여백 */
  left: 50%;
  transform: translateX(-50%);
  background-color: rgba(0, 0, 0, 0.75);
  color: white;
  padding: 16px 24px;
  border-radius: 12px;
  z-index: 9999;
  font-size: 16px;
  font-weight: 600;
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.2);
  transition: opacity 0.3s ease, transform 0.4s cubic-bezier(0.25, 0.8, 0.25, 1);
}

/* Transition enter (나타날 때) */
.toast-slide-enter-from {
  opacity: 0;
  transform: translate(-50%, -20px);
}
.toast-slide-enter-to {
  opacity: 1;
  transform: translateX(-50%);
}
.toast-slide-enter-active {
  transition: opacity 0.3s ease, transform 0.4s cubic-bezier(0.25, 0.8, 0.25, 1);
}

/* Transition leave (사라질 때) */
.toast-slide-leave-from {
  opacity: 1;
  transform: translateX(-50%);
}
.toast-slide-leave-to {
  opacity: 0;
  transform: translate(-50%, -20px);
}
</style>