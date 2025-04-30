<template>
  <section class="detail-section">
    <!-- 타임라인 항목 (수평 배열) -->
    <div class="timeline horizontal">
      <div class="timeline-item" v-for="(item, index) in designItems" :key="index">
        <!-- 기본 동그라미: 기본은 빈 동그라미, 완료되면 채워짐 -->
        <div class="circle" :class="{ active: item.completed }"></div>
        <p class="timeline-text">{{ item.name }}</p>
      </div>
    </div>

    <!-- 각 항목에 대한 상세 입력 박스 (아코디언 + 편집/저장 기능) -->
    <div class="detail-inputs">
      <div class="detail-box" v-for="(item, index) in designItems" :key="index">
        <h3 class="detail-title" @click="toggleEdit(index)">
          <!-- 타이틀 왼쪽에 동그라미 (동일 스타일 적용) -->
          <div class="circle" :class="{ active: item.completed }"></div>
          <span class="title-text">{{ item.name }}</span>
          <i class="edit-icon" @click.stop="toggleEdit(index)">
  <img
    v-if="item.name === '클래스 다이어그램'"
    src="@/assets/arrowicon.png"
    alt="이동"
    class="arrow-icon"
  />
  <span v-else-if="!item.editing">&#9998;</span>
  <img
    v-else
    src="@/assets/saveicon.png"
    alt="저장"
    class="save-icon"
  />
</i>

        </h3>
        <!-- 편집 모드일 때만 파일 업로드 UI 표시 -->
        <div v-if="item.editing">
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
        </div>
        <!-- 편집 모드가 아닐 때는 파일 목록을 숨김 -->
      </div>
    </div>
  </section>
</template>

<script setup>
import { ref, watch } from 'vue'
import { useRouter } from 'vue-router'

const router = useRouter()

const designItems = ref([
  {
    name: "유스케이스 다이어그램",
    completed: false,
    files: [],
    placeholder: "유스케이스 다이어그램 파일을 첨부하세요.",
    editing: false,
  },
  {
    name: "클래스 다이어그램",
    completed: false,
    files: [],
    placeholder: "클래스 다이어그램 파일을 첨부하세요.",
    editing: false,
  },
  {
    name: "시퀀스 다이어그램",
    completed: false,
    files: [],
    placeholder: "시퀀스 다이어그램 파일을 첨부하세요.",
    editing: false,
  },
  {
    name: "UI 디자인",
    completed: false,
    files: [],
    placeholder: "UI 디자인 파일을 첨부하세요.",
    editing: false,
  },
  {
    name: "ER 다이어그램",
    completed: false,
    files: [],
    placeholder: "ER 다이어그램 파일을 첨부하세요.",
    editing: false,
  },
  {
    name: "테이블 명세",
    completed: false,
    files: [],
    placeholder: "테이블 명세 파일을 첨부하세요.",
    editing: false,
  },
  {
    name: "시스템 아키텍쳐",
    completed: false,
    files: [],
    placeholder: "시스템 아키텍쳐 파일을 첨부하세요.",
    editing: false,
  },
  {
    name: "프로젝트 일정 계획",
    completed: false,
    files: [],
    placeholder: "프로젝트 일정 계획 파일을 첨부하세요.",
    editing: false,
  },
])

watch(
  designItems,
  (newItems) => {
    newItems.forEach(item => {
      item.completed = item.files && item.files.length > 0;
    });
  },
  { deep: true }
)

function toggleEdit(index) {
  const item = designItems.value[index];
  if (item.name === "클래스 다이어그램") {
    router.push("/DiagramPage");
    return;
  }
  if (item.name === "ER 다이어그램") {
    router.push("/ErdDiagramPage");
    return;
  }
  item.editing = !item.editing;
}


function handleFileUpload(event, index) {
  const selectedFiles = event.target.files;
  if (selectedFiles && selectedFiles.length > 0) {
    for (let i = 0; i < selectedFiles.length; i++) {
      const file = selectedFiles[i];
      const reader = new FileReader();
      reader.onload = function(e) {
        const uploadDate = new Date().toLocaleString();
        designItems.value[index].files.push({
          name: file.name,
          data: e.target.result,
          uploadDate: uploadDate,
        });
      };
      reader.readAsDataURL(file);
    }
    event.target.value = "";
  }
}

function deleteFile(itemIndex, fileIndex) {
  designItems.value[itemIndex].files.splice(fileIndex, 1);
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
  margin: 20px 0;
}

.timeline-item {
  display: flex;
  flex-direction: column;
  align-items: center;
}

/* 동그라미 스타일: 기본은 속이 빈 동그라미 (하얀 배경, 회색 테두리) */
.circle {
  width: 12px;
  height: 12px;
  border-radius: 50%;
  background: #fff;
  border: 2px solid #ddd;
  margin-bottom: 0px;
  margin-right: 8px;
  transition: background-color 0.3s, border-color 0.3s;
}

/* 완료된 항목은 동그라미가 채워짐 */
.circle.active {
  background: #3f8efc;
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

/* 파일 목록 스타일 */
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

/* textarea 스타일 (필요한 경우) */
.detail-textarea {
  width: 100%;
  min-height: 60px;
  border: 1px solid #ccc;
  border-radius: 4px;
  padding: 6px;
  resize: vertical;
}

.detail-textarea:focus {
  outline: none;
  border-color: #3f8efc;
}
.edit-icon img,
.arrow-icon {
  width: 20px;
  height: 20px;
  vertical-align: middle;
  cursor: pointer;
}

</style>
