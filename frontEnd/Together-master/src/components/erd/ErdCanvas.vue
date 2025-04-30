<template>
  <div class="erd-canvas" ref="canvasContainer" style="position: relative;">
    <v-stage :config="stageConfig" @click="clearSelections">
      <v-layer>
        <!-- 엔티티 박스 -->
        <ErdEntityBox
          v-for="entity in entities"
          :key="entity.id"
          :entity="entity"
          :selected="selectedEntity?.id === entity.id"
          @update="onEntityUpdate"
          @select="onSelectEntity"
          @anchor-click="onAnchorClick"
          @context="onEntityContext"
          @edit="startEdit"
        />

        <!-- 관계선 -->
        <ErdRelationshipLine
          v-for="rel in relationships"
          :key="rel.id"
          :rel="rel"
          :entities="entities"
          @update="onUpdateRelationship"
          @delete="onDeleteRelationship"
        />
      </v-layer>
    </v-stage>

    <!-- 인라인 편집 input -->
    <input
      v-if="editing"
      ref="inputRef"
      v-model="editing.value"
      :style="{
        position: 'absolute',
        left: editing.x + 'px',
        top: editing.y + 'px',
        padding: '4px',
        fontSize: '14px',
        width: editing.type === 'name' ? '150px' : '200px'
      }"
      @blur="finishEdit"
      @keydown.enter="finishEdit"
    />
  </div>
</template>

<script setup>
import { ref, computed, nextTick } from 'vue'
import ErdEntityBox       from './ErdEntityBox.vue'
import ErdRelationshipLine from './ErdRelationshipLine.vue'

const props = defineProps({
  entities:      { type: Array, required: true },
  relationships: { type: Array, required: true }
})
const emit = defineEmits(['update:entities','update:relationships'])

// 상태
const selectedEntity = ref(null)
const anchorStart    = ref(null)
const editing        = ref(null)
const inputRef       = ref(null)
const canvasContainer= ref(null)

// 캔버스 크기
const stageConfig = computed(() => ({
  width:  window.innerWidth,
  height: window.innerHeight
}))

// 엔티티 업데이트
function onEntityUpdate(updated) {
  emit('update:entities',
    props.entities.map(e => e.id === updated.id ? updated : e)
  )
}

// 관계선 업데이트 / 삭제
function onUpdateRelationship(updated) {
  emit('update:relationships',
    props.relationships.map(r => r.id === updated.id ? updated : r)
  )
}
function onDeleteRelationship(id) {
  emit('update:relationships',
    props.relationships.filter(r => r.id !== id)
  )
}

// 선택 처리
function onSelectEntity(entity) {
  selectedEntity.value = entity
}
function clearSelections() {
  selectedEntity.value = null
  anchorStart.value    = null
  editing.value        = null
}

// 앵커 클릭 시 관계 추가
function onAnchorClick({ id, direction }) {
  if (!anchorStart.value) {
    anchorStart.value = { id, direction }
  } else {
    const newRel = {
      id:                crypto.randomUUID(),
      fromEntityId:      anchorStart.value.id,
      fromDirection:     anchorStart.value.direction,
      toEntityId:        id,
      toDirection:       direction,
      fromCardinality:   '1',
      toCardinality:     '0..*'
    }
    emit('update:relationships', [...props.relationships, newRel])
    anchorStart.value = null
  }
}

// 컨텍스트 메뉴 핸들러 (필요 시 구현)
function onEntityContext({ entity, event }) {
  // TODO
}

// 인라인 편집
function startEdit({ type, entity, fieldIndex, clientPos }) {
  const rect = canvasContainer.value.getBoundingClientRect()
  const value = type === 'name'
    ? entity.name
    : `${entity.fields[fieldIndex].name}:${entity.fields[fieldIndex].type}`

  editing.value = {
    type, entity, fieldIndex, value,
    x: clientPos.x - rect.left,
    y: clientPos.y - rect.top
  }
  nextTick(() => inputRef.value.focus())
}
function finishEdit() {
  if (!editing.value) return
  const { type, entity, fieldIndex, value } = editing.value

  if (type === 'name') {
    onEntityUpdate({ ...entity, name: value })
  } else {
    const [n,t] = value.split(':')
    const updated = {
      ...entity,
      fields: entity.fields.map((f,i) =>
        i === fieldIndex ? { ...f, name: n, type: t || f.type } : f
      )
    }
    onEntityUpdate(updated)
  }
  editing.value = null
}
</script>

<style scoped>
.erd-canvas {
  width:  100%;
  height: calc(100vh - 50px);
  background: #f3f4f6;
}
</style>
