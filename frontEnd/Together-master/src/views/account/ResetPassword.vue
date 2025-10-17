<template>
  <div class="reset-page">
    <div class="reset-card">
      <!-- 로고 (원하는 곳에 교체) -->
      <div class="logo-wrapper">
        <img src="@/assets/togetherlogo.png" alt="Logo" class="logo" />
      </div>

      <!-- 1) 이메일 입력 STEP -->
      <template v-if="step === 'email'">
        <h2 class="title">비밀번호 찾기</h2>
        <p class="subtitle">회원가입 시 등록한 이메일 주소를 입력해주세요</p>
        <div class="form-group">
          <label for="email">이메일</label>
          <input
              id="email"
              v-model="email"
              type="email"
              placeholder="이메일을 입력해주세요"
              class="input-field"
          />
        </div>
        <button
            class="btn-primary"
            @click="sendEmail"
            :disabled="!isValidEmail"
        >
          확인
        </button>
      </template>

      <!-- 2) 인증번호 입력 STEP -->
      <template v-else-if="step === 'verify'">
        <h2 class="title">비밀번호 찾기</h2>
        <p class="subtitle">회원님의 이메일로 인증번호 6자리를 보내드렸습니다.</p>
        <div class="form-group">
          <label for="code">인증 번호</label>
          <div class="code-input-wrapper">
            <input
                id="code"
                v-model="code"
                type="text"
                maxlength="6"
                placeholder="인증 번호 6자리를 입력해주세요."
                :class="['input-field', { 'input-error': errorMessage }]"
            />
            <button
                class="btn-resend"
                @click="resendCode"
                :disabled="isResending"
            >
              재요청
            </button>
          </div>
          <p v-if="errorMessage" class="error-text">{{ errorMessage }}</p>
        </div>
        <button class="btn-primary" @click="toNewPassword" :disabled="!code">
          확인
        </button>
      </template>

      <!-- 3) 새 비밀번호 입력 STEP -->
      <template v-else-if="step === 'reset'">
        <h2 class="title">비밀번호 변경</h2>
        <p class="subtitle">새로운 비밀번호를 입력해주세요.</p>
        <div class="form-group">
          <label for="newPassword">새 비밀번호</label>
          <input
              id="newPassword"
              v-model="newPassword"
              type="password"
              placeholder="새 비밀번호"
              class="input-field"
          />
        </div>
        <div class="form-group">
          <label for="confirmPassword">비밀번호 확인</label>
          <input
              id="confirmPassword"
              v-model="confirmPassword"
              type="password"
              placeholder="비밀번호 확인"
              class="input-field"
          />
        </div>
        <p v-if="resetError" class="error-text">{{ resetError }}</p>
        <button
            class="btn-primary"
            @click="resetPassword"
            :disabled="!isValidNewPassword"
        >
          변경하기
        </button>
      </template>

      <!-- 4) 비밀번호 변경 완료 STEP -->
      <template v-else-if="step === 'success'">
        <h2 class="title">비밀번호 변경 완료</h2>
        <p class="subtitle">비밀번호가 성공적으로 변경되었습니다.</p>
        <button class="btn-primary" @click="goLogin">
          로그인 하러가기
        </button>
      </template>
    </div>
  </div>
</template>

<script>
import api from "@/api";
import router from "@/router"; // 프로젝트의 router 설정 경로에 맞춰주세요

export default {
  name: "ResetPassword",
  data() {
    return {
      step: "email",       // 'email' → 'verify' → 'reset' → 'success'
      email: "",
      code: "",
      isResending: false,
      errorMessage: "",    // 인증 코드 단계의 에러 메시지
      newPassword: "",
      confirmPassword: "",
      resetError: "",      // 비밀번호 변경 단계의 에러 메시지
    };
  },
  computed: {
    // 이메일 형식 간단 검증
    isValidEmail() {
      const re = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
      return re.test(this.email);
    },
    // 새 비밀번호가 두 필드(비밀번호, 확인)가 비어있지 않고 일치하는지
    isValidNewPassword() {
      return (
          this.newPassword.length >= 6 &&
          this.confirmPassword === this.newPassword
      );
    },
  },
  methods: {
    /**
     * 1) 이메일로 인증 코드 요청
     *    - 백엔드: POST /auth/find-password?email=…
     */
    async sendEmail() {
      this.errorMessage = "";
      if (!this.isValidEmail) {
        this.errorMessage = "유효한 이메일을 입력해주세요.";
        return;
      }
      try {
        await api.post("/auth/find-password", null, {
          params: { email: this.email },
        });
        this.step = "verify";
        this.code = "";
      } catch (err) {
        if (err.response && err.response.data) {
          this.errorMessage = err.response.data;
        } else {
          this.errorMessage = "인증 코드를 전송하는 중 오류가 발생했습니다.";
        }
      }
    },

    /**
     * 2) 인증번호 확인 및 다음 단계(비밀번호 입력)로 이동
     *    - 실제 비밀번호 변경은 3단계에서 수행하므로,
     *      여기서는 단순히 코드를 입력했는지 확인하고 다음 화면으로 넘어갑니다.
     */
    async toNewPassword() {
      this.errorMessage = "";
      if (!this.code) {
        this.errorMessage = "인증번호를 입력해주세요.";
        return;
      }
      // 백엔드에서 실제 코드 검증은 reset-password 단계에서 수행하므로,
      // 여기서는 단순히 다음 단계로 넘어가도록 합니다.
      // 그러나, 미리 코드 검증만 하고 싶다면 /auth/find-password/verify 엔드포인트를 별도로 만들고 호출하세요.
      this.step = "reset";
    },

    /**
     * 3) 새 비밀번호 변경
     *    - 백엔드: POST /auth/reset-password?email=…&code=…&newPassword=…
     */
    async resetPassword() {
      this.resetError = "";
      if (!this.isValidNewPassword) {
        this.resetError = "비밀번호가 일치하지 않거나 형식이 올바르지 않습니다.";
        return;
      }
      try {
        await api.post("/auth/reset-password", null, {
          params: {
            email: this.email,
            code: this.code,
            newPassword: this.newPassword,
          },
        });
        this.step = "success";
      } catch (err) {
        if (err.response && err.response.status === 400) {
          this.resetError = err.response.data || "인증 코드가 올바르지 않습니다.";
        } else {
          this.resetError = "비밀번호 변경 중 오류가 발생했습니다.";
        }
      }
    },

    /**
     * 4) 로그인 페이지로 이동
     */
    goLogin() {
      router.push("/login"); // 실제 로그인 라우트로 맞춰주세요
    },

    /**
     * 인증번호 재전송
     */
    async resendCode() {
      this.isResending = true;
      this.errorMessage = "";
      try {
        await api.post("/auth/find-password", null, {
          params: { email: this.email },
        });
      } catch (err) {
        if (err.response && err.response.data) {
          this.errorMessage = err.response.data;
        } else {
          this.errorMessage = "재요청 중 오류가 발생했습니다.";
        }
      } finally {
        // 버튼 비활성화 풀어주기 (1초 뒤)
        setTimeout(() => {
          this.isResending = false;
        }, 1000);
      }
    },
  },
};
</script>

<style scoped>
.reset-page {
  display: flex;
  justify-content: center;
  align-items: center;
  height: 100vh;
  background-color: #f7f7f7;
}

.reset-card {
  width: 500px;
  background: #ffffff;
  border-radius: 12px;
  box-shadow: 0 10px 20px rgba(0, 0, 0, 0.1);
  padding: 40px 50px;
  text-align: center;
}

/* 로고 */
.logo-wrapper {
  margin-bottom: 30px;
}
.logo {
  width: 48px;
  height: 48px;
}

/* 제목/부제목 */
.title {
  font-size: 24px;
  font-weight: 600;
  margin-bottom: 8px;
}
.subtitle {
  font-size: 14px;
  color: #777777;
  margin-bottom: 30px;
}

/* form-group */
.form-group {
  margin-bottom: 20px;
  text-align: left;
}
.form-group label {
  display: block;
  font-size: 14px;
  margin-bottom: 6px;
  color: #333333;
}
.input-field {
  width: 100%;
  padding: 12px 14px;
  font-size: 16px;
  border: 1px solid #ced4da;
  border-radius: 6px;
  outline: none;
  transition: border-color 0.2s;
}
.input-field:focus {
  border-color: #4978fc;
}

/* 인증번호 입력+재요청 버튼 wrapper */
.code-input-wrapper {
  display: flex;
}
.code-input-wrapper .input-field {
  flex: 1;
}
.btn-resend {
  margin-left: 10px;
  padding: 0 12px;
  background-color: #cccccc;
  border: none;
  color: #ffffff;
  border-radius: 6px;
  font-size: 14px;
  cursor: pointer;
  transition: background-color 0.2s;
}
.btn-resend:disabled {
  background-color: #e0e0e0;
  cursor: not-allowed;
}

/* 에러 스타일 */
.input-error {
  border-color: #e74c3c !important;
}
.error-text {
  margin-top: 6px;
  font-size: 13px;
  color: #e74c3c;
}

/* 새 비밀번호 입력 STEP */
/* (이전과 동일하게 input-field 스타일이 적용됨) */

/* 성공/완료 텍스트 */
.found-text {
  font-size: 18px;
  margin: 20px 0;
  color: #333333;
}

/* 기본 버튼 */
.btn-primary {
  width: 100%;
  padding: 14px;
  background-color: #4978fc;
  color: #ffffff;
  border: none;
  border-radius: 8px;
  font-size: 16px;
  cursor: pointer;
  transition: background-color 0.2s;
}
.btn-primary:disabled {
  background-color: #a0c1ff;
  cursor: not-allowed;
}
.btn-primary:hover:enabled {
  background-color: #3c65d4;
}
</style>
