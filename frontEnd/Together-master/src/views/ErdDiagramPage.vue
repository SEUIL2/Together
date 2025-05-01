<template>
  <div class="erd-page">
    <ErdToolbar
      @add-entity="addEntity"
      @export-json="exportJson"
      @import-json="importJson"
      @clear-all="clearAll"
    />

    <ErdCanvas
      :entities="entities"
      :relationships="relationships"
      @update:entities="onEntitiesUpdate"
      @update:relationships="onRelationshipsUpdate"
    />
  </div>
</template>

<script setup>
import { ref } from 'vue'
import ErdCanvas from '@/components/erd/ErdCanvas.vue'
import ErdToolbar from '@/components/erd/ErdToolbar.vue'
import { exportToSQL } from '@/utils/erdExportUtils.js'

// 반응형 상태 선언
const entities      = ref([])
const relationships = ref([])

// ref.value 로 반드시 갱신
function onEntitiesUpdate(val) {
  entities.value = val
}
function onRelationshipsUpdate(val) {
  relationships.value = val
}

// 테이블(엔티티) 추가
function addEntity() {
  const newEntity = {
    id: crypto.randomUUID(),
    name: 'NewTable',
    x: 200 + Math.random() * 200,
    y: 150 + Math.random() * 200,
    fields: [
      { id: crypto.randomUUID(), name: 'id', type: 'int', isPK: true, isFK: false }
    ]
  }
  entities.value.push(newEntity)
}

// 전체 삭제
function clearAll() {
  entities.value = []
  relationships.value = []
}

// JSON 저장 (파일 다운로드)
function exportJson() {
  const data = {
    entities: entities.value,
    relationships: relationships.value,
    sql: exportToSQL(entities.value, relationships.value)
  }
  const blob = new Blob([JSON.stringify(data, null, 2)], { type: 'application/json' })
  const link = document.createElement('a')
  link.href = URL.createObjectURL(blob)
  link.download = 'erd-diagram.json'
  link.click()
}

// JSON 불러오기 (파일 업로드)
function importJson() {
  const input = document.createElement('input')
  input.type = 'file'
  input.accept = '.json'
  input.onchange = e => {
    const file = e.target.files[0]
    if (!file) return
    const reader = new FileReader()
    reader.onload = ev => {
      const json = JSON.parse(ev.target.result)
      entities.value      = json.entities      || []
      relationships.value = json.relationships || []
    }
    reader.readAsText(file)
  }
  input.click()
}
</script>

<style scoped>
.erd-page {
  width: 100%;
  height: 100vh;
  overflow: hidden;
  display: flex;
  flex-direction: column;
}
</style>
