<template>
  <div class="member-invitation">
    <h2>멤버 초대 페이지</h2>
    <div class="search-section">
      <input
          type="email"
          v-model="searchEmail"
          placeholder="초대할 사용자의 이메일을 입력하세요"
      />
      <button @click="searchUser">검색</button>
    </div>
    <div v-if="errorMessage" class="error">
      {{ errorMessage }}
    </div>
    <div v-if="successMessage" class="success">
      {{ successMessage }}
    </div>
    <div v-if="users && users.length">
      <h3>검색 결과</h3>
      <ul>
        <li v-for="user in users" :key="user.userId">
          <span>{{ user.userName }} ({{ user.userEmail }})</span>
          <button @click="inviteUser(user.userEmail)">초대하기</button>
        </li>
      </ul>
    </div>
  </div>
</template>

<script>
import axios from "axios";
import { ref } from "vue";

export default {
  name: "MemberInvitation",
  // 초대할 프로젝트의 ID는 상위 컴포넌트에서 prop으로 전달한다고 가정합니다.
  props: {
    projectId: {
      type: Number,
      required: true,
    },
  },
  setup(props) {
    const searchEmail = ref("");
    const users = ref([]);
    const errorMessage = ref("");
    const successMessage = ref("");

    // Basic Auth 설정 – 실제 운영환경에서는 별도 인증 관리 방식(예: 토큰 기반) 사용 권장
    const axiosInstance = axios.create({
      // 백엔드 서버 URL을 baseURL로 지정할 수 있습니다.
      auth: {
        username: "YOUR_USERNAME",
        password: "YOUR_PASSWORD",
      },
    });

    // 이메일로 사용자 검색 (projectId 없이 호출하면 사용자 리스트만 반환)
    const searchUser = async () => {
      errorMessage.value = "";
      successMessage.value = "";
      users.value = [];
      if (!searchEmail.value) {
        errorMessage.value = "이메일을 입력하세요.";
        return;
      }
      try {
        const response = await axiosInstance.get("/projects/search", {
          params: {
            email: searchEmail.value,
          },
        });
        users.value = response.data;
        if (users.value.length === 0) {
          errorMessage.value = "일치하는 사용자를 찾지 못했습니다.";
        }
      } catch (error) {
        errorMessage.value =
            error.response && error.response.data
                ? error.response.data
                : "사용자 검색 중 오류가 발생했습니다.";
      }
    };

    // 검색된 사용자 중 선택한 사용자에게 초대 요청
    const inviteUser = async (email) => {
      errorMessage.value = "";
      successMessage.value = "";
      try {
        const response = await axiosInstance.post(
            `/projects/${props.projectId}/invite`,
            null,
            {
              params: {
                email: email,
              },
            }
        );
        successMessage.value = response.data;
      } catch (error) {
        errorMessage.value =
            error.response && error.response.data
                ? error.response.data
                : "초대 요청 중 오류가 발생했습니다.";
      }
    };

    return {
      searchEmail,
      users,
      errorMessage,
      successMessage,
      searchUser,
      inviteUser,
    };
  },
};
</script>

<style scoped>
.member-invitation {
  max-width: 600px;
  margin: 0 auto;
  padding: 20px;
}

.search-section {
  margin-bottom: 20px;
}

input[type="email"] {
  padding: 8px;
  margin-right: 10px;
  width: 300px;
}

button {
  padding: 8px 12px;
}

.error {
  color: red;
  margin-bottom: 10px;
}

.success {
  color: green;
  margin-bottom: 10px;
}
</style>
