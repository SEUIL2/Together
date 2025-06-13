import { fileURLToPath, URL } from 'node:url'
import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'

const BASE_URL = 'http://localhost:8081'  // 여기만 바꾸면 끝!
export default defineConfig({
  plugins: [vue()],
  resolve: {
    alias: {
      '@': fileURLToPath(new URL('./src', import.meta.url))
    }
  },
  server: {
    proxy: {
      '/auth': {
        target: BASE_URL,
        changeOrigin: true,
        secure: false
      },
      '/projects': {
        target: BASE_URL,
        changeOrigin: true,
        secure: false
      },
      '/planning': {
        target: BASE_URL,
        changeOrigin: true,
        secure: false
      },
      '/notifications': {
        target: BASE_URL,
        changeOrigin: true,
        secure: false
      },
      '/notices': {
        target: BASE_URL,
        changeOrigin: true,
        secure: false
      },
      '/design': {
        target: BASE_URL,
        changeOrigin: true,
        secure: false
      },
      '/develop': {
        target: BASE_URL,
        changeOrigin: true,
        secure: false
      },
      '/work-tasks': {
        target: BASE_URL,
        changeOrigin: true,
        secure: false
      },
      '/votes': {
        target: BASE_URL,
        changeOrigin: true,
        secure: false
      },
      '/feedbacks': {
        target: BASE_URL,
        changeOrigin: true,
        secure: false
      },
      '/notes': {
        target: BASE_URL,
        changeOrigin: true,
        secure: false },
      '/memos': {
        target: BASE_URL,
        changeOrigin: true,
        secure: false }
    }
  }
})
