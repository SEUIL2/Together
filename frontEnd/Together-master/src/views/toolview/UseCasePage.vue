<template>
  <div class="diagram-layout" @click="hideAllMenus" @wheel.ctrl.prevent="handleWheel">
    <!-- 툴박스 -->
    <ToolBox />

    <!-- 다이어그램 영역 -->
    <div class="diagram-canvas" ref="canvasRef" @dragover.prevent @drop="handleDrop">
      <v-stage
        ref="stageRef"
        :config="{
          width: stageWidth,
          height: stageHeight,
          scaleX: scale,
          scaleY: scale
        }"

      >
        <v-layer>
          <!-- 액터(사람 아이콘) -->
          <ActorNode
            v-for="actor in actors"
            :key="actor.id"
            :config="actor"
            @update-position="updateActorPosition"
            @contextmenu="onActorContextMenu"
            @anchor-click="handleAnchorClick"
            @dblclick="onNodeDblClick('actor', actor)"
          />

          <!-- 유스케이스(타원) -->
          <UsecaseNode
            v-for="uc in usecases"
            :key="uc.id"
            :config="uc"
            @update-position="updateUsecasePosition"
            @contextmenu="onUsecaseContextMenu"
            @anchor-click="handleAnchorClick"
            @dblclick="onNodeDblClick('usecase', uc)"
          />

          <!-- 관계선(Include/Extend 등) -->
          <UsecaseLink
            v-for="rel in links"
            :key="rel.id"
            :from="findAnchor(rel.from)"
            :to="findAnchor(rel.to)"
            :type="rel.type"
            :hitStrokeWidth="8"
            @contextmenu="onLinkContextMenu(rel, $event)"
          />
        </v-layer>
      </v-stage>

      <!-- 컨텍스트 메뉴 -->
      <UsecaseContextMenu
        v-if="contextMenu.visible"
        :x="contextMenu.x -200"
        :y="contextMenu.y- 60"
        :target="contextMenu.target"
        @close="contextMenu.visible = false"
        @delete="deleteTarget"
        @toggle-type="toggleLinkType"
      />

      <!-- 이름 변경 모달 -->
      <div v-if="nameEditModal" class="edit-modal">
        <div class="modal-backdrop" @click="closeNameEdit"></div>
        <div class="modal-body">
          <h3>이름 변경</h3>
          <input
            v-model="nameEditValue"
            @keydown.enter="confirmEditName"
            ref="nameEditInput"
          />
          <div class="modal-actions">
            <button @click="confirmEditName">확인</button>
            <button @click="closeNameEdit">취소</button>
          </div>
        </div>
      </div>

      <!-- 저장 상태 토스트 -->
      <div v-if="saveStatus !== 'idle'" class="save-toast" :class="saveStatus">
        {{ saveStatus === 'saving' ? '저장 중...' : saveStatus === 'saved' ? '💾 저장 완료' : '저장 실패!' }}
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, watch, onMounted, nextTick } from 'vue'
import { useRoute } from 'vue-router'
import api from '@/api'
import debounce from 'lodash/debounce'

import ToolBox from '@/components/diagramtool/ToolBox.vue'
import ActorNode from '@/components/diagramtool/usecase/ActorNode.vue'
import UsecaseNode from '@/components/diagramtool/usecase/UsecaseNode.vue'
import UsecaseLink from '@/components/diagramtool/usecase/UsecaseLink.vue'
import UsecaseContextMenu from '@/components/diagramtool/usecase/UsecaseContextMenu.vue'

const stageWidth = window.innerWidth - 220
const stageHeight = window.innerHeight - 40

const stageRef = ref(null)
const canvasRef = ref(null)

const actors = ref([])
const usecases = ref([])
const links = ref([])

const scale = ref(1)
const minScale = 0.4
const maxScale = 2.2


const connectState = ref({ start: null })
const handleAnchorClick = (info) => {
  if (!connectState.value.start) {
    connectState.value.start = info
  } else {
    links.value.push({
      id: 'rel_' + Date.now(),
      type: 'association',
      from: connectState.value.start,
      to: info
    })
    connectState.value.start = null
  }
}

const handleDrop = (e) => {
  const data = e.dataTransfer.getData('application/json')
  if (!data) return
  const tool = JSON.parse(data)
  const pos = getRelativePos(e)
  if (tool.subtype === 'actor') {
    actors.value.push({
      id: 'actor_' + Date.now(),
      name: '액터',
      x: pos.x,
      y: pos.y
    })
  }
  if (tool.subtype === 'usecase') {
    usecases.value.push({
      id: 'usecase_' + Date.now(),
      name: '유스케이스',
      x: pos.x,
      y: pos.y
    })
  }
}
function getRelativePos(e) {
  const rect = canvasRef.value.getBoundingClientRect()
  return {
    x: e.clientX - rect.left,
    y: e.clientY - rect.top
  }
}

const updateActorPosition = (id, x, y) => {
  const item = actors.value.find(a => a.id === id)
  if (item) {
    item.x = x
    item.y = y
  }
}
const updateUsecasePosition = (id, x, y) => {
  const item = usecases.value.find(u => u.id === id)
  if (item) {
    item.x = x
    item.y = y
  }
}

const contextMenu = reactive({
  visible: false,
  x: 0,
  y: 0,
  target: null,
})
const hideAllMenus = () => { contextMenu.visible = false }
const onActorContextMenu = (id, e) => {
  e.evt.preventDefault()
  contextMenu.visible = true
  contextMenu.x = e.evt.clientX
  contextMenu.y = e.evt.clientY
  contextMenu.target = { type: 'actor', id }
}
const onUsecaseContextMenu = (id, e) => {
  e.evt.preventDefault()
  contextMenu.visible = true
  contextMenu.x = e.evt.clientX
  contextMenu.y = e.evt.clientY
  contextMenu.target = { type: 'usecase', id }
}
const onLinkContextMenu = (link, e) => {
  e.evt.preventDefault()
  contextMenu.visible = true
  contextMenu.x = e.evt.clientX
  contextMenu.y = e.evt.clientY
  contextMenu.target = {
    type: 'link',
    id: link.id,
    currentType: link.type // ✅ 이 줄 꼭 필요
  }
}

const deleteTarget = (target) => {
  if (target.type === 'actor') {
    actors.value = actors.value.filter(a => a.id !== target.id)
  }
  if (target.type === 'usecase') {
    usecases.value = usecases.value.filter(u => u.id !== target.id)
  }
  if (target.type === 'link') {
    links.value = links.value.filter(l => l.id !== target.id)
  }
  contextMenu.visible = false
}
const toggleLinkType = (target) => {
  const link = links.value.find(l => l.id === target.id)
  if (link && target.nextType) {
    link.type = target.nextType
  }
}


const findAnchor = (anchorObj) => {
  if (!anchorObj || !anchorObj.nodeId || !anchorObj.direction) return { x: 0, y: 0 }
  let node
  if (anchorObj.type === 'actor') {
    node = actors.value.find(a => a.id === anchorObj.nodeId)
    if (!node) return { x: 0, y: 0 }
    const anchorOffset = {
      top:    { x: 0,    y: 0 },
      bottom: { x: 0,    y: 110 },
      left:   { x: -28,  y: 48 },
      right:  { x: 28,   y: 48 }
    }
    return { x: node.x + anchorOffset[anchorObj.direction].x, y: node.y + anchorOffset[anchorObj.direction].y }
  }
  if (anchorObj.type === 'usecase') {
    node = usecases.value.find(u => u.id === anchorObj.nodeId)
    if (!node) return { x: 0, y: 0 }
    const anchorOffset = {
      top:    { x: 0,   y: -28 },
      bottom: { x: 0,   y: 28 },
      left:   { x: -75, y: 0 },
      right:  { x: 75,  y: 0 }
    }
    return { x: node.x + anchorOffset[anchorObj.direction].x, y: node.y + anchorOffset[anchorObj.direction].y }
  }
  return { x: 0, y: 0 }
}

const nameEditModal = ref(false)
const nameEditTarget = ref({ type: '', id: '' })
const nameEditValue = ref('')
const nameEditInput = ref(null)

function onNodeDblClick(type, node) {
  nameEditTarget.value = { type, id: node.id }
  nameEditValue.value = node.name
  nameEditModal.value = true
  nextTick(() => {
    nameEditInput.value && nameEditInput.value.focus()
  })
}
function confirmEditName() {
  const val = nameEditValue.value.trim()
  if (!val) return
  if (nameEditTarget.value.type === 'actor') {
    const item = actors.value.find(a => a.id === nameEditTarget.value.id)
    if (item) item.name = val
  }
  if (nameEditTarget.value.type === 'usecase') {
    const item = usecases.value.find(u => u.id === nameEditTarget.value.id)
    if (item) item.name = val
  }
  closeNameEdit()
}
function closeNameEdit() {
  nameEditModal.value = false
  nameEditValue.value = ''
  nameEditTarget.value = { type: '', id: '' }
}

// === 저장 관련 ===
const route = useRoute()
const saveStatus = ref('idle')

  const saveUsecase = debounce(async () => {
    const readonly = route.query.readonly === 'true'
    if (readonly) {
      console.log('🔒 읽기 전용 모드입니다. 저장하지 않습니다.')
      return
    }
 
    saveStatus.value = 'saving'
 
    const stage = stageRef.value.getStage();
    if (!stage) {
      console.error('Stage를 찾을 수 없어 캡처할 수 없습니다.');
      saveStatus.value = 'error';
      return;
    }
 
    // 1. 캔버스를 이미지 데이터 URL로 변환
    const dataURL = stage.toDataURL({ pixelRatio: 2 });
 
    // 2. 데이터 URL을 Blob 객체로 변환
    const dataURLtoBlob = (dataurl) => {
      const arr = dataurl.split(','), mimeMatch = arr[0].match(/:(.*?);/);
      if (!mimeMatch) return null;
      const mime = mimeMatch[1], bstr = atob(arr[1]);
      let n = bstr.length;
      const u8arr = new Uint8Array(n);
      while (n--) u8arr[n] = bstr.charCodeAt(n);
      return new Blob([u8arr], { type: mime });
    };
    const imageBlob = dataURLtoBlob(dataURL);
 
    const jsonData = {
      actors: actors.value,
      usecases: usecases.value,
      links: links.value
    }
 
    const formData = new FormData()
    formData.append('type', 'usecase')
    formData.append('json', JSON.stringify(jsonData))
    if (imageBlob) {
      formData.append('files', imageBlob, 'usecase_capture.png');
    }
 
    formData.append('projectId', route.params.projectId);
 
    try {
      await api.post('/design/upload', formData);
      saveStatus.value = 'saved'
      setTimeout(() => saveStatus.value = 'idle', 1200)
      console.log('✅ 유스케이스 다이어그램 저장 성공')
    } catch (err) {
      console.error('❌ 유스케이스 저장 실패:', err)
      saveStatus.value = 'error'
      setTimeout(() => saveStatus.value = 'idle', 3000)
      alert('⚠️ 유스케이스 저장 중 오류 발생')
    }
  }, 1000)

  watch([actors, usecases, links], saveUsecase, { deep: true })

// === 줌 기능 ===
function handleWheel(e) {
  e.preventDefault()
  const stage = stageRef.value.getStage()
  const box = stage.container().getBoundingClientRect()
  const x = e.clientX - box.left
  const y = e.clientY - box.top
  const oldScale = stage.scaleX()
  const mousePointTo = {
    x: (x - stage.x()) / oldScale,
    y: (y - stage.y()) / oldScale,
  }
  const scaleBy = 1.05
  const newScale = e.deltaY > 0 ? oldScale / scaleBy : oldScale * scaleBy
  stage.scale({ x: newScale, y: newScale })
  stage.position({
    x: x - mousePointTo.x * newScale,
    y: y - mousePointTo.y * newScale,
  })
  stage.batchDraw()
}

// === 불러오기 ===
onMounted(async () => {
  try {
    const res = await api.get('/design/all', {
      params: { projectId: route.params.projectId } // props 대신 route.params 사용
    })

    const { usecase } = res.data
    if (usecase?.json) {
      const parsed = JSON.parse(usecase.json)
      actors.value = parsed.actors || []
      usecases.value = parsed.usecases || []
      links.value = parsed.links || []
      console.log('✅ 유스케이스 불러오기 성공:', parsed)
    } else {
      console.warn('⚠️ 저장된 유스케이스 데이터가 없습니다.')
      actors.value = []
      usecases.value = []
      links.value = []
    }
  } catch (err) {
    console.error('❌ 유스케이스 초기 데이터 로드 실패:', err)
  }
})



</script>



<style scoped>
.diagram-layout {
  display: flex;
  height: 100vh;
  width: 100%;
  background: #f4f6f8;
}
.diagram-canvas {
  flex: 1;
  background: #fff;
  position: relative;
  min-width: 0;
}
/* 모달 스타일 */
.edit-modal {
  position: fixed; left: 0; top: 0; width: 100vw; height: 100vh;
  z-index: 1000;
}
.modal-backdrop {
  position: absolute; left: 0; top: 0; width: 100vw; height: 100vh;
  background: #0005;
}
.modal-body {
  position: absolute; left: 50%; top: 50%; transform: translate(-50%,-50%);
  background: #fff; border-radius: 12px; padding: 32px 24px;
  box-shadow: 0 4px 32px #0002;
  min-width: 260px; min-height: 120px;
  display: flex; flex-direction: column; align-items: center;
}
.modal-body input {
  width: 200px; font-size: 18px; padding: 6px 8px; margin: 16px 0;
  border: 1.5px solid #1976d2; border-radius: 5px;
}
.modal-actions { display: flex; gap: 12px; justify-content: flex-end; }
.modal-actions button { padding: 7px 18px; border-radius: 4px; border: none; }

.save-toast {
  position: fixed;
  bottom: 20px;
  right: 20px;
  padding: 10px 20px;
  border-radius: 8px;
  color: white;
  font-weight: 600;
  z-index: 1000;
  transition: opacity 0.3s;
}
.save-toast.saving { background-color: #777; }
.save-toast.saved { background-color: #28a745; }
.save-toast.error { background-color: #dc3545; }
</style>
