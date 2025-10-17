<template>
    <div class="signup-wrapper">
      <!-- 중앙 정렬을 위한 컨테이너 -->
      <div class="signup-card">
        <!-- 회원가입 제목 -->
        <h2 class="signup-title">회원가입</h2>
  
        <!-- 학생/교수 전환 버튼 -->
        <div class="toggle-buttons">
          <button
            :class="{ active: userRole === 'STUDENT' }"
            @click="userRole = 'STUDENT'"
          >
            학생
          </button>
          <button
            :class="{ active: userRole === 'PROFESSOR' }"
            @click="userRole = 'PROFESSOR'"
          >
            교수
          </button>
        </div>
  
        <!-- 회원가입 폼 -->
        <form @submit.prevent="handleSignup">
          <!-- 이름 / 학번 -->
          <div class="form-row">
            <div class="form-group">
              <label for="userName">*이름</label>
              <input
                id="userName"
                type="text"
                v-model="userName"
                placeholder="이름을 입력해주세요"
                required
              />
            </div>
            <!-- 학번 (학생일 경우만 표시) -->
            <div class="form-group" v-if="userRole === 'STUDENT'">
              <label for="studentNumber">*학번</label>
              <input
                id="studentNumber"
                type="text"
                v-model="studentNumber"
                placeholder="학번을 입력해주세요"
                required
              />
            </div>
          </div>
  
          <!-- 이메일 -->
          <div class="form-group">
            <label for="userEmail">*E-mail</label>
            <input
              id="userEmail"
              type="email"
              v-model="userEmail"
              placeholder="이메일을 입력해주세요"
              required
            />
          </div>
  
          <!-- 아이디 + 중복검사 -->
          <div class="form-group">
            <label for="userLoginId">*아이디</label>
            <div class="input-with-button">
              <input
                id="userLoginId"
                type="text"
                v-model="userLoginId"
                placeholder="아이디를 입력해주세요"
                required
              />
              <button type="button" class="check-btn" @click="checkDuplicate">
                중복검사
              </button>
            </div>
          </div>
  
          <!-- 비밀번호 / 비밀번호 확인 -->
          <div class="form-row">
            <div class="form-group">
              <label for="password">*비밀번호</label>
              <input
                id="password"
                type="password"
                v-model="password"
                placeholder="비밀번호를 입력해주세요"
                required
              />
            </div>
            <div class="form-group">
              <label for="confirmPassword">*비밀번호 확인</label>
              <input
                id="confirmPassword"
                type="password"
                v-model="confirmPassword"
                placeholder="비밀번호를 한 번 더 입력해주세요"
                required
              />
            </div>
          </div>
  
          <!-- 회원가입 버튼 -->
          <button type="submit" class="submit-btn">회원가입</button>
  
          <!-- 성공/오류 메시지 -->
          <p v-if="successMessage" class="success-msg">{{ successMessage }}</p>
          <p v-if="errorMessage" class="error-msg">{{ errorMessage }}</p>
        </form>
      </div>
    </div>
  </template>
  
  <script setup>
  import { ref } from "vue";
  import api from "@/api";
  
  // ===== 폼 상태 =====
  const userName = ref("");
  const userEmail = ref("");
  const userLoginId = ref("");
  const password = ref("");
  const confirmPassword = ref("");
  const userRole = ref("STUDENT"); // 기본값: 학생
  const studentNumber = ref("");
  
  // ===== 메시지 상태 =====
  const successMessage = ref("");
  const errorMessage = ref("");
  
  // ===== 회원가입 API 호출 =====
  const handleSignup = async () => {
    // 메시지 초기화
    successMessage.value = "";
    errorMessage.value = "";
  
    // 비밀번호 일치 확인
    if (password.value !== confirmPassword.value) {
      errorMessage.value = "비밀번호가 일치하지 않습니다.";
      return;
    }
  
    // 백엔드에 전달할 데이터 구성
    const signupData = {
      userName: userName.value,
      userEmail: userEmail.value,
      userLoginId: userLoginId.value,
      password: password.value,
      confirmPassword: confirmPassword.value,
      userRole: userRole.value,
      studentNumber: userRole.value === "STUDENT" ? studentNumber.value : ""
    };
  
    try {
      const response = await api.post("/auth/signup", signupData, {
        headers: { "Content-Type": "application/json" }
      });
      successMessage.value = response.data; // 예: "회원가입이 완료되었습니다."
    } catch (error) {
      errorMessage.value = error.response?.data || "회원가입 실패";
    }
  };
  
  // ===== 아이디 중복 검사 =====
  const checkDuplicate = async () => {
    successMessage.value = "";
    errorMessage.value = "";
  
    try {
      const response = await api.get(`/check-username?username=${userLoginId.value}`);
      if (response.data.exists) {
        errorMessage.value = "이미 존재하는 아이디입니다.";
      } else {
        successMessage.value = "사용 가능한 아이디입니다.";
      }
    } catch (error) {
      errorMessage.value = "아이디 중복 검사 실패";
    }
  };
  </script>
  
  <style scoped>
  /* 전체 래퍼: 화면 가운데 정렬 */
  .signup-wrapper {
    display: flex;
    justify-content: center;
    align-items: center;
    min-height: 100vh;
    background-color: #f9f9f9;
  }
  
  /* 카드 형태 */
  .signup-card {
    background-color: #fff;
    width: 500px;
    border-radius: 10px;
    box-shadow: 0 4px 10px rgba(0, 0, 0, 0.1);
    padding: 2rem;
  }
  
  /* 제목 */
  .signup-title {
    text-align: center;
    font-size: 1.5rem;
    font-weight: bold;
    margin-bottom: 1.5rem;
    color: #333;
  }
  
  /* 학생/교수 전환 버튼 래퍼 */
  .toggle-buttons {
    display: flex;
    justify-content: center;
    gap: 1rem;
    margin-bottom: 2rem;
  }
  
  /* 토글 버튼 기본 스타일 */
  .toggle-buttons button {
    padding: 0.5rem 1.5rem;
    font-size: 1rem;
    border: none;
    border-radius: 5px;
    background-color: #e0e0e0;
    cursor: pointer;
    transition: background-color 0.3s;
  }
  
  /* 토글 버튼 활성화 상태 */
  .toggle-buttons .active {
    background-color: #3f8efc;
    color: #fff;
  }
  
  /* 폼 레이아웃 */
  form {
    display: flex;
    flex-direction: column;
    gap: 1rem;
  }
  
  /* 두 칸짜리 행 */
  .form-row {
    display: flex;
    gap: 1rem;
  }
  
  /* 라벨 + 인풋 */
  .form-group {
    display: flex;
    flex-direction: column;
    flex: 1;
  }
  
  /* 라벨 스타일 */
  .form-group label {
    font-weight: bold;
    margin-bottom: 0.5rem;
    color: #555;
  }
  
  /* 인풋 스타일 */
  .form-group input {
    padding: 0.7rem;
    border: 1px solid #ccc;
    border-radius: 5px;
  }
  
  /* 아이디 중복검사 버튼 */
  .input-with-button {
    display: flex;
    gap: 0.5rem;
  }
  
  .input-with-button input {
    flex: 1;
  }
  
  .check-btn {
    background-color: #555;
    color: #fff;
    border-radius: 5px;
    border: none;
    padding: 0.5rem 1rem;
    cursor: pointer;
  }
  
  /* 회원가입 버튼 */
  .submit-btn {
    display: block;
    width: 100%;
    padding: 0.75rem;
    font-size: 1rem;
    font-weight: bold;
    color: #fff;
    background-color: #3f8efc;
    border: none;
    border-radius: 5px;
    cursor: pointer;
  }
  
  /* 메시지 */
  .success-msg {
    margin-top: 1rem;
    color: green;
    text-align: center;
  }
  .error-msg {
    margin-top: 1rem;
    color: red;
    text-align: center;
  }
  </style>
  