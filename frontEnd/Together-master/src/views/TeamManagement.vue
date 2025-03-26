<template>
  <div class="team-management-container">
    <main class="main-content">
      <div class="team-management-header">
        <h2>팀원 관리</h2>
        <button class="add-member-btn" @click="openInviteModal('teamMember')">+ 팀원 초대</button>
      </div>

      <table class="team-management-table">
        <thead>
        <tr>
          <th>팀명</th>
          <th>학번</th>
          <th>이름</th>
          <th>역할</th>
          <th>평가</th>
        </tr>
        </thead>

        <tbody>
        <tr v-for="(member, idx) in teamMembers" :key="idx">
          <td>{{ member.teamName }}</td>
          <td>{{ member.studentId }}</td>
          <td>
            <div class="name-with-avatar">
              <div class="avatar-wrapper" @click="toggleColorPicker(idx)">
                <span class="avatar" :style="{ backgroundColor: member.avatarColor }"></span>
                <div v-if="member.showColorPicker" class="color-picker-menu">
                  <div
                      v-for="color in availableColors"
                      :key="color"
                      class="color-option"
                      :style="{ backgroundColor: color }"
                      @click.stop="setColor(idx, color)"
                  ></div>
                </div>
              </div>
              <span>{{ member.name }}</span>
            </div>
          </td>
          <td>{{ member.role }}</td>
          <td>
            <button class="evaluate-btn" @click="evaluateMember(member)">평가하기</button>
          </td>
        </tr>
        </tbody>
      </table>

      <!-- 🔹 교수 초대 섹션 추가 -->
      <div class="professor-invite-container">
        <h3>담당 교수</h3>
        <div class="invite-box" @click="openInviteModal('professor')">
          <span class="plus-icon">+</span>
        </div>
      </div>

      <!-- 초대 모달 -->
      <InviteModal
          v-if="showInviteModal"
          :inviteType="inviteType"
          @close="showInviteModal = false"
          @invite="handleInvite"
      />
    </main>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import InviteModal from '@/components/InviteModal.vue' // 초대 모달 컴포넌트

const availableColors = ['#FF8C00', '#F44336', '#2196F3', '#4CAF50', '#9C27B0']

// 팀원 목록 (예시 데이터)
const teamMembers = ref([
  { teamName: '팀명', studentId: '2033062', name: '정유명', role: '프론트엔드', avatarColor: '#FF8C00', showColorPicker: false },
  { teamName: '팀명', studentId: '2033062', name: '황스일', role: '백엔드', avatarColor: '#F44336', showColorPicker: false }
])

// 초대 모달 상태 및 타입
const showInviteModal = ref(false)
const inviteType = ref('')

// 초대 모달 열기
function openInviteModal(type) {
  inviteType.value = type
  showInviteModal.value = true
}

// 초대 처리 로직
function handleInvite(invitedPerson) {
  if (inviteType.value === 'teamMember') {
    teamMembers.value.push({ ...invitedPerson, avatarColor: '#2196F3', showColorPicker: false })
  } else if (inviteType.value === 'professor') {
    alert(`'${invitedPerson.name}' 교수님이 초대되었습니다.`)
  }
  showInviteModal.value = false
}

// 평가하기 로직
function evaluateMember(member) {
  alert(`'${member.name}' 님을 평가합니다.`)
}

function toggleColorPicker(idx) {
  teamMembers.value = teamMembers.value.map((member, index) => ({
    ...member,
    showColorPicker: index === idx ? !member.showColorPicker : false
  }))
}

function setColor(idx, color) {
  teamMembers.value[idx].avatarColor = color
  teamMembers.value[idx].showColorPicker = false
}
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
}

.avatar-wrapper {
  cursor: pointer;
  position: relative;
}

.avatar {
  width: 20px;
  height: 20px;
  border-radius: 50%;
  display: inline-block;
  border: 1px solid #ccc;
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

.professor-invite-container {
  margin-top: 20px;
  text-align: left;
  width: 50%;
}

.professor-invite-container h3 {
  font-weight: bold;
  margin-bottom: 8px;
}

.invite-box {
  width: 100%;
  height: 80px;
  border: 2px solid #000;
  display: flex;
  justify-content: center;
  align-items: center;
  cursor: pointer;
}

.plus-icon {
  font-size: 24px;
  font-weight: bold;
}
</style>
