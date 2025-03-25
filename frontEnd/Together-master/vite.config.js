import { fileURLToPath, URL } from 'node:url'
import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'

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
        target: 'http://localhost:8081', // ✅ 백엔드 주소
        changeOrigin: true,
        secure: false
      }
    }
  }
})
