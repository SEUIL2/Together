<template>
  <div class="video-chat-page">
    <h2>화상 회의</h2>
    <div class="video-container">
      <div id="local-player" class="video-player"></div>
      <div v-for="user in remoteUsers" :key="user.uid" :id="`remote-player-${user.uid}`" class="video-player"></div>
    </div>
    <div class="controls">
      <button @click="leaveChannel">회의 종료</button>
    </div>
  </div>
</template>

<script setup>
import { onMounted, onUnmounted, ref } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import AgoraRTC from 'agora-rtc-sdk-ng';
import api from '@/api';

const route = useRoute();
const router = useRouter();

const appId = 'YOUR_AGORA_APP_ID'; // 본인의 Agora App ID를 입력하세요.
const channel = ref(route.query.channel);
const token = ref(route.query.token);
const client = ref(null);
const localAudioTrack = ref(null);
const localVideoTrack = ref(null);
const remoteUsers = ref([]);

onMounted(async () => {
  if (!token.value) {
    try {
      const response = await api.get('/agora/token');
      token.value = response.data.token;
    } catch (error) {
      console.error('Failed to fetch agora token', error);
      if(error.response.status === 403) {
        alert('프로젝트에 참여한 사용자만 화상회의를 시작할 수 있습니다.');
        router.push('/MeetingPage');
      }
      return;
    }
  }

  client.value = AgoraRTC.createClient({ mode: 'rtc', codec: 'vp8' });

  client.value.on('user-published', async (user, mediaType) => {
    await client.value.subscribe(user, mediaType);
    if (mediaType === 'video') {
      const remoteVideoTrack = user.videoTrack;
      const playerContainer = document.createElement('div');
      playerContainer.id = `remote-player-${user.uid}`;
      playerContainer.className = 'video-player';
      document.querySelector('.video-container').append(playerContainer);
      remoteVideoTrack.play(playerContainer);
    }
    if (mediaType === 'audio') {
      user.audioTrack.play();
    }
    remoteUsers.value.push(user);
  });

  client.value.on('user-unpublished', user => {
    const playerContainer = document.getElementById(`remote-player-${user.uid}`);
    if (playerContainer) {
      playerContainer.remove();
    }
    remoteUsers.value = remoteUsers.value.filter(u => u.uid !== user.uid);
  });

  try {
    const uid = await client.value.join(appId, channel.value, token.value, null);
    localAudioTrack.value = await AgoraRTC.createMicrophoneAudioTrack();
    localVideoTrack.value = await AgoraRTC.createCameraVideoTrack();

    document.getElementById('local-player').innerHTML = '';
    localVideoTrack.value.play('local-player');

    await client.value.publish([localAudioTrack.value, localVideoTrack.value]);
  } catch (error) {
    console.error('Failed to join channel', error);
  }
});

const leaveChannel = async () => {
  localAudioTrack.value?.close();
  localVideoTrack.value?.close();
  await client.value?.leave();
  router.push('/MeetingPage');
};

onUnmounted(() => {
  leaveChannel();
});
</script>

<style scoped>
.video-chat-page {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 20px;
}
.video-container {
  display: flex;
  flex-wrap: wrap;
  gap: 20px;
  margin-bottom: 20px;
}
.video-player {
  width: 400px;
  height: 300px;
  border: 1px solid #ccc;
}
.controls button {
  padding: 10px 20px;
  background-color: #f44336;
  color: white;
  border: none;
  border-radius: 5px;
  cursor: pointer;
}
</style>