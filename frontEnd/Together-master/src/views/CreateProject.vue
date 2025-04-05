<template>
  <div class="project-container">
    <!-- 뒤로 가기 버튼 -->
    <button class="back-button" @click="goBack">←</button>

    <h2 class="title">프로젝트 생성</h2>

    <div class="project-box">
      <h3 class="subtitle">프로젝트 이름을 정해주세요</h3>

      <!-- 프로젝트 이름 입력 -->
      <input 
        v-model="projectName" 
        type="text" 
        placeholder="프로젝트 이름을 입력해주세요" 
        class="input-box"
      />

      <!-- 생성 버튼 -->
      <button class="create-btn" @click="createProject">생성</button>

      <!-- 구분선 -->
      <div class="divider">아직 프로젝트 주제를 못 정했어요</div>

      <!-- 프로젝트 추천 버튼 -->
      <button class="recommend-btn" @click="recommendTopic">프로젝트 주제 추천 받기</button>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import axios from 'axios'

const router = useRouter()
const projectName = ref("")

// 프로젝트 생성 로직
const createProject = async () => {
  if (!projectName.value.trim()) {
    alert("프로젝트 이름을 입력해주세요.")
    return
  }

  try {
    const payload = {
      title: projectName.value
    }

    const res = await axios.post("/projects/create", payload)
    alert("프로젝트가 성공적으로 생성되었습니다!")

    console.log("생성된 프로젝트:", res.data)

    // 나중에 상세 페이지로 이동하고 싶다면 이 부분 사용
    // router.push(`/projects/${res.data.projectId}`)
  } catch (err) {
    console.error(err)
    alert("프로젝트 생성 중 오류가 발생했습니다.")
  }
}

// 추천 주제 기능 (임시)
const recommendTopic = () => {
  alert("추천 기능은 준비 중입니다 :)")
}

// 뒤로 가기 버튼
const goBack = () => {
  router.go(-1)
}
</script>


<style scoped>
/* 전체 페이지 스타일 */
.project-container {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  height: 100vh;
  background-color: #ffffff;
}

/* 뒤로 가기 버튼 */
.back-button {
  position: absolute;
  top: 20px;
  left: 20px;
  font-size: 20px;
  background: none;
  border: none;
  cursor: pointer;
}

/* 타이틀 */
.title {
  font-size: 22px;
  font-weight: bold;
  margin-bottom: 20px;
}

/* 중앙 박스 */
.project-box {
  background: #f9f9f9;
  padding: 30px;
  border-radius: 15px;
  box-shadow: 0 4px 8px rgba(0, 0, 0, 0.1);
  height: 480px;
  width: 640px;
  text-align: center;
}

/* 부제목 */
.subtitle {
  font-size: 22px;
  font-weight: bold;
  margin-bottom: 20px;
}

/* 입력 박스 */
.input-box {
  width: 80%;
  padding: 12px;
  margin: 20px;
  font-size: 14px;
  border: 1px solid #ccc;
  border-radius: 5px;
  outline: none;
  margin-bottom: 15px;
}

/* 생성 버튼 */
.create-btn {
  width: 30%;
  padding: 8px;
  margin: 10px;
  margin-bottom: 80px;
  font-size: 16px;
  background-color: #3f8efc;
  color: white;
  border: none;
  border-radius: 5px;
  cursor: pointer;
  box-shadow: 0px 3px 6px rgba(0, 0, 0, 0.15);
}

.create-btn:hover {
  background-color: #3077e5;
}

/* 구분선 */
.divider {
  margin: 20px 0;
  font-size: 14px;
  color: gray;
  position: relative;
}

.divider::before,
.divider::after {
  content: "";
  position: absolute;
  width: 30%;
  height: 1px;
  background: gray;
  top: 50%;
}

.divider::before {
  left: 0;
}

.divider::after {
  right: 0;
}

/* 추천 버튼 */
.recommend-btn {
  width: 50%;
  padding: 10px;
  font-size: 14px;
  background: white;
  border: 1px solid #ccc;
  border-radius: 5px;
  cursor: pointer;
}

.recommend-btn:hover {
  background-color: #f2f2f2;
}
</style>
