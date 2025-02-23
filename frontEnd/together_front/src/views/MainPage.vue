<template>
  <div class="main-container">
    <!-- 상단 네비게이션 영역 -->

    <!-- 메인 컨텐츠 영역 -->
    <main class="content">
      <h2 class="sub-title">졸업작품을 위한 든든한 동반자</h2>
      <h1 class="main-title">TOGETHER</h1>
      <div class="button-group">
        <button class="btn primary">회원가입 하러가기</button>
        <button class="btn secondary">로그인</button>
      </div>
    </main>
  </div>
</template>

<script>
export default {
  name: 'MainPage',
}
</script>

<style scoped>
/* 전체 레이아웃 */
.main-container {
  width: 100vw;
  height: 100vh;
  margin: 0;
  padding: 0;
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
}

/* 메뉴 리스트 */
.nav-menu {
  list-style: none;
  display: flex;
  gap: 1.5rem;
  margin: 0;
  padding: 0;
}

.nav-menu li {
  cursor: pointer;
  font-size: 0.95rem;
}

/* 설정(기어) 아이콘 */
.nav-settings {
  font-size: 1.2rem;
  cursor: pointer;
}

/* 메인 컨텐츠 */
.content {
  display: flex;
  flex-direction: column;
  align-items: center;
  margin-top: 4rem;
  text-align: center;
}

/* 부제목 */
.sub-title {
  font-size: 1.1rem;
  color: #666;
  margin-bottom: 0.5rem;
}

/* 메인 타이틀 */
.main-title {
  font-size: 2.5rem;
  color: #3f8efc; /* TOGETHER에 어울리는 파란색 예시 */
  margin-bottom: 2rem;
}

/* 버튼 그룹 */
.button-group {
  display: flex;
  gap: 1rem;
}

.btn {
  padding: 0.75rem 1.25rem;
  font-size: 1rem;
  border: none;
  cursor: pointer;
  border-radius: 4px;
}

.primary {
  background-color: #3f8efc;
  color: #fff;
}

.secondary {
  background-color: #eee;
  color: #333;
}
</style>
