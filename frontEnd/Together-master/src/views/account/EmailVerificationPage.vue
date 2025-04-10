<template>
    <div class="email-verification-container">
      <div class="verification-card">
        <h2 class="title">회원가입</h2>
        <p class="subtitle">이메일 주소를 입력하고 인증을 진행해주세요</p>
  
        <div class="input-group">
          <label>이메일</label>
          <input v-model="email" type="email" placeholder="이메일을 입력해주세요" />
          <button @click="sendCode" :disabled="codeSent">인증 코드 요청</button>
        </div>
  
        <div v-if="codeSent" class="input-group">
          <label>인증 코드</label>
          <input v-model="code" type="text" placeholder="인증 코드를 입력해주세요" />
          <button @click="verifyCode">인증 확인</button>
        </div>
  
        <p v-if="message" class="message">{{ message }}</p>
      </div>
    </div>
  </template>
  
  <script setup>
  import { ref } from 'vue'
  import { useRouter } from 'vue-router'
  import api from '@/api'
  
  const router = useRouter()
  const email = ref('')
  const code = ref('')
  const codeSent = ref(false)
  const message = ref('')
  
  // 인증 코드 요청
  const sendCode = async () => {
    try {
      await api.post('/auth/email/send', null, {
        params: { email: email.value.trim() }
      })
      codeSent.value = true
      message.value = '인증 코드가 이메일로 전송되었습니다.'
    } catch (error) {
      message.value = error.response?.data || '이메일 전송 실패'
    }
  }
  
  // 인증 코드 확인 후 회원가입 페이지 이동
  const verifyCode = async () => {
    try {
      await api.post('/auth/email/verify', null, {
        params: {
          email: email.value.trim(),
          code: code.value.trim()
        }
      })
      message.value = '이메일 인증이 완료되었습니다.'
  
      setTimeout(() => {
        router.push({ name: 'SignupDetails', query: { email: email.value.trim() } })
      }, 1000)
    } catch (error) {
      message.value = error.response?.data || '인증 코드가 올바르지 않습니다.'
    }
  }
  </script>
  
  
  
  <style scoped>
  .email-verification-container {
    display: flex;
    justify-content: center;
    align-items: center;
    height: 100vh;
    background-color: #f5f5f5;
  }
  
  .verification-card {
    background-color: #fff;
    padding: 40px;
    border-radius: 16px;
    box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
    width: 400px;
    text-align: center;
  }
  
  .title {
    font-size: 22px;
    font-weight: bold;
    margin-bottom: 8px;
  }
  
  .subtitle {
    font-size: 14px;
    color: #888;
    margin-bottom: 24px;
  }
  
  .input-group {
    display: flex;
    flex-direction: column;
    margin-bottom: 16px;
    text-align: left;
  }
  
  .input-group input {
    padding: 10px;
    margin-top: 4px;
    border: 1px solid #ccc;
    border-radius: 8px;
  }
  
  .input-group button {
    margin-top: 8px;
    padding: 10px;
    background-color: #4e7cf1;
    color: #fff;
    border: none;
    border-radius: 8px;
    cursor: pointer;
    transition: background-color 0.3s;
  }
  
  .input-group button:disabled {
    background-color: #aaa;
    cursor: not-allowed;
  }
  
  .next-btn {
    margin-top: 20px;
    width: 100%;
    padding: 12px;
    background-color: #4e7cf1;
    color: #fff;
    border: none;
    border-radius: 8px;
    font-weight: bold;
    cursor: pointer;
    transition: background-color 0.3s;
  }
  
  .message {
    font-size: 14px;
    color: #4e7cf1;
    margin-top: 10px;
    transition: all 0.3s ease;
    text-align: center;
  }
  
  .fade-enter-active, .fade-leave-active {
    transition: opacity 0.4s;
  }
  .fade-enter-from, .fade-leave-to {
    opacity: 0;
  }
  </style>