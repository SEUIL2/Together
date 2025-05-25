<template>
  <div class="team-management-container">
    <main class="main-content">
      <div class="team-management-header">
        <h2>팀원 관리</h2>
        <button class="add-member-btn" @click="openInviteModal">+ 팀원 초대</button>
      </div>

      <table class="team-management-table">
        <thead>
        <tr>
          <th>학번</th>
          <th>이름</th>
          <th>역할</th>
          <th>메모</th>
        </tr>
        </thead>
        <tbody>
        <tr v-for="(member, idx) in teamMembers" :key="member.userId">
          <td>{{ member.studentId }}</td>
          <td>
            <div class="name-with-avatar">
              <div class="avatar-wrapper" @click="toggleColorPicker(idx)">
                <span class="avatar" :style="{ backgroundColor: member.avatarColor }"></span>
              </div>
              <span>{{ member.name }}</span>
              <div
                  v-if="member.showColorPicker"
                  class="color-picker-menu"
                  @click.stop
              >
                <div
                    v-for="color in availableColors"
                    :key="color"
                    class="color-option"
                    :style="{ backgroundColor: color }"
                    @click="setColor(idx, color)"
                ></div>
              </div>
            </div>
          </td>
          <td>{{ member.role }}</td>
          <td>
            <button class="evaluate-btn" @click="evaluateMember(member)">메모</button>
          </td>
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
import { ref, onMounted } from 'vue'
import axios from 'axios'
import { useRoute } from 'vue-router'
import InviteModal from './InviteModal.vue'
import MemoModal from './MemoModal.vue'

// 라우터에서 프로젝트 ID 가져오기
const route = useRoute()
const projectId = Number(route.params.projectId)

// 현재 사용자 정보
const currentUser = ref({})

const availableColors = ['#FF8C00', '#F44336', '#2196F3', '#4CAF50', '#9C27B0']
const teamMembers = ref([])
const showInviteModal = ref(false)
const showMemoModal = ref(false)
const memoTarget = ref(null)

// 내 정보 불러오기
async function fetchCurrentUser() {
  try {
    const { data } = await axios.get('/users/profile', { withCredentials: true })
    currentUser.value = data
  } catch (e) {
    console.error('내 정보 조회 실패', e)
  }
}

// 팀원 불러오기
async function fetchTeamMembers() {
  try {
    const { data } = await axios.get(`/projects/${projectId}/members`, { withCredentials: true })
    teamMembers.value = data.map(member => ({
      userId: member.userId,
      studentId: member.loginId,
      name: member.userName,
      role: member.role,
      avatarColor: getRandomColor(),
      showColorPicker: false,
      memo: member.memo || '',
      evaluationId: member.evaluationId || null
    }))
    // 개인 메모 로드
    await Promise.all(
        teamMembers.value.map((m, idx) => loadMemo(m.userId, idx))
    )
  } catch (e) {
    console.error('팀원 정보 가져오기 실패', e)
  }
}

// 개인 메모 로드
async function loadMemo(evaluateeId, idx) {
  try {
    const { data } = await axios.get(`/evaluations/user/${evaluateeId}`, { withCredentials: true })
    const mine = data.find(e => e.evaluatorName === currentUser.value.userName)
    if (mine) {
      teamMembers.value[idx].memo = mine.comment
      teamMembers.value[idx].evaluationId = mine.evaluationId
    }
  } catch (e) {
    console.error('메모 불러오기 실패', e)
  }
}

function openInviteModal() {
  showInviteModal.value = true
}

function handleInvite(invitedPerson) {
  teamMembers.value.push({
    userId: invitedPerson.userId,
    studentId: invitedPerson.loginId,
    name: invitedPerson.userName,
    role: invitedPerson.role,
    avatarColor: getRandomColor(),
    showColorPicker: false,
    memo: '',
    evaluationId: null
  })
  showInviteModal.value = false
}

function evaluateMember(member) {
  memoTarget.value = member
  showMemoModal.value = true
}

function toggleColorPicker(idx) {
  teamMembers.value = teamMembers.value.map((member, i) => ({
    ...member,
    showColorPicker: i === idx ? !member.showColorPicker : false
  }))
}

function setColor(idx, color) {
  teamMembers.value[idx].avatarColor = color
  teamMembers.value[idx].showColorPicker = false
}

function getRandomColor() {
  return availableColors[Math.floor(Math.random() * availableColors.length)]
}

// 메모 저장 후 상위 컴포넌트 상태 업데이트
function onMemoSaved({ comment, evaluationId }) {
  memoTarget.value.memo = comment
  memoTarget.value.evaluationId = evaluationId
}

onMounted(async () => {
  await fetchCurrentUser()
  await fetchTeamMembers()
})
</script>

<style scoped>
.team-management-container {
  width: 100%;
  min-height: 100vh;
  background-color: #fafafa;
  display: flex;
  justify-content: center;
  align-items: flex-start;
  padding: 20px;
  box-sizing: border-box;
}

.main-content {
  width: 100%;
  max-width: 1000px;
  background-color: #fff;
  border-radius: 10px;
  box-shadow: 0 4px 10px rgba(0, 0, 0, 0.1);
  padding: 20px;
}

.team-management-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.add-member-btn {
  background-color: #3f8efc;
  color: #fff;
  border: none;
  padding: 8px 16px;
  font-size: 0.9rem;
  border-radius: 4px;
  cursor: pointer;
}

.team-management-table {
  width: 100%;
  border-collapse: collapse;
  background-color: #fff;
  border-radius: 10px;
  overflow: hidden;
  box-shadow: 0 4px 10px rgba(0, 0, 0, 0.1);
}

.team-management-table th,
.team-management-table td {
  padding: 12px 16px;
  text-align: left;
  border-bottom: 1px solid #eee;
  vertical-align: middle;
}

.name-with-avatar {
  display: flex;
  align-items: center;
  gap: 8px;
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
  top: 28px;
  left: 0;
  display: flex;
  flex-wrap: wrap;
  width: 100px;
  background: #fff;
  border: 1px solid #eee;
  box-shadow: 0 2px 5px rgba(0, 0, 0, 0.2);
  padding: 4px;
  border-radius: 4px;
  z-index: 100;
}

.color-option {
  width: 20px;
  height: 20px;
  border-radius: 50%;
  margin: 2px;
  cursor: pointer;
}

.evaluate-btn {
  background-color: #fff;
  border: 1px solid #3f8efc;
  color: #3f8efc;
  padding: 4px 8px;
  border-radius: 4px;
  cursor: pointer;
  font-size: 0.8rem;
}

.evaluate-btn:hover {
  background-color: #3f8efc;
  color: #fff;
}
</style>
