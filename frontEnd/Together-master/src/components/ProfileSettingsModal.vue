<template>
  <div v-if="visible" class="modal-overlay">
    <div class="modal-container">
      <!-- 모달 종료 버튼 -->
      <button class="close-btn" @click="closeModal">&times;</button>
      <h2 class="modal-title">프로필 설정</h2>
      <div class="profile-settings">
        <!-- 좌측 폼 -->
        <div class="form-section">
          <div class="form-group">
            <label for="name">이름</label>
            <input
                id="name"
                v-model="userName"
                type="text"
                placeholder="이름을 입력하세요"
            />
            <small>기여한 프로젝트에 표시될 이름입니다. 언제든지 변경할 수 있습니다.</small>
          </div>

          <div class="form-group">
            <label for="email">이메일</label>
            <input
                id="email"
                v-model="userEmail"
                type="email"
                disabled
            />
            <small>다른 사람들이 알게 될 이메일 주소입니다. 변경할 수 없습니다.</small>
          </div>

          <div class="form-group">
            <label for="bio">자기소개</label>
            <textarea
                id="bio"
                v-model="bio"
                placeholder="나를 소개하는 말을 적어 보세요."
                rows="3"
            ></textarea>
            <small>다른 @유저를 멘션해서 링크할 수 있습니다.</small>
          </div>

          <button class="btn-save" @click="saveProfile">저장</button>
        </div>

        <!-- 우측 이미지 -->
        <div class="image-section">
          <div class="image-wrapper">
            <img
                :src="profileImageUrl || defaultImage"
                alt="Profile Image"
                class="profile-image"
            />
            <button class="edit-image-btn" @click="triggerFileInput">
              ✎
            </button>
            <input
                ref="fileInput"
                type="file"
                accept="image/*"
                @change="onFileChange"
                style="display: none;"
            />
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, watch, onMounted } from 'vue'
import axios from 'axios'

// Props
const props = defineProps({
  visible: { type: Boolean, default: false }
})
const emit = defineEmits(['close', 'updated'])

// 백엔드 기본 URL 설정
const API_URL = 'http://localhost:8081'
axios.defaults.baseURL = API_URL
// 쿠키 및 세션 기반 인증 사용 시 필요
axios.defaults.withCredentials = true
// 토큰 기반 인증 시
const authHeader = localStorage.getItem('authHeader')
if (authHeader) axios.defaults.headers.common['Authorization'] = authHeader

// form data
const userName = ref('')
const userEmail = ref('')
const bio = ref('')
const profileImageUrl = ref('')
const theme = ref(null)
const defaultImage = '/default-profile.png'
const fileInput = ref(null)

function closeModal() {
  emit('close')
}

async function fetchProfile() {
  try {
    const res = await axios.get('/users/profile')
    const data = res.data
    userName.value = data.userName
    userEmail.value = data.userEmail
    bio.value = data.bio
    profileImageUrl.value = data.profileImageUrl
    theme.value = data.theme
  } catch (err) {
    console.error('프로필 조회 실패', err)
    alert('프로필 정보를 불러오는 중 오류가 발생했습니다.')
  }
}

async function saveProfile() {
  try {
    await axios.put(
        '/users/profile',
        {
          userName: userName.value,
          bio: bio.value,
          profileImageUrl: profileImageUrl.value,
          theme: theme.value
        }
    )
    alert('프로필이 성공적으로 저장되었습니다.')
    emit('updated')
    closeModal()
  } catch (err) {
    console.error('프로필 저장 실패', err)
    alert('프로필 저장 중 오류가 발생했습니다.')
  }
}

function triggerFileInput() {
  fileInput.value.click()
}

async function onFileChange(e) {
  const file = e.target.files[0]
  if (!file) return
  const formData = new FormData()
  formData.append('image', file)
  try {
    const res = await axios.put(
        '/users/profile/image',
        formData
    )
    profileImageUrl.value = res.data
    alert('프로필 이미지가 업데이트되었습니다.')
  } catch (err) {
    console.error('이미지 업로드 실패', err)
    alert('이미지 업로드 중 오류가 발생했습니다.')
  }
}

// visible prop이 true가 될 때마다 프로필 로드
watch(() => props.visible, (newVal) => {
  if (newVal) fetchProfile()
})

onMounted(() => {
  if (props.visible) fetchProfile()
})
</script>

<style scoped>
.modal-overlay {
  position: fixed;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background-color: rgba(0,0,0,0.4);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 1000;
}
.modal-container {
  position: relative;
  background: #fff;
  border-radius: 8px;
  padding: 24px;
  width: 800px;
  max-width: 95%;
}
.close-btn {
  position: absolute;
  top: 16px;
  right: 16px;
  background: transparent;
  border: none;
  font-size: 1.5rem;
  cursor: pointer;
}
.modal-title {
  margin-bottom: 16px;
  font-size: 1.5rem;
  border-bottom: 1px solid #ddd;
  padding-bottom: 8px;
}
.profile-settings {
  display: flex;
  gap: 24px;
}
.form-section {
  flex: 1;
}
.form-group {
  margin-bottom: 16px;
}
.form-group label {
  display: block;
  margin-bottom: 4px;
  font-weight: bold;
}
.form-group input,
.form-group textarea {
  width: 100%;
  padding: 8px;
  border: 1px solid #ccc;
  border-radius: 4px;
}
.form-group small {
  display: block;
  margin-top: 4px;
  color: #666;
  font-size: 0.85rem;
}
.btn-save {
  padding: 10px 20px;
  background-color: #555;
  color: #fff;
  border: none;
  border-radius: 20px;
  cursor: pointer;
}
.btn-save:hover {
  background-color: #444;
}
.image-section {
  width: 200px;
  display: flex;
  align-items: center;
  justify-content: center;
}
.image-wrapper {
  position: relative;
  width: 200px;
  height: 200px;
}
.profile-image {
  width: 100%;
  height: 100%;
  object-fit: cover;
  border-radius: 50%;
}
.edit-image-btn {
  position: absolute;
  bottom: 8px;
  left: 8px;
  background: #fff;
  border: none;
  border-radius: 4px;
  padding: 6px;
  font-size: 1rem;
  cursor: pointer;
  box-shadow: 0 0 4px rgba(0,0,0,0.2);
}
</style>
