import { ref } from 'vue';

// 전역 토스트 상태
const isVisible = ref(false);
const message = ref('');

let timeoutId = null;

export function useGlobalToast() {
  const showToast = (msg, duration = 5000) => {
    message.value = msg;
    isVisible.value = true;
    clearTimeout(timeoutId);
    timeoutId = setTimeout(() => (isVisible.value = false), duration);
  };

  return { isVisible, message, showToast };
}