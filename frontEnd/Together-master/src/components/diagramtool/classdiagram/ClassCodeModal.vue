<template>
  <div class="modal-overlay" @click.self="close">
    <div class="modal">
      <div class="modal-header">
        <span>코드 변환 결과</span>
        <button class="close-btn" @click="close">✕</button>
      </div>

      <!-- 언어 선택 드롭다운 -->
      <div class="row" style="margin-bottom:14px;">
        <label style="color:#b6b6b6; font-size:14px; margin-right:10px;">
          언어 선택
        </label>
        <select v-model="selectedLang" :disabled="loading" class="lang-select">
          <option v-for="lang in availableLangs" :key="lang" :value="lang">
            {{ displayLang(lang) }}
          </option>
        </select>
        <button class="refresh-btn" @click="refreshCode" :disabled="loading || !selectedLang">
          <span v-if="!loading">🔄 코드 변환</span>
          <span v-else>⏳ 변환 중...</span>
        </button>
      </div>

      <!-- 코드 뷰/에러/로딩 -->
      <div v-if="error" class="error-box">{{ error }}</div>
      <div v-else>
        <template v-if="codeResults[selectedLang]">
          <pre v-if="!loading" class="code-view">
            <code :class="'language-' + getPrismLang(selectedLang)" v-html="highlightedCode"></code>
          </pre>
        </template>
        <template v-else>
          <div class="no-code">아직 변환된 코드가 없습니다.<br>코드 변환 버튼을 눌러 코드를 생성하세요.</div>
        </template>
        <div v-if="loading" class="loading-box">
          <span class="spinner"></span> AI 코드 생성 중...
        </div>
      </div>

      <div class="actions">
        <button @click="copyCode" :disabled="loading || !codeResults[selectedLang]">복사</button>
        <button @click="downloadCode" :disabled="loading || !codeResults[selectedLang]">다운로드</button>
      </div>
      <div v-if="copyMsg" class="copy-msg">{{ copyMsg }}</div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, watch, onMounted } from 'vue'
import api from '@/api'
import Prism from 'prismjs'
import 'prismjs/components/prism-java'
import 'prismjs/components/prism-python'
import 'prismjs/components/prism-sql'
import 'prismjs/components/prism-csharp'
import 'prismjs/components/prism-kotlin'
import 'prismjs/components/prism-javascript'
import 'prismjs/themes/prism-tomorrow.css'

const props = defineProps({
  codeId: { type: [String, Number], default: null },
  codeName: { type: String, default: 'GeneratedCode' },
  // 드롭다운 고정 5개
  availableLangOptions: {
    type: Array,
    default: () => [
      'java-SpringBoot',
      'nodeJS(Express)',
      'python-FastAPI',
      'csharp',
      'kotlin'
    ]
  }
})
const emit = defineEmits(['close'])

const codeResults = ref({})
const availableLangs = computed(() => props.availableLangOptions)
const selectedLang = ref(props.availableLangOptions[0] || '')
const loading = ref(false)
const error = ref('')
const copyMsg = ref('')

// 보기 좋게 표시
function displayLang(lang) {
  const map = {
    'java-SpringBoot': 'Java (SpringBoot)',
    nodeJS: 'Node.js',
    'python-FastAPI': 'Python (FastAPI)',
    csharp: 'C#',
    kotlin: 'Kotlin'
  }
  return map[lang] || lang
}

// Prism 하이라이트용 언어 매핑
function getPrismLang(lang) {
  const map = {
    'java-SpringBoot': 'java',
    nodeJS: 'javascript',
    'python-FastAPI': 'python',
    csharp: 'csharp',
    kotlin: 'kotlin'
  }
  return map[lang] || 'javascript'
}

const highlightedCode = computed(() => {
  if (!selectedLang.value) return ''
  const code = codeResults.value[selectedLang.value] || ''
  const prismLang = Prism.languages[getPrismLang(selectedLang.value)] || Prism.languages.javascript
  return Prism.highlight(code, prismLang, getPrismLang(selectedLang.value))
})

// 복사
function copyCode() {
  if (!selectedLang.value || !codeResults.value[selectedLang.value]) return
  navigator.clipboard.writeText(codeResults.value[selectedLang.value])
  copyMsg.value = '복사 완료!'
  setTimeout(() => (copyMsg.value = ''), 1200)
}

// 다운로드
function downloadCode() {
  if (!selectedLang.value || !codeResults.value[selectedLang.value]) return
  const code = codeResults.value[selectedLang.value]
  const extMap = {
    'java-SpringBoot': 'java',
    nodeJS: 'js',
    'python-FastAPI': 'py',
    csharp: 'cs',
    kotlin: 'kt'
  }
  const ext = extMap[selectedLang.value] || 'txt'
  const blob = new Blob([code], { type: 'text/plain' })
  const link = document.createElement('a')
  link.href = URL.createObjectURL(blob)
  link.download = `${props.codeName}.${ext}`
  link.click()
}

// 코드 상세 불러오기
async function fetchCodeDetail(id = props.codeId) {
  if (!id) return
  loading.value = true
  error.value = ''
  try {
    const res = await api.get(`/api/ai-code/${id}`)
    codeResults.value = { [res.data.language]: res.data.generatedCode }
    selectedLang.value = res.data.language
  } catch (e) {
    error.value = '코드 불러오기에 실패했습니다.'
  }
  loading.value = false
}

// 언어별 코드 새로 변환(새로고침)
async function refreshCode() {
  if (!selectedLang.value) return
  loading.value = true
  error.value = ''
  try {
    await api.post('/api/ai-code/generate/class', {
      codeName: props.codeName,
      language: selectedLang.value
    })
    // 전체 코드 목록에서 가장 최근 코드 찾기
    const allRes = await api.get('/api/ai-code')
    const newCode = allRes.data
      .filter(item => item.language === selectedLang.value)
      .sort((a, b) => new Date(b.createdAt) - new Date(a.createdAt))[0]
    if (newCode) {
      await fetchCodeDetail(newCode.id)
    } else {
      error.value = '코드 변환에 실패했습니다.'
    }
  } catch (e) {
    error.value = '코드 변환 요청에 실패했습니다.'
  }
  loading.value = false
}

// 최초 codeId 있으면 조회
onMounted(() => {
  if (props.codeId) fetchCodeDetail(props.codeId)
})

// 코드 id 바뀌면 다시 조회
watch(() => props.codeId, (newId) => {
  if (newId) fetchCodeDetail(newId)
})

// 드롭다운 언어 바뀌면 codeResults에 코드 없으면 안내 초기화
watch(selectedLang, (lang) => {
  if (!codeResults.value[lang] && !error.value) error.value = ''
})

function close() { emit('close') }
</script>

<style scoped>
.modal-overlay {
  position: fixed; top: 0; left: 0; width: 100vw; height: 100vh;
  background: rgba(0,0,0,0.6); z-index: 9999;
  display: flex; align-items: center; justify-content: center;
}
.modal {
  width: 1000px; background: #23272e; color: #eee;
  border-radius: 16px; padding: 28px 28px 16px 28px; box-shadow: 0 10px 40px #000a;
  display: flex; flex-direction: column; font-family: 'Fira Mono', 'Source Code Pro', monospace;
}
.modal-header {
  display: flex; justify-content: space-between; align-items: center;
  font-size: 1.25rem; margin-bottom: 8px;
}
.close-btn { background: none; border: none; color: #eee; font-size: 1.5rem; cursor: pointer; }
.lang-select {
  font-size: 16px; border-radius: 6px; border: none; padding: 5px 14px;
  background: #363636; color: #fff; outline: none; font-family: inherit; margin-right: 12px;
}
.refresh-btn {
  background: #3d5afe; color: #fff; border: none; border-radius: 6px;
  padding: 5px 12px; font-weight: bold; cursor: pointer; margin-left: 10px;
  transition: 0.13s;
}
.refresh-btn:disabled { opacity: 0.55; cursor: not-allowed; }
pre.code-view {
  background: #18191c;
  border-radius: 8px;
  padding: 18px 20px;
  overflow-x: auto;
  max-height: 400px;
  overflow-y: auto;
  scrollbar-width: none; /* Firefox */
  -ms-overflow-style: none; /* IE 10+ */
  font-size: 16px;
  margin-bottom: 16px;
  min-height: 180px;
  font-family: 'Fira Mono', 'Source Code Pro', monospace;
}
.notice-list::-webkit-scrollbar {
  display: none; /* Chrome, Safari, Opera */
}
.no-code {
  color: #aaa; background: #18191c; border-radius: 8px;
  padding: 50px 0; text-align: center; font-size: 17px;
  margin-bottom: 16px; min-height: 180px;
}
.actions {
  display: flex; gap: 8px; justify-content: flex-end; margin-top: 8px;
}
.actions button {
  background: #3d5afe; color: #fff; border: none;
  border-radius: 6px; padding: 7px 18px; font-weight: bold; cursor: pointer;
  transition: 0.13s;
}
.actions button:disabled { opacity: 0.5; cursor: not-allowed; }
.copy-msg {
  color: #90ff90; font-size: 13px; margin-top: 4px; text-align: right;
}
.error-box {
  background: #422; color: #fbb; padding: 14px 12px; border-radius: 7px; margin-bottom: 10px;
  font-size: 15px; font-family: 'Pretendard', sans-serif;
}
.loading-box {
  color: #5cf; font-size: 16px; text-align: center; padding: 40px 0;
  font-family: 'Pretendard', sans-serif;
}
.spinner {
  display: inline-block; width: 18px; height: 18px; border: 3px solid #6fdfff;
  border-top: 3px solid transparent; border-radius: 50%; margin-right: 10px;
  animation: spin 1s linear infinite;
}
@keyframes spin { 100% { transform: rotate(360deg); } }
</style>
