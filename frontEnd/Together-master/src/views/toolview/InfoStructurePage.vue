<template>
  <div
    class="ia-page-root"
    @mousedown="closeAllMenus"
    @wheel.ctrl.prevent="handleWheel"
  >
    <div class="ia-body">
      <ToolBox />

      <div
        class="ia-canvas"
        @dragover.prevent
        @drop="onDrop"
      >
        <v-stage
          ref="stageRef"
          :config="{ width: canvasW, height: canvasH, draggable: false }"
        >
          <v-layer>
            <!-- 블록 노드 -->
            <IABlock
              v-for="block in blocks"
              :key="block.id"
              :config="block"
              :isEditing="!!editing"
              @update="updateBlock"
              @delete-block="deleteBlock"
              @edit-title="editBlockTitle"
              @edit-item="editBlockItem"
              @add-item="addBlockItem"
              @delete-item="deleteBlockItem"
              @anchor-click="onAnchorClick"
              @open-context="openBlockContext"
            />

            <!-- 임시 관계선(드래그중) -->
            <v-line
              v-if="tempLine"
              :points="tempLinePoints"
              stroke="#888"
              :dash="[4, 4]"
            />

            <!-- 실제 관계선(꺾인 선, 중간점 지원) -->
            <IARelationship
              v-for="rel in relationships"
              :key="rel.id"
              :rel="rel"
              :from="getAnchorPosById(rel.fromBoxId, rel.fromDirection, rel.fromItemIndex)"
              :to="getAnchorPosById(rel.toBoxId, rel.toDirection, rel.toItemIndex)"
              :midPoints="rel.midPoints || []"
              :lineStyle="rel.lineStyle || 'solid'"
              @open-context="openRelContext"
              @add-mid-point="addMidpoint"
              @update-mid-point="moveMidpoint"
              @delete-mid-point="deleteMidpoint"
            />
          </v-layer>
        </v-stage>
      </div>
    </div>

    <!-- 블록/항목 편집 오버레이 -->
    <div v-if="editing" class="edit-overlay">
      <div class="edit-box">
        <input v-model="editing.value" @keyup.enter="submitEdit" placeholder="내용 입력" />
        <div class="actions">
          <button @click="submitEdit">확인</button>
          <button @click="cancelEdit">취소</button>
        </div>
      </div>
    </div>

    <!-- 우클릭 메뉴 -->
    <div
      v-if="relContextMenu.visible"
      class="custom-context-menu"
      :style="{ left: relContextMenu.x + 'px', top: relContextMenu.y + 'px' }"
      @mousedown.stop
    >
      <button class="close-btn" @click="closeAllMenus">✖</button>
      <div class="menu-content">
        <div
          v-if="relContextMenu.type==='relationship'"
          class="menu-item danger"
          @click="deleteRelationship(relContextMenu.rel.id)"
        >
          <span class="icon">🗑️</span> 관계선 삭제
        </div>
        <div
          v-if="relContextMenu.type==='block'"
          class="menu-item danger"
          @click="deleteBlock(relContextMenu.rel.id)"
        >
          <span class="icon">🗑️</span> 블록 삭제
        </div>
      </div>
    </div>

    <!-- 저장 알림 -->
    <transition name="fade">
      <div v-if="showSaveNotice" class="save-notice">
        💾 저장 완료
      </div>
    </transition>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, watch } from 'vue'
import axios from 'axios'
import ToolBox from '@/components/diagramtool/ToolBox.vue'
import IABlock from '@/components/diagramtool/ia/IABlock.vue'
import IARelationship from '@/components/diagramtool/ia/IARelationship.vue'

// 프로젝트 ID를 props로 받음 (라우트에서 받을 수도 있음)
const props = defineProps({ projectId: { type: [String, Number], required: true } })

// 캔버스 크기
const canvasW = window.innerWidth - 200
const canvasH = window.innerHeight

// 상태
const blocks = ref([])
const relationships = ref([])
const tempLine = ref(null)
const editing = ref(null)
const relContextMenu = ref({ visible:false, x:0, y:0, rel:null, type:null })
const showSaveNotice = ref(false)
let saveTimer = null

// 레이어/스테이지 참조 (줌용)
const stageRef = ref(null)

// 계산: 임시선 포인트
const tempLinePoints = computed(() => tempLine.value ? [...tempLine.value.from, ...tempLine.value.to] : [])

// 최초 진입 시 데이터 불러오기
onMounted(async () => {
  try {
    const res = await axios.get('/planning/all', {
      params: { projectId: props.projectId },
      headers: { Authorization: localStorage.getItem('authHeader') },
      withCredentials: true,
    })
    const json = res.data?.infostructure?.json
    if (json) {
      const parsed = JSON.parse(json)
      blocks.value = parsed.blocks || []
      relationships.value = parsed.relationships || []
    } else {
      blocks.value = []
      relationships.value = []
    }
  } catch (e) {
    blocks.value = []
    relationships.value = []
    console.error('정보구조도 불러오기 실패', e)
  }
})

// 자동저장 (디바운스, 백엔드에 저장)
async function doSave() {
  try {
    const form = new FormData()
    form.append('type', 'infostructure')
    form.append('projectId', props.projectId)
    form.append('json', JSON.stringify({
      blocks: blocks.value,
      relationships: relationships.value
    }))
    // 필요시 text 등 추가 가능
    await axios.put('/planning/update', form, {
      headers: { Authorization: localStorage.getItem('authHeader') },
      withCredentials: true
    })
    showSaveNotice.value = true
    setTimeout(() => showSaveNotice.value = false, 2000)
  } catch (err) {
    console.error('정보구조도 저장 실패', err)
    // 저장 실패 안내를 하고 싶으면 여기에 알림 추가
  }
}

// blocks, relationships 변경시 자동 저장
watch([blocks, relationships], () => {
  clearTimeout(saveTimer)
  saveTimer = setTimeout(doSave, 1000)
}, { deep: true })

// Ctrl+휠 줌 (native 이벤트 on root)
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

// 메뉴 닫기
function closeAllMenus() { relContextMenu.value.visible = false }

// 이하: 블록/관계선 기능 (기존 코드 그대로)
function getAnchorPos(box, direction, itemIndex = null) {
  const boxWidth = 180; const headerHeight = 30; const itemHeight = 26;
  const totalH = headerHeight + box.items.length*itemHeight + 30
  if (direction==='left') return {x:box.x,y:box.y+totalH/2}
  if (direction==='right') return {x:box.x+boxWidth,y:box.y+totalH/2}
  if (direction==='top') return {x:box.x+boxWidth/2,y:box.y}
  if (direction==='bottom') return {x:box.x+boxWidth/2,y:box.y+totalH}
  if (direction==='item'&&itemIndex!=null) return {x:box.x+boxWidth-10,y:box.y+headerHeight+itemIndex*itemHeight+itemHeight/2}
  return {x:box.x+boxWidth/2,y:box.y+totalH/2}
}
function getAnchorPosById(id,dir,idx){ const b=blocks.value.find(x=>x.id===id); return b?getAnchorPos(b,dir,idx):{x:0,y:0} }
function onDrop(e){ const tool=JSON.parse(e.dataTransfer.getData('application/json')); if(tool.type!=='box')return; const rect=e.currentTarget.getBoundingClientRect(); const x=e.clientX-rect.left,y=e.clientY-rect.top; const nb={id:Date.now(),x,y,width:280,title:'제목',items:[{id:Date.now()+1,title:'항목 1',subtitle:''}]}; nb.height=32+nb.items.length*48+16;blocks.value.push(nb) }
function updateBlock(u){ const i=blocks.value.findIndex(x=>x.id===u.id); if(i<0)return; u.height=32+u.items.length*48+16; blocks.value.splice(i,1,{...u}) }
function deleteBlock(id){ blocks.value=blocks.value.filter(x=>x.id!==id); relationships.value=relationships.value.filter(r=>r.fromBoxId!==id&&r.toBoxId!==id); closeAllMenus() }
function editBlockTitle(p){ editing.value={type:'title',...p} }
function editBlockItem(p){ editing.value={type:'item',...p} }
function submitEdit(){ const{type,id,index,value}=editing.value; const b=blocks.value.find(x=>x.id===id); if(!b)return; if(type==='title')b.title=value;else b.items[index].title=value;updateBlock(b);editing.value=null }
function cancelEdit(){ editing.value=null }
function addBlockItem(id){ const b=blocks.value.find(x=>x.id===id); const c=b.items.length+1; b.items.push({id:Date.now(),title:`항목 ${c}`,subtitle:''}); updateBlock(b) }
function deleteBlockItem(p){ const b=blocks.value.find(x=>x.id===p.id); b.items.splice(p.index,1); updateBlock(b) }
function onAnchorClick(p){ if(!tempLine.value) tempLine.value={fromBoxId:p.boxId,fromDirection:p.direction,fromItemIndex:p.itemIndex,from:p.pos,to:p.pos}; else{ relationships.value.push({id:Date.now().toString(),...tempLine.value,toBoxId:p.boxId,toDirection:p.direction,toItemIndex:p.itemIndex,midPoints:[],lineStyle:'solid'}); tempLine.value=null } }
function deleteRelationship(id){ relationships.value=relationships.value.filter(r=>r.id!==id); closeAllMenus() }
function addMidpoint(p){ relationships.value=relationships.value.map(r=>r.id!==p.rel.id?r:{...r,midPoints:[...(r.midPoints||[]),{x:p.x,y:p.y}]}) }
function moveMidpoint(p){ relationships.value=relationships.value.map(r=>r.id!==p.rel.id?r:(()=>{const m=[...(r.midPoints||[])]; m[p.idx]={x:p.x,y:p.y}; return {...r,midPoints:m}})()) }
function deleteMidpoint(p){ relationships.value=relationships.value.map(r=>r.id!==p.rel.id?r:(()=>{const m=[...(r.midPoints||[])]; m.splice(p.idx,1); return {...r,midPoints:m}})()) }
function openRelContext({rel,x,y}){ relContextMenu.value={visible:true,x,y,rel,type:'relationship'} }
function openBlockContext({block,event}){ relContextMenu.value={visible:true,x:event.clientX,y:event.clientY,rel:block,type:'block'} }
</script>


<style scoped>
.ia-page-root {
  display: flex;
  height: 100vh;
}
.ia-body {
  display: flex;
  width: 100%;
}
.ia-canvas {
  flex: 1;
  background: #fafafa;
  position: relative;
}

.edit-overlay {
  position: fixed;
  top: 0; left: 0; right: 0; bottom: 0;
  background: rgba(0, 0, 0, 0.3);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 1000;
}
.edit-box {
  background: #fff;
  padding: 20px;
  border-radius: 8px;
  box-shadow: 0 4px 12px rgba(0,0,0,0.2);
  min-width: 300px;
}
.edit-box input {
  width: 100%;
  padding: 8px;
  margin-bottom: 12px;
  font-size: 14px;
}
.edit-box .actions {
  display: flex;
  justify-content: flex-end;
  gap: 10px;
}
.custom-context-menu {
  position: fixed;
  z-index: 1000;
  min-width: 180px;
  background: #fff;
  border-radius: 11px;
  box-shadow: 0 4px 24px 0 #2230462a;
  border: 1.5px solid #e8eaf0;
  padding: 0 0 4px 0;
  animation: pop-in 0.12s cubic-bezier(.41,.84,.67,1.2);
  user-select: none;
}
@keyframes pop-in {
  0% { transform: scale(0.95); opacity: 0.5; }
  100% { transform: scale(1); opacity: 1; }
}
.close-btn {
  border: none;
  background: none;
  color: #bbb;
  font-size: 17px;
  font-weight: bold;
  float: right;
  margin: 6px 6px 0 0;
  cursor: pointer;
  transition: color 0.18s;
}
.close-btn:hover {
  color: #e74c3c;
}
.menu-content {
  padding: 12px 8px 4px 10px;
}
.menu-item {
  display: flex;
  align-items: center;
  font-size: 15px;
  border-radius: 7px;
  padding: 8px 6px 8px 7px;
  margin-bottom: 2px;
  color: #2c3e50;
  cursor: pointer;
  transition: background 0.15s, color 0.18s;
}
.menu-item .icon {
  font-size: 17px;
  margin-right: 8px;
}
.menu-item.danger {
  color: #e44e5c;
  font-weight: 500;
}
.menu-item.danger:hover {
  background: #ffe6e7;
  color: #d7263d;
}

/* 저장 알림 */
.save-notice { position:fixed; right:20px; bottom:20px; background:#323232;
  color:#fff; padding:8px 12px; border-radius:6px; font-size:13px;
  box-shadow:0 2px 12px rgba(0,0,0,0.2); }
.fade-enter-active, .fade-leave-active{ transition:opacity .3s; }
.fade-enter-from, .fade-leave-to{ opacity:0; }
</style>