<template>
  <div v-if="visible" class="modal-overlay" @click="closeModal">
    <div class="modal-container" @click.stop>
      <button class="close-btn" @click="closeModal">&times;</button>
      <h2 class="modal-title">프로필 설정</h2>

      <div class="profile-settings">
        <div class="form-section">
          <div class="form-group">
            <label for="name">이름</label>
            <input id="name" v-model="userName" type="text" placeholder="이름을 입력하세요" />
            <small>프로젝트에 표시될 이름입니다. 언제든지 변경할 수 있습니다.</small>
          </div>

          <div class="form-group">
            <label for="email">이메일</label>
            <input id="email" v-model="userEmail" type="email" disabled />
            <small>계정의 이메일 주소입니다. 변경할 수 없습니다.</small>
          </div>

          <div class="form-group">
            <label for="bio">자기소개</label>
            <textarea id="bio" v-model="bio" placeholder="나를 소개하는 말을 적어 보세요." rows="4"></textarea>
            <small>다른 사람들에게 표시될 자기소개입니다.</small>
          </div>

          <div class="form-group delete-section">
            <label>회원 탈퇴</label>
            <button class="btn-delete" @click="deleteAccount">회원 탈퇴</button>
            <small>탈퇴 시 모든 정보가 영구 삭제되며, 복구할 수 없습니다.</small>
          </div>
        </div>

        <div class="side-section">
          <div class="image-section">
            <label>프로필 사진</label>
            <div class="image-wrapper">
              <img :src="profileImageUrl || defaultImage" alt="Profile Image" class="profile-image" />
              <button class="edit-image-btn" @click="triggerFileInput" title="이미지 변경">
                ✎
              </button>
              <input ref="fileInput" type="file" accept="image/*" @change="onFileChange" style="display: none;" />
            </div>
          </div>

          <div class="form-group">
            <label>테마 색상</label>
            <div class="color-picker-section">
              <div class="color-circle" :style="{ backgroundColor: userColor }" @click="toggleColorMenu"></div>
              <div v-if="showColorMenu" class="color-menu">
                <div v-for="color in availableColors" :key="color" class="color-option" :style="{ backgroundColor: color }" @click="selectColor(color)"></div>
              </div>
            </div>
            <small>프로필에 적용할 테마 색상을 선택하세요.</small>
          </div>
        </div>
      </div>

      <div class="save-container">
        <button class="btn-save" @click="saveProfile">저장</button>
      </div>
    </div>
  </div>
</template>

<script setup>
import {ref, watch, onMounted} from 'vue'
import api from '@/api'
import {useRouter} from 'vue-router'
import defaultImage from '@/assets/defaultimage.png'
import { API_BASE_URL } from '@/config'

// Props
const props = defineProps({
  visible: {type: Boolean, default: false}
})
const emit = defineEmits(['close', 'updated'])

const router = useRouter()

const authHeader = localStorage.getItem('authHeader')
if (authHeader) api.defaults.headers.common['Authorization'] = authHeader

// State
const userId = ref(null)
const userName = ref('')
const userEmail = ref('')
const bio = ref('')
const profileImageUrl = ref('')
const userColor = ref('#cccccc')
const theme = ref('LIGHT')
const fileInput = ref(null)

const apiInstance = api.create({
  baseURL: API_BASE_URL,
  headers: {
    'Authorization': localStorage.getItem('authHeader') || ''
  },
  withCredentials: true
})

apiInstance.interceptors.request.use(config => {
  const token = localStorage.getItem('authHeader')
  if (token) {
    config.headers.Authorization = token
  }
  return config
})

const availableColors = ['#FF8C00', '#F44336', '#2196F3', '#4CAF50', '#9C27B0']
const showColorMenu = ref(false)

function closeModal() {
  emit('close')
}

async function fetchProfile() {
  try {
    const {data} = await apiInstance.get('/users/profile')
    userId.value = data.userId
    userName.value = data.userName
    userEmail.value = data.userEmail
    bio.value = data.bio
    profileImageUrl.value = data.profileImageUrl
    theme.value = data.theme || 'LIGHT'

    // Fetch user color (프로젝트 컨텍스트 없이도 안전하게 처리)
    // 프로필에 userColor가 포함되어 있으면 사용, 없으면 기본값 사용
    userColor.value = data.userColor || '#cccccc'

    // 선택적으로 프로젝트 멤버 색상 정보를 가져오되, 실패해도 계속 진행
    try {
      const memberRes = await apiInstance.get('/projects/members')
      const me = memberRes.data.find(m => m.userId === userId.value)
      if (me?.userColor) {
        userColor.value = me.userColor
      }
    } catch (memberErr) {
      // 프로젝트 멤버 정보 조회 실패는 무시 (교수나 프로젝트 컨텍스트 없는 경우)
      console.log('프로젝트 멤버 정보 조회 실패 (무시됨):', memberErr.message)
    }

  } catch (err) {
    console.error('프로필 조회 실패', err)
    if (err.response?.status === 401 || err.response?.status === 403) {
      alert('로그인 세션이 만료되었습니다. 다시 로그인해주세요.')
      localStorage.removeItem('authHeader')
      router.push('/login')
    } else {
      alert('프로필 정보를 불러오는 중 오류가 발생했습니다.')
    }
  }
}

async function saveProfile() {
  try {
    await apiInstance.put('/users/profile', {
      userName: userName.value,
      bio: bio.value,
      profileImageUrl: profileImageUrl.value,
      theme: theme.value
    })
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
    const res = await apiInstance.put(
        '/users/profile/image',
        formData,
        {headers: {'Content-Type': 'multipart/form-data'}}
    )
    profileImageUrl.value = res.data
    alert('프로필 이미지가 업데이트되었습니다.')
  } catch (err) {
    console.error('이미지 업로드 실패', err)
    alert('이미지 업로드 중 오류가 발생했습니다.')
  }
}

function toggleColorMenu() {
  showColorMenu.value = !showColorMenu.value
}

async function selectColor(color) {
  if (!userId.value) {
    alert('사용자 정보를 불러오는 중입니다. 잠시만 기다려주세요.')
    return
  }
  try {
    await apiInstance.put(
        `/projects/members/${userId.value}/color`,
        null,
        {params: {colorHex: color}}
    )
    userColor.value = color
    showColorMenu.value = false
    alert('색상이 성공적으로 저장되었습니다.')
  } catch (err) {
    console.error('색상 저장 실패', err)
    alert('색상 저장 중 오류가 발생했습니다.')
  }
}

async function deleteAccount() {
  const ok = confirm('정말로 회원 탈퇴하시겠습니까? 모든 정보가 영구적으로 삭제됩니다.')
  if (!ok) return

  try {
    await apiInstance.delete('/auth/me')
    alert('회원 탈퇴가 완료되었습니다. 그동안 이용해 주셔서 감사합니다.')
    localStorage.removeItem('authHeader')
    router.push({name: 'MainPage'})
  } catch (err) {
    console.error('회원 탈퇴 실패', err)
    alert('회원 탈퇴 중 오류가 발생했습니다.')
  }
}

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
  top: 0; /* 어두운 배경이 화면 전체를 덮도록 top: 0 으로 수정 */
  left: 0;
  width: 100%;
  height: 100%; /* 어두운 배경이 화면 전체를 덮도록 height: 100% 으로 수정 */
  background-color: rgba(0, 0, 0, 0.6);
  display: flex;
  justify-content: center; /* 가로 중앙 정렬 */
  align-items: flex-start; /* 세로 정렬 기준을 상단으로 변경 */
  z-index: 1000;
  overflow-y: auto; /* 모달 컨텐츠가 길어질 경우 스크롤을 위함 */
}

.modal-container {
  margin-top: 100px; /* 요청하신대로 100px만큼 상단 여백 추가 */
  margin-bottom: 50px; /* 하단 여백 추가 (스크롤 시 보기 좋게) */
  position: relative;
  background: #fff;
  border-radius: 16px;
  padding: 28px 32px;
  width: 100%;
  max-width: 800px;
  box-shadow: 0 10px 30px rgba(0,0,0,0.2);
  animation: fadeIn 0.3s ease-out;
}

@keyframes fadeIn {
  from {
    opacity: 0;
    transform: scale(0.95);
  }
  to {
    opacity: 1;
    transform: scale(1);
  }
}

.close-btn {
  position: absolute;
  top: 16px;
  right: 16px;
  background: transparent;
  border: none;
  font-size: 1.8rem;
  cursor: pointer;
  color: #888;
}

.modal-title {
  margin-bottom: 24px;
  font-size: 1.8rem;
  font-weight: 700;
  color: #333;
}

.profile-settings {
  display: flex;
  gap: 32px;
}

.form-section {
  flex: 1;
}

.form-group {
  margin-bottom: 20px;
}

.form-group label {
  display: block;
  margin-bottom: 8px;
  font-weight: 600;
  font-size: 0.9rem;
  color: #555;
}

.form-group input,
.form-group textarea {
  width: 100%;
  padding: 12px;
  border: 1px solid #ccc;
  border-radius: 8px;
  font-size: 1rem;
  transition: border-color 0.2s, box-shadow 0.2s;
}

.form-group textarea {
  resize: none;
}

.form-group input:focus,
.form-group textarea:focus {
  outline: none;
  border-color: #3f8efc;
  box-shadow: 0 0 0 3px rgba(63, 142, 252, 0.2);
}

.form-group small {
  display: block;
  margin-top: 6px;
  color: #777;
  font-size: 0.8rem;
}

.side-section {
  width: 220px;
  display: flex;
  flex-direction: column;
  align-items: center;
}

.image-section {
  width: 100%;
  text-align: center;
  margin-bottom: 24px;
}

.image-wrapper {
  position: relative;
  width: 180px;
  height: 180px;
  margin: 0 auto;
}

.profile-image {
  width: 100%;
  height: 100%;
  object-fit: cover;
  border-radius: 50%;
  border: 4px solid #fff;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
}

.edit-image-btn {
  position: absolute;
  bottom: 10px;
  right: 10px;
  background: #fff;
  border: 1px solid #ccc;
  border-radius: 50%;
  width: 40px;
  height: 40px;
  font-size: 1.2rem;
  cursor: pointer;
  box-shadow: 0 2px 6px rgba(0, 0, 0, 0.2);
  display: flex;
  align-items: center;
  justify-content: center;
}

.color-picker-section {
  display: flex;
  align-items: center;
  gap: 12px;
  position: relative;
}

.color-circle {
  width: 36px;
  height: 36px;
  border-radius: 50%;
  cursor: pointer;
  border: 2px solid #fff;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
}

.color-menu {
  display: flex;
  gap: 8px;
  padding: 8px;
  background: #fff;
  border-radius: 8px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
  position: absolute;
  top: 45px;
  left: 0;
  z-index: 1;
}

.color-option {
  width: 28px;
  height: 28px;
  border-radius: 50%;
  cursor: pointer;
  border: 2px solid #fff;
  transition: transform 0.2s;
}

.color-option:hover {
  transform: scale(1.1);
}

.delete-section {
  margin-top: auto;
  padding-top: 20px;
  border-top: 1px solid #eee;
}

.btn-delete {
  padding: 10px 20px;
  background-color: #e74c3c;
  color: #fff;
  border: none;
  border-radius: 8px;
  cursor: pointer;
  font-weight: 600;
}

.btn-delete:hover {
  background-color: #c0392b;
}

.save-container {
  margin-top: 24px;
  display: flex;
  justify-content: flex-end;
}

.btn-save {
  padding: 12px 24px;
  background-color: #3f8efc;
  color: #fff;
  border: none;
  border-radius: 8px;
  cursor: pointer;
  font-weight: 600;
  font-size: 1rem;
}

.btn-save:hover {
  background-color: #3578e5;
}
</style>