<template>
  <div class="signup-container">
    <!-- 로고 -->
    <div class="logo">
      <img src="@/assets/togetherlogo.png" alt="로고" />
    </div>

    <div class="signup-form">
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

        <div class="input-group small-input2">
          <label>*E-mail</label>
          <input v-model="form.userEmail" type="email" placeholder="이메일을 입력해주세요" required />
        </div>

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
  </div>
</template>

<script setup>
import { ref } from "vue";
import api from "@/api.js"; // vite.config.js에 alias 설정 필요 (@ -> src 디렉토리)

// 사용자 유형 (학생 또는 교수)
const userType = ref("student");

// 성공/오류 메시지
const successMessage = ref("");
const errorMessage = ref("");

// 폼 데이터 (백엔드의 UserSignUpRequestDto와 일치)
const form = ref({
  userName: "",
  userEmail: "",
  userLoginId: "",
  password: "",
  confirmPassword: "",
  userRole: "STUDENT",
  studentNumber: ""
});

// 사용자 유형 설정 및 폼 데이터 업데이트
const setUserType = (type) => {
  userType.value = type;
  if (type === "professor") {
    // 교수의 경우 학번은 필요 없으므로 초기화
    form.value.studentNumber = "";
  }
};

// 회원가입 요청 함수
const signup = async () => {
  // 메시지 초기화
  successMessage.value = "";
  errorMessage.value = "";

  // 비밀번호 확인 검증
  if (form.value.password !== form.value.confirmPassword) {
    errorMessage.value = "비밀번호가 일치하지 않습니다.";
    return;
  }

  // 사용자 역할 설정
  form.value.userRole = userType.value === "student" ? "STUDENT" : "PROFESSOR";
  if (form.value.userRole !== "STUDENT") {
    form.value.studentNumber = "";
  }

  try {
    // 백엔드 /auth/signup API 호출
    const response = await api.post("/auth/signup", form.value, {
      headers: { "Content-Type": "application/json" }
    });
    successMessage.value = response.data; // 예: "회원가입이 완료되었습니다."
    errorMessage.value = "";
  } catch (error) {
    errorMessage.value = error.response?.data || "회원가입 실패";
    successMessage.value = "";
  }
};

// 아이디 중복 검사 함수
const checkDuplicateId = async () => {
  try {
    const response = await api.get(`/auth/check-username?username=${form.value.userLoginId}`);
    if (response.data.exists) {
      errorMessage.value = "이미 존재하는 아이디입니다.";
    } else {
      successMessage.value = "사용 가능한 아이디입니다.";
      errorMessage.value = "";
    }
  } catch (error) {
    errorMessage.value = "아이디 중복 검사 실패";
  }
};
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
  box-shadow: 2px 2px 5px rgba(0, 0, 0, 0.2);
}

.toggle-buttons button:active {
  box-shadow: 1px 1px 3px rgba(0, 0, 0, 0.3);
}

.signup-title,
.toggle-buttons {
  display: flex;
  justify-content: center;
  align-items: center;
  width: 100%;
  text-align: center;
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

/* 이름과 학번 가로 정렬 */
.row-group {
  display: flex;
  gap: 20px;
  width: 100%;
}

.row-group .input-group {
  flex: 1;
}

/* 이메일 및 아이디 칸 너비 조정 */
.small-input {
  max-width: 355px;
  width: 100%;
}

.small-input2 {
  max-width: 280px;
  width: 100%;
}

/* 아이디 입력 칸과 중복검사 버튼 */
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
  margin-top: 0;
  padding: 10px;
  font-size: 0.7rem;
  border: none;
  background-color: #555;
  color: white;
  border-radius: 5px;
  cursor: pointer;
}

/* 버튼 컨테이너 */
.button-container {
  display: flex;
  justify-content: center;
  margin-top: 20px;
}

/* 회원가입 버튼 */
.submit-btn {
  width: 80%;
  max-width: 250px;
  padding: 10px;
  font-size: 1rem;
  border: none;
  border-radius: 5px;
  background-color: #3f8efc;
  color: white;
  cursor: pointer;
}
</style>
