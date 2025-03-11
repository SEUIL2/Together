<template>
    <section class="detail-section">
      <!-- 타임라인 항목 (수평 배열) -->
      <div class="timeline horizontal">
        <div
          class="timeline-item"
          v-for="(item, index) in planningItems"
          :key="index"
        >
          <div class="circle" :class="{ active: item.completed }"></div>
          <p class="timeline-text">{{ item.name }}</p>
        </div>
      </div>
      <!-- 각 항목에 대한 상세 입력 박스 (접었다 펴는 기능) -->
      <div class="detail-inputs">
        <div
          class="detail-box"
          v-for="(item, index) in planningItems"
          :key="index"
        >
          <!-- 제목 클릭 시 toggleDetail(index) 실행 -->
          <h3 class="detail-title" @click="toggleDetail(index)">
            {{ item.name }}
            <!-- 기본 화살표를 &#9654; (▶) 사용 -->
            <i class="toggle-icon" :class="{ expanded: item.expanded }">&#9654;</i>
          </h3>
          <!-- expanded 상태일 때만 텍스트 입력란 보임 -->
          <textarea
            v-if="item.expanded"
            class="detail-textarea"
            v-model="item.content"
            :placeholder="item.placeholder"
          ></textarea>
        </div>
      </div>
    </section>
  </template>
  
  <script setup>
  import { ref } from 'vue'
  
  const planningItems = ref([
    {
      name: "프로젝트 동기",
      completed: true,
      content: "",
      placeholder: "프로젝트 동기를 작성하세요.",
      expanded: false,
    },
    {
      name: "프로젝트 목표",
      completed: true,
      content: "",
      placeholder: "프로젝트 목표를 작성하세요.",
      expanded: false,
    },
    {
      name: "요구사항 정의",
      completed: true,
      content: "",
      placeholder: "요구사항 정의를 작성하세요.",
      expanded: false,
    },
    {
      name: "정보구조도",
      completed: false,
      content: "",
      placeholder: "정보구조도 내용을 작성하세요.",
      expanded: false,
    },
    {
      name: "스토리보드",
      completed: false,
      content: "",
      placeholder: "스토리보드 내용을 작성하세요.",
      expanded: false,
    },
  ])
  
  function toggleDetail(index) {
    planningItems.value[index].expanded = !planningItems.value[index].expanded;
  }
  </script>
  
  <style scoped>
  .detail-section {
    background: white;
    padding: 15px;
    margin-bottom: 20px;
    border-radius: 10px;
    box-shadow: 0 4px 10px rgba(0,0,0,0.1);
  }
  
  .timeline.horizontal {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 20px;
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
    margin-bottom: 5px;
  }
  .circle.active {
    background: #3f8efc;
  }
  .timeline-text {
    font-size: 0.8rem;
  }
  
  .detail-inputs {
    display: flex;
    flex-direction: column;
    gap: 20px;
  }
  
  .detail-box {
    background: #f9f9f9;
    border: 1px solid #ddd;
    border-radius: 5px;
    padding: 10px;
  }
  
  .detail-title {
    margin: 0 0 8px 0;
    font-size: 1rem;
    color: #333;
    cursor: pointer;
    display: flex;
    justify-content: space-between;
    align-items: center;
  }
  
  .toggle-icon {
  font-size: 1rem;
  transition: transform 0.3s;
  display: inline-block;
  transform: rotate(0deg);       /* 기본 상태: 오른쪽을 바라봄 */
  transform-origin: center;      /* 회전 기준을 중앙으로 설정 */
}

.toggle-icon.expanded {
  /* 아래쪽(6시 방향)이 아니라 약간 위쪽으로 맞추려면, 90deg 대신 80deg 등으로 조정해보세요. */
  transform: rotate(90deg);      
}

  
  .detail-textarea {
    width: 100%;
    min-height: 200px;
    border: 2px solid #ccc;
    border-radius: 8px;
    padding: 10px;
    font-size: 1rem;
    transition: border-color 0.3s, box-shadow 0.3s;
    background-color: #fff;
    resize: none;
  }
  
  .detail-textarea:focus {
    outline: none;
    border-color: #3f8efc;
    box-shadow: 0 0 8px rgba(63, 142, 252, 0.5);
  }
  </style>
  