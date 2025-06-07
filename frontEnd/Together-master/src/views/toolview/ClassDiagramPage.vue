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
          />

          <RelationshipArrow
            v-for="rel in relationships"
            :key="rel.id"
            :from="getAnchorPosition(rel.from.boxId, rel.from.direction)"
            :to="getAnchorPosition(rel.to.boxId, rel.to.direction)"
            :fromType="rel.fromType"
            :toType="rel.toType"
            :lineStyle="rel.lineStyle"
            :rel="rel"
            :bendPoints="rel.bendPoints"
            @select="handleSelect"
            @add-bend="addBendPoint"
          />
        </v-layer>
      </v-stage>

      <RelationshipContextMenu
        v-if="arrowContextMenuVisible && selectedRelationship"
        :rel="selectedRelationship"
        :x="contextMenuX"
        :y="contextMenuY"
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

      <input
        v-if="editing.visible"
        class="overlay-editor"
        v-model="editing.value"
        :style="{ top: editing.y + 'px', left: editing.x + 'px' }"
        @blur="applyEdit"
        @keydown.enter="applyEdit"
      />

      <div v-if="showSavedMessage" class="save-toast">저장 완료✔️</div>
      <div v-if="saveError" class="error-toast">저장 실패 ⚠️</div>
    </div>
  </div>
</template>

<script setup>
import { ref, watch, onMounted } from 'vue'
import axios from 'axios'
import { debounce } from 'lodash'
import { useToolStore } from '@/stores/toolStore'
import ToolBox from '@/components/ToolBox.vue'
import ClassBox from '@/components/konva/ClassBox.vue'
import RelationshipArrow from '@/components/konva/RelationshipArrow.vue'
import RelationshipContextMenu from '@/components/konva/RelationshipContextMenu.vue'

const token = localStorage.getItem('authHeader')
if (token) {
  axios.defaults.headers.common['Authorization'] = token
}

const toolStore = useToolStore()
const stageWidth = window.innerWidth
const stageHeight = window.innerHeight

const stageRef = ref(null)
const scale = ref(1)
const stageX = ref(0)
const stageY = ref(0)

const classBoxes = ref([])
const relationships = ref([])
const relationshipStart = ref(null)

const selectedRelationship = ref(null)
const arrowContextMenuVisible = ref(false)
const contextMenuX = ref(0)
const contextMenuY = ref(0)

const editing = ref({ visible: false, value: '', x: 0, y: 0, boxId: null, type: '', index: null })
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

const updateBoxPosition = ({ id, x, y }) => {
  const box = classBoxes.value.find(b => b.id === id)
  if (box) { box.x = x; box.y = y }
}

const handleAnchorClick = (anchor) => {
  if (!relationshipStart.value) {
    relationshipStart.value = anchor
  } else {
    relationships.value.push({
      id: Date.now(), from: relationshipStart.value, to: anchor,
      type: 'association', fromType: 'none', toType: 'arrow', lineStyle: 'solid', bendPoints: []
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

const handleUpdate = (updated) => {
  const rel = selectedRelationship.value
  const index = relationships.value.findIndex(r => r.id === rel.id)
  if (index !== -1) {
    relationships.value[index] = { ...relationships.value[index], ...updated }
  }
}

const handleDelete = () => {
  const id = selectedRelationship.value?.id
  relationships.value = relationships.value.filter(r => r.id !== id)
  arrowContextMenuVisible.value = false
}

const handleBoxRightClick = ({ event, id }) => {
  event.evt.preventDefault()
  boxMenuX.value = event.evt.clientX - 180
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
  const width = box.width
  const attrH = box.attributes.length * 18
  const methodH = box.methods.length * 18
  const height = 50 + attrH + methodH

  switch (direction) {
    case 'top': return { x: box.x + width / 2, y: box.y }
    case 'bottom': return { x: box.x + width / 2, y: box.y + height }
    case 'left': return { x: box.x, y: box.y + height / 2 }
    case 'right': return { x: box.x + width, y: box.y + height / 2 }
    default: return { x: box.x, y: box.y }
  }
}

const startEditing = ({ boxId, type, index, x, y, value }) => {
  editing.value = { visible: true, boxId, type, index, x, y, value }
}

const applyEdit = () => {
  const { boxId, type, index, value } = editing.value
  const box = classBoxes.value.find(b => b.id === boxId)
  if (!box) return
  if (type === 'name') box.name = value
  else if (type === 'attribute') box.attributes[index] = value
  else if (type === 'method') box.methods[index] = value
  editing.value.visible = false
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

const saveToServer = async () => {
  const formData = new FormData()
  formData.append('type', 'classDiagram')
  formData.append('json', JSON.stringify({
    classes: classBoxes.value,
    relationships: relationships.value
  }))

  try {
    // 🔥 학생이면 projectId 전달하지 않음
    await axios.put('/design/update', formData, {
      headers: { 'Content-Type': 'multipart/form-data' }
    })

    showSavedMessage.value = true
    saveError.value = false
    setTimeout(() => showSavedMessage.value = false, 3000)
  } catch (err) {
    console.warn('❌ 자동 저장 실패:', err)
    saveError.value = true
    alert('⚠️ 저장 중 오류 발생')
  }
}


const autoSave = debounce(saveToServer, 1000)
watch([classBoxes, relationships], autoSave, { deep: true })

onMounted(async () => {
  const token = localStorage.getItem('authHeader')
  console.log('✅ authHeader:', token)

  if (token) {
    axios.defaults.headers.common['Authorization'] = token
  }

  try {
    const me = await axios.get('/auth/me')
    console.log('✅ /auth/me 응답:', me.data)

    // ✅ 클래스 다이어그램 불러오기
    const res = await axios.get('/design/all')
    const { classDiagram } = res.data

    if (classDiagram?.json) {
      const parsed = JSON.parse(classDiagram.json)
      classBoxes.value = parsed.classes || []
      relationships.value = parsed.relationships || []
      console.log('✅ 클래스 다이어그램 불러오기 성공:', parsed)
    } else {
      console.warn('⚠️ 저장된 클래스 다이어그램이 없습니다.')
    }

  } catch (err) {
    console.error('❌ 초기 데이터 로드 실패:', err)
  }
})

</script>


<style scoped>
.diagram-layout {
  display: flex;
  height: 100vh;
}
.diagram-page {
  flex: 1;
  overflow: hidden;
  position: relative;
}
.overlay-editor {
  position: absolute;
  width: 140px;
  height: 20px;
  font-size: 14px;
  border: 1px solid #ccc;
  padding: 2px 6px;
  z-index: 1000;
  outline: none;
  background: white;
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
</style>
