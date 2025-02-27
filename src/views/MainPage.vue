<template>
    <div class="main-container">
      <main class="content">
        <h2 class="sub-title">졸업작품을 위한 든든한 동반자</h2>
        <h1 class="main-title">TOGETHER</h1>
        <div class="button-group">
          <button class="btn primary" @click="goToSignup">회원가입 하러가기</button>
          <button class="btn secondary" @click="goToLogin">로그인</button>
        </div>
      </main>
    </div>
  </template>
  
  <script>
  import { useRouter } from 'vue-router'
  
  export default {
    name: 'MainPage',
    setup() {
      const router = useRouter();
  
      // 회원가입 페이지로 이동하는 함수
      const goToSignup = () => {
        router.push('/Signup'); 
      };
  
      // 로그인 페이지로 이동하는 함수
      const goToLogin = () => {
        router.push('/Login');
      };
  
      return { goToSignup, goToLogin }; // 두 개의 함수를 반환해야 함
    }
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
  
  /* 메인 컨텐츠 */
  .content {
    display: flex;
    flex-direction: column;
    align-items: center;
    justify-content: center;
    text-align: center;
    flex-grow: 1;
    margin-bottom: 100px;
  }
  
  /* 부제목 */
  .sub-title {
    font-size: 1.1rem;
    color: #666;
    margin-bottom: 0.1rem;
  }
  
  /* 메인 타이틀 */
  .main-title {
    font-size: 2.5rem;
    color: #3f8efc;
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
  