<template>
  <div class="modal-overlay" @click.self="$emit('close')">
    <div class="modal-container">
      <h2 v-if="inviteType === 'teamMember'">팀원 초대</h2>
      <h2 v-if="inviteType === 'professor'">교수 초대</h2>

      <form @submit.prevent="handleInvite">
        <div class="form-group">
          <label for="name">이름</label>
          <input v-model="inviteData.name" type="text" id="name" required />
        </div>

        <div v-if="inviteType === 'teamMember'" class="form-group">
          <label for="studentId">학번</label>
          <input v-model="inviteData.studentId" type="text" id="studentId" required />
        </div>

        <div v-if="inviteType === 'teamMember'" class="form-group">
          <label for="role">역할</label>
          <select v-model="inviteData.role" id="role" required>
            <option value="프론트엔드">프론트엔드</option>
            <option value="백엔드">백엔드</option>
          </select>
        </div>

        <div class="button-group">
          <button type="submit" class="invite-btn">초대하기</button>
          <button type="button" @click="$emit('close')" class="cancel-btn">취소</button>
        </div>
      </form>
    </div>
  </div>
</template>

<script setup>
import { ref, defineProps, defineEmits } from 'vue'

const props = defineProps({
  inviteType: String
})

const emit = defineEmits(['close', 'invite'])

const inviteData = ref({
  name: '',
  studentId: '',
  role: '프론트엔드'
})

function handleInvite() {
  emit('invite', { ...inviteData.value })
  inviteData.value = { name: '', studentId: '', role: '프론트엔드' }
}
</script>

<style scoped>
.modal-overlay {
  position: fixed;
  top: 0;
  left: 0;
  width: 100vw;
  height: 100vh;
  background-color: rgba(0, 0, 0, 0.5);
  display: flex;
  justify-content: center;
  align-items: center;
  z-index: 200;
}

.modal-container {
  background: #fff;
  padding: 20px;
  border-radius: 10px;
  width: 100%;
  max-width: 400px;
  box-shadow: 0 4px 10px rgba(0, 0, 0, 0.2);
}

h2 {
  text-align: center;
  margin-bottom: 20px;
}

.form-group {
  margin-bottom: 12px;
}

.form-group label {
  display: block;
  font-weight: bold;
  margin-bottom: 6px;
}

input, select {
  width: 100%;
  padding: 8px;
  border: 1px solid #ccc;
  border-radius: 4px;
}

.button-group {
  display: flex;
  justify-content: space-between;
  margin-top: 20px;
}

.invite-btn {
  background-color: #3f8efc;
  color: #fff;
  border: none;
  padding: 8px 16px;
  border-radius: 4px;
  cursor: pointer;
}

.cancel-btn {
  background-color: #ccc;
  border: none;
  padding: 8px 16px;
  border-radius: 4px;
  cursor: pointer;
}
</style>
