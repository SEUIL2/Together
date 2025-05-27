<template>
  <div class="ai-container">
    <transition name="fade" mode="out-in">
      <!-- 1단계 키워드 선택 -->
      <div v-if="step === 1" key="step1" class="step keywords-step">
        <h2 class="page-title">관심있는 키워드를 골라주세요!</h2>
        <!-- 추천 키워드 박스 -->
        <div class="box recommend-box">
          <div class="recommend-header">
            <h3>키워드 추천</h3>
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
        <h2 class="page-title">이런 주제는 어때요?</h2>
        <div class="box result-box">
          <div class="box-tab">주제 추천</div>
          <ul class="topic-list">
            <li v-for="(t, i) in topics" :key="i" class="topic-item">
              <div class="topic-title">{{ parseTopicTitle(t) }}</div>
              <div class="topic-desc">{{ parseTopicDesc(t) }}</div>
            </li>
          </ul>
        </div>
        <button class="btn-primary" @click="finishAi">
          프로젝트 생성으로 돌아가기
        </button>
      </div>
    </transition>
  </div>
</template>

<script>
import axios from 'axios'
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
    }
  },
  created() {
    this.fetchKeywords()
  },
  methods: {
    fetchKeywords() {
      this.loadingKeywords = true
      axios.get('/api/ai/keywords')
          .then(res => (this.keywords = res.data))
          .finally(() => (this.loadingKeywords = false))
    },
    refreshKeywords() {
      this.loadingKeywords = true
      this.keywords = []
      axios.get('/api/ai/keywords')
          .then(res => (this.keywords = res.data.filter(k => !this.selectedKeywords.includes(k))))
          .finally(() => (this.loadingKeywords = false))
    },
    selectKeyword(kw) {
      if (!this.selectedKeywords.includes(kw)) this.selectedKeywords.push(kw)
    },
    removeKeyword(kw) {
      this.selectedKeywords = this.selectedKeywords.filter(x => x !== kw)
    },
    confirmKeywords() {
      this.step = 2
      this.startProgress()
      axios.post('/api/ai/topics', { keywords: this.selectedKeywords })
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
  },
}
</script>

<style scoped>
.ai-container { padding-top: 80px; text-align: center; }
.page-title { margin-bottom: 40px; }
.keywords-step { display: flex; flex-direction: column; align-items: center; gap: 30px; width: 100%; max-width: 800px; margin: auto; }
.box { border: 1px solid #ccc; border-radius: 8px; padding: 20px; max-width: 800px; width: 100%; text-align: left; margin-bottom: 30px; margin-left: auto; margin-right: auto; }
.recommend-box, .selected-box { width: 100%; }
.recommend-header { display: flex; justify-content: space-between; align-items: center; }
.keyword-grid { display: grid; grid-template-columns: repeat(4, 1fr); gap: 12px; margin: 20px 0; }
.keyword-btn { background: #666; color: #fff; padding: 10px 16px; border: none; border-radius: 6px; cursor: pointer; }
.spinner { width: 40px; height: 40px; border: 4px solid #ccc; border-top: 4px solid #007BFF; border-radius: 50%; animation: spin 1s linear infinite; margin: 40px auto; }
@keyframes spin { to { transform: rotate(360deg); } }
.selected-list { display: flex; flex-wrap: wrap; gap: 10px; min-height: 40px; margin: 20px 0; }
.empty-selected { color: #999; margin-bottom: 20px; }
.selected-keyword { background: #999; color: #fff; padding: 6px 12px; border-radius: 6px; cursor: pointer; }
.btn-outline { border: 1px solid #007BFF; background: #fff; color: #007BFF; padding: 8px 16px; border-radius: 4px; cursor: pointer; }
.btn-primary { background: #007BFF; color: #fff; padding: 8px 16px; border: none; border-radius: 4px; cursor: pointer; }
.loading-step .progress-bar {
  position: relative;
  width: 80%;
  height: 12px;
  border: 1px solid #007BFF;
  border-radius: 6px;
  margin: 80px auto; /* 위로 내려서 위치 조정 */
}
.progress-fill { height: 100%; background: #007BFF; border-radius: 6px 0 0 6px; transition: width .3s; }
.progress-thumb { position: absolute; bottom: 100%; margin-bottom: 8px; transform: translateX(-50%); z-index: 10; }
.progress-thumb img { transform: scale(0.66); transform-origin: center; }
/* 3단계 결과 화면 가운데 정렬 */
.result-step { display: flex; flex-direction: column; align-items: center; gap: 20px; }
.result-box { position: relative; padding-top: 20px; }
.box-tab { position: absolute; top: -12px; left: 50%; transform: translateX(-50%); background: #fff; padding: 0 12px; color: #007BFF; font-weight: 500; }
.topic-list { list-style: none; padding: 0; margin: 0; }
.topic-item { background: #f9f9f9; border-radius: 6px; padding: 16px; margin-bottom: 12px; max-width: 600px; width: 100%; }
.topic-title { font-size: 18px; font-weight: 600; margin-bottom: 8px; }
.topic-desc { font-size: 14px; color: #555; }
</style>


