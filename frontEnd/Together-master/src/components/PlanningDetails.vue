<template>
  <section
    class="detail-section"
    @contextmenu.prevent="handleRightClick"
    style="position: relative"
  >
    <!-- 타임라인 -->
    <div class="timeline horizontal">
      <div
        class="timeline-item"
        v-for="(item, index) in planningItems"
        :key="index"
      >
        <span class="status-circle" :class="{ filled: item.completed }"></span>
        <p class="timeline-text">{{ item.name }}</p>
      </div>
    </div>

    <!-- 세부 항목 -->
    <div class="detail-inputs">
      <div
        class="detail-box"
        v-for="(item, index) in planningItems"
        :key="index"
      >
        <h3 class="detail-title" @click="toggleEdit(index)">
          <span class="status-circle" :class="{ filled: item.completed }"></span>
          <span class="title-text">{{ item.name }}</span>
          <i
            class="edit-icon"
            v-if="!readonly"
            @click.stop="toggleEdit(index)"
          >
            <span v-if="!item.editing">✎</span>
            <img
              v-else
              src="@/assets/saveicon.png"
              alt="저장"
              class="save-icon"
            />
          </i>
        </h3>

        <div v-if="item.editing">
          <textarea
            v-model="item.content"
            :placeholder="item.placeholder"
            :readonly="readonly"
            class="detail-textarea"
          ></textarea>

          <!-- 파일 업로드 -->
          <div class="file-upload-container">
            <label
              class="custom-file-upload"
              :for="`file-upload-${index}`"
              v-if="!readonly"
            >
              파일 선택
            </label>
            <input
              :id="`file-upload-${index}`"
              type="file"
              multiple
              @change="handleFileUpload($event, index)"
              hidden
              v-if="!readonly"
            />
            <div v-if="item.files && item.files.length" class="file-list">
              <div
                class="file-display"
                v-for="(file, fIndex) in item.files"
                :key="fIndex"
              >
                <a :href="file.url" download class="file-name">
                  {{ extractFileName(file.url) }}
                </a>
                <span class="upload-date">
                  ({{ formatDate(file.uploadedAt) }})
                </span>
                <button
                  class="delete-file-btn"
                  @click="deleteFile(index, fIndex, file.url)"
                  v-if="!readonly"
                >
                  ×
                </button>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- 📌 기존 피드백 마커 -->
    <div
      v-for="(fb, index) in feedbacks"
      :key="index"
      class="feedback-marker"
      :style="{ top: fb.y + 'px', left: fb.x + 'px', position: 'absolute' }"
    >
      📌
    </div>

<!-- 바꿔줘야 할 부분 -->
<FeedbackInput
  v-if="showFeedbackInput"
  :x="feedbackPosition.x"
  :y="feedbackPosition.y"
  :page="'planning'"
  :readonly="isReadOnly"
  :projectId="projectId"
  @close="showFeedbackInput = false"
  @submitted="loadFeedbacks"
/>



  </section>
</template>

<script setup>
import { ref, watch, onMounted, computed } from 'vue'
import axios from 'axios'
import FeedbackInput from '@/components/feedback/FeedbackInput.vue'
const isReadOnly = computed(() => route.query.readonly === 'true')
import { useRoute } from 'vue-router'
const route = useRoute()

const props = defineProps({
  projectId: { type: Number, required: true },
  readonly: { type: Boolean, default: false }
})
const emit = defineEmits(['updateStepProgress'])

const planningItems = ref([
  { name: '프로젝트 동기', type: 'motivation', content: '', files: [], placeholder: '내용 또는 파일을 입력하세요', editing: false, completed: false },
  { name: '프로젝트 목표', type: 'goal', content: '', files: [], placeholder: '내용 또는 파일을 입력하세요', editing: false, completed: false },
  { name: '요구사항 정의', type: 'requirement', content: '', files: [], placeholder: '내용 또는 파일을 입력하세요', editing: false, completed: false },
  { name: '정보구조도', type: 'infostructure', content: '', files: [], placeholder: '내용 또는 파일을 입력하세요', editing: false, completed: false },
  { name: '스토리보드', type: 'storyboard', content: '', files: [], placeholder: '내용 또는 파일을 입력하세요', editing: false, completed: false }
])

const feedbacks = ref([])
const showFeedbackInput = ref(false)
const feedbackPosition = ref({ x: 0, y: 0 })

const handleRightClick = (e) => {
if (!(props.readonly)) return

  console.log('📌 우클릭 위치:', e.clientX, e.clientY)
  const section = e.currentTarget
  const sectionRect = section.getBoundingClientRect()

  // 스크롤 보정 추가!
  const scrollTop = section.scrollTop
  const scrollLeft = section.scrollLeft

  feedbackPosition.value = {
    x: e.clientX - sectionRect.left + scrollLeft,
    y: e.clientY - sectionRect.top + scrollTop
  }

  showFeedbackInput.value = true
}



const loadFeedbacks = async () => {
  try {
    const res = await axios.get('/feedbacks/project', {
      params: {
        page: 'planning',
        projectId: props.projectId   // ✅ 직접 넘기도록 수정
      },
      headers: {
        Authorization: localStorage.getItem('authHeader')
      },
      withCredentials: true
    })

    feedbacks.value = res.data
  } catch (err) {
    console.error('❌ 피드백 불러오기 오류:', err)
  }
}


// 진행도 감지
watch(
  planningItems,
  (items) => {
    items.forEach(item => {
      item.completed = item.content.trim() !== '' || item.files.length > 0
    })
    const count = items.filter(i => i.completed).length
    emit('updateStepProgress', count)
  },
  { deep: true }
)

function toggleEdit(index) {
  if (props.readonly) return
  const item = planningItems.value[index]
  item.editing = !item.editing
  if (!item.editing) saveItem(index)
}

function extractFileName(url) {
  try {
    return url.split('/').pop()
  } catch {
    return '파일'
  }
}

function formatDate(dateString) {
  return new Date(dateString).toLocaleString()
}

function handleFileUpload(event, index) {
  if (props.readonly) return
  const item = planningItems.value[index]
  const formData = new FormData()
  formData.append('type', item.type)
  formData.append('projectId', props.projectId)
  for (const f of event.target.files) formData.append('files', f)

  axios.post('/planning/upload', formData, {
    headers: { Authorization: localStorage.getItem('authHeader') },
    withCredentials: true
  })
  .then(res => {
    if (res.data.files) item.files.push(...res.data.files)
  })
  .catch(err => {
    console.error('파일 저장 실패', err)
    alert(`${item.name} 파일 저장 오류`)
  })
}

function deleteFile(index, fIndex, url) {
  if (props.readonly) return
  const item = planningItems.value[index]
  axios.delete('/planning/delete-file', {
    data: { projectId: props.projectId, type: item.type, fileUrl: url },
    headers: { Authorization: localStorage.getItem('authHeader') },
    withCredentials: true
  })
  .then(() => {
    item.files.splice(fIndex, 1)
  })
  .catch(err => {
    console.error('파일 삭제 실패', err)
    alert('파일 삭제 오류')
  })
}

async function saveItem(index) {
  if (props.readonly) return
  const item = planningItems.value[index]
  const formData = new FormData()
  formData.append('type', item.type)
  formData.append('text', item.content)
  formData.append('projectId', props.projectId)

  try {
    await axios.put('/planning/update', formData, {
      headers: { Authorization: localStorage.getItem('authHeader') },
      withCredentials: true
    })
  } catch (err) {
    console.error('❌ 저장 오류:', err)
    alert(`${item.name} 저장 중 오류 발생`)
  }
}

onMounted(async () => {
  try {
    // 🔽 select 제거! 직접 넘기니까 이제 필요 없음

    // 🔽 기존 로직 유지 (planning 항목 불러오기)
    const res = await axios.get('/planning/all', {
      params: { projectId: props.projectId },  // 그대로 유지
      headers: {
        Authorization: localStorage.getItem('authHeader')
      },
      withCredentials: true
    })

    const data = res.data
    planningItems.value.forEach(item => {
      const d = data[item.type]
      if (d) {
        item.content = d.text || ''
        item.files = d.files || []
      }
      if (props.readonly) item.editing = true
    })

    planningItems.value.forEach(item => {
      item.completed = item.content.trim() !== '' || item.files.length > 0
    })
    emit('updateStepProgress', planningItems.value.filter(i => i.completed).length)

    // ✅ 피드백도 직접 projectId 넘겨서 로딩
    await loadFeedbacks()
  } catch (err) {
    console.error('❌ 데이터 불러오기 실패:', err)
  }
})
</script>

<style scoped>
.feedback-marker {
  position: absolute;
  font-size: 18px;
  cursor: pointer;
}
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
  gap: 8px;
  width: 100%;
}

.file-display {
  display: flex;
  align-items: center;
  justify-content: space-between;
  background-color: #fff;
  border: 1px solid #ddd;
  border-radius: 6px;
  padding: 10px 12px;
  font-size: 0.95rem;
  box-shadow: 0 2px 5px rgba(0, 0, 0, 0.05);
}

.file-name {
  flex-grow: 1;
  color: #333;
  text-decoration: none;
  font-weight: 500;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.upload-date {
  margin-left: 12px;
  color: #999;
  font-size: 0.85rem;
}

.delete-file-btn {
  background-color: transparent;
  color: #999;
  border: none;
  font-size: 1rem;
  cursor: pointer;
  margin-left: 12px;
  transition: color 0.3s;
}

.delete-file-btn:hover {
  color: #ff4d4f;
}

</style>
