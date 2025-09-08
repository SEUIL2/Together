<template>
  <div v-if="isOpen" class="modal-overlay">
    <div class="modal-content">
      <button class="close-btn" @click="$emit('close')" aria-label="Close">&times;</button>
      <h3 class="modal-title">리더 위임</h3>
      <ul class="member-list">
        <li v-for="student in students" :key="student.userId" class="member-item">
          <label>
            <input type="radio" v-model="selected" :value="student.userId" />
            {{ student.userName }} ({{ student.studentNumber }})
          </label>
        </li>
      </ul>
      <div class="actions">
        <button class="cancel-btn" @click="$emit('close')">취소</button>
        <button class="confirm-btn" :disabled="!selected" @click="confirm">위임하기</button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, watch } from 'vue'

const props = defineProps({
  isOpen: { type: Boolean, required: true },
  students: { type: Array, required: true }
})

const emit = defineEmits(['close', 'change'])
const selected = ref(null)

watch(() => props.isOpen, (val) => {
  if (!val) selected.value = null
})

function confirm() {
  if (selected.value) {
    emit('change', selected.value)
  }
}
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
  text-align: center;
}

.member-list {
  list-style: none;
  padding: 0;
  margin: 0 0 20px 0;
  max-height: 250px;
  overflow-y: auto;
}

.member-item {
  margin-bottom: 8px;
}

.actions {
  display: flex;
  justify-content: flex-end;
  gap: 10px;
}

.cancel-btn,
.confirm-btn {
  padding: 8px 16px;
  border-radius: 8px;
  border: none;
  cursor: pointer;
  font-size: 0.9rem;
}

.cancel-btn {
  background-color: #f0f0f0;
  color: #555;
}

.confirm-btn {
  background-color: #3f8efc;
  color: white;
}

.confirm-btn:disabled {
  background-color: #aac4f6;
  cursor: not-allowed;
}
</style>