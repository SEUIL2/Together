<template>
    <div class="signup-container">
      <!-- 로고 -->
      <div class="logo">
        <img src="@/assets/togetherlogo.png" alt="로고" />
      </div>
  
      <transition name="fade">
        <div class="signup-form" v-show="formVisible">
          <!-- 회원가입 제목 -->
          <h2 class="signup-title">회원가입</h2>
  
          <!-- 학생/교수 전환 버튼 -->
          <div class="toggle-buttons">
            <button :class="{ active: userType === 'student' }" @click="setUserType('student')">학생</button>
            <button :class="{ active: userType === 'professor' }" @click="setUserType('professor')">교수</button>
          </div>
  
          <!-- 회원가입 폼 -->
          <form @submit.prevent="signup">
            <div class="row-group">
              <div class="input-group">
                <label>*이름</label>
                <input v-model="form.userName" type="text" placeholder="이름을 입력해주세요" required />
              </div>
              <div class="input-group" v-if="userType === 'student'">
                <label>*학번</label>
                <input v-model="form.studentNumber" type="text" placeholder="학번을 입력해주세요" required />
              </div>
            </div>
  
            <!-- <div class="input-group small-input">
              <label>*이메일</label>
              <input v-model="form.userEmail" type="email" placeholder="이메일을 입력해주세요" required />
            </div> -->
  
            <div class="input-group small-input">
              <label>*아이디</label>
              <div class="input-with-button">
                <input v-model="form.userLoginId" type="text" placeholder="아이디를 입력해주세요" required />
                <button type="button" class="check-btn" @click="checkDuplicateId">중복검사</button>
              </div>
            </div>
  
            <div class="row-group">
              <div class="input-group">
                <label>*비밀번호</label>
                <input v-model="form.password" type="password" placeholder="비밀번호를 입력해주세요" required />
              </div>
              <div class="input-group">
                <label>*비밀번호 확인</label>
                <input v-model="form.confirmPassword" type="password" placeholder="비밀번호를 한 번 더 입력해주세요" required />
              </div>
            </div>
  
            <!-- 회원가입 버튼 -->
            <div class="button-container">
              <button type="submit" class="submit-btn">회원가입</button>
            </div>
  
            <p v-if="successMessage" style="color: green;">{{ successMessage }}</p>
            <p v-if="errorMessage" style="color: red;">{{ errorMessage }}</p>
          </form>
        </div>
      </transition>
    </div>
  </template>
  
  <script setup>
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import api from '@/api'

const router = useRouter()
const route = useRoute()

const userType = ref('student')
const successMessage = ref('')
const errorMessage = ref('')
const formVisible = ref(false)

const form = ref({
  userName: '',
  userEmail: '', // 쿼리로부터 주입
  userLoginId: '',
  password: '',
  confirmPassword: '',
  userRole: 'STUDENT',
  studentNumber: '',
  emailVerified: true
})

onMounted(() => {
  formVisible.value = true
  form.value.userEmail = route.query.email || ''
})

const setUserType = (type) => {
  userType.value = type
  form.value.userRole = type === 'student' ? 'STUDENT' : 'PROFESSOR'
  if (type !== 'student') {
    form.value.studentNumber = ''
  }
}

const checkDuplicateId = async () => {
  try {
    const response = await api.get(`/auth/check-username?username=${form.value.userLoginId}`)
    if (response.data.exists) {
      errorMessage.value = '이미 존재하는 아이디입니다.'
      successMessage.value = ''
    } else {
      successMessage.value = '사용 가능한 아이디입니다.'
      errorMessage.value = ''
    }
  } catch (error) {
    errorMessage.value = '아이디 중복 검사 실패'
  }
}

const signup = async () => {
  successMessage.value = ''
  errorMessage.value = ''

  if (!form.value.userEmail) {
    errorMessage.value = '이메일 인증이 누락되었습니다.'
    return
  }

  if (form.value.password !== form.value.confirmPassword) {
    errorMessage.value = '비밀번호가 일치하지 않습니다.'
    return
  }

  if (form.value.userRole === 'STUDENT' && !form.value.studentNumber) {
    errorMessage.value = '학생의 경우 학번을 입력해야 합니다.'
    return
  }

  try {
    const response = await api.post('/auth/signup', form.value, {
      headers: { 'Content-Type': 'application/json' }
    })
    successMessage.value = response.data
    errorMessage.value = ''
    alert('회원가입이 완료되었습니다! 로그인 페이지로 이동합니다.')
    router.push('/login')
  } catch (error) {
    errorMessage.value = error.response?.data || '회원가입 실패'
    successMessage.value = ''
  }
}
</script>
  
  <style scoped>
  .signup-container {
    display: flex;
    flex-direction: column;
    align-items: center;
    justify-content: center;
    height: 100vh;
  }
  .logo img {
    width: 80px;
    margin-bottom: 20px;
  }
  .signup-form {
    width: 450px;
    padding: 40px;
    border-radius: 16px;
    box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
    background: white;
  }
  .signup-title {
    text-align: center;
    font-size: 24px;
    margin-bottom: 24px;
  }
  .toggle-buttons {
    display: flex;
    justify-content: center;
    margin-bottom: 24px;
  }
  .toggle-buttons button {
    margin: 0 8px;
    padding: 8px 16px;
    border-radius: 8px;
    border: none;
    background: #ddd;
    cursor: pointer;
  }
  .toggle-buttons button.active {
    background: #4e7cf1;
    color: white;
  }
  .row-group {
    display: flex;
    gap: 10px;
    margin-bottom: 16px;
  }
  .input-group {
    flex: 1;
    display: flex;
    flex-direction: column;
  }
  .input-group input,
  .input-group select {
    padding: 10px;
    border-radius: 8px;
    border: 1px solid #ccc;
  }
  .input-group label {
    font-size: 14px;
    margin-bottom: 4px;
  }
  .input-with-button {
    display: flex;
    gap: 10px;
  }
  .small-input {
    margin-bottom: 16px;
  }
  .button-container {
    margin-top: 24px;
    text-align: center;
  }
  .submit-btn {
    width: 100%;
    padding: 12px;
    background-color: #4e7cf1;
    color: white;
    font-weight: bold;
    border: none;
    border-radius: 8px;
    cursor: pointer;
  }
  .check-btn {
    padding: 10px;
    background-color: #4e7cf1;
    color: white;
    border: none;
    border-radius: 8px;
    font-size: 14px;
    cursor: pointer;
  }
  
  /* 애니메이션 */
  .fade-enter-active, .fade-leave-active {
    transition: opacity 0.6s;
  }
  .fade-enter-from, .fade-leave-to {
    opacity: 0;
  }
  </style>