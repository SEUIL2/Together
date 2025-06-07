<template>
  <div class="erd-layout" @click="contextMenuVisible = false">
    <ToolBox />
    <div class="diagram" @dragover.prevent @drop="handleDrop">
      <v-stage :config="{ width: 2000, height: 2000 }">
        <v-layer>
          <ErdEntityBox
            v-for="box in boxes"
            :key="box.id"
            :config="box"
            @update-position="updatePosition"
            @anchor-click="handleAnchorClick"
          />
          <ErdRelationshipBent
            v-for="rel in relationships"
            :key="rel.id"
            :from="rel.from"
            :mid="rel.mid"
            :to="rel.to"
            :lineStyle="rel.lineStyle"
            :rel="rel"
            @open-context="openRelationshipContext"
          />
        </v-layer>
      </v-stage>
    </div>

    <!-- 관계선 우클릭 메뉴 -->
    <div
      v-if="contextMenuVisible"
      class="context-menu"
      :style="{ top: contextMenuY + 'px', left: contextMenuX + 'px' }"
    >
      <button @click="addMidPoint">중간점 지정</button>
      <button @click="deleteRelationship">삭제</button>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue';
import ErdEntityBox from '@/components/konva/ErdEntityBox.vue';
import ErdRelationshipBent from '@/components/konva/ErdRelationshipBent.vue';
import ToolBox from '@/components/ToolBox.vue';

const boxes = ref([]);
const relationships = ref([]);
const tempAnchor = ref(null);

let relationshipId = 1;

const handleDrop = (e) => {
  const newBox = {
    id: Date.now(),
    name: 'NewTable',
    x: e.offsetX,
    y: e.offsetY,
    fields: [{ name: 'id', type: 'PK', editing: false }]
  };
  boxes.value.push(newBox);
};

const updatePosition = ({ id, x, y }) => {
  const box = boxes.value.find(b => b.id === id);
  if (box) {
    box.x = x;
    box.y = y;
  }
};

const handleAnchorClick = ({ boxId, direction }) => {
  if (!tempAnchor.value) {
    tempAnchor.value = { boxId, direction };
    return;
  }

  const fromBox = boxes.value.find(b => b.id === tempAnchor.value.boxId);
  const toBox = boxes.value.find(b => b.id === boxId);

  if (!fromBox || !toBox) return;

  const from = getAnchorPos(fromBox, tempAnchor.value.direction);
  const to = getAnchorPos(toBox, direction);
  const mid = { x: (from.x + to.x) / 2, y: (from.y + to.y) / 2 };

  relationships.value.push({
    id: relationshipId++,
    from,
    to,
    mid,
    lineStyle: 'solid'
  });

  tempAnchor.value = null;
};

const getAnchorPos = (box, direction) => {
  const boxWidth = 180;
  const headerHeight = 30;
  const fieldHeight = 28;
  const fieldCount = box.fields.length;
  const totalHeight = headerHeight + fieldCount * fieldHeight;

  const cx = box.x + boxWidth / 2;
  const cy = box.y + totalHeight / 2;

  if (direction === 'top') return { x: cx, y: box.y };
  if (direction === 'bottom') return { x: cx, y: box.y + totalHeight };
  if (direction === 'left') return { x: box.x, y: cy };
  if (direction === 'right') return { x: box.x + boxWidth, y: cy };
};

const contextMenuVisible = ref(false);
const contextMenuX = ref(0);
const contextMenuY = ref(0);
const selectedRel = ref(null);

const openRelationshipContext = ({ rel, event }) => {
  event.preventDefault();
  selectedRel.value = rel;
  contextMenuX.value = event.clientX;
  contextMenuY.value = event.clientY;
  contextMenuVisible.value = true;
};

const addMidPoint = () => {
  const rel = selectedRel.value;
  if (!rel) return;

  const midpoint = {
    x: (rel.from.x + rel.to.x) / 2 + 30,
    y: (rel.from.y + rel.to.y) / 2 + 30
  };
  rel.mid = midpoint;
  contextMenuVisible.value = false;
};

const deleteRelationship = () => {
  relationships.value = relationships.value.filter(r => r.id !== selectedRel.value.id);
  contextMenuVisible.value = false;
};
</script>

<style scoped>
.erd-layout {
  display: flex;
    height: 100%;
}
.diagram-page {
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
  box-shadow: 0 2px 8px rgba(0,0,0,0.1);
}
</style>
