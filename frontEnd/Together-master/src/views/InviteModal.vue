<template>
  <div v-if="isOpen" class="modal-overlay">
    <div class="modal-content">
      <h3>팀원/교수 초대</h3>
      <input
          type="email"
          v-model="inviteEmail"
          placeholder="초대할 사용자의 이메일"
      />
      <div class="modal-buttons">
        <button @click="sendInvitation">초대하기</button>
        <button @click="closeModal">취소</button>
      </div>
      <p v-if="message" :class="{'success': success, 'error': !success}">
        {{ message }}
      </p>
    </div>
  </div>
</template>

<script>
import axios from 'axios';

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
        const response = await axios.post(
            '/projects/invite',
            {},
            {
              params: {
                email: this.inviteEmail,
              },
            }
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
  background: rgba(0, 0, 0, 0.5);
}
.modal-content {
  background: white;
  padding: 20px;
  max-width: 400px;
  margin: 100px auto;
  border-radius: 8px;
}
.modal-buttons {
  margin-top: 10px;
  display: flex;
  gap: 10px;
}
.success {
  color: green;
}
.error {
  color: red;
}
</style>