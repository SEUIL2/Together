<template>
  <div class="diagram-layout" @click="hideAllMenus" @wheel.prevent="handleWheel">
    <!-- 툴박스 -->
    <ToolBox/>

    <!-- 다이어그램 캔버스 -->
    <div class="diagram-canvas" ref="canvasRef" @dragover.prevent @drop="handleDrop">
      <v-stage
        ref="stageRef"
        :config="{
          width: stageConfig.width,
          height: stageConfig.height,
          scaleX: stageConfig.scale,
          scaleY: stageConfig.scale,
          x: stageConfig.x,
          y: stageConfig.y,
        }"
        @mousedown="handleStageMouseDown"
        @mousemove="handleStageMouseMove"
        @mouseup="handleStageMouseUp"
      >
        <v-layer ref="layerRef">
          <!-- 프래그먼트 (loop, alt, opt) -->
          <InteractionFragment
            v-for="fragment in fragments"
            :key="fragment.id"
            :config="fragment"
            @update="updateFragment"
            @contextmenu="onFragmentContextMenu"
            @dblclick="onNodeDblClick('fragment', fragment, $event)"
          />

          <!-- 생명선 (Lifeline) -->
          <LifelineNode
            v-for="lifeline in lifelines"
            :key="lifeline.id"
            :config="lifeline"
            :canvasHeight="lifeline.height"
            @update-position="updateLifelinePosition"
            @update-height="updateLifelineHeight"
            @contextmenu="onLifelineContextMenu"
            @dblclick="onNodeDblClick('lifeline', lifeline)"
          />

          <!-- 메시지 (Message) -->
          <MessageLink
            v-for="message in messages"
            :key="message.id"
            :config="message"
            :lifelines="lifelines"
            @contextmenu="onMessageContextMenu"
            @dblclick="onNodeDblClick('message', message, $event)"
          />

          <!-- 메시지 생성 중 임시선 -->
          <v-arrow
            v-if="tempLine.visible"
            :points="[tempLine.startX, tempLine.startY, tempLine.endX, tempLine.endY]"
            :pointerLength="10"
            :pointerWidth="8"
            stroke="#3f8efc"
            :strokeWidth="2"
            :dash="[4, 4]"
          />
        </v-layer>
      </v-stage>

      <!-- 컨텍스트 메뉴 -->
      <SequenceContextMenu
        v-if="contextMenu.visible"
        :x="contextMenu.x"
        :y="contextMenu.y"
        :target="contextMenu.target"
        @close="contextMenu.visible = false"
        @delete="deleteTarget"
        @update="updateTarget"
      />

      <!-- 이름 변경 모달 -->
      <div v-if="nameEdit.visible" class="edit-modal" :style="{ top: nameEdit.y + 'px', left: nameEdit.x + 'px' }">
        <input
          v-model="nameEdit.value"
          @keydown.enter="confirmEditName"
          @blur="confirmEditName"
          ref="nameEditInput"
          class="name-edit-input"
        />
      </div>
    </div>

    <!-- 저장 상태 토스트 -->
    <div v-if="saveStatus !== 'idle'" class="save-toast" :class="saveStatus">
      {{ saveStatus === 'saving' ? '저장 중...' : saveStatus === 'saved' ? '💾 저장 완료' : '저장 실패!' }}
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, onUnmounted, watch, nextTick, computed } from 'vue';
import { useRoute } from 'vue-router';
import api from '@/api';
import { debounce } from 'lodash';

import ToolBox from '@/components/diagramtool/ToolBox.vue';
import LifelineNode from '@/components/diagramtool/sequence/LifelineNode.vue';
import MessageLink from '@/components/diagramtool/sequence/MessageLink.vue';
import InteractionFragment from '@/components/diagramtool/sequence/InteractionFragment.vue';
import SequenceContextMenu from '@/components/diagramtool/sequence/SequenceContextMenu.vue';

const props = defineProps({ projectId: { type: [String, Number], required: true } });
const route = useRoute();

// --- 상태 (State) ---
const lifelines = ref([]);
const messages = ref([]);
const fragments = ref([]);
const stageRef = ref(null);
const layerRef = ref(null);
const canvasRef = ref(null);

const stageConfig = reactive({
  width: window.innerWidth - 210,
  height: window.innerHeight,
  scale: 1,
  x: 0,
  y: 0,
});


const minLifelineHeight = computed(() => {
  let maxY = 0;
  // 메시지들의 최대 y 위치 계산
  if (messages.value.length > 0) {
    maxY = Math.max(...messages.value.map(m => m.y));
  }
  // 프래그먼트들의 최대 y 위치 계산 (y + height)
  if (fragments.value.length > 0) {
    const maxFragmentY = Math.max(...fragments.value.map(f => f.y + f.height));
    maxY = Math.max(maxY, maxFragmentY);
  }
  // 기본 높이(stageConfig.height)와 계산된 최대 높이 중 더 큰 값을 사용하고, 추가 여백을 줍니다.
  return Math.max(stageConfig.height, maxY) + 200;
});

const updateLifelineHeight = (id, newHeight) => {
  const lifeline = lifelines.value.find(l => l.id === id);
  if (lifeline) {
    // 사용자가 드래그하여 조절한 높이를 반영하되,
    // 다이어그램 내용이 차지하는 최소 높이(minLifelineHeight)보다는 작아지지 않도록 보장합니다.
    lifeline.height = Math.max(minLifelineHeight.value, newHeight);
  }
};

const isSpacebarDown = ref(false);
const isPanning = ref(false);
const lastPanPoint = reactive({ x: 0, y: 0 });

const tempLine = reactive({ visible: false, startX: 0, startY: 0, endX: 0, endY: 0, fromLifelineId: null, fromLifelineNode: null });

// --- 다이어그램 요소 추가 (Drag & Drop) ---
const handleDrop = (e) => {
  const tool = JSON.parse(e.dataTransfer.getData('application/json'));
  const stage = stageRef.value.getStage();
  const pos = stage.getPointerPosition();

  if (tool.subtype === 'lifeline' || tool.subtype === 'actor') {
    lifelines.value.push({
      id: `${tool.subtype}_${Date.now()}`,
      type: tool.subtype,
      name: tool.subtype === 'actor' ? '액터' : '객체',
      x: pos.x,
      y: pos.y,
      height: 800, // 개별 높이 속성 추가
    });
  } else if (['loop', 'alt', 'opt'].includes(tool.subtype)) {
    fragments.value.push({
      id: `${tool.subtype}_${Date.now()}`,
      type: tool.subtype,
      name: tool.subtype,
      x: pos.x - 100,
      y: pos.y - 50,
      width: 300,
      height: 200,
    });
  }
};

watch(minLifelineHeight, (newMinHeight) => {
  // 다이어그램 요소가 추가/삭제되어 최소 높이가 변경될 때, 모든 생명선의 높이를 업데이트합니다.
  // 현재 높이가 최소 높이보다 작으면, 최소 높이로 강제 조정합니다.
  lifelines.value.forEach(lifeline => {
    if (lifeline.height < newMinHeight) {
      lifeline.height = newMinHeight;
    }
  });
});

// --- 생명선 및 프래그먼트 위치/크기 업데이트 ---
const updateLifelinePosition = (id, x, y) => { // y 파라미터 추가
  const lifeline = lifelines.value.find(l => l.id === id);
  if (lifeline) {
    lifeline.x = x;
    lifeline.y = y; // y 위치 업데이트
  }
};

const updateFragment = (config) => {
  const index = fragments.value.findIndex(f => f.id === config.id);
  if (index !== -1) fragments.value.splice(index, 1, config);
};

// --- 메시지 생성 로직 ---
const handleStageMouseDown = (e) => {
  // 스페이스바 패닝 로직
  if (isSpacebarDown.value) {
    isPanning.value = true;
    const stage = stageRef.value.getStage();
    stage.container().style.cursor = 'grabbing';
    lastPanPoint.x = e.evt.clientX;
    lastPanPoint.y = e.evt.clientY;
    // e.evt.preventDefault() is important to prevent text selection
    e.evt.preventDefault();
    return; // 다른 mousedown 로직 실행 방지
  }

  // 1. 왼쪽 마우스 버튼 클릭인지, 그리고 클릭된 대상이 Lifeline의 점선('lifeline-stem')인지 확인합니다.
  if (e.evt.button === 0 && e.target.className === 'Line' && e.target.attrs.name === 'lifeline-stem') {
    const stage = stageRef.value.getStage();
    const pos = stage.getPointerPosition(); // 캔버스 내부의 정확한 포인터 위치를 가져옵니다.
    const lifelineNode = e.target.getParent();
    const fromLifelineId = e.target.attrs.lifelineId;
    const fromLifeline = lifelines.value.find(l => l.id === fromLifelineId);

    if (!fromLifeline) return; // 안전 장치

    if (lifelineNode) {
      const draggableGroup = lifelineNode.findOne('Group');
      if (draggableGroup) draggableGroup.draggable(false);
      tempLine.fromLifelineNode = lifelineNode;
    }

    tempLine.fromLifelineId = fromLifelineId;
    tempLine.startX = fromLifeline.x;
    tempLine.startY = pos.y; // 캔버스 내부의 정확한 포인터 위치를 사용합니다.
    tempLine.endX = pos.x;   // 임시선은 마우스 위치를 따라다니도록 초기화합니다.
    tempLine.endY = pos.y;
    tempLine.visible = true;
  }
};

// 3. 마우스를 움직이면 임시선의 끝점을 계속 업데이트합니다.
const handleStageMouseMove = (e) => {
  // 스페이스바 패닝 로직 (수정)
  if (isPanning.value) {
    const stage = stageRef.value.getStage();
    const newPos = {
      x: e.evt.clientX - lastPanPoint.x,
      y: e.evt.clientY - lastPanPoint.y,
    };
    stage.x(stage.x() + newPos.x);
    stage.y(stage.y() + newPos.y);
    lastPanPoint.x = e.evt.clientX;
    lastPanPoint.y = e.evt.clientY;
    return; // 다른 mousemove 로직 실행 방지
  }

  if (!tempLine.visible) return;
  const stage = stageRef.value.getStage();
  const pos = stage.getPointerPosition();
  tempLine.endX = pos.x;
  tempLine.endY = pos.y;
};

// 4. 마우스 버튼을 놓으면 연결을 완료합니다.
const handleStageMouseUp = (e) => {
  // 스페이스바 패닝 로직
  if (isPanning.value) { 
    isPanning.value = false;
    const stage = stageRef.value.getStage();
    stage.container().style.cursor = isSpacebarDown.value ? 'grab' : 'default';
    stageConfig.x = stage.x(); // 패닝이 끝난 후 stageConfig에 최종 위치를 동기화합니다.
    stageConfig.y = stage.y();
  }

  if (!tempLine.visible) return;
  tempLine.visible = false;

  // 3번 문제 해결: 비활성화했던 객체 드래그를 다시 활성화
  if (tempLine.fromLifelineNode) {
    const draggableGroup = tempLine.fromLifelineNode.findOne('Group');
    if (draggableGroup) draggableGroup.draggable(true);
    tempLine.fromLifelineNode = null;
  }

  // 5. 마우스를 놓은 위치에 다른 Lifeline이 있는지 확인합니다.
  const toLifeline = lifelines.value.find(l => {
    const isOver = tempLine.endX >= l.x - 20 && tempLine.endX <= l.x + 20;
    return isOver;
  });

  if (toLifeline && tempLine.fromLifelineId) {
    // 6. 대상 Lifeline이 있다면, messages 배열에 새로운 메시지 객체를 추가합니다.
    messages.value.push({
      id: `msg_${Date.now()}`,
      from: tempLine.fromLifelineId,
      to: toLifeline.id,
      y: tempLine.startY, // 수정: 마우스를 처음 클릭한 지점의 Y좌표를 사용합니다.
      name: '메시지',
      type: 'sync', // 기본 동기 메시지,
      dash: [10, 5], // 점선 간격 기본값 추가
    });
    messages.value.sort((a, b) => a.y - b.y);
  }
  tempLine.fromLifelineId = null;

};

// --- 컨텍스트 메뉴 ---
const contextMenu = reactive({ visible: false, x: 0, y: 0, target: null });
const hideAllMenus = () => {
  contextMenu.visible = false;
  if (nameEdit.visible) confirmEditName();
};

const onLifelineContextMenu = (id, e) => {
  e.evt.preventDefault();
  contextMenu.target = { type: 'lifeline', id };
  contextMenu.x = e.evt.clientX;
  contextMenu.y = e.evt.clientY;
  contextMenu.visible = true;
};

const onMessageContextMenu = (config, e) => {
  e.evt.preventDefault();
  contextMenu.target = { type: 'message', id: config.id, currentType: config.type, currentDash: config.dash || [10, 5] };
  contextMenu.x = e.evt.clientX;
  contextMenu.y = e.evt.clientY;
  contextMenu.visible = true;
};

const onFragmentContextMenu = (id, e) => {
  e.evt.preventDefault();
  contextMenu.target = { type: 'fragment', id };
  contextMenu.x = e.evt.clientX;
  contextMenu.y = e.evt.clientY;
  contextMenu.visible = true;
};

const deleteTarget = (target) => {
  if (target.type === 'lifeline') {
    lifelines.value = lifelines.value.filter(l => l.id !== target.id);
    messages.value = messages.value.filter(m => m.from !== target.id && m.to !== target.id);
  } else if (target.type === 'message') {
    messages.value = messages.value.filter(m => m.id !== target.id);
  } else if (target.type === 'fragment') {
    fragments.value = fragments.value.filter(f => f.id !== target.id);
  }
  contextMenu.visible = false;
};

const updateTarget = (update) => {
  if (update.type === 'message') {
    const msg = messages.value.find(m => m.id === update.id);
    if (msg) {
      if (update.newType) msg.type = update.newType;
      if (update.newDash) msg.dash = update.newDash;
    }
  }
  contextMenu.visible = false;
};

// --- 인라인 이름 편집 ---
const nameEdit = reactive({ visible: false, type: '', id: null, value: '', x: 0, y: 0 });
const nameEditInput = ref(null);

const onNodeDblClick = (type, node, e) => {
  if (e && e.evt) {
    e.evt.stopPropagation(); // 이벤트 버블링을 막아 hideAllMenus가 실행되는 것을 방지합니다.
  }

  const stage = stageRef.value.getStage();
  let nodePos;
  if (type === 'lifeline') {
    nodePos = { x: node.x, y: node.y };
  } else if (type === 'message') {
    const fromNode = lifelines.value.find(l => l.id === node.from);
    const toNode = lifelines.value.find(l => l.id === node.to);
    if (!fromNode || !toNode) return;
    nodePos = { x: (fromNode.x + toNode.x) / 2, y: node.y - 20 };
  } else { // fragment
    nodePos = { x: node.x, y: node.y };
  }
  const screenPos = stage.getAbsoluteTransform().point(nodePos);

  nameEdit.visible = true;
  nameEdit.type = type;
  nameEdit.id = node.id;
  nameEdit.value = node.name;
  nameEdit.x = screenPos.x;
  nameEdit.y = screenPos.y;

  nextTick(() => nameEditInput.value?.focus());
};

const confirmEditName = () => {
  if (!nameEdit.visible) return;
  const val = nameEdit.value.trim();
  if (val) {
    let target;
    if (nameEdit.type === 'lifeline') target = lifelines.value.find(n => n.id === nameEdit.id);
    if (nameEdit.type === 'message') target = messages.value.find(n => n.id === nameEdit.id);
    if (nameEdit.type === 'fragment') target = fragments.value.find(n => n.id === nameEdit.id);
    if (target) target.name = val;
  }
  nameEdit.visible = false;
};

// --- 캔버스 제어 (줌, 패닝) ---
const handleWheel = (e) => {
  e.preventDefault();
  const stage = stageRef.value.getStage();
  const oldScale = stageConfig.scale;

  if (e.ctrlKey) { // Zoom
    const pointer = stage.getPointerPosition();
    const mousePointTo = {
      x: (pointer.x - stage.x()) / oldScale,
      y: (pointer.y - stage.y()) / oldScale,
    };
    const newScale = e.deltaY > 0 ? oldScale * 0.95 : oldScale * 1.05;
    stageConfig.scale = Math.max(0.2, Math.min(newScale, 3));
    stageConfig.x = pointer.x - mousePointTo.x * stageConfig.scale;
    stageConfig.y = pointer.y - mousePointTo.y * stageConfig.scale;
  } else if (e.shiftKey) { // Horizontal Pan
    stageConfig.x -= e.deltaY;
  } else { // Vertical Pan
    stageConfig.y -= e.deltaY;
  }
};

// --- 라이프사이클 훅 ---
const handleKeyDown = (e) => {
  if (e.code === 'Space' && !isSpacebarDown.value) {
    e.preventDefault();
    isSpacebarDown.value = true;
    const stage = stageRef.value?.getStage();
    if (stage) {
      stage.container().style.cursor = 'grab';
    }
  }
};

const handleKeyUp = (e) => {
  if (e.code === 'Space') {
    isSpacebarDown.value = false;
    // 패닝 중이 아니었다면 커서를 기본값으로 되돌림
    if (!isPanning.value) {
      const stage = stageRef.value?.getStage();
      if (stage) {
        stage.container().style.cursor = 'default';
      }
    }
  }
};

// --- 데이터 저장/불러오기 ---
const saveStatus = ref('idle');
const saveData = debounce(async () => {
  if (route.query.readonly === 'true') return;
  saveStatus.value = 'saving';
  try {
    const diagramData = {
      lifelines: lifelines.value,
      messages: messages.value,
      fragments: fragments.value,
    };
    const formData = new FormData();
    formData.append('type', 'sequence');
    formData.append('projectId', props.projectId);

    const jsonString = JSON.stringify(diagramData);
    formData.append('json', jsonString);
    formData.append('text', jsonString); // 💡 서버 API 호환성을 위해 text 필드 추가

    // 💡 수정: 다른 다이어그램과 동일하게 PUT 메서드를 사용합니다.
    await api.post('/design/upload', formData, {
      headers: { Authorization: localStorage.getItem('authHeader') },
      withCredentials: true,
    });
    saveStatus.value = 'saved';
  } catch (err) {
    console.error('시퀀스 다이어그램 저장 실패:', err);
    saveStatus.value = 'error';
  } finally {
    setTimeout(() => saveStatus.value = 'idle', 2000);
  }
}, 1500);

watch([lifelines, messages, fragments], saveData, { deep: true });

onMounted(async () => {
  window.addEventListener('keydown', handleKeyDown);
  window.addEventListener('keyup', handleKeyUp);

  try {
    const res = await api.get('/design/all', {
      params: { projectId: props.projectId },
      headers: { Authorization: localStorage.getItem('authHeader') },
      withCredentials: true,
    });
    const { sequence } = res.data;
    if (sequence?.json) { // 💡 수정: 'sequenceJson' 대신 'json' 필드를 사용합니다.
      const data = JSON.parse(sequence.json);
      lifelines.value = (data.lifelines || []).map(l => ({
        ...l, height: l.height || 800 // 이전 데이터 호환성을 위해 height 기본값 추가
      }));
      messages.value = data.messages || [];
      fragments.value = data.fragments || [];
    }
  } catch (err) {
    console.error('시퀀스 다이어그램 불러오기 실패:', err);
  }
});

onUnmounted(() => {
  window.removeEventListener('keydown', handleKeyDown);
  window.removeEventListener('keyup', handleKeyUp);
});

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
  overflow: hidden;
  position: relative;
}
.edit-modal {
  position: absolute;
  z-index: 100;
}
.name-edit-input {
  padding: 6px 8px;
  border: 1.5px solid #3f8efc;
  border-radius: 6px;
  background-color: #f0f8ff;
  font-size: 14px;
  outline: none;
  box-shadow: 0 2px 8px rgba(0,0,0,0.15);
}
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