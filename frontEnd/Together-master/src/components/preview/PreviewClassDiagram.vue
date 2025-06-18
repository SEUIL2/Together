<template>
  <div class="preview-container">
    <iframe
      v-if="projectId"
      :src="`/class-diagram?projectId=${props.projectId}&preview=true`"
      class="diagram-preview"
    />
    <div v-else class="no-id">
      ⚠️ 프로젝트 ID가 없습니다.
    </div>
  </div>
</template>

<script setup>
import { useRoute } from 'vue-router'

const route = useRoute()
const projectId = route.params.projectId || route.query.projectId
</script>

<style scoped>
.preview-container {
  width: 100%;
  height: 100%;
}
.diagram-preview {
  width: 100%;
  height: 600px;     /* 필요에 따라 조정 */
  border: 1px solid #ddd;
}
.no-id {
  padding: 1rem;
  color: #c00;
}
</style>
