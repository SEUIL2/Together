<!-- App.vue -->
<template>
  <div id="app">
    <HeaderBar />
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
  min-width: 100vw; /* 화면 전체 너비 */
  overflow-x: hidden; /* 가로 스크롤 방지 */
  background-color: #fff;
}

/* #app을 Flex 컨테이너로 세로 배치 */
#app {
  display: flex;
  flex-direction: column;
}

/* 콘텐츠 영역이 남은 공간을 채우도록 */
.content {
  flex: 1;
}
</style>
