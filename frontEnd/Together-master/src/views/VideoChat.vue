<template>
  <div class="video-chat-page" :class="{ 'minutes-open': !isNotesCollapsed }">
    <!-- 왼쪽: 참여자 목록 -->
    <div class="participant-list-section" :style="{ width: `${participantListWidth}px` }">
      <div class="participant-header">
        <h3>참여자 ({{ totalUsers }})</h3>
      </div>
      <ul class="participant-list">
        <!-- 참여자 목록을 allParticipants로 변경 -->
        <li v-for="user in allParticipants" :key="user.uid" @click="togglePinUser(user.uid)" :class="{ pinned: pinnedUserId === user.uid }"><span class="user-avatar">👤</span> {{ user.isLocal ? `나 (${user.name})` : user.name }}</li>
      </ul>
    </div>

    <!-- 왼쪽: 비디오 영역 -->
    <div class="video-section" :style="{ width: `calc(100% - ${participantListWidth}px - ${notesWidth}px)` }">
      <!-- 사용자 고정 레이아웃 -->
      <div v-if="pinnedUserId" class="pinned-layout">
        <div class="pinned-video-wrapper">
          <div :id="`pinned-player-${pinnedUserId}`" class="video-player"></div>
          <div class="user-label">{{ getUserName(pinnedUserId) }}</div>
          <div v-if="pinnedUserId === uid && isVideoMuted" class="video-muted-overlay">
            <svg xmlns="http://www.w3.org/2000/svg" width="64" height="64" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M16 16v1a2 2 0 0 1-2 2H3a2 2 0 0 1-2-2V7a2 2 0 0 1 2-2h2m5.66 0H14a2 2 0 0 1 2 2v3.34l1 1L23 7v10"></path><line x1="1" y1="1" x2="23" y2="23"></line></svg>
          </div>
        </div>
        <div class="thumbnail-section" :class="{ 'collapsed': !showThumbnails }">
          <div class="thumbnail-grid">
            <div v-for="user in thumbnailUsers" :key="`thumb-${user.uid}`" class="video-player-wrapper">
              <div :id="`thumb-player-${user.uid}`" class="video-player"></div>
              <div class="user-label">{{ getUserName(user.uid) }}</div>
               <div v-if="user.uid === uid && isVideoMuted" class="video-muted-overlay">
                <svg xmlns="http://www.w3.org/2000/svg" width="32" height="32" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M16 16v1a2 2 0 0 1-2 2H3a2 2 0 0 1-2-2V7a2 2 0 0 1 2-2h2m5.66 0H14a2 2 0 0 1 2 2v3.34l1 1L23 7v10"></path><line x1="1" y1="1" x2="23" y2="23"></line></svg>
              </div>
            </div>
          </div>
           <button class="thumbnail-toggle-btn" @click="showThumbnails = !showThumbnails" :title="showThumbnails ? '썸네일 숨기기' : '썸네일 보이기'">
            <svg v-if="showThumbnails" xmlns="http://www.w3.org/2000/svg" width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"></polyline></svg>
            <svg v-else xmlns="http://www.w3.org/2000/svg" width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="6 9 12 15 18 9"></polyline></svg>
          </button>
        </div>
      </div>
      <!-- 일반 그리드 레이아웃 -->
      <div v-else class="video-grid" :style="gridStyle">
        <!-- Local User -->
        <div class="video-player-wrapper">
          <div id="local-player" class="video-player"></div>
          <div class="user-label">나 ({{ getUserName(uid) }})</div>
          <div v-if="isVideoMuted" class="video-muted-overlay">
            <svg xmlns="http://www.w3.org/2000/svg" width="64" height="64" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M16 16v1a2 2 0 0 1-2 2H3a2 2 0 0 1-2-2V7a2 2 0 0 1 2-2h2m5.66 0H14a2 2 0 0 1 2 2v3.34l1 1L23 7v10"></path><line x1="1" y1="1" x2="23" y2="23"></line></svg>
          </div>
        </div>
        <!-- Remote Users -->
        <div v-for="user in remoteUsers" :key="user.uid" class="video-player-wrapper">
          <div :id="`remote-player-${user.uid}`" class="video-player"></div>
          <div class="user-label">{{ getUserName(user.uid) }}</div>
        </div>
      </div>

       <div class="controls-bar">
        <button @click="toggleAudio" :class="{ active: isAudioMuted }">
          <svg v-if="!isAudioMuted" xmlns="http://www.w3.org/2000/svg" width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M12 1a3 3 0 0 0-3 3v8a3 3 0 0 0 6 0V4a3 3 0 0 0-3-3z"></path><path d="M19 10v2a7 7 0 0 1-14 0v-2"></path><line x1="12" y1="19" x2="12" y2="23"></line><line x1="8" y1="23" x2="16" y2="23"></line></svg>
          <svg v-else xmlns="http://www.w3.org/2000/svg" width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><line x1="1" y1="1" x2="23" y2="23"></line><path d="M9 9v3a3 3 0 0 0 5.12 2.12M15 9.34V4a3 3 0 0 0-5.94-.6"></path><path d="M17 16.95A7 7 0 0 1 5 12v-2m14 0v2a7 7 0 0 1-.11 1.23"></path><line x1="12" y1="19" x2="12" y2="23"></line><line x1="8" y1="23" x2="16" y2="23"></line></svg>
          <span>{{ isAudioMuted ? '음소거 해제' : '음소거' }}</span>
        </button>
        <button @click="toggleVideo" :class="{ active: isVideoMuted }">
          <svg v-if="!isVideoMuted" xmlns="http://www.w3.org/2000/svg" width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polygon points="23 7 16 12 23 17 23 7"></polygon><rect x="1" y="5" width="15" height="14" rx="2" ry="2"></rect></svg>
          <svg v-else xmlns="http://www.w3.org/2000/svg" width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M16 16v1a2 2 0 0 1-2 2H3a2 2 0 0 1-2-2V7a2 2 0 0 1 2-2h2m5.66 0H14a2 2 0 0 1 2 2v3.34l1 1L23 7v10"></path><line x1="1" y1="1" x2="23" y2="23"></line></svg>
          <span>{{ isVideoMuted ? '카메라 켜기' : '카메라 끄기' }}</span>
        </button>
        <button @click="toggleScreenShare" :class="{ active: isScreenSharing }">
          <svg xmlns="http://www.w3.org/2000/svg" width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M21 12V7a2 2 0 0 0-2-2H5a2 2 0 0 0-2 2v10a2 2 0 0 0 2 2h4"></path><path d="m22 14-4 4 4 4"></path><path d="M18 18h-7"></path></svg>
          <span>{{ isScreenSharing ? '공유 중지' : '화면 공유' }}</span>
        </button>
        <button @click="toggleRecording" :class="{ active: isRecording }">
          <svg xmlns="http://www.w3.org/2000/svg" width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><circle cx="12" cy="12" r="10"></circle><circle v-if="isRecording" cx="12" cy="12" r="4" fill="red" stroke="none"></circle></svg>
          <span>{{ isRecording ? '녹화 중지' : '녹화 시작' }}</span>
        </button>
        <button @click="isParticipantListCollapsed = !isParticipantListCollapsed" class="participants-toggle-btn" :class="{ active: !isParticipantListCollapsed }">
          <svg xmlns="http://www.w3.org/2000/svg" width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M17 21v-2a4 4 0 0 0-4-4H5a4 4 0 0 0-4 4v2"></path><circle cx="9" cy="7" r="4"></circle><path d="M23 21v-2a4 4 0 0 0-3-3.87"></path><path d="M16 3.13a4 4 0 0 1 0 7.75"></path></svg>
          <span>참여자</span>
        </button>
        <button @click="isNotesCollapsed = !isNotesCollapsed" class="notes-toggle-btn" :class="{ active: !isNotesCollapsed }">
          <svg xmlns="http://www.w3.org/2000/svg" width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"></path><polyline points="14 2 14 8 20 8"></polyline><line x1="16" y1="13" x2="8" y2="13"></line><line x1="16" y1="17" x2="8" y2="17"></line><polyline points="10 9 9 9 8 9"></polyline></svg>
          <span>회의록</span>
        </button>
        <button @click="leaveChannel" class="leave-btn">
          <svg xmlns="http://www.w3.org/2000/svg" width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M10.68 13.31a16 16 0 0 0 3.41 2.6l1.27-1.27a2 2 0 0 1 2.11-.45 12.84 12.84 0 0 0 2.81.7 2 2 0 0 1 1.72 2v3a2 2 0 0 1-2.18 2 19.79 19.79 0 0 1-8.63-3.07 19.5 19.5 0 0 1-6-6 19.79 19.79 0 0 1-3.07-8.67A2 2 0 0 1 4.11 2h3a2 2 0 0 1 2 1.72 12.84 12.84 0 0 0 .7 2.81 2 2 0 0 1-.45 2.11L8.09 9.91"></path><path d="M22 2L2 22"></path></svg>
          <span>회의 종료</span>
        </button>
      </div>
    </div>

    <!-- Resizer -->
    <div v-if="!isNotesCollapsed" class="resizer" @mousedown="startResize"></div>

    <!-- 오른쪽: 회의록 영역 -->
    <div class="notes-section" :style="{ width: `${notesWidth}px` }">
      <div class="minutes-header">
        <h3>실시간 회의록</h3>
        <div class="header-controls">
          <select v-model="meetingCategory" @change="debouncedSaveNotes" class="category-select">
            <option v-for="(label, key) in categories" :key="key" :value="key">{{ label }}</option>
          </select>
          <span v-if="saveStatus === 'saving'" class="save-status">저장 중...</span>
          <span v-else-if="saveStatus === 'saved'" class="save-status saved">✓ 저장됨</span>
        </div>
        <button class="ai-summary-btn" @click="summarizeNotes" title="AI 요약 (베타)">
          <svg xmlns="http://www.w3.org/2000/svg" width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M12 8V4H8"/><rect x="4" y="4" width="16" height="16" rx="2"/><path d="M12 12h4"/><path d="M12 16h4"/><path d="M8 12h.01"/><path d="M8 16h.01"/></svg>
        </button>
      </div>
      <v-md-editor
        v-model="meetingNotes"
        :height="`calc(100vh - 80px - 66px)`"
        @change="debouncedSaveNotes"
      />
    </div>
  </div>
</template>

<script setup>
import { onMounted, onUnmounted, ref, computed, nextTick, watch } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import AgoraRTC from 'agora-rtc-sdk-ng';
import api from '@/api'; // ✅ 프로젝트 공용 api 인스턴스 사용
import debounce from 'lodash/debounce';

const route = useRoute();
const router = useRouter();

const appId = 'becf4d48a91f41c88b7908fc88d049b2'; // 본인의 Agora App ID를 입력하세요.
const channel = ref(route.query.channel);
const token = ref(null); // URL에서 토큰을 읽지 않고 null로 초기화
const uid = ref(null); // UID를 저장할 ref 추가
const client = ref(null);
const localAudioTrack = ref(null);
const localVideoTrack = ref(null);
const localScreenTrack = ref(null); // 화면 공유 트랙
const remoteUsers = ref([]);
const joinedRemoteUsers = ref([]); // Users who have joined the channel (from user-joined event)

const allParticipants = ref([]); // API로 가져온 전체 참여자 목록
let participantPollInterval = null; // 참여자 목록 폴링을 위한 인터벌 ID

const isScreenSharing = ref(false);
const isAudioMuted = ref(false);
const isVideoMuted = ref(false);

// 녹화 관련 상태
const isRecording = ref(false);
const mediaRecorder = ref(null);
const recordedChunks = ref([]);

// 사용자 정보 맵 (UID -> 이름)
const memberInfoMap = ref(new Map());

// 사용자 고정(Pin) 관련 상태
const pinnedUserId = ref(null);

// 썸네일 표시 여부 상태
const showThumbnails = ref(true);

// 참여자 목록 관련 상태
const isParticipantListCollapsed = ref(true);
const participantListWidth = ref(0);
// 회의록 관련 상태
const isNotesCollapsed = ref(true); // 회의록 접기/펼치기 상태 (기본값: 접힘)
const notesWidth = ref(0); // 회의록 너비 (반응형)
const meetingId = ref(null);
const meetingTitle = ref(''); // 회의록 제목을 저장할 ref
const meetingCategory = ref(''); // 회의록 카테고리를 저장할 ref
const meetingNotes = ref('');
const saveStatus = ref('idle'); // 'idle', 'saving', 'saved'
const categories = {
  PLANNING: '기획',
  DESIGN: '설계',
  DEVELOPMENT: '개발',
  TEST: '테스트'
};

const getUserName = (userId) => {
  if (!userId) return '알 수 없음';
  return memberInfoMap.value.get(userId) || `사용자 ${userId}`;
};

const thumbnailUsers = computed(() => {
  const allUsers = [{ uid: uid.value }, ...remoteUsers.value];
  return allUsers.filter(user => user.uid !== pinnedUserId.value);
});

const togglePinUser = (userId) => {
  if (pinnedUserId.value === userId) {
    pinnedUserId.value = null; // Unpin
  } else {
    pinnedUserId.value = userId; // Pin
  }
  // 사용자를 고정할 때 항상 썸네일 목록을 보이도록 설정
  if (pinnedUserId.value) {
    showThumbnails.value = true;
  }
};

watch(pinnedUserId, async (newPin, oldPin) => {
  await nextTick(); // DOM 업데이트 기다리기

  // 현재 로컬에서 송출 중인 비디오 트랙 (카메라인지 화면공유인지)
  const currentLocalVideoTrack = isScreenSharing.value ? localScreenTrack.value : localVideoTrack.value;

  const allUsers = [
    { uid: uid.value, track: currentLocalVideoTrack },
    ...remoteUsers.value.map(u => ({ uid: u.uid, track: u.videoTrack }))
  ];

  allUsers.forEach(user => {
    if (!user.track) return;

    // Stop playing everywhere first to avoid conflicts
    user.track.stop();

    if (newPin) { // 사용자를 고정했을 때
      if (newPin === user.uid) {
        // 1. 고정된 사용자는 메인 뷰에서 재생
        const playerEl = document.getElementById(`pinned-player-${user.uid}`);
        if (playerEl) user.track.play(playerEl);
      } else {
        // 2. 나머지 사용자는 썸네일 뷰에서 재생
        const playerEl = document.getElementById(`thumb-player-${user.uid}`);
        if (playerEl) user.track.play(playerEl);
      }
    } else { // 고정을 해제했을 때 (newPin is null)
      // 3. 모든 사용자를 원래의 그리드 뷰에서 재생
      const playerElId = user.uid === uid.value ? 'local-player' : `remote-player-${user.uid}`;
      const playerEl = document.getElementById(playerElId);
      if (playerEl) user.track.play(playerEl);
    }
  });
});

// 참여자 목록 접기/펼치기 상태 감시
watch(isParticipantListCollapsed, (collapsed) => {
  if (collapsed) {
    participantListWidth.value = 0;
  } else {
    participantListWidth.value = 240; // 펼쳤을 때 기본 너비
  }
});

// 회의록 접기/펼치기 상태 감시
watch(isNotesCollapsed, (collapsed) => {
  if (collapsed) {
    notesWidth.value = 0;
  } else {
    // 이전에 설정된 너비가 있다면 유지, 없다면 기본값 400
    notesWidth.value = notesWidth.value > 0 ? notesWidth.value : 400;
  }
});

// --- 회의록 너비 조절 로직 ---
const isResizing = ref(false);

const startResize = (event) => {
  event.preventDefault();
  isResizing.value = true;
  document.body.style.cursor = 'col-resize';
  document.body.style.userSelect = 'none';
  window.addEventListener('mousemove', handleMouseMove);
  window.addEventListener('mouseup', stopResize);
};

const handleMouseMove = (event) => {
  if (!isResizing.value) return;
  const newWidth = window.innerWidth - event.clientX;
  // 최소/최대 너비 제한
  if (newWidth > 300 && newWidth < window.innerWidth * 0.8) {
    notesWidth.value = newWidth;
  }
};

const stopResize = () => {
  isResizing.value = false;
  document.body.style.cursor = '';
  document.body.style.userSelect = '';
  window.removeEventListener('mousemove', handleMouseMove);
  window.removeEventListener('mouseup', stopResize);
};

onMounted(async () => {
  // 🔑 [수정] API 요청 전에 인증 헤더를 설정합니다.
  const authHeader = localStorage.getItem('authHeader');
  if (authHeader) {
    api.defaults.headers.common['Authorization'] = authHeader;
  }

  // 0. 프로젝트 멤버 정보 가져오기 (UID-이름 매핑용)
  try {
    const projectId = route.query.projectId || channel.value;
    if (projectId) {
      const response = await api.get('/projects/members', { params: { projectId } });
      const members = response.data || [];
      const newMap = new Map();
      members.forEach(member => {
        newMap.set(member.userId, member.userName);
      });
      memberInfoMap.value = newMap;
      console.log('✅ 팀원 정보 로드 완료:', memberInfoMap.value);
    }
  } catch (error) {
    console.error('❌ 팀원 정보 로드 실패:', error);
  }

  // 0.5. 참여자 목록 주기적으로 가져오기 시작
  const fetchParticipants = async () => {
    try {
      const projectId = route.query.projectId || channel.value;
      if (!projectId) return;

      const response = await api.get('/agora/participants', { params: { projectId } });
      const participantData = response.data?.data;
      if (participantData && participantData.users) {
        const userIds = participantData.users;
        // 로컬 사용자를 포함하여 전체 참여자 목록 업데이트
        allParticipants.value = userIds.map(id => ({
          uid: id,
          name: getUserName(id),
          isLocal: id === uid.value
        }));
      }
    } catch (error) {
      console.error('❌ 참여자 목록 조회 실패:', error);
    }
  };

  await fetchParticipants(); // 최초 즉시 실행
  participantPollInterval = setInterval(fetchParticipants, 5000); // 5초마다 폴링

  // 1. 화상회의 준비 (토큰 발급, 회의록 생성)
  const isReady = await prepareForMeeting();
  if (!isReady) {
    alert('화상회의 입장에 실패했습니다. 잠시 후 다시 시도해주세요.');
    router.back();
    return;
  }

  client.value = AgoraRTC.createClient({ mode: 'rtc', codec: 'vp8' });

  // Event: Remote user joins the channel
  client.value.on('user-joined', user => {
    console.log(`[DEBUG] User joined event: UID=${user.uid}`);
    if (!joinedRemoteUsers.value.find(u => u.uid === user.uid)) {
      joinedRemoteUsers.value.push(user);
    }
  });

  // Event: Remote user leaves the channel
  client.value.on('user-left', user => {
    console.log(`[DEBUG] User left event: UID=${user.uid}`);
    joinedRemoteUsers.value = joinedRemoteUsers.value.filter(u => u.uid !== user.uid);
    remoteUsers.value = remoteUsers.value.filter(u => u.uid !== user.uid); // Also remove from published list
  });

  client.value.on('user-published', async (user, mediaType) => {
    console.log(`[DEBUG] User published event: UID=${user.uid}, mediaType=${mediaType}`);

    // Find the user from the client's internal list to ensure we have the most current object.
    // This helps mitigate potential race conditions where the 'user' object from the event
    // might not be fully ready for subscription immediately.
    const remoteUserInClient = client.value.remoteUsers.find(u => u.uid === user.uid);

    if (!remoteUserInClient) {
      console.warn(`[WARN] User ${user.uid} published, but not found in client.remoteUsers. Skipping subscription.`);
      return; // Do not attempt to subscribe if not officially recognized by the client.
    }

    // A short delay to mitigate race conditions before subscribing.
    await new Promise(resolve => setTimeout(resolve, 500));

    try {
      // Now, subscribe to the user using the object from client.remoteUsers.
      await client.value.subscribe(remoteUserInClient, mediaType);
      console.log(`[DEBUG] Successfully subscribed to UID=${remoteUserInClient.uid}, mediaType=${mediaType}`);

      // Add user to remoteUsers list if not already present, only after successful subscription.
      // This ensures the video player container is created and only for successfully subscribed users.
      if (!remoteUsers.value.find(u => u.uid === remoteUserInClient.uid)) {
        remoteUsers.value.push(remoteUserInClient);
      }

      if (mediaType === 'video') {
        const remoteVideoTrack = remoteUserInClient.videoTrack;
        await nextTick();
        const playerElement = document.getElementById(`remote-player-${remoteUserInClient.uid}`);
        if (playerElement) {
          remoteVideoTrack.play(playerElement);
          console.log(`[DEBUG] Playing video for UID=${remoteUserInClient.uid}`);
        } else {
          console.warn(`[WARN] Player element for UID=${remoteUserInClient.uid} not found after subscription.`);
        }
      }

      if (mediaType === 'audio') {
        remoteUserInClient.audioTrack.play();
        console.log(`[DEBUG] Playing audio for UID=${remoteUserInClient.uid}`);
      }
    } catch (subscribeError) {
      console.error(`[ERROR] Failed to subscribe to user ${remoteUserInClient.uid} (${mediaType}):`, subscribeError);
      // If subscription fails, ensure user is removed from remoteUsers list
      remoteUsers.value = remoteUsers.value.filter(u => u.uid !== remoteUserInClient.uid);
    }
  });

  client.value.on('user-unpublished', user => {
    console.log(`[DEBUG] User unpublished event: UID=${user.uid}`);
  });

  // 3. Agora 채널 참여 및 미디어 발행
  try {
    // join 메서드에 백엔드에서 받은 uid.value를 명시적으로 전달
    console.log('--- Agora Join 시도 정보 ---');
    console.log(`App ID: ${appId}`);
    console.log(`Channel Name: ${channel.value}`);
    console.log(`UID: ${uid.value}`);
    console.log(`Token: ${token.value}`);
    console.log('--------------------------');

    const joinedUid = await client.value.join(appId, channel.value, token.value, uid.value);
    console.log(`Successfully joined channel ${channel.value} with UID ${joinedUid}`);
    // join 후에 실제 할당된 UID로 값을 업데이트합니다.
    uid.value = joinedUid;

    localAudioTrack.value = await AgoraRTC.createMicrophoneAudioTrack();
    localVideoTrack.value = await AgoraRTC.createCameraVideoTrack();

    document.getElementById('local-player').innerHTML = '';
    localVideoTrack.value.play('local-player');

    await client.value.publish([localAudioTrack.value, localVideoTrack.value]);
  } catch (error) {
    console.error('Failed to join channel', error);
    alert('화상회의 채널 입장에 실패했습니다. 네트워크 연결을 확인하거나 잠시 후 다시 시도해주세요.');
    await leaveChannel(true); // 리다이렉션만 수행
  }
});

const prepareForMeeting = async () => {
    if (!channel.value) {
        alert('채널 정보가 없습니다. 메인 페이지로 돌아갑니다.');
        return false;
    }

    try {
        let isProfessor = false;
        try {
            const meResp = await api.get('/auth/me');
            const roles = meResp.data.roles || [];
            isProfessor = roles.some(r => r.authority === 'ROLE_PROFESSOR');
        } catch (e) {
            console.warn('auth/me 호출 실패, 교수 여부 확인 불가:', e);
        }

        const [tokenResult, meetingResult] = await Promise.allSettled([
            api.get('/agora/token', { params: { projectId: channel.value } }),
            setupMeeting()
        ]);

        if (tokenResult.status !== 'fulfilled') {
            throw tokenResult.reason || new Error('토큰 요청에 실패했습니다.');
        }
        const tokenResponse = tokenResult.value;
        console.log('[DEBUG] Agora 토큰 응답:', tokenResponse.data);
        token.value = tokenResponse.data.token;
        uid.value = tokenResponse.data.userId ?? tokenResponse.data.user_id;

        if (!token.value || uid.value === undefined) {
            throw new Error('토큰 또는 UID가 유효하지 않습니다.');
        }

        if (meetingResult.status === 'rejected' || meetingResult.value === false) {
            console.error('회의록 생성 중 오류가 발생했습니다:', meetingResult.status === 'rejected' ? meetingResult.reason : '생성 실패');
            if (!isProfessor) {
                alert('회의록을 생성하는 데 실패했습니다. 회의 내용은 저장되지 않을 수 있습니다.');
            } else {
                meetingId.value = null;
            }
        }

        console.log('✅ Agora 토큰 및 회의 준비 완료');
        return true;
    } catch (error) {
        console.error('❌ 화상회의 준비 실패:', error.response?.data || error.message);
        alert('화상회의 준비에 실패했습니다. ' + (error.response?.data?.message || error.message));
        return false;
    }
};

// --- 회의록 관련 로직 ---
const setupMeeting = async () => {
    try {
        const projectId = route.query.projectId || channel.value;
        const today = new Date().toLocaleDateString('ko-KR', { year: 'numeric', month: '2-digit', day: '2-digit' }).replace(/\. /g, '-').replace('.', '');
        const meetingDto = {
            title: `화상회의 - ${today}`,
            content: '# 화상회의 실시간 회의록',
            meetingDate: new Date().toISOString(),
            category: 'DEVELOPMENT'
        };
        const response = await api.post('/meeting/create', meetingDto, { params: { projectId } });
        meetingId.value = response.data.meetingId;
        meetingTitle.value = response.data.title;
        meetingCategory.value = response.data.category;
        meetingNotes.value = response.data.content;
        console.log(`✅ 화상회의를 위한 회의록(ID: ${meetingId.value})이 생성되었습니다.`);
        return true;
    } catch (err) {
        console.error('❌ 회의록 생성 실패:', err);
        return false;
    }
};

const saveNotes = async () => {
    if (!meetingId.value || saveStatus.value === 'saving') return;
    saveStatus.value = 'saving';
    try {
        await api.put(`/meeting/update/${meetingId.value}`, {
            title: meetingTitle.value,
            content: meetingNotes.value,
            category: meetingCategory.value,
            updatedAt: new Date().toISOString()
        });
        saveStatus.value = 'saved';
        setTimeout(() => (saveStatus.value = 'idle'), 2000);
    } catch (err) {
        console.error('❌ 회의록 저장 실패:', err);
        saveStatus.value = 'idle';
    }
};

// --- AI 요약 관련 로직 (프론트엔드 데모) ---
const summarizeNotes = () => {
  const text = meetingNotes.value;
  if (!text || text.trim().length < 100) {
    alert('요약할 내용이 충분하지 않습니다. (최소 100자 이상)');
    return;
  }

  try {
    // 1. 문장 분리
    const sentences = text.match(/[^.!?]+[.!?]+/g) || [];
    if (sentences.length < 3) {
      alert('요약할 문장이 부족합니다. (최소 3문장 이상)');
      return;
    }

    // 2. 단어 빈도수 계산 (간단한 불용어 처리 포함)
    const stopWords = new Set(['이', '그', '저', '것', '수', '등', '및', '에', '의', '를', '은', '는', '가', '와', '과', '도', '으로', '에서', '입니다', '합니다', '그리고', '하지만']);
    const wordFrequencies = {};
    text.split(/\s+/).forEach(word => {
      const cleanWord = word.replace(/[.,!?]/g, '').toLowerCase();
      if (cleanWord && !stopWords.has(cleanWord)) {
        wordFrequencies[cleanWord] = (wordFrequencies[cleanWord] || 0) + 1;
      }
    });

    // 3. 문장별 점수 계산
    const sentenceScores = sentences.map(sentence => {
      let score = 0;
      sentence.split(/\s+/).forEach(word => {
        const cleanWord = word.replace(/[.,!?]/g, '').toLowerCase();
        if (wordFrequencies[cleanWord]) {
          score += wordFrequencies[cleanWord];
        }
      });
      return { sentence, score };
    });

    // 4. 점수가 높은 상위 3개 문장 추출
    const summarySentences = sentenceScores
      .sort((a, b) => b.score - a.score)
      .slice(0, 3)
      .map(item => item.sentence.trim());

    alert('✨ 회의록 요약 (Beta)\n\n- ' + summarySentences.join('\n- '));
  } catch (error) {
    console.error("요약 중 오류 발생:", error);
    alert("회의록을 요약하는 중 오류가 발생했습니다.");
  }
};

const debouncedSaveNotes = debounce(saveNotes, 3000);

const toggleAudio = async () => {
  if (localAudioTrack.value) {
    isAudioMuted.value = !isAudioMuted.value;
    await localAudioTrack.value.setMuted(isAudioMuted.value);
  }
};

const toggleVideo = async () => {
  if (localVideoTrack.value) {
    isVideoMuted.value = !isVideoMuted.value;
    await localVideoTrack.value.setMuted(isVideoMuted.value);
  }
};

const toggleScreenShare = async () => {
  if (!client.value) return;

  if (isScreenSharing.value) {
    // --- 화면 공유 중지 ---
    try {
      // 1. 스크린 트랙 송출 중단 및 닫기
      await client.value.unpublish(localScreenTrack.value);
      localScreenTrack.value.close();
      localScreenTrack.value = null;

      // 2. 로컬 플레이어 정리 및 카메라 트랙 다시 재생
      document.getElementById('local-player').innerHTML = '';
      if (localVideoTrack.value) {
        localVideoTrack.value.play('local-player');
        // 3. 카메라 트랙 다시 송출
        await client.value.publish(localVideoTrack.value);
      }
      isScreenSharing.value = false;
    } catch (error) {
      console.error('화면 공유 중지 실패:', error);
    }
  } else {
    // --- 화면 공유 시작 ---
    try {
      // 1. 스크린 트랙 생성 (사용자가 공유할 화면/창/탭 선택)
      localScreenTrack.value = await AgoraRTC.createScreenVideoTrack({}, "auto");

      // 2. 카메라 트랙 송출 중단
      if (localVideoTrack.value) {
        await client.value.unpublish(localVideoTrack.value);
      }

      // 3. 로컬 플레이어에 스크린 트랙 재생
      document.getElementById('local-player').innerHTML = '';
      localScreenTrack.value.play('local-player');

      // 4. 스크린 트랙 송출
      await client.value.publish(localScreenTrack.value);
      isScreenSharing.value = true;

      // 사용자가 브라우저의 '공유 중지' 버튼을 눌렀을 때 처리
      localScreenTrack.value.on("track-ended", () => toggleScreenShare());
    } catch (error) {
      console.error('화면 공유 시작 실패:', error);
      localScreenTrack.value?.close();
      localScreenTrack.value = null;
    }
  }
};

const toggleRecording = async () => {
  if (isRecording.value) {
    // --- 녹화 중지 ---
    if (mediaRecorder.value && mediaRecorder.value.state === 'recording') {
      mediaRecorder.value.stop();
    }
    isRecording.value = false;
  } else {
    // --- 녹화 시작 ---
    try {
      // 1. 화면 캡처 스트림 가져오기 (오디오 포함)
      const displayStream = await navigator.mediaDevices.getDisplayMedia({
        video: { cursor: "always" },
        audio: true
      });

      // 2. 회의 오디오 트랙들 가져오기
      const audioTracks = [];
      if (localAudioTrack.value) {
        audioTracks.push(localAudioTrack.value.getMediaStreamTrack());
      }
      remoteUsers.value.forEach(user => {
        if (user.audioTrack) {
          audioTracks.push(user.audioTrack.getMediaStreamTrack());
        }
      });

      // 3. 모든 오디오 트랙을 화면 캡처 스트림에 추가
      audioTracks.forEach(track => displayStream.addTrack(track));

      // 4. MediaRecorder 설정 및 시작
      recordedChunks.value = [];
      mediaRecorder.value = new MediaRecorder(displayStream, {
        mimeType: 'video/webm; codecs=vp9'
      });

      mediaRecorder.value.ondataavailable = (event) => {
        if (event.data.size > 0) {
          recordedChunks.value.push(event.data);
        }
      };

      mediaRecorder.value.onstop = () => {
        const blob = new Blob(recordedChunks.value, { type: 'video/webm' });
        const url = URL.createObjectURL(blob);
        const a = document.createElement('a');
        a.href = url;
        a.download = `Together-Recording-${new Date().toISOString()}.webm`;
        a.click();
        window.URL.revokeObjectURL(url);
        // 스트림의 모든 트랙 중지
        displayStream.getTracks().forEach(track => track.stop());
      };

      mediaRecorder.value.start();
      isRecording.value = true;

      // 사용자가 브라우저의 '공유 중지' 버튼을 누르면 녹화도 중지
      displayStream.getVideoTracks()[0].onended = () => toggleRecording();
    } catch (err) {
      console.error("녹화 시작 실패:", err);
      alert("화면 녹화를 시작할 수 없습니다. 권한을 확인해주세요.");
    }
  }
};

// totalUsers를 allParticipants 기반으로 변경
const totalUsers = computed(() => allParticipants.value.length);

// const totalUsers = computed(() => remoteUsers.value.length + 1);

const gridStyle = computed(() => {
  const columns = Math.ceil(Math.sqrt(totalUsers.value));
  return {
    'grid-template-columns': `repeat(${columns}, 1fr)`,
  };
});

const leaveChannel = async (redirectOnly = false) => {
  if (!redirectOnly) {
    localAudioTrack.value?.close();
    localVideoTrack.value?.close();
    localScreenTrack.value?.close(); // 화면 공유 트랙도 닫기

    if (isRecording.value) {
      toggleRecording(); // 녹화 중이면 중지
    }

    // client.value가 null이 아닐 때만 leave() 호출
    if (client.value) {
      await client.value.leave();
    }

    // 참여자 목록 폴링 중지
    if (participantPollInterval) {
      clearInterval(participantPollInterval);
    }
  }

  const pid = route.query?.projectId || channel.value;
  const readonly = route.query?.readonly;
  const projectTitle = route.query?.projectTitle;

  // 교수 열람 모드로 들어온 경우, 퇴장 시에도 읽기 모드를 유지해 프로젝트 뷰로 복귀
  if (readonly === 'true' && pid) {
    router.push({ path: `/professor/project/${pid}`, query: { readonly, projectTitle } });
    return;
  }

  // 기본: VideoConferenceLobby로 이동
  router.push({
    name: 'VideoConferenceLobby',
    query: { projectId: pid, projectTitle: projectTitle, readonly: readonly },
  });
};

onUnmounted(() => {
  leaveChannel();
});
</script>

<style scoped>
.video-chat-page {
  display: flex;
  height: 100vh; /* 전체 화면 높이 사용 */
  background-color: #1a1a1a;
  color: #fff;
  overflow: hidden;
  position: relative;
}

.participant-list-section {
  background-color: #2a2a2a;
  color: #fff;
  height: 100%;
  flex-shrink: 0;
  overflow: hidden;
  transition: width 0.3s ease;
  display: flex;
  flex-direction: column;
  border-right: 1px solid #444;
}
.participant-header {
  padding: 16px;
  border-bottom: 1px solid #444;
  flex-shrink: 0;
}
.participant-header h3 {
  margin: 0;
  font-size: 16px;
}
.participant-list {
  list-style: none;
  padding: 8px;
  margin: 0;
  overflow-y: auto;
  flex-grow: 1;
}
.participant-list li {
  padding: 10px 8px;
  border-radius: 4px;
  display: flex;
  align-items: center;
  gap: 8px;
  cursor: pointer;
}
.participant-list li.pinned {
  background-color: #3f8efc;
  font-weight: bold;
}
.video-section {
  flex-grow: 1;
    display: flex;
    flex-direction: column;
    height: 100%;
    position: relative;
    /* transition: width 0.3s ease; */ /* 드래그 시 부자연스러워 보일 수 있어 제거 */
    width: 100%;
}

.pinned-layout {
  display: flex;
  flex-direction: column;
  height: 100%;
  width: 100%;
  padding: 10px;
  gap: 10px;
}
.pinned-video-wrapper {
  flex-grow: 1;
  position: relative;
  background-color: #2c2c2c;
  border-radius: 12px;
  overflow: hidden;
  min-height: 0; /* flex 아이템이 부모보다 작아질 수 있도록 설정 */
}
.thumbnail-section {
  position: relative;
  flex-shrink: 0;
  transition: height 0.3s ease;
  height: 150px; /* 썸네일 그리드 높이 + 토글 버튼 높이 */
}
.thumbnail-section.collapsed {
  height: 30px; /* 토글 버튼 높이만 남김 */
}
.thumbnail-grid {
  display: flex;
  gap: 10px;
  height: 120px;
  padding-bottom: 30px; /* 토글 버튼 공간 확보 */
  overflow-x: auto;
}
.thumbnail-toggle-btn {
  position: absolute;
  bottom: 0;
  left: 50%;
  transform: translateX(-50%);
  width: 100px;
  height: 30px;
  background-color: rgba(44, 44, 44, 0.8);
  border: none;
  color: white;
  border-radius: 10px 10px 0 0;
  cursor: pointer;
  display: flex;
  justify-content: center;
  align-items: center;
}
.video-grid {
  flex-grow: 1;
  width: 100%;
  padding: 10px;
  gap: 10px;
  display: grid;
}

.video-player-wrapper {
  position: relative;
  background-color: #2c2c2c;
  border-radius: 12px;
  overflow: hidden;
  display: flex;
  align-items: center;
  justify-content: center;
}

.video-player {
  width: 100%;
  height: 100%;
}

/* This is a global style for Agora's video element */
:global(.video-player video) {
  width: 100% !important;
  height: 100% !important;
  object-fit: cover;
}

/* 사용자 고정 시, 비디오가 잘리지 않도록 contain으로 변경 */
:global(.pinned-video-wrapper .video-player video) {
  object-fit: contain !important;
}


.user-label {
  position: absolute;
  bottom: 8px;
  left: 8px;
  background-color: rgba(0, 0, 0, 0.5);
  color: white;
  padding: 4px 8px;
  border-radius: 4px;
  font-size: 14px;
}

.video-muted-overlay {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  display: flex;
  align-items: center;
  justify-content: center;
  background-color: #2c2c2c;
  color: #888;
}

.controls-bar {
  height: 80px;
  flex-shrink: 0;
  background-color: rgba(0, 0, 0, 0.3);
  display: flex;
  justify-content: center;
  align-items: center;
  gap: 16px;
}

.controls-bar button {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 4px;
  width: 80px;
  height: 60px;
  background-color: #4a4a4a;
  color: white;
  border: none;
  border-radius: 12px;
  cursor: pointer;
  font-size: 12px;
  transition: background-color 0.2s;
}

.controls-bar button:hover {
  background-color: #6a6a6a;
}

.controls-bar button.active,
.controls-bar button.leave-btn {
  background-color: #f44336;
}

.controls-bar button.active {
  background-color: #f44336; /* 음소거, 카메라 끄기 등 활성 상태 */
}
.controls-bar button.active:not(.leave-btn) { /* 화면공유 버튼 등 다른 active 상태 */
    background-color: #3f8efc;
}
.controls-bar button.notes-toggle-btn.active {
    background-color: #3f8efc;
}
.controls-bar button.participants-toggle-btn.active {
    background-color: #3f8efc;
}

/* Resizer 스타일 */
.resizer {
  width: 5px;
  cursor: col-resize;
  background-color: #444;
  flex-shrink: 0;
  height: 100%;
  z-index: 10;
}

/* 회의록 섹션 스타일 */
.notes-section {
  background-color: #fff;
  color: #000;
  display: flex;
  flex-direction: column;
  height: 100%;
  /* transition: width 0.3s ease; */ /* 드래그 시 부자연스러워 보일 수 있어 제거 */
  overflow: hidden;
  flex-shrink: 0; /* 너비가 0이 되어도 사라지지 않도록 함 */
}

:deep(.v-md-editor) {
  box-shadow: none !important;
}

.minutes-header {
  padding: 16px;
  border-bottom: 1px solid #e0e0e0;
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 16px;
  height: 66px; /* 헤더 높이 고정 */
}

.minutes-header h3 {
  margin: 0;
  font-size: 18px;
  flex-shrink: 0;
}

.header-controls {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-left: auto;
}

.ai-summary-btn {
  background: none;
  border: 1px solid #ccc;
  border-radius: 6px;
  padding: 4px 8px;
  cursor: pointer;
  display: flex;
  align-items: center;
  color: #555;
}
.ai-summary-btn:hover {
  background-color: #f0f0f0;
}

.save-status {
  font-size: 14px;
  color: #888;
}

.save-status.saved {
  color: #28a745;
}

.category-select {
  padding: 4px 8px;
  border-radius: 6px;
  border: 1px solid #ccc;
  font-size: 13px;
}
</style>