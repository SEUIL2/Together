<template>
  <div class="video-chat-page">
    <!-- 왼쪽: 비디오 영역 -->
    <div class="video-section" :class="{ 'notes-open': !isNotesCollapsed }">
      <div class="video-grid" :style="gridStyle">
        <!-- Local User -->
        <div class="video-player-wrapper">
          <div id="local-player" class="video-player"></div>
          <div class="user-label">나 ({{ uid }})</div>
          <div v-if="isVideoMuted" class="video-muted-overlay">
            <svg xmlns="http://www.w3.org/2000/svg" width="64" height="64" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M16 16v1a2 2 0 0 1-2 2H3a2 2 0 0 1-2-2V7a2 2 0 0 1 2-2h2m5.66 0H14a2 2 0 0 1 2 2v3.34l1 1L23 7v10"></path><line x1="1" y1="1" x2="23" y2="23"></line></svg>
          </div>
        </div>
        <!-- Remote Users -->
        <div v-for="user in remoteUsers" :key="user.uid" class="video-player-wrapper">
          <div :id="`remote-player-${user.uid}`" class="video-player"></div>
          <div class="user-label">{{ user.uid }}</div>
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
        <button @click="leaveChannel" class="leave-btn">
          <svg xmlns="http://www.w3.org/2000/svg" width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M10.68 13.31a16 16 0 0 0 3.41 2.6l1.27-1.27a2 2 0 0 1 2.11-.45 12.84 12.84 0 0 0 2.81.7 2 2 0 0 1 1.72 2v3a2 2 0 0 1-2.18 2 19.79 19.79 0 0 1-8.63-3.07 19.5 19.5 0 0 1-6-6 19.79 19.79 0 0 1-3.07-8.67A2 2 0 0 1 4.11 2h3a2 2 0 0 1 2 1.72 12.84 12.84 0 0 0 .7 2.81 2 2 0 0 1-.45 2.11L8.09 9.91"></path><path d="M22 2L2 22"></path></svg>
          <span>회의 종료</span>
        </button>
        <button @click="isNotesCollapsed = !isNotesCollapsed" class="notes-toggle-btn">
          <svg xmlns="http://www.w3.org/2000/svg" width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M2 3h6a4 4 0 0 1 4 4v14a3 3 0 0 0-3-3H2z"></path><path d="M22 3h-6a4 4 0 0 0-4 4v14a3 3 0 0 1 3-3h7z"></path></svg>
          <span>회의록</span>
        </button>
      </div>
    </div>
    <!-- 오른쪽: 회의록 작성 영역 -->
    <div class="notes-section" :class="{ collapsed: isNotesCollapsed }">
      <div class="notes-header">
        <h3>실시간 회의록</h3>
        <div class="header-controls">
          <select v-model="meetingCategory" @change="debouncedSaveNotes" class="category-select">
            <option v-for="(label, key) in categories" :key="key" :value="key">{{ label }}</option>
          </select>
          <span v-if="saveStatus === 'saving'" class="save-status">저장 중...</span>
          <span v-else-if="saveStatus === 'saved'" class="save-status saved">✓ 저장됨</span>
        </div>
      </div>
      <v-md-editor
        v-model="meetingNotes"
        height="100%"
        @change="debouncedSaveNotes"
      ></v-md-editor>
    </div>
  </div>
</template>

<script setup>
import { onMounted, onUnmounted, ref, computed, nextTick } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import AgoraRTC from 'agora-rtc-sdk-ng';
import { debounce } from 'lodash-es';
import api from '@/api'; // ✅ 프로젝트 공용 api 인스턴스 사용
import meetingApi from '@/api/meetingApi';

const route = useRoute();
const router = useRouter();

const appId = 'becf4d48a91f41c88b7908fc88d049b2'; // 본인의 Agora App ID를 입력하세요.
const channel = ref(route.query.channel);
const token = ref(null); // URL에서 토큰을 읽지 않고 null로 초기화
const uid = ref(null); // UID를 저장할 ref 추가
const client = ref(null);
const localAudioTrack = ref(null);
const localVideoTrack = ref(null);
const remoteUsers = ref([]);
const isAudioMuted = ref(false);
const isVideoMuted = ref(false);
const meetingId = ref(null);
const meetingTitle = ref(''); // 회의록 제목을 저장할 ref
const meetingCategory = ref(''); // 회의록 카테고리를 저장할 ref
const meetingNotes = ref('');
const saveStatus = ref('idle'); // 'idle', 'saving', 'saved'
const isNotesCollapsed = ref(true); // 회의록 접기/펼치기 상태 (기본값: 접힘)

const categories = {
  PLANNING: '기획',
  DESIGN: '설계',
  DEVELOPMENT: '개발',
  TEST: '테스트'
};

onMounted(async () => {
  // 1. 화상회의 준비 (토큰 발급, 회의록 생성)
  const isReady = await prepareForMeeting();
  if (!isReady) {
    alert('화상회의 입장에 실패했습니다. 잠시 후 다시 시도해주세요.');
    router.back();
    return;
  }

  client.value = AgoraRTC.createClient({ mode: 'rtc', codec: 'vp8' });

  client.value.on('user-published', async (user, mediaType) => {
    // 원격 사용자를 구독합니다.
    await client.value.subscribe(user, mediaType);
    if (mediaType === 'video') {
      const remoteVideoTrack = user.videoTrack;
      // 구독에 성공한 사용자만 목록에 추가합니다.
      if (!remoteUsers.value.find(u => u.uid === user.uid)) {
        remoteUsers.value.push(user);
      }
      // DOM이 업데이트된 후 영상을 재생합니다.
      nextTick(() => {
        remoteVideoTrack.play(`remote-player-${user.uid}`);
      });
    }
    if (mediaType === 'audio') {
      user.audioTrack.play();
    }
  });

  client.value.on('user-unpublished', user => {
    // Vue의 반응성에 따라 remoteUsers 배열에서 사용자를 제거하면 DOM도 자동으로 업데이트됩니다.const playerContainer = document.getElementById(`remote-player-${user.uid}`);
    remoteUsers.value = remoteUsers.value.filter(u => u.uid !== user.uid);
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
    // 토큰 발급과 회의록 생성을 동시에 요청
    const [tokenResponse] = await Promise.all([
      api.get('/agora/token', { params: { projectId: channel.value } }),
      setupMeeting()
    ]);

    console.log('[DEBUG] Agora 토큰 응답:', tokenResponse.data);
    token.value = tokenResponse.data.token;
    uid.value = tokenResponse.data.userId ?? tokenResponse.data.user_id;

    if (!token.value || uid.value === undefined) {
      throw new Error('토큰 또는 UID가 유효하지 않습니다.');
    }

    console.log('✅ Agora 토큰 및 회의록 준비 완료');
    return true;
  } catch (error) {
    console.error('❌ 화상회의 준비 실패:', error.response?.data || error.message);
    return false;
  }
};

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

const totalUsers = computed(() => remoteUsers.value.length + 1);
const gridStyle = computed(() => {
  const columns = Math.ceil(Math.sqrt(totalUsers.value));
  return {
    'grid-template-columns': `repeat(${columns}, 1fr)`,
  };
});

const setupMeeting = async () => {
  try {
    const today = new Date().toLocaleDateString('ko-KR', { year: 'numeric', month: '2-digit', day: '2-digit' }).replace(/\. /g, '-').replace('.', '');
    const meetingDto = {
      title: `화상회의 - ${today}`,
      content: '# 화상회의 실시간 회의록',
      meetingDate: new Date().toISOString(),
      category: 'DEVELOPMENT' // 기본 카테고리 설정
    };
    const response = await meetingApi.post('/create', meetingDto);
    meetingId.value = response.data.meetingId;
    meetingTitle.value = response.data.title; // 생성된 회의록의 제목 저장
    meetingCategory.value = response.data.category; // 생성된 회의록의 카테고리 저장
    meetingNotes.value = response.data.content;
    console.log(`✅ 화상회의를 위한 회의록(ID: ${meetingId.value})이 생성되었습니다.`);
  } catch (err) {
    console.error('❌ 회의록 생성 실패:', err);
    alert('회의록을 생성하는 데 실패했습니다. 회의 내용은 저장되지 않을 수 있습니다.');
  }
};

const saveNotes = async () => {
  if (!meetingId.value || saveStatus.value === 'saving') return;
  saveStatus.value = 'saving';
  try {
    await meetingApi.put(`/update/${meetingId.value}`, { // ✅
      title: meetingTitle.value, // 제목 정보 추가
      content: meetingNotes.value,
      category: meetingCategory.value, // 카테고리 정보 추가
      updatedAt: new Date().toISOString()
    });
    saveStatus.value = 'saved';
    setTimeout(() => (saveStatus.value = 'idle'), 2000);
  } catch (err) {
    console.error('❌ 회의록 저장 실패:', err);
    saveStatus.value = 'idle'; // 저장 실패 시 다시 시도할 수 있도록
  }
};

const debouncedSaveNotes = debounce(saveNotes, 3000);

const leaveChannel = async (redirectOnly = false) => {
  if (!redirectOnly) {
    localAudioTrack.value?.close();
    localVideoTrack.value?.close();
    // client.value가 null이 아닐 때만 leave() 호출
    if (client.value) {
      await client.value.leave();
    }
  }
  // MeetingPage로 돌아갈 때 projectId를 쿼리에 포함
  router.push({ name: 'MeetingPage', query: { projectId: channel.value } });
};

onUnmounted(() => {
  leaveChannel();
});
</script>

<style scoped>
.video-chat-page {
  display: flex;
  height: calc(100vh - 60px); /* 헤더 높이 제외 */
  background-color: #1a1a1a;
  color: white;
  overflow: hidden;
}

.video-section {
  flex-grow: 1;
  display: flex;
  flex-direction: column;
  height: 100%;
  position: relative;
  transition: width 0.4s ease;
  width: 100%;
}

.video-section:not(.notes-open) {
  width: 100%;
}

.notes-section {
  background-color: #fff;
  color: #000;
  display: flex;
  flex-direction: column;
  height: 100%;
  border-left: 1px solid #444;
  transition: width 0.4s ease, padding 0.4s ease;
  width: 80%;
  overflow: hidden;
}
.notes-section.collapsed {
  width: 0;
  padding: 0;
  border-left: none;
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

.notes-header {
  padding: 16px;
  border-bottom: 1px solid #e0e0e0;
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 16px;
}

.notes-header h3 {
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