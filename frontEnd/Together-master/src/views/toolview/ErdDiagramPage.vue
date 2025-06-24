<template>
  <div class="erd-layout" @click=closeAllMenus @wheel="onWheel">
    <div v-if="saveStatus === 'saving'" class="save-toast saving">저장 중...</div>
<div v-else-if="saveStatus === 'saved'" class="save-toast saved">💾 저장 완료</div>
<div v-else-if="saveStatus === 'error'" class="save-toast error">저장 실패!</div>

    <ToolBox />
    <div class="diagram" ref="diagramRef" @dragover.prevent @drop="handleDrop">
      <ErdRelationContextMenu
  v-if="menuVisible"
  :x="menuX"
  :y="menuY"
:initFromOuter="selectedRel.fromOuter"
  :initFromInner="selectedRel.fromInner"
  :initToInner="selectedRel.toInner"
  :initToOuter="selectedRel.toOuter"
  @update-symbol="onUpdateRelSymbol"
  @delete="deleteRelationship"
  @close="menuVisible = false"
/>
      <v-stage 
      ref="stageRef"
  :config="{ width: 2000, height: 2000, scale: { x: scale, y: scale }, x: stageX, y: stageY }"
        >
        <v-layer>
          <ErdEntityBox
            v-for="box in boxes"
            :key="box.id"
            :config="box"
            :isEditing="editing.visible"
            @start-edit="startEditing"
            @update-position="updatePosition"
            @anchor-click="handleAnchorClick"
            @add-field="onAddField"
            :midDragging="midDragging"
  @mid-drag-start="midDragging = true"
  @mid-drag-end="midDragging = false"
    :from="from"
  :to="to"
  :midPoints="midPoints"
  :fromSymbol="'bar'"
  :toSymbol="'crowsfoot'"
  @open-context="onOpenBoxContextMenu"
          />

<ErdRelationshipBent
  v-for="rel in relationships"
  :key="rel.id"
  
  :from="findAnchor(rel.from.boxId, rel.from.direction)"
  :to="findAnchor(rel.to.boxId, rel.to.direction)"
  :midPoints="rel.midPoints"
  :rel="rel"
  @open-context="onOpenContextMenu"
  @add-mid-point="handleAddMidPoint"
  @update-mid-point="handleUpdateMidPoint"
  @delete-mid-point="handleDeleteMidPoint"
  @mid-drag-start="midDragging = true"
  @mid-drag-end="midDragging = false"
    :fromOuter="rel.fromOuter"
  :fromInner="rel.fromInner"
  :toInner="rel.toInner"
  :toOuter="rel.toOuter"
/>
        </v-layer>
      </v-stage>

      <!-- 테이블명 편집 오버레이 -->
      <div
        v-if="editing.visible && editing.type === 'table'"
        class="overlay-editor"
        :style="{ top: (editing.y-70) + 'px', left: (editing.x-170) + 'px' }"
      >
        <div class="edit-row">
          <input
            v-model="editing.value"
            placeholder="테이블명"
            @keydown.enter="applyEdit"
            class="edit-input"
          />
          <button class="edit-btn" @click="applyEdit">✔</button>
        </div>
      </div>
      <!-- 관계선 context 메뉴 -->
<!-- 박스(테이블) context 메뉴 -->
<div
  v-if="boxMenuVisible"
  class="context-menu"
  :style="{ top: (boxMenuY - 40) + 'px', left: (boxMenuX - 180) + 'px' }"
  @click.stop
>
  <button @click="deleteBox(selectedBox.id)">🗑️ 테이블 삭제</button>
</div>


      <!-- 필드 편집 오버레이 -->
      <div
        v-if="editing.visible && editing.type === 'field'"
        class="overlay-editor"
        :style="{ top: (editing.y-60) + 'px', left: (editing.x-160) + 'px' }"
      >
        <div class="edit-row">
          <select v-model="editing.fieldType" class="edit-select">
            <option value="">일반</option>
            <option value="PK">PK</option>
            <option value="FK">FK</option>
          </select>
          <input
            v-model="editing.value"
            placeholder="필드명"
            @keydown.enter="applyEdit"
            class="edit-input"
          />
          <button class="edit-btn" @click="applyEdit">✔</button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import axios from 'axios'
import { ref, watch, onMounted } from 'vue'
import ErdEntityBox from '@/components/konva/ErdEntityBox.vue'
import ErdRelationshipBent from '@/components/konva/ErdRelationshipBent.vue'
import ToolBox from '@/components/ToolBox.vue'
import ErdRelationContextMenu from '@/components/konva/ErdRelationContextMenu.vue'
import { debounce } from 'lodash'

const scale = ref(1)
const stageX = ref(0)
const stageY = ref(0)
const stageRef = ref(null)

const boxes = ref([])
const relationships = ref([])
const tempAnchor = ref(null)
let relationshipId = 1
const midDragging = ref(false)

const diagramRef = ref(null)

const from = ref({ x: 0, y: 0 }) // 또는 적절한 값으로!
const to = ref({ x: 0, y: 0 })
const midPoints = ref([])

const onAddField = (id) => {
  
  const box = boxes.value.find(b => b.id === id)
  if (!box) return
  if (!box.fields) box.fields = []
  box.fields.push({ name: 'new_field', type: '' })
   updateBoxHeight(box)
   updateBoxHeight(box)
}

const handleDrop = (e) => {
  const diagramRect = diagramRef.value.getBoundingClientRect()
  const dropX = e.clientX - diagramRect.left
  const dropY = e.clientY - diagramRect.top
boxes.value.push({
  id: Date.now(),
  name: 'NewTable',
  x: dropX,
  y: dropY,
  width: 180,   // 반드시 실제 디자인에 맞게!
  height: 86,   // 필드 개수별로 동적으로 늘릴 수도 있음
  fields: [{ name: 'id', type: 'PK' }]
})

}
const FIELD_HEIGHT = 26;
function updateBoxHeight(box) {
  box.height = 62 + FIELD_HEIGHT * box.fields.length // 32는 헤더 높이
}
// 관계선 생성
const handleAnchorClick = ({ boxId, direction }) => {
  if (!tempAnchor.value) {
    tempAnchor.value = { boxId, direction }
    return
  }
  const fromBox = boxes.value.find(b => b.id === tempAnchor.value.boxId)
  const toBox = boxes.value.find(b => b.id === boxId)
  if (!fromBox || !toBox) return
  relationships.value.push({
    id: relationshipId++,
    from: { boxId: fromBox.id, direction: tempAnchor.value.direction },
    to: { boxId: toBox.id, direction },
    midPoints: [],
    lineStyle: 'solid'
  })
  tempAnchor.value = null
}

function getAnchorPos(box, anchorDirection) {
  const width = box.width;
  const height = box.height;
  switch (anchorDirection) {
    case 'top':    return { x: box.x + width / 2, y: box.y }
    case 'bottom': return { x: box.x + width / 2, y: box.y + height }
    case 'left':   return { x: box.x, y: box.y + height / 2 }
    case 'right':  return { x: box.x + width, y: box.y + height / 2 }
    default:       return { x: box.x, y: box.y }
  }
}




// 관계선이 항상 박스 따라가게 from/to 좌표 계산
function findAnchor(boxId, direction) {
 const box = boxes.value.find(b => b.id === boxId);

  if (!box) return { x: 0, y: 0 }
  return getAnchorPos(box, direction)
}

// context menu 관리
const contextMenuX = ref(0)
const contextMenuY = ref(0)
const contextMenuVisible = ref(false)
const selectedRel = ref(null)

const boxMenuVisible = ref(false)
const boxMenuX = ref(0)
const boxMenuY = ref(0)
const selectedBox = ref(null)

// 중간점 추가는 context menu에서만!
// function addMidPointAtContextMenu() {
//   const rel = selectedRel.value
//   if (!rel) return
//   const from = findAnchor(rel.from.boxId, rel.from.direction)
//   const to = findAnchor(rel.to.boxId, rel.to.direction)
//   const mid = {
//     x: (from.x + to.x) / 2 + 40,
//     y: (from.y + to.y) / 2 + 30
//   }
//   rel.midPoints = rel.midPoints || []
//   rel.midPoints.push(mid)
//   contextMenuVisible.value = false
// }

function handleAddMidPoint({ rel, x, y }) {
  rel.midPoints = rel.midPoints || []
  rel.midPoints.push({ x, y })
}
function handleUpdateMidPoint({ rel, idx, x, y }) {
  console.log('before:', JSON.stringify(rel.midPoints))
  rel.midPoints.splice(idx, 1, { x, y })
  console.log('after:', JSON.stringify(rel.midPoints))
}


function handleDeleteMidPoint({ rel, idx }) {
  if (rel.midPoints) rel.midPoints.splice(idx, 1)
}


// 관계선 삭제
function deleteRelationship() {
  relationships.value = relationships.value.filter(r => r !== selectedRel.value)
  contextMenuVisible.value = false
}

// 박스 이동 시 from/to 자동 최신화 → 이미 findAnchor에서 동작

// 편집 관련(기존과 동일)
const editing = ref({
  visible: false,
  boxId: null,
  type: '',
  index: null,
  value: '',
  fieldType: '',
  x: 0,
  y: 0
})

const startEditing = (payload) => {
  editing.value = {
    visible: true,
    type: payload.type,
    index: payload.index,
    boxId: payload.boxId,
    value: payload.value,
    fieldType: payload.fieldType || '',
    x: payload.x,
    y: payload.y
  }
}
function updatePosition({ id, x, y }) {
  const box = boxes.value.find(b => b.id === id)
  if (box) {
    box.x = x
    box.y = y
  }
}

const applyEdit = () => {
  const box = boxes.value.find(b => b.id === editing.value.boxId)
  if (!box) return
  if (editing.value.type === 'table') {
    box.name = editing.value.value
  } else if (editing.value.type === 'field') {
    box.fields[editing.value.index].name = editing.value.value
    box.fields[editing.value.index].type = editing.value.fieldType
  }
  editing.value.visible = false
}
// 상위 setup 예시
const menuVisible = ref(false)
const menuX = ref(0)
const menuY = ref(0)


function onUpdateRelSymbol({ which, value }) {
  // relationships 배열에서 해당 관계선 객체를 찾아서 값을 갱신해야 함!
  // selectedRel.value가 진짜 relationships.value[idx]와 "같은 객체"여야 함
  if (selectedRel.value) {
    selectedRel.value[which] = value
  }
  // 만약 이 방식으로 즉시 반영 안 되면
  // (Vue3 reactive 배열이 아니면) 아래처럼 강제 트리거
  const idx = relationships.value.findIndex(r => r.id === selectedRel.value.id)
  if (idx !== -1) {
    // (배열 새로 할당해서 강제 트리거, 또는 reactive 함수 사용)
    relationships.value = [
      ...relationships.value.slice(0, idx),
      { ...relationships.value[idx], [which]: value },
      ...relationships.value.slice(idx + 1)
    ]
  }
}

function onOpenBoxContextMenu({ box, event }) {
  boxMenuVisible.value = true
  boxMenuX.value = event?.clientX ?? 100
  boxMenuY.value = event?.clientY ?? 100
  selectedBox.value = box
  // 선 메뉴 닫기!
  contextMenuVisible.value = false
}

// ⭐ 반드시 이 함수 선언!
function onOpenContextMenu({ rel, event }) {
  menuVisible.value = true
  menuX.value = event?.clientX ?? 100
  menuY.value = event?.clientY ?? 100
  selectedRel.value = rel
}

function closeAllMenus() {
  boxMenuVisible.value = false
  contextMenuVisible.value = false
  menuVisible.value = false  // 만약 관계선 메뉴까지 다 쓴다면!
}

function deleteBox(id) {
  boxes.value = boxes.value.filter(b => b.id !== id)
  boxMenuVisible.value = false
}
function onWheel(e) {
  // Ctrl 키 안 누르면 확대/축소 안함 (브라우저 스크롤 방지)
  if (!e.ctrlKey) return

  e.preventDefault?.() // 브라우저 기본 확대/축소 막기

  const oldScale = scale.value
  const pointer = { x: e.clientX, y: e.clientY }

  // scale 계산 (휠 방향에 따라)
  const direction = e.deltaY > 0 ? -1 : 1
  let newScale = oldScale * (direction > 0 ? 1.1 : 0.9)

  // scale 제한 (최소/최대)
  newScale = Math.max(0.3, Math.min(2.5, newScale))

  // 마우스 기준으로 확대/축소 위치 맞추기
  if (stageRef.value) {
    const stage = stageRef.value.getStage ? stageRef.value.getStage() : stageRef.value
    const mousePointTo = {
      x: (pointer.x - stage.x()) / oldScale,
      y: (pointer.y - stage.y()) / oldScale
    }
    stage.scale({ x: newScale, y: newScale })
    scale.value = newScale
    // 스테이지 위치 보정
    const newPos = {
      x: pointer.x - mousePointTo.x * newScale,
      y: pointer.y - mousePointTo.y * newScale
    }
    stage.position(newPos)
    stage.batchDraw()
  } else {
    // stageRef 아직 연결 안됐을 경우 단순 scale만
    scale.value = newScale
  }
}
const props = defineProps({
  projectId: Number,
  readonly: Boolean,
  projectTitle: String
})
const saveStatus = ref('idle')
const saveErd = debounce(async () => {
  saveStatus.value = 'saving' // 저장 중 표시
  const erdData = { boxes: boxes.value, relationships: relationships.value }
  const formData = new FormData()
  formData.append('type', 'erd')
  formData.append('json', JSON.stringify(erdData))

  const token = localStorage.getItem('authHeader')
  const config = token ? { headers: { Authorization: token } } : {}

  try {
    await axios.post('/design/upload', formData, config)
    saveStatus.value = 'saved' // 저장됨 표시
    setTimeout(() => saveStatus.value = 'idle', 1200) // 1.2초 뒤 숨김
  } catch (e) {
    saveStatus.value = 'error' // 실패 표시
    setTimeout(() => saveStatus.value = 'idle', 3000) // 3초 뒤 숨김
  }
}, 1000) // 1초 딜레이


// boxes 또는 relationships가 바뀔 때마다 자동 저장
watch([boxes, relationships], saveErd, { deep: true })



onMounted(async () => {
  try {
    const res = await axios.get('/design/all', {
      params: { projectId: props.projectId },
      headers: { Authorization: localStorage.getItem('authHeader') },
      withCredentials: true
    })

    const { erd } = res.data
    if (erd?.json) {
      const parsed = JSON.parse(erd.json)
      boxes.value = parsed.boxes || []
      relationships.value = parsed.relationships || []
      console.log('✅ ERD 다이어그램 불러오기 성공:', parsed)
    } else {
      console.warn('⚠️ 저장된 ERD 다이어그램이 없습니다.')
      boxes.value = []
      relationships.value = []
    }
  } catch (err) {
    console.error('❌ ERD 초기 데이터 로드 실패:', err)
  }
})

</script>



<style scoped>
.erd-layout {
  display: flex;
  height: 100vh;
}
/* .toolbox {
  width: 200px;
  background: #f4f4f4;
  padding: 12px;
  border-right: 1px solid #ccc;
} */
.diagram {
  flex: 1;
  overflow: hidden;
  position: relative;
}
.context-menu {
  position: absolute;
  background: white;
  border: 1px solid #888;
  padding: 8px;
  z-index: 10;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}
.overlay-editor {
  position: absolute;
  background: white;
  border: 1px solid #ccc;
  padding: 8px 10px;
  border-radius: 8px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
  z-index: 9999;
  display: flex;
  flex-direction: column;
}
.edit-row {
  display: flex;
  align-items: center;
  gap: 6px;
}

.edit-input {
  padding: 6px 8px;
  font-size: 14px;
  width: 140px;
  border: 1px solid #ccc;
  border-radius: 4px;
}

.edit-select {
  padding: 6px 8px;
  font-size: 14px;
  border: 1px solid #ccc;
  border-radius: 4px;
}

.edit-btn {
  padding: 6px 10px;
  font-size: 14px;
  background-color: #1976d2;
  color: white;
  border: none;
  border-radius: 4px;
  cursor: pointer;
}
.save-toast {
  position: fixed;
  top: 24px;
  right: 36px;
  z-index: 9000;
  background: #4b80ff;
  color: #fff;
  border-radius: 6px;
  font-size: 15px;
  padding: 10px 26px;
  box-shadow: 0 4px 16px #0022;
  transition: opacity .3s;
  pointer-events: none;
}
.save-toast.saving { background: #000000; }
.save-toast.saved { background: #000000; }
.save-toast.error { background: #ff5555; }

</style>
