<template>
  <div class="modal-backdrop" @click.self="close">
    <div class="modal">
      <button class="close-btn" @click="close">✖</button>
      <div class="modal-content">
        <h2 class="modal-title">🗳️ 새 투표 만들기</h2>
        <form @submit.prevent="submit">
          <!-- 제목 -->
          <label class="field-label">투표 제목</label>
          <input
            v-model="title"
            type="text"
            maxlength="60"
            class="input"
            placeholder="예: 오늘 점심 뭐 먹을까?"
            autofocus
            required
          />

          <!-- 옵션 리스트 -->
          <label class="field-label" style="margin-top:1rem;">항목(옵션)</label>
          <div class="option-list">
            <div v-for="(option, i) in options" :key="i" class="option-row">
              <input
                v-model="options[i]"
                type="text"
                maxlength="24"
                class="input option-input"
                :placeholder="`항목 ${i + 1}`"
                required
              />
              <button
                type="button"
                class="remove-btn"
                @click="removeOption(i)"
                :disabled="options.length <= 2"
                v-if="options.length > 2"
                title="삭제"
              >✖</button>
            </div>
            <button
              type="button"
              class="add-btn"
              @click="addOption"
              :disabled="options.length >= 10"
            >+ 옵션 추가</button>
          </div>

          <!-- 추가 설정 -->
          <div class="setting-row">
            <label>
              <input type="checkbox" v-model="isAnonymous" />
              익명 투표
            </label>
            <label style="margin-left: 16px;">
              마감 기한:
              <input
                type="date"
                v-model="deadline"
                :min="today"
                class="input date-input"
                required
              />
            </label>
          </div>

          <!-- 에러/안내 -->
          <div v-if="errorMsg" class="error-msg">{{ errorMsg }}</div>

          <!-- 버튼 -->
          <div class="btn-row">
            <button type="button" class="cancel-btn" @click="close">취소</button>
            <button
              type="submit"
              class="submit-btn"
              :disabled="!canSubmit"
            >투표 생성</button>
          </div>
        </form>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed } from 'vue'
import axios from 'axios'

const emit = defineEmits(['close', 'created'])

const title = ref('')
const options = ref(['', '']) // 최소 2개
const isAnonymous = ref(false)
const deadline = ref('')
const errorMsg = ref('')

const today = new Date().toISOString().split('T')[0] // yyyy-mm-dd

function addOption() {
  if (options.value.length < 10) options.value.push('')
}
function removeOption(i) {
  if (options.value.length > 2) options.value.splice(i, 1)
}

const canSubmit = computed(() => {
  return (
    title.value.trim().length > 0 &&
    options.value.filter(o => o.trim()).length >= 2 &&
    options.value.every(o => o.trim().length > 0) &&
    deadline.value
  )
})

async function submit() {
  errorMsg.value = ''
  if (!canSubmit.value) {
    errorMsg.value = '모든 항목을 올바르게 입력해주세요.'
    return
  }
  // durationDays 계산
  const now = new Date()
  const end = new Date(deadline.value)
  const durationDays = Math.ceil((end - now) / (1000 * 60 * 60 * 24))
  if (durationDays < 0) {
    errorMsg.value = '마감 기한이 올바르지 않습니다.'
    return
  }
  try {
    const projectId = Number(localStorage.getItem('currentProjectId'))
    await axios.post(
      '/votes/create',
      {
        projectId,
        title: title.value,
        isAnonymous: isAnonymous.value,
        durationDays,
        options: options.value,
      },
      { headers: { Authorization: localStorage.getItem('authHeader') } }
    )
    emit('created')
    close()
  } catch (err) {
    errorMsg.value = '생성 실패! 다시 시도해주세요.'
  }
}
function close() {
  emit('close')
}
</script>

<style scoped>
.modal-backdrop {
  position: fixed; z-index: 4000; inset: 0;
  background: rgba(32,32,40, 0.36);
  display: flex; align-items: center; justify-content: center;
}
.modal {
  min-width: 390px;
  max-width: 440px;
  background: #fff;
  border-radius: 18px;
  box-shadow: 0 6px 32px rgba(60,64,80,0.13);
  padding: 1.3rem 1.3rem 1.1rem 1.3rem;
  position: relative;
  animation: popup .2s;
}
@keyframes popup { from { scale:0.95; opacity:0.4;} to { scale:1; opacity:1;} }

.close-btn {
  position: absolute;
  top: 1.1rem; right: 1.15rem;
  background: none;
  border: none;
  font-size: 1.20rem;
  color: #444; cursor: pointer;
  z-index: 10;
}
.modal-title {
  font-size: 1.2rem; font-weight: 700; text-align: center; margin-bottom: 0.8rem;
}
.field-label {
  font-size: 0.98rem; color: #454d60; font-weight: 600; margin-bottom: 0.35rem; display: block;
}
.input {
  display: block; width: 100%; font-size: 1rem; padding: 0.54em 0.7em;
  border: 1.4px solid #cfd5ee; border-radius: 8px; margin-bottom: 0.2rem;
  margin-top: 0.09rem; background: #fafbfe;
}
.input:focus { outline: 2px solid #4f46e5; }
.option-list {
  display: flex; flex-direction: column; gap: 7px;
}
.option-row {
  display: flex; align-items: center; gap: 7px;
}
.option-input { flex: 1 0 60px; }
.remove-btn {
  background: none; border: none; font-size: 1.03rem; color: #e23333; cursor: pointer;
  padding: 0.19em 0.4em; border-radius: 5px; transition: background 0.12s;
}
.remove-btn:disabled { color: #ccc; cursor: not-allowed; }
.add-btn {
  background: #f4f6ff; border: 1px dashed #9eb3ea;
  border-radius: 7px; padding: 0.32em 0.85em;
  font-size: 0.97rem; color: #3463e6; margin-top: 3px;
  cursor: pointer; font-weight: 500; align-self: flex-start;
}
.add-btn:disabled { color: #bbb; border-color: #eee; cursor: not-allowed; }
.setting-row {
  margin: 1.15rem 0 0.5rem 0;
  font-size: 0.97rem;
  color: #444;
  display: flex; align-items: center; gap: 8px;
}
.date-input {
  width: 130px; margin-left: 6px;
  padding: 0.42em 0.54em;
}
.error-msg {
  color: #e23333;
  font-size: 0.97rem;
  margin: 0.35rem 0 0.15rem 0;
  text-align: left;
}
.btn-row {
  display: flex; justify-content: flex-end; gap: 11px;
  margin-top: 1.2rem;
}
.cancel-btn {
  background: #ececec; color: #666; border: none; border-radius: 7px;
  font-size: 1rem; padding: 0.5em 1.1em; cursor: pointer;
  font-weight: 500; transition: background 0.2s;
}
.cancel-btn:hover { background: #e1e9f9; }
.submit-btn {
  background: #4f46e5; color: #fff; border: none; border-radius: 7px;
  font-size: 1rem; padding: 0.5em 1.3em; cursor: pointer;
  font-weight: bold; transition: background 0.2s;
}
.submit-btn:disabled {
  background: #e7e7ee; color: #aaa; cursor: not-allowed;
}
</style>
