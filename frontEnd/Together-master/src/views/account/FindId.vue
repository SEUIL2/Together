<template>
  <div class="find-id-page">
    <div class="find-id-card">
      <!-- 로고 (원하는 곳에 교체) -->
      <div class="logo-wrapper">
        <img src="@/assets/togetherlogo.png" alt="Logo" class="logo" />
      </div>

      <!-- 이메일 입력 STEP -->
      <template v-if="step === 'email'">
        <h2 class="title">아이디 찾기</h2>
        <p class="subtitle">회원가입시 등록한 이메일 주소를 입력해주세요</p>
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
        <button class="btn-primary" @click="sendEmail" :disabled="!isValidEmail">
          확인
        </button>
      </template>

      <!-- 인증번호 입력 STEP -->
      <template v-else-if="step === 'verify'">
        <h2 class="title">아이디 찾기</h2>
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
            <button class="btn-resend" @click="resendCode" :disabled="isResending">
              재요청
            </button>
          </div>
          <p v-if="errorMessage" class="error-text">{{ errorMessage }}</p>
        </div>
        <button class="btn-primary" @click="verifyCode" :disabled="!code">
          확인
        </button>
      </template>

      <!-- 성공 STEP -->
      <template v-else-if="step === 'success'">
        <h2 class="title">아이디 찾기</h2>
        <p class="subtitle">이메일 인증이 완료되었습니다.</p>
        <p class="found-text">
          회원님의 아이디는 <strong>{{ foundId }}</strong> 입니다.
        </p>
        <button class="btn-primary" @click="goLogin">
          로그인 하러가기
        </button>
      </template>
    </div>
  </div>
</template>

<script>
import api from "@/api";
import router from "@/router"; // 프로젝트의 router 인스턴스 경로에 맞춰 경로 수정

export default {
  name: "FindId",
  data() {
    return {
      step: "email",       // 'email', 'verify', 'success'
      email: "",
      code: "",
      errorMessage: "",
      foundId: "",
      isResending: false,
    };
  },
  computed: {
    // 이메일 형식 간단 검증
    isValidEmail() {
      const re = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
      return re.test(this.email);
    },
  },
  methods: {
    // 1) 이메일로 인증 코드 요청
    async sendEmail() {
      this.errorMessage = "";
      if (!this.isValidEmail) {
        this.errorMessage = "유효한 이메일을 입력해주세요.";
        return;
      }

      try {
        await api.post("/auth/find-id", null, { params: { email: this.email } });
        // 호출 성공 시 인증번호 입력 STEP으로 전환
        this.step = "verify";
        this.code = "";
        this.errorMessage = "";
      } catch (err) {
        // 400 에러 등
        if (err.response && err.response.data) {
          this.errorMessage = err.response.data;
        } else {
          this.errorMessage = "인증 코드를 전송하는 중 오류가 발생했습니다.";
        }
      }
    },

    // 2) 인증번호 확인
    async verifyCode() {
      this.errorMessage = "";
      if (!this.code) {
        this.errorMessage = "인증번호를 입력해주세요.";
        return;
      }

      try {
        const res = await api.post(
            "/auth/find-id/verify",
            null,
            {
              params: {
                email: this.email,
                code: this.code,
              },
            }
        );
        // 성공 시 res.data에 "회원님의 아이디는 xxx 입니다." 형태 문자열이 오지만,
        // 실제로는 아이디만 따로 받고 싶다면 백엔드를 수정해야 합니다.
        // 여기서는 백엔드 문자열 파싱이 아니라, 새로운 API를 만들어서 JSON { userId: xxx } 형태로 반환하는 방법도 추천드립니다.
        // 현재 예제에서는 문자열에서 아이디만 파싱하는 간단한 방법으로 구현:
        const msg = res.data; // "회원님의 아이디는 mooyoung01 입니다."
        const match = msg.match(/회원님의 아이디는\s+(.+)\s+입니다/);
        if (match && match[1]) {
          this.foundId = match[1].trim();
        } else {
          this.foundId = msg; // 파싱 실패 시 전체 메시지
        }
        this.step = "success";
      } catch (err) {
        if (err.response && err.response.status === 400) {
          this.errorMessage = "인증번호가 일치하지 않습니다";
        } else {
          this.errorMessage = "인증 확인 중 오류가 발생했습니다.";
        }
      }
    },

    // 3) 인증번호 재전송
    async resendCode() {
      this.isResending = true;
      this.errorMessage = "";
      try {
        await api.post("/auth/find-id", null, {
          params: { email: this.email },
        });
        // 재전송 후에도 동일하게 인증번호 입력 화면을 유지
      } catch (err) {
        if (err.response && err.response.data) {
          this.errorMessage = err.response.data;
        } else {
          this.errorMessage = "재요청 중 오류가 발생했습니다.";
        }
      } finally {
        setTimeout(() => {
          this.isResending = false;
        }, 1000);
      }
    },

    // 4) 로그인 페이지로 이동
    goLogin() {
      router.push("/login"); // 실제 로그인 경로에 맞춰 수정
    },
  },
};
</script>

<style scoped>
.find-id-page {
  display: flex;
  justify-content: center;
  align-items: center;
  height: 100vh;
  background-color: #f7f7f7;
}

.find-id-card {
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

/* 성공 시 아이디 노출 */
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
