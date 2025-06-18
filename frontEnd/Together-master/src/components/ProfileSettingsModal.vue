나의 말:
<template>
  <div v-if="visible" class="modal-overlay">
    <div class="modal-container">
      <!-- 모달 종료 버튼 -->
      <button class="close-btn" @click="closeModal">&times;</button>
      <h2 class="modal-title">프로필 설정</h2>
      <!-- 입력 영역 -->
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
            <small>다른 사람들에게 표시될 자기소개입니다.</small>
          </div>

          <!-- 색상 선택 (설명은 아래로) -->
          <div class="form-group">
            <label>테마 색상</label>
            <div class="color-picker-section">
              <div
                  class="color-circle"
                  :style="{ backgroundColor: userColor }"
                  @click="toggleColorMenu"
              ></div>
              <div v-if="showColorMenu" class="color-menu">
                <div
                    v-for="color in availableColors"
                    :key="color"
                    class="color-option"
                    :style="{ backgroundColor: color }"
                    @click="selectColor(color)"
                ></div>
              </div>
            </div>
            <small>프로필에 적용할 테마 색상을 선택하세요.</small>
          </div>

          <!-- 회원 탈퇴 섹션 -->
          <div class="form-group delete-section">
            <label>회원 탈퇴</label>
            <button class="btn-delete" @click="deleteAccount">회원 탈퇴</button>
            <small>회원 탈퇴 시 모든 정보가 영구 삭제됩니다.</small>
          </div>
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

      <!-- 저장 버튼 (프로필 사진 아래, 오른쪽 끝) -->
      <div class="save-container">
        <button class="btn-save" @click="saveProfile">저장</button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, watch, onMounted } from 'vue'
import axios from 'axios'
import { useRouter } from 'vue-router'

// Props
const props = defineProps({
  visible: { type: Boolean, default: false }
})
const emit = defineEmits(['close', 'updated'])

const router = useRouter()

// // Axios 기본 설정
// const API_URL = 'http://localhost:8081'
// axios.defaults.baseURL = API_URL
// axios.defaults.withCredentials = true
const authHeader = localStorage.getItem('authHeader')
if (authHeader) axios.defaults.headers.common['Authorization'] = authHeader

// reactive data
const userId = ref(null)
const userName = ref('')
const userEmail = ref('')
const bio = ref('')
const profileImageUrl = ref('')
const userColor = ref('#cccccc')
const theme = ref('LIGHT')
const defaultImage = '/default-profile.png'
const fileInput = ref(null)

const axiosInstance = axios.create({
  baseURL: 'http://localhost:8081', // 꼭 백엔드 주소 맞게
  headers: {
    'Authorization': localStorage.getItem('authHeader') || ''
  },
  withCredentials: true
})

axiosInstance.interceptors.request.use(config => {
  const token = localStorage.getItem('authHeader')
  if (token) {
    config.headers.Authorization = token
  } else {
    delete config.headers.Authorization
  }
  return config
})


// 색상 옵션
const availableColors = ['#FF5733', '#33FF57', '#3357FF', '#FFD133', '#33FFF2']
const showColorMenu = ref(false)

// 모달 닫기
function closeModal() {
  emit('close')
}

// 프로필 조회 및 userId 설정
async function fetchProfile() {
  try {
    const res = await axiosInstance.get('/users/profile')
    const data = res.data
    userId.value           = data.userId
    userName.value         = data.userName
    userEmail.value        = data.userEmail
    bio.value              = data.bio
    profileImageUrl.value  = data.profileImageUrl
    theme.value            = data.theme || theme.value
    // 유저 색상 조회
    try {
      const memberRes = await axiosInstance.get('/projects/members')
      const me = memberRes.data.find(m => m.userId === userId.value)
      userColor.value = me?.userColor || userColor.value
    } catch (colorErr) {
      console.error('색상 조회 실패', colorErr)
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



// 프로필 저장
async function saveProfile() {
  try {
    await axiosInstance.put('/users/profile', {
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

// 이미지 업로드 트리거
function triggerFileInput() {
  fileInput.value.click()
}

// 파일 변경 시 이미지 업로드
async function onFileChange(e) {
  const file = e.target.files[0]
  if (!file) return
  const formData = new FormData()
  formData.append('image', file)
  try {
    const res = await axiosInstance.put(
        '/users/profile/image',
        formData,
        { headers: { 'Content-Type': 'multipart/form-data' } }
    )
    console.log('▶ upload response.data:', res.data)
    profileImageUrl.value = res.data
    alert('프로필 이미지가 업데이트되었습니다.')
  } catch (err) {
    console.error('이미지 업로드 실패', err)
    alert('이미지 업로드 중 오류가 발생했습니다.')
  }
}

// 색상 메뉴 토글
function toggleColorMenu() {
  showColorMenu.value = !showColorMenu.value
}

// 색상 선택 및 서버 저장
async function selectColor(color) {
  if (!userId.value) {
    alert('사용자 정보를 불러오는 중입니다. 잠시만 기다려주세요.')
    return
  }
  try {
     await axiosInstance.put(
             `/projects/members/${userId.value}/color`,
             null,
             { params: { colorHex: color } }
         )
    userColor.value = color
    showColorMenu.value = false
    alert('색상이 성공적으로 저장되었습니다.')
  } catch (err) {
    console.error('색상 저장 실패', err)
    alert('색상 저장 중 오류가 발생했습니다.')
  }
}

// 회원 탈퇴
async function deleteAccount() {
  const ok = confirm('정말로 회원 탈퇴하시겠습니까? 탈퇴하면 모든 정보가 삭제됩니다.')
  if (!ok) return

  try {
    await axiosInstance.delete('/auth/me')
    alert('회원 탈퇴가 완료되었습니다. 그동안 이용해 주셔서 감사합니다.')
    localStorage.removeItem('authHeader')
    router.push({ name: 'MainPage' })
  } catch (err) {
    console.error('회원 탈퇴 실패', err)
    alert('회원 탈퇴 중 오류가 발생했습니다.')
  }
}

// 모달 visible 변경 시 프로필 다시 로드
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
  display: flex;
  flex-direction: column;
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
/* 색상 선택: 레이아웃 분리 */
.color-picker-section {
  display: flex;
  align-items: center;
  gap: 12px;
}
.color-circle {
  width: 32px;
  height: 32px;
  border-radius: 50%;
  cursor: pointer;
  border: 1px solid #ccc;
}
.color-menu {
  display: flex;
  gap: 8px;
  margin-top: 4px;
}
.color-option {
  width: 24px;
  height: 24px;
  border-radius: 50%;
  cursor: pointer;
  border: 1px solid #ccc;
}
.form-group small {
  display: block;
  margin-top: 4px;
  color: #666;
  font-size: 0.85rem;
}
/* 회원 탈퇴 버튼 스타일 */
.btn-delete {
  padding: 10px 20px;
  background-color: #c0392b;
  color: #fff;
  border: none;
  border-radius: 20px;
  cursor: pointer;
  margin-top: 4px;
}
.btn-delete:hover {
  background-color: #a93226;
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
/* 저장 버튼 컨테이너: 모달 오른쪽 끝 정렬 */
.save-container {
  margin-top: 16px;
  display: flex;
  justify-content: flex-end;
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
/* 회원 탈퇴 설명 텍스트 간격 */
.delete-section small {
  margin-top: 4px;
  display: block;
}
</style>