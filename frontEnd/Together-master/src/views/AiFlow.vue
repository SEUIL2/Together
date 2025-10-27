<template>
  <div class="ai-container">
    <transition name="fade" mode="out-in">
      <!-- 1단계 키워드 선택 -->
      <div v-if="step === 1" key="step1" class="step keywords-step">
        <div class="title-section">
          <h2 class="page-title">관심있는 키워드를 골라주세요!</h2>
          <p class="page-subtitle">AI가 추천하는 키워드를 선택하거나 직접 입력하여 프로젝트 주제를 탐색해보세요</p>
        </div>
        
        <!-- AI 추천 키워드 박스 -->
        <div class="box recommend-box">
          <div class="recommend-header">
            <h3>AI 추천 키워드</h3>
            <button @click="refreshKeywords" class="btn-outline refresh-btn">
              새로고침
            </button>
          </div>
          <div v-if="loadingKeywords" class="spinner"></div>
          <div v-else class="keyword-grid">
            <button
                v-for="kw in keywords"
                :key="kw"
                class="keyword-btn"
                @click="selectKeyword(kw)"
            >{{ kw }}</button>
          </div>
        </div>

        <!-- 직접 입력 박스 -->
        <div class="box custom-box">
          <div class="custom-header">
            <h3>직접 입력</h3>
            <p class="custom-hint">
              관심 키워드를 직접 입력하면 선택 목록에 바로 담깁니다.
            </p>
          </div>
          <div class="custom-input-row">
            <input
                v-model="userInput"
                class="custom-input"
                type="text"
                placeholder="관심 키워드를 쉼표(,)로 구분해서 입력해주세요."
                @keyup.enter="handleUserInput"
            />
            <button class="btn-primary custom-submit" @click="handleUserInput">
              추가하기
            </button>
          </div>
          <div v-if="customError" class="error-text">{{ customError }}</div>
        </div>

        <!-- 선택된 키워드 박스 -->
        <div class="box selected-box">
          <h3>선택된 키워드</h3>
          <div v-if="selectedKeywords.length === 0" class="empty-selected">
            선택된 키워드가 없습니다.
          </div>
          <div v-else class="selected-list">
            <span
                v-for="kw in selectedKeywords"
                :key="kw"
                class="selected-keyword"
                @click="removeKeyword(kw)"
            >{{ kw }} ✕</span>
          </div>
          <button
              @click="confirmKeywords"
              class="btn-primary"
              :disabled="!selectedKeywords.length"
          >선택완료</button>
        </div>
      </div>

      <!-- 2단계 로딩 -->
      <div v-else-if="step === 2" key="step2" class="step loading-step">
        <h2 class="page-title">투게더 봇이 프로젝트 주제를 준비하고 있어요</h2>
        <div class="progress-bar">
          <div class="progress-fill" :style="{ width: progress + '%' }"></div>
          <div class="progress-thumb" :style="{ left: progress + '%' }">
            <img src="@/assets/mood.png" />
          </div>
        </div>
      </div>

      <!-- 3단계 결과 -->
      <div v-else key="step3" class="step result-step">
        <div class="result-header">
          <div class="result-icon">
            <svg xmlns="http://www.w3.org/2000/svg" width="48" height="48" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
              <path d="M12 2l3.09 6.26L22 9.27l-5 4.87 1.18 6.88L12 17.77l-6.18 3.25L7 14.14 2 9.27l6.91-1.01L12 2z"/>
            </svg>
          </div>
          <h2 class="result-title">AI가 추천하는 프로젝트 주제</h2>
          <p class="result-subtitle">선택한 키워드를 바탕으로 맞춤형 프로젝트 아이디어를 준비했어요</p>
        </div>

        <div class="topics-container">
          <div v-for="(t, i) in topics" :key="i" class="topic-card">
            <div class="topic-badge">아이디어 {{ i + 1 }}</div>
            <div class="topic-content">
              <h3 class="topic-title">{{ parseTopicTitle(t) }}</h3>
              <p class="topic-desc">{{ parseTopicDesc(t) }}</p>
            </div>
            <div class="topic-actions">
              <button class="topic-action-btn" @click="copyTopic(t)">
                <svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                  <rect x="9" y="9" width="13" height="13" rx="2" ry="2"></rect>
                  <path d="M5 15H4a2 2 0 0 1-2-2V4a2 2 0 0 1 2-2h9a2 2 0 0 1 2 2v1"></path>
                </svg>
                복사
              </button>
            </div>
          </div>
        </div>

        <div class="result-footer">
          <button class="btn-back" @click="step = 1">
            <svg xmlns="http://www.w3.org/2000/svg" width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
              <polyline points="15 18 9 12 15 6"></polyline>
            </svg>
            다시 선택하기
          </button>
          <button class="btn-finish" @click="finishAi">
            프로젝트 생성하기
            <svg xmlns="http://www.w3.org/2000/svg" width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
              <polyline points="9 18 15 12 9 6"></polyline>
            </svg>
          </button>
        </div>
      </div>
    </transition>
  </div>
</template>

<script>
import api from '@/api'
export default {
  data() {
    return {
      step: 1,
      keywords: [],
      selectedKeywords: [],
      topics: [],
      progress: 0,
      intervalId: null,
      loadingKeywords: true,
      userInput: '',
      customError: '',
    }
  },
  created() {
    this.fetchKeywords()
  },
  mounted() {
    this.$emit('update:layout', false);
  },
  beforeUnmount() {
    this.$emit('update:layout', true);
  },
  methods: {
    fetchKeywords() {
      this.loadingKeywords = true
      api.get('/api/ai/keywords')
          .then(res => (this.keywords = this.removeSelectedFromList(res.data)))
          .finally(() => (this.loadingKeywords = false))
    },
    refreshKeywords() {
      this.loadingKeywords = true
      this.keywords = []
      api.get('/api/ai/keywords')
          .then(res => (this.keywords = this.removeSelectedFromList(res.data)))
          .finally(() => (this.loadingKeywords = false))
    },
    selectKeyword(kw) {
      if (!this.selectedKeywords.includes(kw)) this.selectedKeywords.push(kw)
    },
    removeKeyword(kw) {
      this.selectedKeywords = this.selectedKeywords.filter(x => x !== kw)
    },
    handleUserInput() {
      const parsed = this.parseInputKeywords(this.userInput)
      if (!parsed.length) {
        this.customError = '최소 한 개 이상의 키워드를 입력해주세요.'
        return
      }

      this.customError = ''
      this.addKeywordsToSelection(parsed)
      this.userInput = ''
    },
    parseInputKeywords(value) {
      if (!value) return []
      return value
          .split(/[,\n]+/)
          .map(kw => kw.trim())
          .filter(Boolean)
    },
    addKeywordsToSelection(keywords) {
      keywords.forEach(kw => {
        if (kw && !this.selectedKeywords.includes(kw)) {
          this.selectedKeywords.push(kw)
        }
      })
    },
    removeSelectedFromList(list) {
      if (!Array.isArray(list)) return []
      return list.filter(kw => !this.selectedKeywords.includes(kw))
    },
    confirmKeywords() {
      this.step = 2
      this.startProgress()
      api.post('/api/ai/topics', { keywords: this.selectedKeywords })
          .then(res => {
            clearInterval(this.intervalId)
            this.progress = 100
            this.topics = res.data
            setTimeout(() => (this.step = 3), 500)
          })
    },
    startProgress() {
      this.progress = 0
      this.intervalId = setInterval(() => { if (this.progress < 90) this.progress += 10 }, 300)
    },
    finishAi() {
      this.$router.push({ name: 'CreateProject' })
    },
    parseTopicTitle(raw) {
      return raw.split('\n')[0] || raw
    },
    parseTopicDesc(raw) {
      const parts = raw.split('\n').slice(1).join(' ')
      return parts || ''
    },
    copyTopic(topic) {
      const text = `${this.parseTopicTitle(topic)}\n\n${this.parseTopicDesc(topic)}`
      navigator.clipboard.writeText(text).then(() => {
        alert('클립보드에 복사되었습니다!')
      })
    },
  },
}
</script>

<style scoped>
/* 전체 컨테이너 */
.ai-container {
  min-height: 100vh;
  padding: 80px 40px 60px;
  background: #f8fafc;
  position: relative;
}

/* 페이지 타이틀 */
.title-section {
  text-align: center;
  margin-bottom: 40px;
}

.page-title {
  font-size: 32px;
  font-weight: 700;
  color: #1e293b;
  margin-bottom: 12px;
  position: relative;
  z-index: 1;
}

.page-subtitle {
  font-size: 16px;
  color: #64748b;
  font-weight: 400;
  margin: 0;
  line-height: 1.5;
}

/* 1단계: 키워드 선택 */
.keywords-step {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 30px;
  width: 100%;
  max-width: 900px;
  margin: auto;
  position: relative;
  z-index: 1;
}

.box {
  background: white;
  border-radius: 16px;
  padding: 32px;
  width: 100%;
  box-shadow: 0 2px 8px rgba(0,0,0,0.06);
  border: 1px solid #e2e8f0;
  transition: transform 0.2s ease, box-shadow 0.2s ease;
}

.box:hover {
  transform: translateY(-2px);
  box-shadow: 0 8px 20px rgba(0,0,0,0.1);
}

/* 추천 키워드 박스 */
.recommend-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24px;
}

.recommend-header h3 {
  font-size: 20px;
  font-weight: 700;
  color: #1e293b;
  margin: 0;
}

.refresh-btn {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 10px 20px;
  border: 2px solid #2a7de9;
  background: white;
  color: #2a7de9;
  border-radius: 8px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.2s ease;
}

.refresh-btn:hover {
  background: #2a7de9;
  color: white;
}

.keyword-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(180px, 1fr));
  gap: 12px;
  margin: 20px 0;
}

.keyword-btn {
  background: white;
  color: #2a7de9;
  padding: 14px 20px;
  border: 2px solid #e0edff;
  border-radius: 8px;
  cursor: pointer;
  font-size: 15px;
  font-weight: 600;
  transition: all 0.2s ease;
}

.keyword-btn:hover {
  background: #2a7de9;
  color: white;
  border-color: #2a7de9;
  transform: translateY(-2px);
}

/* 스피너 */
.spinner {
  width: 50px;
  height: 50px;
  border: 4px solid #e2e8f0;
  border-top: 4px solid #2a7de9;
  border-radius: 50%;
  animation: spin 0.8s linear infinite;
  margin: 40px auto;
}

@keyframes spin {
  to { transform: rotate(360deg); }
}

/* 직접 입력 박스 */
.custom-header {
  margin-bottom: 20px;
}

.custom-header h3 {
  font-size: 20px;
  font-weight: 700;
  color: #1e293b;
  margin: 0 0 8px 0;
}

.custom-hint {
  font-size: 14px;
  color: #64748b;
  line-height: 1.6;
}

.custom-input-row {
  display: flex;
  gap: 12px;
  width: 100%;
}

.custom-input {
  flex: 1;
  padding: 14px 18px;
  border: 2px solid #e2e8f0;
  border-radius: 8px;
  font-size: 15px;
  transition: all 0.2s ease;
}

.custom-input:focus {
  outline: none;
  border-color: #2a7de9;
  box-shadow: 0 0 0 3px rgba(42, 125, 233, 0.1);
}

.custom-submit {
  padding: 14px 28px;
  background: #2a7de9;
  color: white;
  border: none;
  border-radius: 8px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.2s ease;
}

.custom-submit:hover {
  background: #1e5bb8;
  transform: translateY(-1px);
}

.custom-suggestions {
  margin-top: 24px;
  padding-top: 24px;
  border-top: 1px solid #e2e8f0;
}

.custom-suggestion-label {
  font-size: 15px;
  font-weight: 600;
  color: #2a7de9;
  margin-bottom: 12px;
}

.error-text {
  margin-top: 12px;
  font-size: 14px;
  color: #ef4444;
  font-weight: 500;
}

.empty-custom {
  margin-top: 20px;
  color: #94a3b8;
  font-size: 15px;
  text-align: center;
  padding: 40px;
}

/* 선택된 키워드 */
.selected-box h3 {
  font-size: 20px;
  font-weight: 700;
  color: #1e293b;
  margin: 0 0 20px 0;
}

.selected-list {
  display: flex;
  flex-wrap: wrap;
  gap: 12px;
  min-height: 60px;
  margin-bottom: 24px;
  padding: 20px;
  background: #f8fafc;
  border-radius: 8px;
  border: 2px dashed #e2e8f0;
}

.empty-selected {
  color: #94a3b8;
  text-align: center;
  padding: 40px;
  margin-bottom: 24px;
}

.selected-keyword {
  background: #2a7de9;
  color: white;
  padding: 10px 18px;
  border-radius: 20px;
  cursor: pointer;
  font-weight: 600;
  font-size: 14px;
  transition: all 0.2s ease;
}

.selected-keyword:hover {
  transform: translateY(-2px);
  background: #ef4444;
}

/* 버튼 스타일 */
.btn-outline {
  border: 2px solid #2a7de9;
  background: white;
  color: #2a7de9;
  padding: 10px 24px;
  border-radius: 8px;
  cursor: pointer;
  font-weight: 600;
  transition: all 0.2s ease;
}

.btn-outline:hover {
  background: #2a7de9;
  color: white;
}

.btn-primary {
  background: #2a7de9;
  color: white;
  padding: 14px 32px;
  border: none;
  border-radius: 8px;
  cursor: pointer;
  font-weight: 700;
  font-size: 16px;
  transition: all 0.2s ease;
  width: 100%;
}

.btn-primary:hover:not(:disabled) {
  background: #1e5bb8;
  transform: translateY(-1px);
}

.btn-primary:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

/* 2단계: 로딩 */
.loading-step {
  position: relative;
  z-index: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
}

.loading-step .page-title {
  max-width: 600px;
}

.loading-step .progress-bar {
  position: relative;
  width: 80%;
  max-width: 600px;
  height: 16px;
  background: #e2e8f0;
  border: 2px solid #cbd5e1;
  border-radius: 100px;
  margin: 80px auto;
  overflow: hidden;
}

.progress-fill {
  height: 100%;
  background: #2a7de9;
  border-radius: 100px;
  transition: width 0.3s ease;
}

.progress-thumb {
  position: absolute;
  bottom: 100%;
  margin-bottom: 16px;
  transform: translateX(-50%);
  z-index: 10;
  animation: bounce 1s infinite;
}

@keyframes bounce {
  0%, 100% { transform: translateX(-50%) translateY(0); }
  50% { transform: translateX(-50%) translateY(-10px); }
}

.progress-thumb img {
  transform: scale(0.8);
  transform-origin: center;
  filter: drop-shadow(0 4px 12px rgba(0,0,0,0.15));
}

/* 3단계: 결과 */
.result-step {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 40px;
  max-width: 1000px;
  margin: 0 auto;
  position: relative;
  z-index: 1;
}

.result-header {
  text-align: center;
  margin-bottom: 20px;
}

.result-icon {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 80px;
  height: 80px;
  background: #e0edff;
  border-radius: 50%;
  margin-bottom: 24px;
  box-shadow: 0 4px 12px rgba(42, 125, 233, 0.15);
}

.result-icon svg {
  color: #2a7de9;
}

.result-title {
  font-size: 32px;
  font-weight: 800;
  margin: 0 0 12px 0;
  color: #1e293b;
}

.result-subtitle {
  font-size: 18px;
  color: #64748b;
  margin: 0;
  font-weight: 400;
}

.topics-container {
  width: 100%;
  display: grid;
  gap: 24px;
  grid-template-columns: repeat(auto-fit, minmax(450px, 1fr));
}

.topic-card {
  background: white;
  border-radius: 16px;
  padding: 32px;
  border: 1px solid #e2e8f0;
  box-shadow: 0 2px 8px rgba(0,0,0,0.06);
  transition: all 0.3s ease;
  position: relative;
  overflow: hidden;
}

.topic-card::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  height: 4px;
  background: #2a7de9;
  transform: scaleX(0);
  transform-origin: left;
  transition: transform 0.3s ease;
}

.topic-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 12px 24px rgba(0,0,0,0.1);
}

.topic-card:hover::before {
  transform: scaleX(1);
}

.topic-badge {
  display: inline-block;
  background: #e0edff;
  color: #2a7de9;
  padding: 6px 16px;
  border-radius: 20px;
  font-size: 12px;
  font-weight: 700;
  text-transform: uppercase;
  letter-spacing: 0.5px;
  margin-bottom: 20px;
}

.topic-content {
  margin-bottom: 24px;
}

.topic-title {
  font-size: 22px;
  font-weight: 700;
  color: #1e293b;
  margin: 0 0 16px 0;
  line-height: 1.4;
}

.topic-desc {
  font-size: 15px;
  color: #475569;
  line-height: 1.7;
  margin: 0;
}

.topic-actions {
  display: flex;
  gap: 12px;
  padding-top: 20px;
  border-top: 1px solid #e2e8f0;
}

.topic-action-btn {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 10px 20px;
  background: #f8fafc;
  border: 2px solid #e2e8f0;
  border-radius: 8px;
  color: #475569;
  font-weight: 600;
  font-size: 14px;
  cursor: pointer;
  transition: all 0.2s ease;
}

.topic-action-btn:hover {
  background: #2a7de9;
  border-color: #2a7de9;
  color: white;
  transform: translateY(-1px);
}

.topic-action-btn svg {
  transition: transform 0.2s ease;
}

.topic-action-btn:hover svg {
  transform: scale(1.1);
}

.result-footer {
  display: flex;
  gap: 16px;
  width: 100%;
  max-width: 600px;
  margin-top: 20px;
}

.btn-back,
.btn-finish {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 10px;
  padding: 16px 32px;
  border: none;
  border-radius: 8px;
  font-weight: 700;
  font-size: 16px;
  cursor: pointer;
  transition: all 0.2s ease;
}

.btn-back {
  background: white;
  color: #2a7de9;
  border: 2px solid #2a7de9;
}

.btn-back:hover {
  background: #f8fafc;
  transform: translateY(-1px);
}

.btn-finish {
  background: #2a7de9;
  color: white;
}

.btn-finish:hover {
  background: #1e5bb8;
  transform: translateY(-1px);
}

/* 페이드 트랜지션 */
.fade-enter-active,
.fade-leave-active {
  transition: all 0.3s ease;
}

.fade-enter-from {
  opacity: 0;
  transform: translateY(10px);
}

.fade-leave-to {
  opacity: 0;
  transform: translateY(-10px);
}

/* 반응형 */
@media (max-width: 768px) {
  .ai-container {
    padding: 60px 20px 40px;
  }

  .page-title {
    font-size: 24px;
  }

  .result-title {
    font-size: 28px;
  }

  .topics-container {
    grid-template-columns: 1fr;
  }

  .keyword-grid {
    grid-template-columns: repeat(auto-fill, minmax(140px, 1fr));
  }

  .result-footer {
    flex-direction: column;
  }
}
</style>
