<!-- App.vue -->
<template>
  <div id="app">
    <!-- Signup, CreateProject 페이지가 아닐 때만 헤더 표시 -->
    <HeaderBar v-if="!['/Signup', '/CreateProject'].includes($route.path)" />
    <div class="content">
      <router-view />
    </div>
  </div>
</template>

<script setup>
import HeaderBar from '@/components/HeaderBar.vue'
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

/* #app를 Flex 컨테이너로 세로 배치 */
#app {
  display: flex;
  flex-direction: column;
}

/* 헤더바를 고정 (HeaderBar.vue 내에 동일한 클래스가 적용되어 있거나, 여기서 덮어쓰기 가능) */
.header-bar {
  position: fixed;
  top: 0;
  left: 0;
  width: 100%;
  z-index: 1000;
}

/* 콘텐츠 영역은 헤더 높이만큼 여백을 주어 겹치지 않도록 */
.content {
  flex: 1;
  padding-top: 60px; /* 헤더 높이에 맞게 조정 */
}
</style>
