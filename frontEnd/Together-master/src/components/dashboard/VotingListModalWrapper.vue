<!-- VotingListModalWrapper.vue -->
<template>
  <div class="modal-overlay" @click.self="$emit('close')">
    <div class="modal-box">
      <VotingList
  :projectId="projectId"
  :show-header="true"
  user-type="professor"
  @close="$emit('close')"
/>

    </div>
    
  </div>
  
</template>

<script setup>
import VotingList from '@/components/dashboard/VotingList.vue'

defineProps({
  projectId: Number,
})

defineEmits(['close'])
</script>

<style scoped>
.modal-overlay {
  position: fixed;
  top: 0;
  left: 0;
  width: 100vw;
  height: 100vh;
  background: rgba(0, 0, 0, 0.5);
  z-index: 999;
  display: flex;
  justify-content: center;
  align-items: center;
  
}

.modal-box {
  background: white;
  border-radius: 8px;
  padding: 20px;
  width: 600px;
  max-height: 80vh;
   overflow-y: auto;
     /* ✨ 스크롤바 숨김 */
  scrollbar-width: none; /* Firefox */
  -ms-overflow-style: none; /* IE 10+ */
}
.notice-list::-webkit-scrollbar {
  display: none; /* Chrome, Safari, Opera */

}
</style>
