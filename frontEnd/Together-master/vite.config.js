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
      '/auth':        { target: API_BASE_URL, changeOrigin: true, secure: false },
      '/projects':    { target: API_BASE_URL, changeOrigin: true, secure: false },
      '/planning':    { target: API_BASE_URL, changeOrigin: true, secure: false },
      '/notifications': { target: API_BASE_URL, changeOrigin: true, secure: false },
      '/notices':     { target: API_BASE_URL, changeOrigin: true, secure: false },
      '/design':      { target: API_BASE_URL, changeOrigin: true, secure: false },
      '/develop':     { target: API_BASE_URL, changeOrigin: true, secure: false },
      '/dev-order-items': { target: API_BASE_URL, changeOrigin: true, secure: false },
      '/work-tasks':  { target: API_BASE_URL, changeOrigin: true, secure: false },
      '/votes':       { target: API_BASE_URL, changeOrigin: true, secure: false },
      '/feedbacks':   { target: API_BASE_URL, changeOrigin: true, secure: false },
      '/notes':       { target: API_BASE_URL, changeOrigin: true, secure: false },
      '/memos':       { target: API_BASE_URL, changeOrigin: true, secure: false },
      '/reports':       { target: API_BASE_URL, changeOrigin: true, secure: false },
      '/export':      { target: API_BASE_URL, changeOrigin: true, secure: false },
      '/new-export':  { target: API_BASE_URL, changeOrigin: true, secure: false },
      '/users':       { target: API_BASE_URL, changeOrigin: true, secure: false },
      '/api':         { target: API_BASE_URL, changeOrigin: true, secure: false }
      '/compare':         { target: API_BASE_URL, changeOrigin: true, secure: false },
      '/token':         { target: API_BASE_URL, changeOrigin: true, secure: false },

    }
  }
})
