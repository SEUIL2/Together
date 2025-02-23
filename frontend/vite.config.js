// vite.config.js
import { fileURLToPath, URL } from 'node:url'
import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'
// import vueDevTools from 'vite-plugin-vue-devtools' // ← 사용 안 할 경우 주석 처리 또는 삭제

export default defineConfig({
  plugins: [
    vue(),
    // vueDevTools() // ← 사용 안 할 경우 주석 처리 또는 삭제
  ],
  resolve: {
    alias: {
      '@': fileURLToPath(new URL('./src', import.meta.url)),
    },
  },
})
