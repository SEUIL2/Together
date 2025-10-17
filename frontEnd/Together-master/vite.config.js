import { fileURLToPath, URL } from 'node:url'
import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'
import { API_BASE_URL } from './src/config'

export default defineConfig({
  plugins: [vue()],
  resolve: {
    alias: {
      '@': fileURLToPath(new URL('./src', import.meta.url))
    }
  },
  server: {
    proxy: {
      // '/api'로 시작하는 모든 요청을 백엔드로 프록시합니다.
      '/api': {
        target: 'http://localhost:8081', // API_BASE_URL에서 '/api'를 제외한 주소
        changeOrigin: true,
        secure: false,
      }
    }
  }
})
