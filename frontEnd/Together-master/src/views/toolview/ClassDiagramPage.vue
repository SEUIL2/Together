<template>
  <div class="diagram-layout" @click="hideAllMenus">
    <div v-if="saveStatus === 'saving'" class="save-toast saving">저장 중...</div>
    <div v-else-if="saveStatus === 'saved'" class="save-toast saved">💾 저장 완료!</div>
    <div v-else-if="saveStatus === 'error'" class="save-toast error">저장 실패!</div>

    <ToolBox diagramType="class" />
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
  @height-update="updateBoxHeight"
  @width-update="updateBoxWidth"
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

    </div>
  </div>
</template>

<script setup>
import { ref, watch, onMounted, onUnmounted, reactive, computed  } from 'vue'
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

// 성능 최적화: ID로 박스를 빠르게 찾기 위한 Map
const boxMap = computed(() => {
  return new Map(classBoxes.value.map(box => [box.id, box]));
});

const selectedRelationship = ref(null)
const arrowContextMenuVisible = ref(false)
const contextMenuX = ref(0)
const contextMenuY = ref(0)

const boxContextMenuVisible = ref(false)
const boxMenuX = ref(0)
const boxMenuY = ref(0)
const selectedBoxId = ref(null)

const showSavedMessage = ref(false)
const saveError = ref(false)
const saveStatus = ref('idle') // 'idle', 'saving', 'saved', 'error'

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
  // 캔버스의 이동 및 줌 상태를 고려하여 정확한 드롭 위치를 계산합니다.
  const x = (event.clientX - boundingRect.left - stageX.value) / scale.value;
  const y = (event.clientY - boundingRect.top - stageY.value) / scale.value;

  classBoxes.value.push({
    id: Date.now(), x, y, width: 160, height: 100,
    name: 'NewClass', 
    attributes: ['+ attribute: type'], 
    methods: ['+ method(): returnType']
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

function handleUpdate(updated) {
  const idx = relationships.value.findIndex(r => r.id === updated.id)
  if (idx !== -1) {
    relationships.value[idx] = {
      ...relationships.value[idx],
      ...updated
    }
  }
  arrowContextMenuVisible.value = false
}


const updateBoxPosition = ({ id, x, y }) => {
  const box = classBoxes.value.find(b => b.id === id)
  if (box) { box.x = x; box.y = y }
}

const updateBoxHeight = ({ id, height }) => {
  const box = classBoxes.value.find(b => b.id === id);
  if (box) {
    box.height = height;
  }
};
function handleAnchorClick(anchor) {
  // 너비가 변경된 후 앵커 위치가 바뀔 수 있으므로, 클릭 시 위치 재계산
  const box = classBoxes.value.find(b => b.id === anchor.boxId);
  if (box) {
    const pos = getAnchorPosition(anchor.boxId, anchor.direction);
    anchor.x = pos.x;
    anchor.y = pos.y;
  }
  if (!relationshipStart.value) {
    relationshipStart.value = anchor
  } else {
    relationships.value.push({
      id: Date.now(),
      from: relationshipStart.value,
      to: anchor,
      type: 'association',
      fromType: 'none',
      toType: 'none',
      lineStyle: 'solid',
      midPoints: []
    })
    relationshipStart.value = null
  }
}

const updateBoxWidth = ({ id, width }) => {
  const box = classBoxes.value.find(b => b.id === id);
  if (box) {
    box.width = width;
    // 너비가 변경되면 캔버스를 다시 그려서 관계선 위치 등을 업데이트합니다.
    const stage = stageRef.value?.getStage();
    if (stage) {
      stage.batchDraw();
    }
  }
};


const handleSelect = ({ rel, event }) => {
  event.preventDefault(); event.stopPropagation()
  selectedRelationship.value = rel
  contextMenuX.value = event.clientX - 180
  contextMenuY.value = event.clientY - 50
  arrowContextMenuVisible.value = true
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
  const box = boxMap.value.get(boxId); // 최적화: Map에서 O(1) 시간으로 조회
  if (!box) return { x: 0, y: 0 };

  const height = box.height || 100; // ClassBox에서 업데이트된 높이 사용, 없으면 기본값
  const width = box.width || 160; // ClassBox에서 업데이트된 너비 사용

  switch (direction) {
    case 'top': return { x: box.x + width / 2, y: box.y }
    case 'bottom': return { x: box.x + width / 2, y: box.y + height }
    case 'left': return { x: box.x, y: box.y + height / 2 }
    case 'right': return { x: box.x + width, y: box.y + height / 2 }
    default: return { x: box.x, y: box.y }
  }
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

  saveStatus.value = 'saving'
  
  try {
    await api.put('/design/update', formData) // Content-Type은 FormData 사용 시 api가 자동으로 설정합니다.

    saveStatus.value = 'saved'
    showSavedMessage.value = true
    saveError.value = false
    setTimeout(() => {
      showSavedMessage.value = false
      saveStatus.value = 'idle'
    }, 1200)
  } catch (err) {
    console.warn('❌ 자동 저장 실패:', err)
    saveStatus.value = 'error'
    saveError.value = true
    setTimeout(() => saveStatus.value = 'idle', 3000)
    alert('⚠️ 저장 중 오류 발생')
  }
}

const props = defineProps({
  projectId: Number,
  readonly: Boolean,
  projectTitle: String
})

const autoSave = debounce(saveToServer, 2500) // debounce 시간을 늘려 잦은 저장 방지
watch([classBoxes, relationships], autoSave, { deep: true })

// 마지막 뷰포트(위치, 줌) 저장
const saveViewport = debounce(() => {
  const viewport = {
    scale: scale.value,
    x: stageX.value,
    y: stageY.value,
  };
  localStorage.setItem(`classDiagramViewport_${props.projectId}`, JSON.stringify(viewport));
}, 500);
watch([scale, stageX, stageY], saveViewport);

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

  // 저장된 뷰포트 불러오기
  const savedViewport = localStorage.getItem(`classDiagramViewport_${props.projectId}`);
  if (savedViewport) {
    try {
      const viewport = JSON.parse(savedViewport);
      scale.value = viewport.scale || 1;
      stageX.value = viewport.x || 0;
      stageY.value = viewport.y || 0;
    } catch (e) {
      console.error("저장된 뷰포트 정보를 불러오는 데 실패했습니다:", e);
    }
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
  height: 100%;
  width: 100%;
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
  bottom: 20px;
  right: 20px;
  padding: 10px 20px;
  border-radius: 8px;
  color: white;
  font-weight: 600;
  z-index: 9999;
  transition: opacity 0.3s;
  pointer-events: none;
  white-space: nowrap;
  display: inline-block;
  width: auto;
  height: auto;
}
.save-toast.saving { background-color: #777; }
.save-toast.saved { background-color: #323232; }
.save-toast.error { background-color: #dc3545; }

</style>
