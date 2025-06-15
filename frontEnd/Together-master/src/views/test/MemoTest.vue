<template>
  <div class="figma-test">
    <h2>🎨 Figma 링크 미리보기 테스트</h2>

    <textarea
      v-model="figmaLink"
      placeholder="https://www.figma.com/file/..."
      class="figma-textarea"
    ></textarea>

    <iframe
      v-if="isValidFigmaLink(figmaLink)"
      :src="`https://www.figma.com/embed?embed_host=share&url=${extractFigmaUrl(figmaLink)}`"
      width="100%"
      height="500"
      allowfullscreen
      style="margin-top: 16px; border: 1px solid #ccc; border-radius: 8px;"
    ></iframe>
  </div>
</template>

<script setup>
import { ref } from 'vue'

const figmaLink = ref('')

// 링크 유효성 검사
function isValidFigmaLink(link) {
  return extractFigmaUrl(link) !== ''
}

// 링크 추출
function extractFigmaUrl(content) {
  if (!content || typeof content !== 'string') return ''
  const match = content.match(/https:\/\/www\.figma\.com\/file\/[^\s<"]+/)
  return match ? match[0] : ''
}
</script>

<style scoped>
.figma-test {
  max-width: 800px;
  margin: 40px auto;
  padding: 20px;
  font-family: sans-serif;
}

.figma-textarea {
  width: 100%;
  height: 120px;
  padding: 12px;
  font-size: 14px;
  border: 1px solid #aaa;
  border-radius: 8px;
  resize: vertical;
}
</style>
