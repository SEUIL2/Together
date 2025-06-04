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
    <FeedbackNote
      v-for="fb in feedbacks"
      :key="fb.feedbackId"
      :x="fb.x"
      :y="fb.y"
      :feedbackId="fb.feedbackId"
      :readonly="true"
      @click="openPopup(fb)"
    />
<FeedbackPopup
  v-if="selectedFeedback"
  :fb="selectedFeedback"
  @close="selectedFeedback = null"
  @read="handleRead"
/>

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
import FeedbackNote from '@/components/feedback/FeedbackNote.vue'
import FeedbackPopup from '@/components/feedback/FeedbackPopup.vue'
const isReadOnly = computed(() => route.query.readonly === 'true')
import { useRoute } from 'vue-router'
const route = useRoute()
// ✅ 필수: ref 선언


// ✅ 필수: 클릭 함수
const openPopup = (fb) => {
  selectedFeedback.value = fb
}
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
const selectedFeedback = ref(null)
const showFeedbackInput = ref(false)
const feedbackPosition = ref({ x: 0, y: 0 })


const handleRead = async (feedbackId) => {
  try {
    await axios.post(`/feedbacks/${feedbackId}/read`, null, {
      headers: {
        Authorization: localStorage.getItem('authHeader')
      },
      withCredentials: true
    })

    // 마커 제거
    feedbacks.value = feedbacks.value.filter(fb => fb.feedbackId !== feedbackId)

    // 말풍선 닫기
    selectedFeedback.value = null
  } catch (err) {
    console.error('❌ 읽음 처리 실패:', err)
    alert('피드백 읽음 처리 중 오류가 발생했습니다.')
  }
}

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
/* 전체 배경 영역 */
.detail-section {
  background: #f4f6fa; /* 연한 회색 배경 */
  padding: 32px;
  border-radius: 12px;
  min-height: 100vh;
}

/* 타임라인 영역 */
.timeline.horizontal {
  display: flex;
  justify-content: space-around;
  align-items: center;
  background: #ffffff;
  padding: 16px 20px;
  border-radius: 12px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.06);
  margin-bottom: 32px;
}

.timeline-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  font-size: 0.95rem;
  font-weight: 600;
  color: #2c3e50;
  margin-bottom: -14px;
  margin-top: 10px;
  height: 80px;
}

.status-circle {
  width: 15px;
  height: 15px;
  border-radius: 50%;
  border: 3px solid #dce3ec;
  background-color: white;
  margin-bottom: 4px;
  transition: all 0.3s ease;
}

.status-circle.filled {
  background-color: #3478f6;
  border-color: #3478f6;
  box-shadow: 0 0 6px rgba(52, 120, 246, 0.5);
}

/* 입력 영역 전체 */
.detail-inputs {
  display: flex;
  flex-direction: column;
  gap: 28px;
}

/* 개별 항목 카드 */
.detail-box {
  background-color: #ffffff;
  border: 2px solid #dce3ec;
  border-left: 6px solid #3478f6;
  border-radius: 12px;
  padding: 24px;
  box-shadow: 0 4px 10px rgba(0, 0, 0, 0.04);
}

/* 제목줄 */
.detail-title {
  display: flex;
  align-items: center;
  justify-content: space-between;
  font-size: 1.1rem;
  font-weight: bold;
  color: #1f2d3d;
  margin-bottom: 14px;
}

.edit-icon {
  font-size: 1rem;
  color: #3478f6;
  cursor: pointer;
}

.save-icon {
  width: 20px;
  height: 20px;
}

/* 텍스트 입력창 */
.detail-textarea {
  width: 100%;
  min-height: 160px;
  border: 2px solid #dce3ec;
  border-radius: 8px;
  padding: 14px;
  font-size: 0.95rem;
  background-color: #fdfdfd;
  transition: border-color 0.3s ease, box-shadow 0.3s ease;
  resize: vertical;
  font-family: 'SUIT', 'Noto Sans KR', sans-serif;
  
}

.detail-textarea:focus {
  border-color: #e1ecff;
  box-shadow: 0 0 0 3px rgba(52, 120, 246, 0.2);
  outline: none;
}

/* 파일 업로드 버튼 */
.file-upload-container {
  margin-top: 18px;
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.custom-file-upload {
  display: inline-block;
  background-color: #3478f6;
  color: white;
  padding: 6px 16px;
  border-radius: 6px;
  font-weight: 500;
  cursor: pointer;
  transition: background-color 0.2s;
}

.custom-file-upload:hover {
  background-color: #265fd1;
}

/* 파일 리스트 */
.file-list {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.file-display {
  background-color: #ffffff;
  border: 1px solid #dce3ec;
  border-radius: 8px;
  padding: 10px 14px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  box-shadow: 0 1px 4px rgba(0,0,0,0.05);
}

.file-name {
  color: #3478f6;
  font-weight: 500;
  text-decoration: underline;
  max-width: 60%;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.upload-date {
  color: #7b8a9b;
  font-size: 0.85rem;
  margin-left: -500px;
}

.delete-file-btn {
  background: none;
  border: none;
  color: #aaa;
  font-size: 1.2rem;
  margin-left: 10px;
  cursor: pointer;
  transition: color 0.2s;
}

.delete-file-btn:hover {
  color: #e53935;
}

/* 피드백 마커 */
.feedback-marker {
  position: absolute;
  font-size: 22px;
  color: #3478f6;
  cursor: pointer;
  z-index: 10;
}
</style>
