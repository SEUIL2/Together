<template>
  <div class="team-management-container">
    <main class="main-content">
      <div class="team-management-header">
        <h2>팀원 관리</h2>
        <button class="add-member-btn" @click="openInviteModal">+ 초대하기</button>
      </div>

      <table class="team-management-table">
        <thead>
        <tr>
          <th>사진</th>
          <th>학번</th>
          <th>이름</th>
          <th>역할</th>
          <th>메모</th>
        </tr>
        </thead>
        <tbody>
        <tr v-for="member in studentMembers" :key="member.userId">
          <td>
            <img
                :src="member.profileImageUrl || defaultAvatar"
                alt="프로필"
                class="profile-img"
            />
          </td>
          <td>{{ member.studentNumber }}</td>
          <td>
            <div class="name-with-avatar">
              <div
                  v-if="member.role === 'STUDENT'"
                  class="avatar-wrapper"
                  @click="toggleColorPicker(member)"
              >
                <span class="avatar" :style="{ backgroundColor: member.avatarColor }"></span>
              </div>
              <span>{{ member.userName }}</span>
              <div
                  v-if="member.role === 'STUDENT' && member.showColorPicker"
                  class="color-picker-menu"
                  @click.stop
              >
                <div
                    v-for="color in availableColors"
                    :key="color"
                    class="color-option"
                    :style="{ backgroundColor: color }"
                    @click="setColor(member, color)"
                ></div>
              </div>
            </div>
          </td>
          <td>{{ member.isLeader ? '팀장' : '팀원' }}</td>
          <td>
            <button class="evaluate-btn" @click="evaluateMember(member)">메모</button>
          </td>
        </tr>
        </tbody>
      </table>

      <h3 class="professor-section-title">담당교수</h3>
      <table class="team-management-table">
        <thead>
        <tr>
          <th>사진</th>
          <th>이메일</th>
          <th>이름</th>
        </tr>
        </thead>
        <tbody>
        <tr v-for="prof in professorMembers" :key="prof.userId">
          <td>
            <img :src="prof.profileImageUrl || defaultAvatar" alt="프로필" class="profile-img" />
          </td>
          <td>{{ prof.userEmail }}</td>
          <td>{{ prof.userName }}</td>
        </tr>
        </tbody>
      </table>

      <InviteModal
          :isOpen="showInviteModal"
          @close="showInviteModal = false"
          @invite="handleInvite"
      />
      <MemoModal
          v-if="showMemoModal"
          :member="memoTarget"
          :currentUser="currentUser"
          :projectId="projectId"
          @close="showMemoModal = false"
          @saved="onMemoSaved"
      />
    </main>
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue'
import axios from 'axios'
import { useRoute } from 'vue-router'
import InviteModal from './InviteModal.vue'
import MemoModal from './MemoModal.vue'

// 프로젝트 ID
const route = useRoute()
const projectId = Number(route.params.projectId)

// 현재 사용자
const currentUser = ref({})

// 기본 아바타 (fallback)
const defaultAvatar = '/images/default-avatar.png'

const availableColors = ['#FF8C00', '#F44336', '#2196F3', '#4CAF50', '#9C27B0']
const teamMembers = ref([])
const showInviteModal = ref(false)
const showMemoModal = ref(false)
const memoTarget = ref(null)

const studentMembers = computed(() =>
    teamMembers.value.filter(m => m.role === 'STUDENT')
)
const professorMembers = computed(() =>
    teamMembers.value.filter(m => m.role === 'PROFESSOR')
)

// 내 정보 불러오기
async function fetchCurrentUser() {
  try {
    const { data } = await axios.get('/users/profile', { withCredentials: true })
    currentUser.value = data
  } catch (e) {
    console.error('내 정보 조회 실패', e)
  }
}

// 팀원 및 학번, 프로필 이미지 로드
// 팀원 및 학번, 프로필 이미지 로드
async function fetchTeamMembers() {
  try {
    const config = { withCredentials: true }
    if (!isNaN(projectId)) {
      config.params = { projectId }
    }
    const { data } = await axios.get(
        '/projects/members',
        config
    )
    teamMembers.value = data.map(member => ({
      userId: member.userId,
      role: member.role,
      studentNumber: member.studentNumber || '',
      userEmail: member.userEmail,
      userName: member.userName,
      profileImageUrl: member.profileImageUrl || null,
      avatarColor: member.userColor || getRandomColor(),
      showColorPicker: false,
      memo: '',
      noteId: null,
      isLeader: member.isLeader ?? member.leader
    }))
    // 개인 메모 로드
    await Promise.all(
        teamMembers.value.map((m, idx) => loadNote(m.userId, idx))
    )
  } catch (e) {
    console.error('팀원 정보 가져오기 실패', e)
  }
}


// 개인 메모 조회 (PrivateNote API)
async function loadNote(targetStudentId, idx) {
  try {
    const { data } = await axios.get(
        `/notes/student/${targetStudentId}`,
        { withCredentials: true }
    )
    if (data.length > 0) {
      const note = data[0]
      teamMembers.value[idx].memo = note.content
      teamMembers.value[idx].noteId = note.noteId
    }
  } catch (e) {
    console.error('메모 불러오기 실패', e)
  }
}

function openInviteModal() {
  showInviteModal.value = true
}

function handleInvite(invited) {
  teamMembers.value.push({
    userId: invited.userId,
    role: invited.role,
    studentNumber: invited.studentNumber || invited.loginId,
    userEmail: invited.userEmail,
    userName: invited.userName,
    profileImageUrl: null,
    avatarColor: getRandomColor(),
    showColorPicker: false,
    memo: '',
    noteId: null,
    isLeader: invited.isLeader ?? invited.leader
  })
  showInviteModal.value = false
}

function evaluateMember(member) {
  memoTarget.value = member
  showMemoModal.value = true
}

function toggleColorPicker(member) {
  teamMembers.value = teamMembers.value.map(m => ({
    ...m,
    showColorPicker: m.userId === member.userId ? !m.showColorPicker : false
  }))
}

function setColor(member, color) {
  const target = teamMembers.value.find(m => m.userId === member.userId)
  if (target) {
    target.avatarColor = color
    target.showColorPicker = false
  }
}

function getRandomColor() {
  return availableColors[Math.floor(Math.random() * availableColors.length)]
}

// 메모 저장 후 상태 반영
function onMemoSaved({ content, noteId }) {
  memoTarget.value.memo = content
  memoTarget.value.noteId = noteId
}

onMounted(async () => {
  await fetchCurrentUser()
  await fetchTeamMembers()
})
</script>

<style scoped>
.team-management-container {
  background-color: #f0f2f5;
  display: flex;
  justify-content: center;
  padding: 60px 20px;
  min-height: 100vh;
  box-sizing: border-box;
}

.main-content {
  width: 100%;
  max-width: 960px;
  background-color: #ffffff;
  border-radius: 20px;
  box-shadow: 0 10px 30px rgba(0, 0, 0, 0.06);
  padding: 32px 40px;
}

.team-management-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 30px;
}

.team-management-header h2 {
  font-size: 1.5rem;
  font-weight: 700;
  color: #1e1e1e;
}

.add-member-btn {
  background-color: #3f8efc;
  color: white;
  border: none;
  padding: 10px 20px;
  border-radius: 8px;
  font-size: 0.95rem;
  font-weight: 500;
  cursor: pointer;
  transition: background-color 0.2s ease;
}

.add-member-btn:hover {
  background-color: #1d6fe6;
}

.team-management-table {
  width: 100%;
  border-collapse: collapse;
  background-color: #fff;
  border-radius: 12px;
  overflow: hidden;
  margin-bottom: 30px;
}

.team-management-table th,
.team-management-table td {
  padding: 16px 20px;
  text-align: left;
  border-bottom: 1px solid #ececec;
  vertical-align: middle;
  font-size: 0.95rem;
}

.team-management-table th {
  background-color: #fafafa;
  font-weight: 600;
  color: #444;
}

.profile-img {
  width: 44px;
  height: 44px;
  border-radius: 50%;
  object-fit: cover;
  border: 1px solid #ddd;
}

.name-with-avatar {
  display: flex;
  align-items: center;
  gap: 10px;
  position: relative;
}

.avatar-wrapper {
  cursor: pointer;
}

.avatar {
  width: 20px;
  height: 20px;
  border-radius: 50%;
  display: inline-block;
}

.color-picker-menu {
  position: absolute;
  bottom: 28px; /* 🔥 top → bottom */
  left: 0;
  display: flex;
  flex-wrap: wrap;
  width: 120px;
  background: #fff;
  border: 1px solid #ddd;
  box-shadow: 0 2px 5px rgba(0, 0, 0, 0.15);
  padding: 6px;
  border-radius: 6px;
  z-index: 9999;
}


.color-option {
  width: 22px;
  height: 22px;
  border-radius: 50%;
  margin: 3px;
  cursor: pointer;
}

.evaluate-btn {
  background-color: #fff;
  border: 1px solid #3f8efc;
  color: #3f8efc;
  padding: 6px 12px;
  border-radius: 6px;
  cursor: pointer;
  font-size: 0.85rem;
  font-weight: 500;
  transition: all 0.2s ease;
}

.evaluate-btn:hover {
  background-color: #3f8efc;
  color: #fff;
}

.professor-section-title {
  font-size: 1.2rem;
  font-weight: 600;
  margin-top: 40px;
  margin-bottom: 16px;
  color: #1e1e1e;
}

</style>