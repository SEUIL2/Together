<template>
  <div class="project-container">
    <Sidebar />

    <main class="main-content">
      <!-- 프로젝트 상단 정보 (생략) -->
      <section class="project-header">
        <img src="@/assets/togetherlogo.png" alt="프로젝트 로고" class="project-logo" />
        <div class="project-info">
          <input v-model="projectName" class="project-name" placeholder="프로젝트 이름" />
          <textarea
            v-model="projectDescription"
            class="project-description"
            placeholder="프로젝트에 대한 설명을 적어주세요"
          ></textarea>
        </div>
        <div class="vertical-line"></div>
        <div class="progress-container">
          <div class="progress-number">{{ progress }}%</div>
          <div class="progress-row">
            <span class="progress-label">프로젝트 진행도</span>
            <div class="progress-bar">
              <div class="progress" :style="{ width: progress + '%' }"></div>
            </div>
          </div>
        </div>
      </section>

      <!-- 단계별 요약 정보 -->
      <section class="project-steps">
        <div
          class="step"
          v-for="(step, idx) in steps"
          :key="idx"
          @click="selectStep(step.name)"
          :class="{ active: selectedStep === step.name }"
        >
          <h4>{{ step.current }}/{{ step.total }}</h4>
          <p>{{ step.name }}</p>
        </div>
      </section>

      <!-- 동적 컴포넌트: 선택된 단계에 따른 상세 입력 컴포넌트 -->
      <component :is="currentDetailComponent" v-if="selectedStep"></component>
    </main>
  </div>
</template>

<script setup>
import { ref, computed } from 'vue'
import Sidebar from '@/components/Sidebar.vue'
import PlanningDetails from '@/components/PlanningDetails.vue'
import DesignDetails from '@/components/DesignDetails.vue'
import DevelopmentDetails from '@/components/DevelopmentDetails.vue'
import TestingDetails from '@/components/TestingDetails.vue'

const projectName = ref("TOGETHER")
const projectDescription = ref("부천대 학생을 위한 졸업작품 도우미")
const progress = ref(55)
const steps = ref([
  { name: "기획", current: 1, total: 5 },
  { name: "설계", current: 1, total: 8 },
  { name: "개발", current: 1, total: 2 },
  { name: "테스트", current: 1, total: 3 },
])
const selectedStep = ref("")

function selectStep(stepName) {
  selectedStep.value = stepName
}

const currentDetailComponent = computed(() => {
  switch (selectedStep.value) {
    case "기획":
      return PlanningDetails
    case "설계":
      return DesignDetails
    case "개발":
      return DevelopmentDetails
    case "테스트":
      return TestingDetails
    default:
      return null
  }
})
</script>

<style scoped>
/* 전체 레이아웃 */
.project-container {
  display: flex;
  width: 100vw;
  height: 100vh;
  overflow: hidden;
  background-color: #fafafa;
}

/* 메인 컨텐츠 */
.main-content {
  margin-left: 220px;
  margin-right: 18px;
  flex: 1;
  overflow-y: auto;
  padding: 20px;
}

/* 프로젝트 상단 정보 */
.project-header {
  display: flex;
  align-items: center;
  background: white;
  border-radius: 10px;
  padding: 20px;
  margin-bottom: 20px;
  box-shadow: 0 4px 10px rgba(0, 0, 0, 0.1);
}
.project-logo {
  width: 60px;
  height: 60px;
  margin-right: 15px;
}
.project-info {
  display: flex;
  flex-direction: column;
  flex: 1;
  gap: 10px;
}
.project-name {
  font-size: 1.5rem;
  font-weight: bold;
  border: none;
  outline: none;
  background: none;
}
.project-description {
  height: 50px;
  border: none;
  outline: none;
  background: none;
  resize: none;
}
.vertical-line {
  width: 1px;
  height: 80px;
  background-color: #ddd;
  margin: 0 20px;
}

/* 진행도 영역 */
.progress-container {
  display: flex;
  flex-direction: column;
  align-items: flex-start;
  gap: 2px;
}
.progress-number {
  font-size: 2rem;
  color: #3f8efc;
  font-weight: bold;
  margin: 0;
}
.progress-row {
  display: flex;
  align-items: center;
  gap: 10px;
}
.progress-label {
  font-size: 1rem;
  color: #666;
  white-space: nowrap;
}
.progress-bar {
  width: 400px;
  height: 10px;
  background: #ddd;
  border-radius: 5px;
  overflow: hidden;
}
.progress {
  height: 100%;
  background: #3f8efc;
}

/* 단계별 요약 정보 */
.project-steps {
  display: flex;
  align-items: center;
  justify-content: space-around;
  background: white;
  padding: 15px;
  margin-bottom: 20px;
  border-radius: 10px;
  box-shadow: 0 4px 10px rgba(0, 0, 0, 0.1);
}
.step {
  cursor: pointer;
  position: relative;
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  text-align: center;
  padding: 0 10px;
  transition: background-color 0.3s;
}
.step h4 {
  color: #3f8efc;
  font-size: 1.2rem;
  margin: 0;
  margin-bottom: 5px;
}
.step p {
  font-size: 0.9rem;
  margin: 0;
}
.step:not(:last-child)::after {
  content: "";
  position: absolute;
  top: 15%;
  bottom: 15%;
  right: 0;
  width: 1px;
  background-color: #ddd;
}

/* active 상태: 배경색 변경, 테두리 추가 등으로 표시 */
.step.active {
  background-color: #e6f0ff; /* 연한 파란색 배경 */
  border-radius: 8px;
}
.step.active h4,
.step.active p {
  font-weight: bold;
  color: #3f8efc;
}

/* 타임라인 (가로) */
.timeline {
  display: flex;
  justify-content: space-between;
  align-items: center;
  background: white;
  padding: 15px;
  margin-bottom: 20px;
  border-radius: 10px;
  box-shadow: 0 4px 10px rgba(0, 0, 0, 0.1);
}
.timeline-item {
  display: flex;
  flex-direction: column;
  align-items: center;
}
.circle {
  width: 12px;
  height: 12px;
  border-radius: 50%;
  background: #ddd;
}
.circle.active {
  background: #3f8efc;
}
.timeline-text {
  font-size: 0.8rem;
  margin-top: 5px;
}
</style>
