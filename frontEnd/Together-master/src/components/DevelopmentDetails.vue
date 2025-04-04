<template>
  <section class="detail-section">
    <div class="timeline horizontal">
      <div class="timeline-item" v-for="(item, index) in devItems" :key="index">
        <div class="circle" :class="{ active: item.completed }"></div>
        <p class="timeline-text">{{ item.name }}</p>
      </div>
    </div>

    <div class="detail-inputs">
      <div class="detail-box" v-for="(item, index) in devItems" :key="index">
        <h3 class="detail-title" @click="toggleEdit(index)">
          <div class="circle" :class="{ active: item.completed }"></div>
          <span class="title-text">{{ item.name }}</span>
          <i class="edit-icon" @click.stop="toggleEdit(index)">
            <span v-if="!item.editing">&#9998;</span>
            <img v-else src="@/assets/saveicon.png" alt="저장" class="save-icon" />
          </i>
        </h3>

        <div v-if="item.editing">
          <div class="file-upload-container">
            <label class="custom-file-upload" :for="'file-upload-' + index">파일 선택</label>
            <input
              :id="'file-upload-' + index"
              type="file"
              multiple
              @change="handleFileUpload($event, index)"
              hidden
            />
            <div v-if="item.files.length > 0" class="file-list">
              <div class="file-display" v-for="(file, fIndex) in item.files" :key="fIndex">
                <template v-if="file.isImage">
                  <div class="image-wrapper">
                    <img :src="file.data" class="image-preview" @click="openImageModal(file.data)" />
                    <a :href="file.data" :download="file.name" class="file-name below">{{ file.name }}</a>
                  </div>
                </template>
                <template v-else>
                  <a :href="file.data" download="첨부파일" class="file-name">{{ file.name }}</a>
                </template>
                <span class="upload-date">({{ file.uploadDate }})</span>
                <button class="delete-file-btn" @click="deleteFile(index, fIndex)">×</button>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- 이미지 미리보기 모달 -->
    <div class="modal-overlay" v-if="showModal" @click="closeModal">
      <div class="modal-content" @click.stop>
        <img :src="modalImageSrc" class="modal-image" />
        <button class="modal-close" @click="closeModal">&times;</button>
      </div>
    </div>
  </section>
</template>

<script setup>
import { ref, watch } from 'vue'

const devItems = ref([
  { name: "개발 환경 설정", completed: false, files: [], editing: false },
  { name: "버전 관리 전략", completed: false, files: [], editing: false },
  { name: "커밋 메시지 규칙", completed: false, files: [], editing: false },
  { name: "폴더 구조 및 파일 규칙", completed: false, files: [], editing: false }
])

const showModal = ref(false)
const modalImageSrc = ref("")

watch(
  devItems,
  (newItems) => {
    newItems.forEach(item => {
      item.completed = item.files.length > 0
    })
  },
  { deep: true }
)

function toggleEdit(index) {
  devItems.value[index].editing = !devItems.value[index].editing
}

function handleFileUpload(event, index) {
  const selectedFiles = event.target.files
  if (selectedFiles && selectedFiles.length > 0) {
    for (let i = 0; i < selectedFiles.length; i++) {
      const file = selectedFiles[i]
      const reader = new FileReader()
      reader.onload = function (e) {
        const uploadDate = new Date().toLocaleString()
        const isImage = file.type.startsWith("image/")
        devItems.value[index].files.push({
          name: file.name,
          data: e.target.result,
          uploadDate,
          isImage
        })
      }
      reader.readAsDataURL(file)
    }
    event.target.value = ""
  }
}

function deleteFile(itemIndex, fileIndex) {
  devItems.value[itemIndex].files.splice(fileIndex, 1)
}

function openImageModal(imageSrc) {
  modalImageSrc.value = imageSrc
  showModal.value = true
}

function closeModal() {
  showModal.value = false
}
</script>

<style scoped>
.detail-section {
  background: white;
  padding: 15px;
  margin-bottom: 20px;
  border-radius: 10px;
  box-shadow: 0 4px 10px rgba(0, 0, 0, 0.1);
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

.image-wrapper {
  display: flex;
  flex-direction: column;
  align-items: center;
}

.image-preview {
  width: 200px;
  height: auto;
  border: 1px solid #ddd;
  border-radius: 4px;
  cursor: pointer;
}

.file-name {
  font-size: 0.9rem;
  color: #3f8efc;
  text-decoration: underline;
  cursor: pointer;
}

.file-name.below {
  margin-top: 4px;
  font-size: 0.85rem;
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

.edit-icon img {
  width: 20px;
  height: 20px;
  vertical-align: middle;
  cursor: pointer;
}

.modal-overlay {
  position: fixed;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background-color: rgba(0, 0, 0, 0.6);
  display: flex;
  justify-content: center;
  align-items: center;
  z-index: 1000;
}

.modal-content {
  position: relative;
  background: white;
  padding: 20px;
  border-radius: 8px;
  max-width: 80%;
  max-height: 90%;
  overflow: auto;
}

.modal-image {
  max-width: 100%;
  max-height: 80vh;
  border-radius: 6px;
}

.modal-close {
  position: absolute;
  top: 8px;
  right: 12px;
  background: transparent;
  border: none;
  font-size: 2rem;
  color: #999;
  cursor: pointer;
}
</style>
