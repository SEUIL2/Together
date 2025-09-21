<!-- App.vue -->
<template>
  <div id="app" :class="{ 'has-sidebar': showLayout, 'sidebar-collapsed': isSidebarCollapsed }">
    <!-- 교수 전용 페이지 사이드바 -->
    <ProfessorSidebar
      v-if="showLayout && isProfessorGlobalView"
      :is-collapsed="isSidebarCollapsed"
      @toggle="toggleSidebar"
    />
    <!-- 학생 또는 교수 프로젝트 조회용 사이드바 -->
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
import { ref, computed } from 'vue';
import { useRoute } from 'vue-router';
import HeaderBar from '@/components/HeaderBar.vue';
import SideBar from '@/components/SideBar.vue';
import ProfessorSidebar from '@/components/ProfessorSidebar.vue';


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
  return !['/', '/login', '/Login', '/Signup', '/CreateProject', '/SignupDetails', '/EmailVerification', '/admin'].includes(route.path);
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
  padding-top: 60px;
  transition: padding-left 0.3s ease-in-out;
}

</style>
