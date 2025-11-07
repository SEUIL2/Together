<template>
  <div class="floating-help" @click="$emit('open-help')">
    <svg xmlns="http://www.w3.org/2000/svg" width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5" stroke-linecap="round" stroke-linejoin="round">
      <circle cx="12" cy="12" r="10"></circle>
      <path d="M9.09 9a3 3 0 0 1 5.83 1c0 2-3 3-3 3"></path>
      <line x1="12" y1="17" x2="12.01" y2="17"></line>
    </svg>
  </div>
</template>

<style scoped>
.floating-help {
  position: fixed;
  bottom: 32px;
  right: 32px;
  background: linear-gradient(135deg, #4e89f8, #3b76e1);
  color: white;
  border-radius: 50%;
  width: 56px;
  height: 56px;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  box-shadow: 0 4px 16px rgba(0, 123, 255, 0.3);
  z-index: 1000;
  transition: all 0.3s ease;
  animation: pulse 2.5s infinite cubic-bezier(0.4, 0, 0.6, 1);
}

/* 호버 시 살짝 확대 + 그림자 강조 */
.floating-help:hover {
  transform: translateY(-4px) scale(1.05);
  box-shadow: 0 8px 24px rgba(0, 123, 255, 0.4);
  animation-play-state: paused; /* 호버 시에는 애니메이션 일시 정지 */
}

/* 클릭 시 작아지는 효과 추가 */
.floating-help:active {
  transform: scale(0.85);
}

@keyframes pulse {
  0%, 100% {
    transform: scale(1);
    box-shadow: 0 4px 16px rgba(0, 123, 255, 0.3);
  }
  50% {
    transform: scale(1.05);
    box-shadow: 0 8px 24px rgba(0, 123, 255, 0.4);
  }
}
</style>
