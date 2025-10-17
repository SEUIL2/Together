<template>
  <div class="diagram-layout" @click="hideAllMenus">
    <ToolBox />
    <div class="diagram-page" @dragover.prevent @drop="handleDrop">
      <v-stage
        ref="stageRef"
        :config="{
          width: stageWidth,
          height: stageHeight,
          scaleX: scale,
          scaleY: scale,
          x: stageX,
          y: stageY
        }"
        @wheel="handleWheel"
        @mousedown="handleStageMouseDown"
        @mousemove="handleStageMouseMove"
        @mouseup="handleStageMouseUp"
      >
        <v-layer>
<ClassBox
  v-for="box in classBoxes"
  :key="box.id"
  :config="box"
  @update-position="updateBoxPosition"
  @anchor-click="handleAnchorClick"
  @start-edit="startEditing"
  @contextmenu="handleBoxRightClick"
  @update-attribute="updateBoxAttribute"
  @delete-attribute="deleteBoxAttribute"
  @update-method="updateBoxMethod"
  @delete-method="deleteBoxMethod"
  @update-name="updateBoxName"
/>


<RelationshipArrow
  v-for="rel in relationships"
  :key="`${rel.id}-${rel.fromType}-${rel.toType}-${rel.lineStyle}-${rel.bendStyle}`"
  :from="getAnchorPosition(rel.from.boxId, rel.from.direction)"
  :to="getAnchorPosition(rel.to.boxId, rel.to.direction)"
  :fromType="rel.fromType"
  :toType="rel.toType"
  :lineStyle="rel.lineStyle"
  :bendStyle="rel.bendStyle"  
  :midPoints="rel.midPoints"        
  :rel="rel"
  :classes="classBoxes"
  @select="handleSelect"
  @open-context="handleArrowContextMenu" 
  @add-mid-point="onAddMidPoint"       
  @update-mid-point="onUpdateMidPoint" 
  @delete-mid-point="onDeleteMidPoint"  
  @mid-drag-end="onMidDragEnd"      
/>

        </v-layer>
      </v-stage>

<RelationshipContextMenu
  v-if="arrowContextMenuVisible"
  :rel="selectedRelationship"
  :x="contextMenuX - 250"
  :y="contextMenuY -60"

  @update="handleUpdate"
  @delete="handleDelete"
/>


      <div
        v-if="boxContextMenuVisible"
        class="context-menu"
        :style="{ top: boxMenuY + 'px', left: boxMenuX + 'px' }"
      >
        <button @click="deleteClassBox">❌ 클래스 삭제</button>
      </div>

<!-- 수정창 컨테이너: 입력창+삭제버튼을 flex로 묶어서 배치 -->
<div
  v-if="editing.visible"
  class="overlay-editbox"
  :style="{
    top: editing.y + 'px',
    left: editing.x + 'px',
  }"
>
  <input
    class="editbox-input"
    v-model="editing.value"
    @keydown.enter="applyEdit"
    @blur="applyEdit"
    autofocus
    placeholder="내용 입력"
  />
  <button
    v-if="editing.type !== 'name'"
    class="editbox-delbtn"
    @mousedown.prevent="deleteEditingItem"
    title="삭제"
  >
    <svg width="20" height="20" viewBox="0 0 20 20">
      <g>
        <rect x="6" y="8.7" width="2" height="6" rx="1"/>
        <rect x="12" y="8.7" width="2" height="6" rx="1"/>
        <path d="M4.3 6.3h11.4l-1.1 10.1a1 1 0 0 1-1 .9H6.4a1 1 0 0 1-1-.9L4.3 6.3zm3.2-2.1a1 1 0 0 1 1-1h3a1 1 0 0 1 1 1v1.1h-5V4.2z" />
      </g>
    </svg>
  </button>
</div>


      <div v-if="showSavedMessage" class="save-toast">저장 완료✔️</div>
      <div v-if="saveError" class="error-toast">저장 실패 ⚠️</div>
    </div>
  </div>
</template>

<script setup>
import { ref, watch, onMounted, onUnmounted, reactive  } from 'vue'
import api from '@/api'
import { debounce } from 'lodash'
import { useToolStore } from '@/stores/toolStore'
import ToolBox from '@/components/diagramtool/ToolBox.vue'
import ClassBox from '@/components/diagramtool/classdiagram/ClassBox.vue'
import RelationshipArrow from '@/components/diagramtool/classdiagram/RelationshipArrow.vue'
import RelationshipContextMenu from '@/components/diagramtool/classdiagram/RelationshipContextMenu.vue'

const token = localStorage.getItem('authHeader')
if (token) {
  api.defaults.headers.common['Authorization'] = token
}

const toolStore = useToolStore()
const stageWidth = window.innerWidth
const stageHeight = window.innerHeight

const stageRef = ref(null)
const scale = ref(1)
const stageX = ref(0)
const stageY = ref(0)

const isSpacebarDown = ref(false)
const isPanning = ref(false)
const lastPanPoint = reactive({ x: 0, y: 0 })

const classBoxes = ref([])
const relationships = ref([])
const relationshipStart = ref(null)

const selectedRelationship = ref(null)
const arrowContextMenuVisible = ref(false)
const contextMenuX = ref(0)
const contextMenuY = ref(0)

const editing = reactive({
  visible: false, // 수정 input 노출 여부
  boxId: null,
  type: '',    // 'name' | 'attribute' | 'method'
  index: null, // attribute/method index, name이면 null
  x: 0, y: 0,  // input 위치 (Stage 기준)
  value: ''    // 수정할 값
})
const boxContextMenuVisible = ref(false)
const boxMenuX = ref(0)
const boxMenuY = ref(0)
const selectedBoxId = ref(null)

const showSavedMessage = ref(false)
const saveError = ref(false)

const hideAllMenus = () => {
  arrowContextMenuVisible.value = false
  boxContextMenuVisible.value = false
}

const handleDrop = (event) => {
  const rawData = event.dataTransfer.getData('application/json')
  if (!rawData) return

  const tool = JSON.parse(rawData)
  if (tool.type !== 'box' || tool.subtype !== 'class') return

  const boundingRect = event.currentTarget.getBoundingClientRect()
  const x = event.clientX - boundingRect.left
  const y = event.clientY - boundingRect.top

  classBoxes.value.push({
    id: Date.now(), x, y, width: 160, height: 100,
    name: 'NewClass', attributes: ['+ id: int'], methods: ['+ getId(): int']
  })
}

// onAddMidPoint
function onAddMidPoint({ rel, x, y }) {
  const target = relationships.value.find(r => r.id === rel.id)
  if (!target.midPoints) target.midPoints = []
  target.midPoints.push({ x, y })
}

// onUpdateMidPoint
function onUpdateMidPoint({ rel, idx, x, y }) {
  const target = relationships.value.find(r => r.id === rel.id)
  if (target?.midPoints?.[idx]) {
    target.midPoints[idx].x = x
    target.midPoints[idx].y = y
  }
}

// 함수명 오타 수정
function onDeleteMidPoint({ rel, idx }) {
  const target = relationships.value.find(r => r.id === rel.id)
  if (target?.midPoints) {
    target.midPoints.splice(idx, 1)
  }
}


// (선택) 드래그가 끝났을 때 동기화
function onMidDragEnd(rel) {
  // 서버 동기화 등 추가 로직
}
function handleArrowContextMenu({ rel, x, y }) {
  selectedRelationship.value = rel
  // 컨텍스트 메뉴 위치 설정
  contextMenuX.value = x
  contextMenuY.value = y
  arrowContextMenuVisible.value = true
}


const updateBoxPosition = ({ id, x, y }) => {
  const box = classBoxes.value.find(b => b.id === id)
  if (box) { box.x = x; box.y = y }
}

function handleAnchorClick(anchor) {
  if (!relationshipStart.value) {
    relationshipStart.value = anchor
  } else {
    relationships.value.push({
      id: Date.now(),
      from: relationshipStart.value,
      to: anchor,
      type: 'association',
      fromType: 'arrow',    // ← 여기!
      toType:   'none',
      lineStyle: 'solid',
      midPoints: []
    })
    relationshipStart.value = null
  }
}


const handleSelect = ({ rel, event }) => {
  event.preventDefault(); event.stopPropagation()
  selectedRelationship.value = rel
  contextMenuX.value = event.clientX - 180
  contextMenuY.value = event.clientY - 50
  arrowContextMenuVisible.value = true
}

function handleUpdate(updated) {
  console.log('🔄 handleUpdate 호출, updated:', updated)
  const idx = relationships.value.findIndex(r => r.id === updated.id)
  if (idx !== -1) {
    relationships.value[idx] = {
      ...relationships.value[idx],
      ...updated
    }
  }
  arrowContextMenuVisible.value = false
}


const handleDelete = () => {
  const id = selectedRelationship.value?.id
  relationships.value = relationships.value.filter(r => r.id !== id)
  arrowContextMenuVisible.value = false
}

const handleBoxRightClick = ({ event, id }) => {
  event.evt.preventDefault()
  boxMenuX.value = event.evt.clientX - 250
  boxMenuY.value = event.evt.clientY - 50
  selectedBoxId.value = id
  boxContextMenuVisible.value = true
}

const deleteClassBox = () => {
  classBoxes.value = classBoxes.value.filter(b => b.id !== selectedBoxId.value)
  boxContextMenuVisible.value = false
}

const getAnchorPosition = (boxId, direction) => {
  const box = classBoxes.value.find(b => b.id === boxId)
  if (!box) return { x: 0, y: 0 }

  // ClassBox.vue와 동일한 높이 계산 로직 적용
  const headerHeight = 44
  const attrLineHeight = 22
  const attrAddBtnGap = 8
  const attrAddBtnHeight = 18
  const methodSectionGap = 10
  const methodAddBtnHeight = 20
  const sectionVPadding = 12
  const minBoxHeight = 80

  const attributesStartY = headerHeight + sectionVPadding
  const attrAddBtnY = attributesStartY + (box.attributes?.length || 0) * attrLineHeight + attrAddBtnGap
  const attrSectionBottom = attrAddBtnY + attrAddBtnHeight
  const methodsStartY = attrSectionBottom + methodSectionGap
  const methodAddBtnY = methodsStartY + (box.methods?.length || 0) * attrLineHeight
  const height = Math.max(methodAddBtnY + methodAddBtnHeight + sectionVPadding, minBoxHeight)
  const width = box.width

  switch (direction) {
    case 'top': return { x: box.x + width / 2, y: box.y }
    case 'bottom': return { x: box.x + width / 2, y: box.y + height }
    case 'left': return { x: box.x, y: box.y + height / 2 }
    case 'right': return { x: box.x + width, y: box.y + height / 2 }
    default: return { x: box.x, y: box.y }
  }
}

// ClassBox에서 emit('start-edit', { ... }) 받기
function startEditing({ boxId, type, index, x, y, value }) {
  editing.visible = true
  editing.boxId = boxId
  editing.type = type
  editing.index = index
  editing.x = x
  editing.y = y
  editing.value = value
}

// 입력 적용 (수정 완료)
function applyEdit() {
  if (!editing.visible) return
  const box = classBoxes.value.find(b => b.id === editing.boxId)
  if (!box) {
    editing.visible = false
    return
  }
  if (editing.type === 'name') {
    box.name = editing.value
  } else if (editing.type === 'attribute') {
    box.attributes[editing.index] = editing.value
  } else if (editing.type === 'method') {
    box.methods[editing.index] = editing.value
  }
  editing.visible = false
}
// 속성/메서드 삭제는 휴지통 버튼 따로 띄우거나,
// 입력창에서 editing.type/boxId/index로 직접 splice해도 됨
function deleteEditingItem() {
  const box = classBoxes.value.find(b => b.id === editing.boxId)
  if (!box) return
  if (editing.type === 'attribute') {
    box.attributes.splice(editing.index, 1)
  } else if (editing.type === 'method') {
    box.methods.splice(editing.index, 1)
  }
  editing.visible = false
}

const addBendPoint = ({ relId, x, y }) => {
  const rel = relationships.value.find(r => r.id === relId)
  if (rel) rel.bendPoints.push({ x, y })
}

const handleWheel = (e) => {
  if (!e.evt.ctrlKey) return
  e.evt.preventDefault()
  const stage = stageRef.value.getStage()
  const oldScale = scale.value
  const pointer = stage.getPointerPosition()

  const mousePointTo = {
    x: (pointer.x - stageX.value) / oldScale,
    y: (pointer.y - stageY.value) / oldScale
  }
  const scaleBy = 1.05
  const direction = e.evt.deltaY > 0 ? -1 : 1
  const newScale = direction > 0 ? oldScale * scaleBy : oldScale / scaleBy
  scale.value = Math.max(0.2, Math.min(3, newScale))
  stageX.value = pointer.x - mousePointTo.x * newScale
  stageY.value = pointer.y - mousePointTo.y * newScale
}

const handleStageMouseDown = (e) => {
  if (e.evt.button !== 0 || !isSpacebarDown.value) return

  e.evt.preventDefault()
  isPanning.value = true
  
  const stage = stageRef.value.getStage()
  stage.container().style.cursor = 'grabbing'
  
  lastPanPoint.x = e.evt.clientX
  lastPanPoint.y = e.evt.clientY
}

const handleStageMouseMove = (e) => {
  if (!isPanning.value) return
  e.evt.preventDefault()

  const dx = e.evt.clientX - lastPanPoint.x
  const dy = e.evt.clientY - lastPanPoint.y

  stageX.value += dx
  stageY.value += dy

  lastPanPoint.x = e.evt.clientX
  lastPanPoint.y = e.evt.clientY
}

const handleStageMouseUp = () => {
  if (!isPanning.value) return
  isPanning.value = false
  
  const stage = stageRef.value.getStage()
  stage.container().style.cursor = isSpacebarDown.value ? 'grab' : 'default'
}

const handleKeyDown = (e) => {
  if (e.code === 'Space' && !isSpacebarDown.value) {
    e.preventDefault()
    isSpacebarDown.value = true
    const stage = stageRef.value?.getStage()
    if (stage && !isPanning.value) {
      stage.container().style.cursor = 'grab'
    }
  }
}

const handleKeyUp = (e) => {
  if (e.code === 'Space') {
    isSpacebarDown.value = false
    const stage = stageRef.value?.getStage()
    if (stage && !isPanning.value) {
      stage.container().style.cursor = 'default'
    }
  }
}

const saveToServer = async () => {
  if (props.readonly) {
    console.log('🔒 읽기 전용 모드입니다. 저장하지 않습니다.')
    return
  }

  const stage = stageRef.value.getStage();
  if (!stage) {
    console.error('Stage를 찾을 수 없어 캡처할 수 없습니다.');
    return;
  }

  // 1. 캔버스를 이미지 데이터 URL로 변환 (고화질을 위해 pixelRatio 사용)
  const dataURL = stage.toDataURL({ pixelRatio: 2 });

  // 2. 데이터 URL을 Blob 객체로 변환하는 헬퍼 함수
  const dataURLtoBlob = (dataurl) => {
    const arr = dataurl.split(',');
    const mimeMatch = arr[0].match(/:(.*?);/);
    if (!mimeMatch) return null;
    const mime = mimeMatch[1];
    const bstr = atob(arr[1]);
    let n = bstr.length;
    const u8arr = new Uint8Array(n);
    while (n--) {
      u8arr[n] = bstr.charCodeAt(n);
    }
    return new Blob([u8arr], { type: mime });
  };

  const imageBlob = dataURLtoBlob(dataURL);

  const formData = new FormData()
  formData.append('type', 'classDiagram')
  formData.append('json', JSON.stringify({
    classes: classBoxes.value,
    relationships: relationships.value
  }))
  if (imageBlob) {
    formData.append('files', imageBlob, 'class_diagram_capture.png');
  }

  if (props.projectId) {
    formData.append('projectId', props.projectId)
  }

  try {
    await api.put('/design/update', formData) // Content-Type은 FormData 사용 시 api가 자동으로 설정합니다.

    showSavedMessage.value = true
    saveError.value = false
    setTimeout(() => showSavedMessage.value = false, 3000)
  } catch (err) {
    console.warn('❌ 자동 저장 실패:', err)
    saveError.value = true
    alert('⚠️ 저장 중 오류 발생')
  }
}

const props = defineProps({
  projectId: Number,
  readonly: Boolean,
  projectTitle: String
})

const autoSave = debounce(saveToServer, 1000)
watch([classBoxes, relationships], autoSave, { deep: true })

onMounted(async () => {
  try {
    const res = await api.get('/design/all', {
      params: { projectId: props.projectId },
      headers: { Authorization: localStorage.getItem('authHeader') },
      withCredentials: true
    })

    const { classDiagram } = res.data
    if (classDiagram?.json) {
      const parsed = JSON.parse(classDiagram.json)
      classBoxes.value = parsed.classes || []
      relationships.value = parsed.relationships || []
      console.log('✅ 클래스 다이어그램 불러오기 성공:', parsed)
    } else {
      console.warn('⚠️ 저장된 클래스 다이어그램이 없습니다.')
      classBoxes.value = []
      relationships.value = []
    }
  } catch (err) {
    console.error('❌ 클래스 다이어그램 초기 데이터 로드 실패:', err)
  }

  window.addEventListener('keydown', handleKeyDown)
  window.addEventListener('keyup', handleKeyUp)
})

onUnmounted(() => {
  window.removeEventListener('keydown', handleKeyDown)
  window.removeEventListener('keyup', handleKeyUp)
})

// 속성 추가/수정 (index: null이면 추가, 아니면 수정)
function updateBoxAttribute({ boxId, index, value }) {
  const box = classBoxes.value.find(b => b.id === boxId)
  if (!box) return
  if (index == null) {
    box.attributes.push(value || '새 속성')
  } else {
    box.attributes[index] = value
  }
}

// 속성 삭제
function deleteBoxAttribute({ boxId, index }) {
  const box = classBoxes.value.find(b => b.id === boxId)
  if (!box) return
  box.attributes.splice(index, 1)
}

// 메서드 추가/수정
function updateBoxMethod({ boxId, index, value }) {
  const box = classBoxes.value.find(b => b.id === boxId)
  if (!box) return
  if (index == null) {
    box.methods.push(value || '새 메서드')
  } else {
    box.methods[index] = value
  }
}

// 메서드 삭제
function deleteBoxMethod({ boxId, index }) {
  const box = classBoxes.value.find(b => b.id === boxId)
  if (!box) return
  box.methods.splice(index, 1)
}

// 클래스명 수정
function updateBoxName({ boxId, value }) {
  const box = classBoxes.value.find(b => b.id === boxId)
  if (!box) return
  box.name = value
}

</script>


<style scoped>
.diagram-layout {
  display: flex;
  height: 100vh;
}
.diagram-page {
  flex: 1;
  overflow: hidden;
  background-color: #f7f8fc;
  position: relative;
}

.context-menu {
  position: absolute;
  background: white;
  border: 1px solid #ccc;
  padding: 5px 10px;
  border-radius: 6px;
  box-shadow: 0 4px 10px rgba(0, 0, 0, 0.15);
  z-index: 1001;
}
.save-toast {
  position: fixed;
  top: 20px;
  right: 20px;
  background: #4caf50;
  color: white;
  padding: 8px 16px;
  border-radius: 6px;
  z-index: 1000;
}
.error-toast {
  position: fixed;
  top: 20px;
  right: 20px;
  background: #e53935;
  color: white;
  padding: 8px 16px;
  border-radius: 6px;
  z-index: 1000;
}
.overlay-editbox {
  position: absolute;
  z-index: 1001;
  display: flex;
  align-items: center;
  background: #fff;
  border-radius: 8px;
  box-shadow: 0 6px 20px rgba(44,62,80,0.12), 0 1.5px 6px rgba(44,62,80,0.06);
  padding: 8px 14px 8px 14px;
  gap: 10px;
  border: 1.2px solid #d3dae6;
  min-width: 150px;
  min-height: 38px;
  transition: box-shadow 0.18s, border 0.15s;
}
.overlay-editbox:focus-within {
  box-shadow: 0 8px 24px rgba(44,62,80,0.18), 0 2px 10px rgba(44,62,80,0.11);
  border-color: #4ba7fa;
}
.editbox-input {
  border: none;
  outline: none;
  font-size: 16px;
  background: transparent;
  min-width: 110px;
  padding: 2px 3px;
  color: #232b39;
}
.editbox-input::placeholder {
  color: #bbb;
}
.editbox-delbtn {
  background: none;
  border: none;
  padding: 0 3px;
  cursor: pointer;
  transition: filter 0.14s;
  display: flex;
  align-items: center;
  justify-content: center;
}
.editbox-delbtn svg {
  fill: #ec7e7e;
  transition: fill 0.2s;
}
.editbox-delbtn:hover svg {
  fill: #d32f2f;
  filter: drop-shadow(0 0 2px #f58b8b);
}

</style>
