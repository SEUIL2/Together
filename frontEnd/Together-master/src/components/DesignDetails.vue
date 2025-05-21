<template>
  <section class="detail-section">
    <div class="timeline horizontal">
      <div class="timeline-item" v-for="(item, index) in designItems" :key="index">
        <div class="circle" :class="{ active: item.completed }"></div>
        <p class="timeline-text">{{ item.name }}</p>
      </div>
    </div>

    <div class="detail-inputs">
      <div class="detail-box" v-for="(item, index) in designItems" :key="index">
        <h3 class="detail-title" @click="handleItemClick(index)">
          <div class="circle" :class="{ active: item.completed }"></div>
          <span class="title-text">{{ item.name }}</span>
          <i class="edit-icon" v-if="!readonly" @click.stop="toggleEdit(index)">
            <span v-if="!item.editing">&#9998;</span>
            <img v-else src="@/assets/saveicon.png" alt="저장" class="save-icon" />
          </i>
        </h3>

        <div v-if="item.editing">
          <textarea
            class="detail-textarea"
            v-model="item.content"
            :placeholder="item.placeholder"
            :readonly="readonly"
          />
          <div class="file-upload-container">
            <label class="custom-file-upload" :for="'file-upload-' + index" v-if="!readonly">
              파일 선택
            </label>
            <input
              :id="'file-upload-' + index"
              type="file"
              multiple
              @change="handleFileUpload($event, index)"
              hidden
              v-if="!readonly"
            />
            <div v-if="item.files.length > 0" class="file-list">
              <div class="file-display" v-for="(file, fIndex) in item.files" :key="fIndex">
                <a :href="file.url" download class="file-name">{{ extractFileName(file.url) }}</a>
                <span class="upload-date">({{ formatDate(file.uploadedAt) }})</span>
                <button class="delete-file-btn" @click="deleteFile(index, fIndex, file.url)" v-if="!readonly">×</button>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  </section>
</template>

<script setup>
import { ref, watch, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import axios from 'axios'

const props = defineProps({
  projectId: Number,
  readonly: Boolean
})

const emit = defineEmits(['updateStepProgress'])
const router = useRouter()

const designItems = ref([
  { name: "유스케이스 다이어그램", type: "usecase", content: "", files: [], placeholder: "", editing: false, completed: false },
  { name: "클래스 다이어그램", type: "classDiagram", content: "", files: [], placeholder: "", editing: false, completed: false },
  { name: "시퀀스 다이어그램", type: "sequence", content: "", files: [], placeholder: "", editing: false, completed: false },
  { name: "UI 디자인", type: "ui", content: "", files: [], placeholder: "", editing: false, completed: false },
  { name: "ER 다이어그램", type: "erd", content: "", files: [], placeholder: "", editing: false, completed: false },
  { name: "테이블 명세", type: "table", content: "", files: [], placeholder: "", editing: false, completed: false },
  { name: "시스템 아키텍쳐", type: "architecture", content: "", files: [], placeholder: "", editing: false, completed: false },
  { name: "프로젝트 일정 계획", type: "schedule", content: "", files: [], placeholder: "", editing: false, completed: false }
])

watch(designItems, (items) => {
  items.forEach(item => {
    item.completed = item.content.trim() !== "" || item.files.length > 0
  })
  const completedCount = items.filter(item => item.completed).length
  emit('updateStepProgress', completedCount)
}, { deep: true, immediate: true })

function handleItemClick(index) {
  if (props.readonly) return
  const item = designItems.value[index]
  if (item.type === 'classDiagram') {
    router.push('/DiagramPage')
  } else if (item.type === 'erd') {
    router.push('/ErdDiagramPage')
  } else {
    toggleEdit(index)
  }
}

function toggleEdit(index) {
  if (props.readonly) return
  const item = designItems.value[index]
  item.editing = !item.editing
  if (!item.editing) saveItem(index)
}

function handleFileUpload(event, index) {
  if (props.readonly) return
  const selectedFiles = event.target.files
  const item = designItems.value[index]
  const formData = new FormData()

  formData.append("type", item.type)
  formData.append("projectId", props.projectId)
  for (const file of selectedFiles) {
    formData.append("files", file)
  }

  axios.post("/design/upload", formData, {
    headers: { Authorization: localStorage.getItem("authHeader") },
    withCredentials: true
  }).then(res => {
    if (res.data.files) {
      item.files.push(...res.data.files)
    }
  }).catch(err => {
    console.error("파일 저장 실패", err)
    alert(`${item.name} 파일 저장 오류`)
  })

  event.target.value = ""
}

function deleteFile(itemIndex, fileIndex, fileUrl) {
  if (props.readonly) return
  const item = designItems.value[itemIndex]
  axios.delete("/design/delete-file", {
    params: { type: item.type, fileUrl, projectId: props.projectId },
    headers: { Authorization: localStorage.getItem("authHeader") },
    withCredentials: true
  }).then(() => {
    item.files.splice(fileIndex, 1)
  }).catch(err => {
    console.error("파일 삭제 실패", err)
  })
}

function extractFileName(url) {
  try {
    const parts = url.split('/')
    const idx = parts.indexOf('d')
    return parts[idx + 1] + '.파일'
  } catch {
    return "파일"
  }
}

function formatDate(dateString) {
  return new Date(dateString).toLocaleString()
}

async function saveItem(index) {
  if (props.readonly) return
  const item = designItems.value[index]
  const formData = new FormData()

  formData.append("type", item.type)
  formData.append("text", item.content)
  formData.append("projectId", props.projectId)

  try {
    await axios.put("/design/update", formData, {
      headers: { Authorization: localStorage.getItem("authHeader") },
      withCredentials: true
    })
  } catch (err) {
    console.error("❌ 저장 오류:", err)
    alert(`${item.name} 저장 중 오류 발생`)
  }
}

onMounted(async () => {
  try {
    const res = await axios.get("/design/all", {
      params: props.readonly ? { projectId: props.projectId } : {},
      headers: { Authorization: localStorage.getItem("authHeader") },
      withCredentials: true
    })

    const data = res.data
    designItems.value.forEach(item => {
      const contentData = data[item.type]
      if (contentData) {
        item.content = contentData.text || ""
        item.files = contentData.files || []
        if (props.readonly) item.editing = true
      }
    })

    const completedCount = designItems.value.filter(item =>
      item.content.trim() !== "" || item.files.length > 0
    ).length
    emit('updateStepProgress', completedCount)
  } catch (err) {
    console.error("❌ 데이터 불러오기 실패:", err)
  }
})
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
