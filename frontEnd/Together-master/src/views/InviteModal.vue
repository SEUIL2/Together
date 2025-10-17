<template>
  <div v-if="isOpen" class="modal-overlay">
    <div class="modal-content">
      <!-- 닫기 버튼 -->
      <button class="close-btn" @click="closeModal" aria-label="Close">&times;</button>

      <h3 class="modal-title">팀원/교수 초대</h3>

      <input
          type="email"
          v-model="inviteEmail"
          class="invite-input"
          placeholder="초대할 사용자의 이메일을 입력하세요"
      />

      <button class="invite-btn" @click="sendInvitation">초대하기</button>

      <p v-if="message" :class="['message', success ? 'success' : 'error']">
        {{ message }}
      </p>
    </div>
  </div>
</template>

<script>
import api from '@/api';

export default {
  name: 'InviteModal',
  props: {
    isOpen: {
      type: Boolean,
      required: true,
    },
  },
  data() {
    return {
      inviteEmail: '',
      message: '',
      success: false,
    };
  },
  methods: {
    async sendInvitation() {
      if (!this.inviteEmail) {
        this.message = '이메일을 입력해주세요.';
        this.success = false;
        return;
      }

      try {
        const response = await api.post(
            '/projects/invite',
            {},
            { params: { email: this.inviteEmail } }
        );
        this.message = response.data;
        this.success = true;
        this.inviteEmail = '';
      } catch (error) {
        this.message = error.response?.data || '초대에 실패했습니다.';
        this.success = false;
      }
    },
    closeModal() {
      this.$emit('close');
      this.inviteEmail = '';
      this.message = '';
    },
  },
};
</script>

<style scoped>
.modal-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background-color: rgba(30, 30, 30, 0.6);
  display: flex;
  justify-content: center;
  align-items: center;
  z-index: 1000;
}

.modal-content {
  position: relative;
  width: 90%;
  max-width: 400px;
  background: #ffffff;
  border-radius: 16px;
  box-shadow: 0 8px 20px rgba(0, 0, 0, 0.2);
  padding: 30px 25px;
  box-sizing: border-box;
  text-align: center;
}

.close-btn {
  position: absolute;
  top: 14px;
  right: 16px;
  font-size: 22px;
  background: none;
  border: none;
  cursor: pointer;
  color: #888;
  transition: color 0.2s;
}
.close-btn:hover {
  color: #333;
}

.modal-title {
  font-size: 20px;
  margin-bottom: 20px;
  font-weight: bold;
  color: #222;
}

.invite-input {
  width: 100%;
  padding: 10px 12px;
  font-size: 14px;
  border: 1px solid #ddd;
  border-radius: 8px;
  box-sizing: border-box;
  margin-bottom: 15px;
  transition: border 0.3s;
}
.invite-input:focus {
  border-color: #cdb893;
  outline: none;
}

.invite-btn {
  width: 100%;
  background-color: #1d6fe6; /* 기본 파란색 */
  color: #fff;
  padding: 10px 12px;
  font-size: 15px;
  border: none;
  border-radius: 8px;
  cursor: pointer;
  transition: background-color 0.2s;
}

.invite-btn:hover {
  background-color: #155bc4; /* hover 시 더 진한 파랑 */
}


.message {
  margin-top: 15px;
  font-size: 14px;
}
.success {
  color: #2e7d32;
}
.error {
  color: #d32f2f;
}
</style>
