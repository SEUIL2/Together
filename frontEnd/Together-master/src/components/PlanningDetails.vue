<template>
  <section class="detail-section">
    <!-- 타임라인 항목 (수평 배열) -->
    <div class="timeline horizontal">
      <div
        class="timeline-item"
        v-for="(item, index) in planningItems"
        :key="index"
      >
        <!-- 동그라미: planningItems의 content 또는 파일 업로드 상태에 따라 완료 여부 업데이트 -->
        <span class="status-circle" :class="{ filled: item.completed }"></span>
        <p class="timeline-text">{{ item.name }}</p>
      </div>
    </div>

    <!-- 각 항목에 대한 상세 입력 박스 (아코디언 + 편집/저장 기능) -->
    <div class="detail-inputs">
      <div
        class="detail-box"
        v-for="(item, index) in planningItems"
        :key="index"
      >
        <h3 class="detail-title" @click="toggleEdit(index)">
          <!-- 동그라미 -->
          <span class="status-circle" :class="{ filled: item.completed }"></span>
          <!-- 제목 텍스트 -->
          <span class="title-text">{{ item.name }}</span>
          <!-- 편집/저장 아이콘; 클릭 시 이벤트 전파 중단 -->
          <i class="edit-icon" @click.stop="toggleEdit(index)">
            <span v-if="!item.editing">&#9998;</span>
            <img
              v-else
              src="@/assets/saveicon.png"
              alt="저장"
              class="save-icon"
            />
          </i>
        </h3>
        <!-- 편집 모드일 때만 입력란 보임 -->
        <div v-if="item.editing">
          <!-- 파일첨부가 필요한 항목: '요구사항 정의'와 '정보구조도' -->
          <template v-if="item.name === '요구사항 정의' || item.name === '정보구조도'">
            <div class="file-upload-container">
              <label class="custom-file-upload" :for="'file-upload-' + index">
                파일 선택
              </label>
              <input
                :id="'file-upload-' + index"
                type="file"
                multiple
                @change="handleFileUpload($event, index)"
                hidden
              />
              <!-- 업로드된 파일 목록 -->
              <div v-if="item.files && item.files.length > 0" class="file-list">
                <div class="file-display" v-for="(file, fIndex) in item.files" :key="fIndex">
                  <a :href="file.data" download="첨부파일" class="file-name">
                    {{ file.name }}
                  </a>
                  <span class="upload-date">({{ file.uploadDate }})</span>
                  <button class="delete-file-btn" @click="deleteFile(index, fIndex)">×</button>
                </div>
              </div>
            </div>
          </template>
          <!-- 나머지 항목은 textarea 사용 -->
          <template v-else>
            <textarea
              class="detail-textarea"
              v-model="item.content"
              :placeholder="item.placeholder"
            ></textarea>
          </template>
        </div>
      </div>
    </div>
  </section>
</template>

<script setup>
import { ref, watch } from 'vue'

// 각 항목 초기 데이터; 파일첨부 항목은 files 배열 추가
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
    files: [],
    placeholder: "요구사항 정의 파일을 첨부하세요.",
    editing: false,
  },
  {
    name: "정보구조도",
    completed: false,
    content: "",
    files: [],
    placeholder: "정보구조도 파일을 첨부하세요.",
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

// watch: 각 항목의 값이 변경되면 완료 여부 업데이트
watch(
  planningItems,
  (newItems) => {
    newItems.forEach(item => {
      if (item.name === "요구사항 정의" || item.name === "정보구조도") {
        item.completed = item.files && item.files.length > 0;
      } else {
        item.completed = item.content.trim() !== "";
      }
    });
  },
  { deep: true }
);

// 편집 모드 토글
function toggleEdit(index) {
  planningItems.value[index].editing = !planningItems.value[index].editing;
}

// 파일 첨부 처리: 여러 파일 처리, 각 파일에 업로드 날짜 저장
function handleFileUpload(event, index) {
  const selectedFiles = event.target.files;
  if (selectedFiles && selectedFiles.length > 0) {
    // 초기화(기존 파일과 병합할 수도 있음; 여기서는 기존 파일에 추가)
    for (let i = 0; i < selectedFiles.length; i++) {
      const file = selectedFiles[i];
      const reader = new FileReader();
      reader.onload = function(e) {
        const uploadDate = new Date().toLocaleString(); // 업로드 날짜를 로컬 문자열로 저장
        planningItems.value[index].files.push({
          name: file.name,
          data: e.target.result,
          uploadDate: uploadDate,
        });
      };
      reader.readAsDataURL(file);
    }
    // 초기화 후 input 파일은 재설정 가능하도록
    event.target.value = "";
  }
}

// 파일 삭제 처리: planningItems[index].files 배열에서 해당 파일 제거
function deleteFile(itemIndex, fileIndex) {
  planningItems.value[itemIndex].files.splice(fileIndex, 1);
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
  justify-content: space-between;
  padding: 0 20px;
  margin-top: 20px;
  margin-bottom: 20px;
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
  border: 2px solid #ddd;
  margin-right: 8px;
  transition: background-color 0.3s, border-color 0.3s;
}

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

.title-text {
  flex-grow: 1;
  text-align: left;
}

.edit-icon {
  font-size: 1.2rem;
  cursor: pointer;
  margin-left: 10px;
}

.save-icon {
  width: 1em;
  height: 1em;
  vertical-align: middle;
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

/* 파일 업로드 커스텀 디자인 */
.file-upload-container {
  display: flex;
  flex-direction: column;
  align-items: flex-start;
  gap: 8px;
}

.custom-file-upload {
  display: inline-block;
  padding: 4px 12px;
  cursor: pointer;
  background-color: #3f8efc;
  color: #fff;
  border-radius: 4px;
  transition: background-color 0.3s;
}

.custom-file-upload:hover {
  background-color: #2869c5;
}

/* 파일 목록 및 삭제 버튼 */
.file-list {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.file-display {
  display: flex;
  align-items: center;
  gap: 8px;
}

.file-name {
  font-size: 0.9rem;
  color: #3f8efc;
  text-decoration: underline;
  cursor: pointer;
}

.upload-date {
  font-size: 0.8rem;
  color: #888;
}

.delete-file-btn {
  background-color: #ff4d4f;
  color: white;
  border: none;
  padding: 0px 4px;
  border-radius: 50%;
  cursor: pointer;
  font-size: 1rem;
  line-height: 1;
  transition: background-color 0.3s;
}

.delete-file-btn:hover {
  background-color: #ff7875;
}
</style>
