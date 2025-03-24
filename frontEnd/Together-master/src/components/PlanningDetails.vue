<template>
  <section class="detail-section">
    <!-- 타임라인 항목 (수평 배열) -->
    <div class="timeline horizontal">
      <div
        class="timeline-item"
        v-for="(item, index) in planningItems"
        :key="index"
      >
        <!-- 동그라미: planningItems의 content에 따라 completed가 업데이트됩니다. -->
        <span class="status-circle" :class="{ filled: item.completed }"></span>
        <p class="timeline-text">{{ item.name }}</p>
      </div>
    </div>

    <!-- 각 항목에 대한 상세 입력 박스 (접었다 펴는 아코디언 + 편집/저장 기능) -->
    <div class="detail-inputs">
      <div
        class="detail-box"
        v-for="(item, index) in planningItems"
        :key="index"
      >
        <h3 class="detail-title" @click="toggleEdit(index)">
          <!-- 동그라미는 앞에 표시 -->
          <span class="status-circle" :class="{ filled: item.completed }"></span>
          <!-- 제목 텍스트 -->
          <span class="title-text">{{ item.name }}</span>
          <!-- 편집/저장 아이콘; 클릭 시 이벤트 전파 중단 -->
          <i class="edit-icon" @click.stop="toggleEdit(index)">
            <span v-if="!item.editing">&#9998;</span>
            <span v-else>&#128190;</span>
          </i>
        </h3>
        <!-- 편집 모드일 때만 텍스트 입력란 보임 -->
        <div v-if="item.editing">
          <textarea
            class="detail-textarea"
            v-model="item.content"
            :placeholder="item.placeholder"
          ></textarea>
        </div>
        <!-- 편집 모드가 아닐 때는 텍스트는 보이지 않음 -->
      </div>
    </div>
  </section>
</template>

<script setup>
import { ref, watch } from 'vue'

const planningItems = ref([
  {
    name: "프로젝트 동기",
    completed: false,
    content: "",
    placeholder: "프로젝트 동기를 작성하세요.",
    editing: false,
  },
  {
    name: "프로젝트 목표",
    completed: false,
    content: "",
    placeholder: "프로젝트 목표를 작성하세요.",
    editing: false,
  },
  {
    name: "요구사항 정의",
    completed: false,
    content: "",
    placeholder: "요구사항 정의를 작성하세요.",
    editing: false,
  },
  {
    name: "정보구조도",
    completed: false,
    content: "",
    placeholder: "정보구조도 내용을 작성하세요.",
    editing: false,
  },
  {
    name: "스토리보드",
    completed: false,
    content: "",
    placeholder: "스토리보드 내용을 작성하세요.",
    editing: false,
  },
]);

// watch를 통해 각 항목의 content 변경 시, completed 값을 업데이트
watch(
  planningItems,
  (newItems) => {
    newItems.forEach(item => {
      item.completed = item.content.trim() !== "";
    });
  },
  { deep: true }
);

function toggleEdit(index) {
  planningItems.value[index].editing = !planningItems.value[index].editing;
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
  align-items: center;
  justify-content: space-between; /* 아이템들 균등 분배 */
  padding: 0 20px;  /* 좌우에 20px씩 여백 추가 */
  margin-bottom: 20px;
  margin-top: 20px;
}


.timeline-item {
  display: flex;
  flex-direction: column;
  align-items: center;
}

.status-circle {
  display: inline-block;
  width: 12px;
  height: 12px;
  border-radius: 50%;
  border: 2px solid #ddd; /* 기본: 빈 동그라미 */
  margin-right: 8px;
  transition: background-color 0.3s, border-color 0.3s;
}

/* 내용이 있으면 파란색으로 채움 */
.status-circle.filled {
  background-color: #3f8efc;
  border-color: #3f8efc;
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
  margin: 4px 0 5px 0;
  font-size: 1rem;
  color: #333;
  cursor: pointer;
  display: flex;
  align-items: center;
}

/* 제목 텍스트는 동그라미 바로 오른쪽에 배치 */
.title-text {
  flex-grow: 1;
  text-align: left;
}

.edit-icon {
  font-size: 1.2rem;
  cursor: pointer;
  margin-left: 10px;
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
