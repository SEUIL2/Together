<template>
    <div class="signup-container">
      <!-- 로고 -->
      <div class="logo">
        <img src="@/assets/togetherlogo.png" alt="로고" />
      </div>

      <div class="signup-form">
              <!-- 회원가입 제목 -->
      <h2 class="signup-title">회원가입</h2>
        <div class="toggle-buttons">
          <button :class="{'active': userType === 'student'}" @click="userType = 'student'">학생</button>
          <button :class="{'active': userType === 'professor'}" @click="userType = 'professor'">교수</button>
        </div>
  
        <form v-if="userType === 'student'">
          <!-- 이름과 학번을 가로 정렬 -->
          <div class="row-group">
            <div class="input-group">
              <label>*이름</label>
              <input type="text" placeholder="이름을 입력해주세요" />
            </div>
            <div class="input-group">
              <label>*학번</label>
              <input type="text" placeholder="학번을 입력해주세요" />
            </div>
          </div>
  
          <!-- 이메일과 아이디 칸의 크기 조정 -->
          <div class="input-group small-input2">
            <label>*E-mail</label>
            <input type="email" placeholder="이메일을 입력해주세요" />
          </div>
          <div class="input-group small-input">
            <label>*아이디</label>
            <div class="input-with-button">
              <input type="text" placeholder="아이디를 입력해주세요" />
              <button class="check-btn">중복검사</button>
            </div>
          </div>
  
          <!-- 비밀번호와 비밀번호 확인을 가로 정렬 -->
          <div class="row-group">
            <div class="input-group">
              <label>*비밀번호</label>
              <input type="password" placeholder="비밀번호를 입력해주세요" />
              <small class="warning-text">비밀번호가 일치하지 않습니다</small>
            </div>
            <div class="input-group">
              <label>*비밀번호 확인</label>
              <input type="password" placeholder="비밀번호를 한 번 더 입력해주세요" />
            </div>
          </div>
  
          <!-- 회원가입 버튼 -->
<div class="button-container">
  <button class="submit-btn">회원가입</button>
</div>
        </form>
      </div>
    </div>
  </template>
  
  <script setup>
  import { ref } from "vue";
  const userType = ref("student");
  </script>
  
  <style scoped>
  /* 전체 컨테이너 */
  .signup-container {
    display: flex;
    flex-direction: column;
    align-items: center;
    justify-content: center;
    width: 100vw;
    height: 100vh;
    background-color: #f9f9f9;
  }
  
  /* 로고 */
  .logo img {
    width: 50px;
    margin-bottom: 10px;
  }
  
  /* 회원가입 제목 */
  .signup-title {
    font-size: 1.5rem;
    font-weight: bold;
    margin-bottom: 20px;
    color: #555;
  }
  
  /* 학생/교수 전환 버튼 */
/* 학생/교수 전환 버튼 */
.toggle-buttons {
  display: flex;
  gap: 10px;
  margin-bottom: 20px;
}

.toggle-buttons button {
  padding: 3px 20px;
  font-size: 1rem;
  border: none;
  border-radius: 5px;
  cursor: pointer;
  background-color: #e0e0e0;
  transition: 0.3s;
  box-shadow: 2px 2px 5px rgba(0, 0, 0, 0.2); /* 버튼 아래 그림자 추가 */
}

/* 버튼 클릭 시 그림자 변경 */
.toggle-buttons button:active {
  box-shadow: 1px 1px 3px rgba(0, 0, 0, 0.3);
}

  .signup-title,
.toggle-buttons {
  display: flex;
  justify-content: center;
  align-items: center;
  width: 100%; /* 부모 컨테이너 내에서 가로 전체 차지 */
  text-align: center; /* 텍스트 가운데 정렬 */
}
  
  .toggle-buttons .active {
    background-color: #3f8efc;
    color: white;
  }
  
  /* 회원가입 폼 */
  .signup-form {
    background: white;
    padding: 30px;
    border-radius: 10px;
    box-shadow: 0 4px 10px rgba(0, 0, 0, 0.1);
    width: 640px;
  }
  
  /* 이름과 학번을 가로로 정렬 */
  .row-group {
    display: flex;
    gap: 20px;
    width: 100%;
  }
  
  .row-group .input-group {
    flex: 1;
  }
  
  /* 이메일과 아이디 칸의 가로 길이 조정 */
  .small-input {
    max-width: 355px; /* 입력 필드의 최대 너비 설정 */
    width: 100%;
  }
  .small-input2 {
    max-width: 280px; /* 입력 필드의 최대 너비 설정 */
    width: 100%;
  }
  
  /* 아이디 입력 칸과 버튼을 한 줄로 정렬 */
  .input-with-button {
    display: flex;
    align-items: center;
    gap: 10px;
  }
  
  .input-with-button input {
    flex: 1;
  }
  
  .input-with-button .check-btn {
    white-space: nowrap;
    padding: 8px 12px;
  }
  
  /* 입력 그룹 */
  .input-group {
    display: flex;
    flex-direction: column;
    margin-bottom: 15px;
  }
  
  .input-group label {
    font-size: 0.9rem;
    font-weight: bold;
    margin-bottom: 5px;
    color: #363636;
  }
  
  .input-group input {
    padding: 10px;
    border: 1px solid #ddd;
    border-radius: 5px;
    font-size: 0.8rem;
  }
  
  .input-group .check-btn {
    margin-top: 0px;
    padding: 10px 10px;
    font-size: 0.7rem;
    border: none;
    background-color: #555;
    color: white;
    border-radius: 5px;
    cursor: pointer;
  }
  
  /* 비밀번호 오류 메시지 */
  .warning-text {
    color: red;
    font-size: 0.8rem;
    margin-top: 5px;
  }
  
/* 버튼 컨테이너 */
.button-container {
  display: flex;
  justify-content: center; /* 가로 중앙 정렬 */
  margin-top: 20px; /* 위쪽 여백 */
}

/* 회원가입 버튼 */
.submit-btn {
  width: 80%; /* 기존 100%에서 80%로 조정 */
  max-width: 250px; /* 최대 너비 설정 */
  padding: 10px;
  font-size: 1rem;
  border: none;
  border-radius: 5px;
  background-color: #3f8efc;
  color: white;
  cursor: pointer;
}


  </style>
  