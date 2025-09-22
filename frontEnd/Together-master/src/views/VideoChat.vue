<template>
  <div class="meeting-page">
    <div class="sidebar">
      <div class="sidebar-header">
        <h2>회의</h2>
        <button @click="openNewMeetingModal" class="create-meeting-btn">회의 생성</button>
      </div>
      <ul class="meeting-list">
        <li
            v-for="meeting in meetings"
            :key="meeting.id"
            @click="selectMeeting(meeting)"
            :class="{ 'active': selectedMeeting && selectedMeeting.id === meeting.id }"
        >
          <span class="meeting-title">{{ meeting.title }}</span>
          <span class="meeting-category">{{ meeting.category }}</span>
        </li>
      </ul>
    </div>

    <div class="video-main-content">
      <div class="video-grid" :style="gridStyle">
        <div v-for="user in participants" :key="user.id" class="video-participant">
          <img :src="user.avatar" alt="Participant's avatar" class="participant-avatar">
          <div class="participant-name">{{ user.name }}</div>
        </div>
      </div>
      <div class="control-bar">
        <button class="control-btn mic">마이크</button>
        <button class="control-btn cam">카메라</button>
        <button class="control-btn share">화면공유</button>
        <button class="control-btn leave">나가기</button>
      </div>
    </div>
  </div>

  <NewMeetingModal v-if="isNewMeetingModalOpen" @close="closeNewMeetingModal" />
</template>

<script setup>
import { ref, onMounted, computed } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import NewMeetingModal from '@/components/dashboard/NewMeetingModal.vue';
import defaultAvatar from '@/assets/defaultimage.png';

const route = useRoute();
const router = useRouter();

// --- MeetingPage.vue에서 가져온 사이드바 관련 로직 ---
const meetings = ref([
  // 임시 회의 데이터
  {id: 1, title: "주간 정기 회의", category: "PLANNING"},
  {id: 2, title: "UI/UX 디자인 리뷰", category: "DESIGN"},
  {id: 3, title: "백엔드 아키텍처 논의", category: "DEVELOPMENT"},
]);
const selectedMeeting = ref(null);
const isNewMeetingModalOpen = ref(false);

const selectMeeting = (meeting) => {
  selectedMeeting.value = meeting;
  // 실제로는 회의록 데이터를 불러오지만, 여기서는 화상 채팅방이므로
  // 해당 회의의 화상채팅으로 연결하는 로직을 나중에 추가할 수 있습니다.
  console.log(`Selected meeting: ${meeting.title}`);
};

const openNewMeetingModal = () => {
  isNewMeetingModalOpen.value = true;
};

const closeNewMeetingModal = () => {
  isNewMeetingModalOpen.value = false;
  // 새 회의 생성 후 목록을 다시 불러오는 로직이 필요합니다.
};

onMounted(() => {
  // projectId를 기반으로 회의 목록을 불러오는 API 호출이 필요합니다.
  // 현재는 임시 데이터를 사용합니다.
  if (meetings.value.length > 0) {
    selectMeeting(meetings.value[0]);
  }
});
// --- 여기까지 사이드바 로직 ---


// --- 화상 채팅 관련 로직 ---
const participants = ref([
  { id: 1, name: '참여자 1', avatar: defaultAvatar },
  { id: 2, name: '참여자 2', avatar: defaultAvatar },
  { id: 3, name: '참여자 3', avatar: defaultAvatar },
  { id: 4, name: '참여자 4', avatar: defaultAvatar },
]);

const gridStyle = computed(() => {
  const count = participants.value.length;
  if (count <= 1) return { gridTemplateColumns: '1fr' };
  if (count <= 4) return { gridTemplateColumns: 'repeat(2, 1fr)' };
  if (count <= 9) return { gridTemplateColumns: 'repeat(3, 1fr)' };
  return { gridTemplateColumns: 'repeat(4, 1fr)' };
});
</script>

<style scoped>
/* MeetingPage.vue의 스타일을 그대로 가져옴 */
.meeting-page {
  display: flex;
  height: 100vh;
  background-color: #f9f9f9;
}

.sidebar {
  width: 280px;
  background-color: #fff;
  border-right: 1px solid #e0e0e0;
  display: flex;
  flex-direction: column;
  padding: 20px;
}

.sidebar-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.sidebar-header h2 {
  margin: 0;
  font-size: 1.5rem;
}

.create-meeting-btn {
  padding: 8px 12px;
  background-color: #4a90e2;
  color: white;
  border: none;
  border-radius: 5px;
  cursor: pointer;
  font-size: 0.9rem;
}

.meeting-list {
  list-style: none;
  padding: 0;
  margin: 0;
  overflow-y: auto;
}

.meeting-list li {
  padding: 15px;
  border-bottom: 1px solid #eee;
  cursor: pointer;
}

.meeting-list li:hover {
  background-color: #f0f0f0;
}

.meeting-list li.active {
  background-color: #eef2f7;
  border-right: 3px solid #4a90e2;
}

.meeting-title {
  font-weight: bold;
  display: block;
  margin-bottom: 5px;
}

.meeting-category {
  font-size: 0.8rem;
  color: #888;
}

/* 화상 채팅 메인 콘텐츠 영역 스타일 */
.video-main-content {
  flex-grow: 1;
  display: flex;
  flex-direction: column;
  background-color: #36393f; /* Discord 다크 모드 배경색 */
}

.video-grid {
  flex-grow: 1;
  display: grid;
  gap: 10px;
  padding: 10px;
}

.video-participant {
  background-color: #202225;
  border-radius: 8px;
  display: flex;
  align-items: center;
  justify-content: center;
  position: relative;
  overflow: hidden;
  min-height: 200px;
}

.participant-avatar {
  max-width: 100px;
  max-height: 100px;
  border-radius: 50%;
}

.participant-name {
  position: absolute;
  bottom: 10px;
  left: 10px;
  background-color: rgba(0, 0, 0, 0.5);
  padding: 5px 10px;
  border-radius: 5px;
  font-size: 0.9rem;
  color: #fff;
}

.control-bar {
  display: flex;
  justify-content: center;
  padding: 15px;
  background-color: #2f3136;
}

.control-btn {
  background-color: #40444b;
  border: none;
  color: white;
  padding: 10px 20px;
  margin: 0 10px;
  border-radius: 5px;
  cursor: pointer;
  font-size: 1rem;
}

.control-btn.leave {
  background-color: #d9534f;
}

.control-btn:hover {
  opacity: 0.8;
}
</style>