<template>
  <div class="floating-help" @click="$emit('open-help')">
    ❔
  </div>
</template>

<style scoped>
.floating-help {
  position: fixed;
  bottom: 30px;
  right: 30px;
  background-color: #007bff;
  color: white;
  font-size: 20px;
  border-radius: 50%;
  width: 50px;
  height: 50px;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.3);
  z-index: 1000;

  /* 애니메이션 */
  animation: pulse 3s ease-in-out infinite;
  transition: all 0.3s ease;
}

/* 호버 시 살짝 확대 + 그림자 강조 */
.floating-help:hover {
  transform: scale(1.1);
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.4);
}

/* 주기적으로 살짝 커졌다 작아지는 애니메이션 */
@keyframes pulse {
  0% {
    transform: scale(1);
  }
  50% {
    transform: scale(1.08);
  }
  100% {
    transform: scale(1);
  }
}
</style>
