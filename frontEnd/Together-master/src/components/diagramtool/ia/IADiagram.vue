<template>
  <div style="width:100vw;height:100vh;position:relative;background:#232323;">
    <!-- 툴바: 박스 추가 버튼(임시) → 원하면 삭제 -->
    <button @click="$emit('add-box')" style="position:absolute;top:18px;left:24px;z-index:5;">
      + 박스 추가
    </button>
    <!-- 전체 다이어그램(Stage) -->
    <v-stage
      ref="stageRef"
      :config="{ width: stageWidth, height: stageHeight }"
      @mousedown="clearSelection"
    >
      <v-layer>
        <!-- 박스(노드) 리스트 -->
        <IABlock
          v-for="box in boxes"
          :key="box.id"
          :config="box"
          @updatePosition="onBoxMove"
          @anchorDown="onAnchorDown"
          :isSelected="selectedBoxId === box.id"
        />
        <!-- 관계선 리스트 -->
        <IARelationship
          v-for="rel in relationships"
          :key="rel.id"
          :from="findAnchor(rel.from)"
          :to="findAnchor(rel.to)"
          :midPoints="rel.midPoints"
          :rel="rel"
          @select="onRelSelect"
          @updateMidPoints="onRelUpdate"
          @deleteMidPoint="onRelMidDelete"
        />
      </v-layer>
    </v-stage>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import IABlock from './IABlock.vue'
import IARelationship from './IARelationship.vue'

// props로 박스/관계선 데이터 받아옴!
const props = defineProps({
  boxes: { type: Array, required: true },
  relationships: { type: Array, default: () => [] }
})

// 선택(스타일 강조, 추후 편집용)
const selectedBoxId = ref(null)
function clearSelection() {
  selectedBoxId.value = null
}

// 박스 이동 위치 emit (상위에서 데이터 관리)
const emit = defineEmits(['update:boxes', 'update:relationships', 'add-box', 'selectBox', 'selectRelationship'])

// 박스 이동 반영
function onBoxMove({ id, x, y }) {
  const boxArr = [...props.boxes]
  const idx = boxArr.findIndex(b => b.id === id)
  if (idx !== -1) {
    boxArr[idx] = { ...boxArr[idx], x, y }
    emit('update:boxes', boxArr)
  }
}

// 앵커 클릭 (선 연결 시작 등)
function onAnchorDown({ boxId, sectionId, direction, event }) {
  selectedBoxId.value = boxId
  // 추후 연결 or 상위 emit 필요시 emit('anchorDown', ...)
  emit('selectBox', props.boxes.find(b => b.id === boxId))
}

// 관계선 관련 이벤트(emit 패턴)
function onRelSelect({ rel, event }) {
  emit('selectRelationship', rel)
}
function onRelUpdate({ relId, midPoints }) {
  const relArr = [...props.relationships]
  const idx = relArr.findIndex(r => r.id === relId)
  if (idx !== -1) {
    relArr[idx] = { ...relArr[idx], midPoints }
    emit('update:relationships', relArr)
  }
}
function onRelMidDelete({ relId, idx }) {
  const relArr = [...props.relationships]
  const r = relArr.find(r => r.id === relId)
  if (r) {
    r.midPoints.splice(idx, 1)
    emit('update:relationships', [...relArr])
  }
}

// 앵커의 좌표 계산 함수(박스/섹션 id 기반)
function findAnchor(anchor) {
  // 예시: 반드시 상위에서 함수 내려주거나, utils로 위치계산(박스/섹션 위치 참조)
  // 실제 구현 필요! 아래는 예시
  // { boxId, direction } or { boxId, sectionId }
  // → 해당 box/section의 x, y 좌표 리턴
  return { x: 0, y: 0 }
}
</script>
