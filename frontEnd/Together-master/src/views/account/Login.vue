<template>
  <div class="login-container">
    <div class="logo">
      <img src="@/assets/togetherlogo.png" alt="로고" />
    </div>

    <div class="login-form">
      <h2 class="login-title">로그인</h2>

      <form @submit.prevent="handleLogin">
        <div class="input-group">
          <input
            type="text"
            placeholder="아이디"
            v-model="userLoginId"
            required
          />
        </div>

        <div class="input-group">
          <input
            type="password"
            placeholder="비밀번호"
            v-model="password"
            required
          />
        </div>

        <button type="submit" class="login-btn">로그인</button>

        <p v-if="successMessage" class="success-msg">{{ successMessage }}</p>
        <p v-if="errorMessage" class="error-msg">{{ errorMessage }}</p>

        <div class="find-links">
          <a href="#">아이디 찾기</a> | <a href="#">비밀번호 찾기</a>
        </div>

        <hr />
        <p class="signup-text">회원이 아니신가요?</p>
        <button type="button" class="signup-btn" @click="goToSignup">
          회원가입
        </button>
      </form>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import axios from 'axios'
import { useRouter } from 'vue-router'

const router = useRouter()

const userLoginId = ref('')
const password = ref('')
const successMessage = ref('')
const errorMessage = ref('')

const handleLogin = async () => {
  successMessage.value = ''
  errorMessage.value = ''

  // Basic Auth 헤더 생성 및 저장
  const encoded = btoa(`${userLoginId.value}:${password.value}`)
  const authHeader = `Basic ${encoded}`
  localStorage.setItem('authHeader', authHeader)

  try {
    // 인증된 유저 정보 요청
    const res = await axios.get('/auth/me', {
      headers: { Authorization: authHeader },
      withCredentials: true
    })

    // 로그인 성공 처리
    successMessage.value = '로그인 성공!'
    window.dispatchEvent(new Event('login-success'))

    // 프로젝트 유무에 따라 라우팅
    const projectId = res.data.projectId
    if (projectId) {
      router.push('/MyProject')
    } else {
      router.push('/MainPage2')
    }
  } catch (error) {
    // 프로젝트 미등록으로 인한 403 일 때는 MainPage2로
    if (error.response?.status === 403) {
      successMessage.value = '로그인 성공! (프로젝트 미등록 계정)'
      window.dispatchEvent(new Event('login-success'))
      router.push('/MainPage2')
    } else {
      // 그 외는 실제 로그인 실패
      errorMessage.value = error.response?.data || '로그인에 실패했습니다.'
    }
  }
}

const goToSignup = () => {
  router.push('/Signup')
}
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
