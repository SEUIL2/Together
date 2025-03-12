<template>
  <div class="login-container">
    <!-- 로고 -->
    <div class="logo">
      <img src="@/assets/togetherlogo.png" alt="로고" />
    </div>

    <!-- 로그인 폼 -->
    <div class="login-form">
      <h2 class="login-title">로그인</h2>

      <form @submit.prevent="handleLogin">
        <!-- 아이디 입력 -->
        <div class="input-group">
          <input
            type="text"
            placeholder="아이디"
            v-model="username"
            required
          />
        </div>

        <!-- 비밀번호 입력 -->
        <div class="input-group">
          <input
            type="password"
            placeholder="비밀번호"
            v-model="password"
            required
          />
        </div>

        <!-- 로그인 버튼 -->
        <button type="submit" class="login-btn">로그인</button>

        <!-- 로그인 성공/실패 메시지 -->
        <p v-if="successMessage" class="success-msg">{{ successMessage }}</p>
        <p v-if="errorMessage" class="error-msg">{{ errorMessage }}</p>

        <!-- 아이디 / 비밀번호 찾기 (옵션) -->
        <div class="find-links">
          <a href="#">아이디 찾기</a> | <a href="#">비밀번호 찾기</a>
        </div>

        <!-- 회원가입 안내 -->
        <hr />
        <p class="signup-text">회원이 아니신가요?</p>

        <!-- 회원가입 버튼 -->
        <button type="button" class="signup-btn" @click="goToSignup">
          회원가입
        </button>
      </form>
    </div>
  </div>
</template>

<script setup>
import { ref } from "vue";
import axios from "axios";
import { useRouter } from "vue-router";

const router = useRouter();

// 입력 필드
const username = ref("");
const password = ref("");

// 메시지
const successMessage = ref("");
const errorMessage = ref("");

// 로그인 함수
const handleLogin = async () => {
  // 매번 클릭 시 메시지 초기화
  successMessage.value = "";
  errorMessage.value = "";

  // 로그인 요청에 보낼 데이터
  const loginData = {
    username: username.value,
    password: password.value
  };

  try {
    // 백엔드 예시 엔드포인트: http://localhost:8081/auth/login
    const response = await axios.post("http://localhost:8081/auth/login", loginData, {
      headers: { "Content-Type": "application/json" }
    });
    
    // 서버에서 200 OK 응답을 받았다면 로그인 성공 처리
    successMessage.value = "로그인 성공!";
    // 필요하다면 토큰 저장, 메인 페이지로 이동 등 로직 추가
    // 예: localStorage.setItem("token", response.data.token);
    //     router.push("/main");

  } catch (error) {
    // 로그인 실패 시 (401, 403 등) 에러 메시지 출력
    // 백엔드에서 에러 메시지를 내려주는 경우 해당 내용 사용
    errorMessage.value = error.response?.data || "로그인 실패!";
  }
};

// 회원가입 페이지로 이동
const goToSignup = () => {
  router.push("/Signup");
};
</script>

<style scoped>
/* 전체 컨테이너 */
.login-container {
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
  margin-bottom: 15px;
}

/* 로그인 폼 */
.login-form {
  background: white;
  padding: 30px;
  border-radius: 12px;
  box-shadow: 0 4px 10px rgba(0, 0, 0, 0.1);
  width: 400px;
  margin-bottom: 100px;
  text-align: center;
}

/* 로그인 제목 */
.login-title {
  font-size: 1.5rem;
  font-weight: bold;
  margin-bottom: 20px;
}

/* 입력 필드 스타일 */
.input-group {
  margin-bottom: 15px;
}

.input-group input {
  width: 100%;
  padding: 12px;
  border: 1px solid #ddd;
  border-radius: 6px;
  font-size: 1rem;
}

/* 로그인 버튼 */
.login-btn {
  width: 100%;
  padding: 12px;
  font-size: 1rem;
  border: none;
  border-radius: 6px;
  background-color: #3f8efc;
  color: white;
  cursor: pointer;
  margin-top: 10px;
  font-weight: bold;
}

.login-btn:hover {
  background-color: #3578e5;
}

/* 메시지 */
.success-msg {
  margin-top: 10px;
  color: green;
}
.error-msg {
  margin-top: 10px;
  color: red;
}

/* 아이디 / 비밀번호 찾기 */
.find-links {
  margin: 10px 0;
  font-size: 0.9rem;
}

.find-links a {
  color: #3f8efc;
  text-decoration: none;
}

.find-links a:hover {
  text-decoration: underline;
}

/* 구분선 */
hr {
  border: none;
  border-top: 1px solid #ddd;
  margin: 20px 0;
}

/* 회원가입 텍스트 */
.signup-text {
  font-size: 0.9rem;
  color: #666;
}

/* 회원가입 버튼 */
.signup-btn {
  width: 100%;
  padding: 12px;
  font-size: 1rem;
  border: none;
  border-radius: 6px;
  background-color: #bdbdbd;
  color: white;
  cursor: pointer;
  font-weight: bold;
}

.signup-btn:hover {
  background-color: #9e9e9e;
}
</style>
