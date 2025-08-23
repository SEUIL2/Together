<template>
  <div class="canvas-container" ref="container" @contextmenu.prevent>
    <v-stage :config="stageConfig" @wheel="handleWheel">
      <v-layer>
        <ClassBox
          v-for="cls in classes"
          :key="cls.id"
          :cls="cls"
          :selected="selectedClassId === cls.id"
          :scale="scale"
          :selectedMethod="selectedMethod"
          :selectedAttribute="selectedAttribute"
          :editing="isEditing(cls.id)"
          @dragmove="updatePosition"
          @edit="startEdit"
          @add-item="addItem"
          @contextmenu="onRightClick"
          @box-selected="selectClass"
          @anchor-click="handleAnchorClick"
          @update-size="onUpdateClassSize"
          @select-method="onSelectMethod"
          @rightclick-method="onRightClickMethod"
          @select-attribute="onSelectAttribute"
          @rightclick-attribute="onRightClickAttribute"
        />
      </v-layer>

      <v-layer>
        <RelationshipArrow
          v-for="rel in relationships"
          :key="rel.id + '-' + rel.fromType + '-' + rel.toType + '-' + rel.lineType"
          :rel="rel"
          :classes="classes"
          :selected="rel.id === selectedRelationshipId"
          @select="selectRelationship"
          @open-menu="openRelationshipMenu"
          @add-mid-point="onAddMidPoint"
          @update-mid-point="onUpdateMidPoint"
          @delete-mid-point="onDeleteMidPoint"
          @mid-drag-end="onMidDragEnd"
        />
      </v-layer>
    </v-stage>

    <input
      v-if="editingClassId !== null"
      ref="textInput"
      class="text-input"
      v-model="editingText"
      :style="{ top: editingPos.y + 'px', left: editingPos.x + 'px' }"
      @blur="finishEdit"
      @keyup.enter="finishEdit"
    />

    <div
      v-if="contextMenuVisible"
      class="context-menu"
      :style="{ top: contextMenuPos.y + 'px', left: contextMenuPos.x + 'px' }"
      @click.stop
    >
      <button @click="deleteCurrentClass">삭제</button>
    </div>

    <div
      v-if="arrowContextMenuVisible"
      class="context-menu"
      :style="{ top: arrowContextMenuPos.y + 'px', left: arrowContextMenuPos.x + 'px' }"
      @click.stop
    >
      <label>  좌
        <select :value="selectedRelationship?.fromType || 'none'" @change="onDropdownChange('fromType', $event.target.value)">
          <option v-for="type in shapeTypes" :key="type.key" :value="type.key">
            {{ type.label }}
          </option>
        </select>
      </label>

      <label>  선
        <select :value="selectedRelationship?.lineType || 'solid'" @change="onDropdownChange('lineType', $event.target.value)">
          <option v-for="type in lineTypes" :key="type.key" :value="type.key">
            {{ type.label }}
          </option>
        </select>
      </label>

      <label>  우
        <select :value="selectedRelationship?.toType || 'none'" @change="onDropdownChange('toType', $event.target.value)">
          <option v-for="type in shapeTypes" :key="type.key" :value="type.key">
            {{ type.label }}
          </option>
        </select>
      </label>

      <button @click="deleteCurrentRelationship">삭제</button>
    </div>

    <div
      v-if="methodMenuVisible"
      class="context-menu"
      :style="{ top: methodMenuPos.y + 'px', left: methodMenuPos.x + 'px' }"
      @click.stop
    >
      <button @click="deleteSelectedMethod">메서드 삭제</button>
    </div>

    <div
      v-if="attrMenuVisible"
      class="context-menu"
      :style="{ top: attrMenuPos.y + 'px', left: attrMenuPos.x + 'px' }"
      @click.stop
    >
      <button @click="deleteSelectedAttribute">속성 삭제</button>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, nextTick, onMounted, onBeforeUnmount } from 'vue';
import ClassBox from './ClassBox.vue';
import RelationshipArrow from './RelationshipArrow.vue';

const props = defineProps({
  classes: Array,
  relationships: Array
});

const emit = defineEmits([
  'update-position',
  'update-text',
  'add-item',
  'delete-class',
  'delete-relationship',
  'add-relationship',
  'update-relationship'
]);

const shapeTypes = [
  { key: 'triangle', label: '삼각형' },
  { key: 'arrow', label: '화살표' },
  { key: 'empty_diamond', label: '빈 다이아몬드' },
  { key: 'filled_diamond', label: '꽉찬 다이아몬드' },
  { key: 'none', label: '없음' }
];

const lineTypes = [
  { key: 'dashed', label: '점선' },
  { key: 'solid', label: '실선' }
];

const stageWidth = 2000;
const stageHeight = 2000;
const scale = ref({ x: 1, y: 1 });
const stageConfig = computed(() => ({ width: stageWidth, height: stageHeight, scale: scale.value }));

const editingClassId = ref(null);
const editingIndex = ref(null);
const editingRegion = ref(null);
const editingText = ref('');
const editingPos = ref({ x: 0, y: 0 });

const contextMenuVisible = ref(false);
const contextMenuPos = ref({ x: 0, y: 0 });
const contextClassId = ref(null);

const arrowContextMenuVisible = ref(false);
const arrowContextMenuPos = ref({ x: 0, y: 0 });
const selectedRelationship = ref(null);
const selectedRelationshipId = ref(null);

const selectedClassId = ref(null);
const anchorStartClassId = ref(null);
const anchorStartDirection = ref(null);

const selectedMethod = ref({ clsId: null, index: null });
const methodMenuVisible = ref(false);
const methodMenuPos = ref({ x: 0, y: 0 });

const selectedAttribute = ref({ clsId: null, index: null });
const attrMenuVisible = ref(false);
const attrMenuPos = ref({ x: 0, y: 0 });

function handleClickOutside() {
  contextMenuVisible.value = false;
  arrowContextMenuVisible.value = false;
  methodMenuVisible.value = false;
  attrMenuVisible.value = false;
}

onMounted(() => document.addEventListener('click', handleClickOutside));
onBeforeUnmount(() => document.removeEventListener('click', handleClickOutside));

function onDropdownChange(type, value) {
  if (!selectedRelationship.value) return;
  const updatedRel = { ...selectedRelationship.value };
  if (type === 'fromType') updatedRel.fromType = value;
  else if (type === 'toType') updatedRel.toType = value;
  else if (type === 'lineType') updatedRel.lineType = value;
  emit('update-relationship', updatedRel);
  selectedRelationship.value = updatedRel;
}

function updatePosition(payload) { emit('update-position', payload); }
function startEdit({ cls, region, index, event }) {
  event?.evt?.stopPropagation();
  editingClassId.value = cls.id;
  editingRegion.value = region;
  editingIndex.value = index;
  editingText.value = region === 'name' ? cls.name : region === 'attributes' ? cls.attributes[index] : cls.methods[index];
  editingPos.value = { x: cls.x + 10, y: cls.y + 10 };
  nextTick(() => document.querySelector('.text-input')?.focus());
}
function finishEdit() {
  emit('update-text', { id: editingClassId.value, region: editingRegion.value, index: editingIndex.value, newText: editingText.value });
  editingClassId.value = null;
}
function addItem(region, id) { emit('add-item', { id, region }); }
function onRightClick(e, cls) {
  e.evt.preventDefault();
  contextMenuPos.value = { x: e.evt.clientX + 2, y: e.evt.clientY + 8 };
  contextClassId.value = cls.id;
  contextMenuVisible.value = true;
}
function selectClass(cls) { selectedClassId.value = cls.id; }
function handleAnchorClick({ id, direction }) {
  if (!anchorStartClassId.value) {
    anchorStartClassId.value = id;
    anchorStartDirection.value = direction;
  } else {
    if (anchorStartClassId.value !== id) {
      emit('add-relationship', { fromId: anchorStartClassId.value, fromDirection: anchorStartDirection.value, toId: id, toDirection: direction, fromType: 'none', toType: 'none', lineType: 'solid' });
    }
    anchorStartClassId.value = null;
    anchorStartDirection.value = null;
  }
}
function selectRelationship({ rel, event }) {
  selectedRelationship.value = rel;
  selectedRelationshipId.value = rel.id;
  arrowContextMenuPos.value = { x: event.evt.clientX + 2, y: event.evt.clientY + 8 };
  arrowContextMenuVisible.value = true;
}
function openRelationshipMenu({ rel, event }) { selectRelationship({ rel, event }); }
function deleteCurrentClass() { emit('delete-class', contextClassId.value); handleClickOutside(); }
function deleteCurrentRelationship() { emit('delete-relationship', selectedRelationship.value); handleClickOutside(); }

// 🆕 중간점 이벤트 핸들러
function onAddMidPoint({ rel, x, y }) {
  const mps = [...(rel.midPoints || []), { x, y }];
  emit('update-relationship', { ...rel, midPoints: mps });
}
function onUpdateMidPoint({ rel, idx, x, y }) {
  const mps = [...(rel.midPoints || [])];
  if (mps[idx]) mps[idx] = { x, y };
  emit('update-relationship', { ...rel, midPoints: mps });
}
function onDeleteMidPoint({ rel, idx }) {
  const mps = [...(rel.midPoints || [])];
  mps.splice(idx, 1);
  emit('update-relationship', { ...rel, midPoints: mps });
}
function onMidDragEnd(rel) {
  // 필요 시 추가 동기화 로직
}

function deleteSelectedMethod() { const cls = props.classes.find(c => c.id === selectedMethod.value.clsId); if (cls && selectedMethod.value.index != null) cls.methods.splice(selectedMethod.value.index, 1); handleClickOutside(); }
function deleteSelectedAttribute() { const cls = props.classes.find(c => c.id === selectedAttribute.value.clsId); if (cls && selectedAttribute.value.index != null) cls.attributes.splice(selectedAttribute.value.index, 1); handleClickOutside(); }
function onSelectMethod({ clsId, index }) { selectedMethod.value = { clsId, index }; метод_menuVisible.value = false; }
function onRightClickMethod({ clsId, index, event }) { selectedMethod.value = { clsId, index }; const pos = event.target.getStage().getPointerPosition() || { x:0,y:0 }; methodMenuPos.value = { x: pos.x + 10, y: pos.y + 5 }; methodMenuVisible.value = true; }
function onSelectAttribute({ clsId, index }) { selectedAttribute.value = { clsId, index }; attrMenuVisible.value = false; }
function onRightClickAttribute({ clsId, index, event }) { selectedAttribute.value = { clsId, index }; const pos = event.target.getStage().getPointerPosition() || { x:0,y:0 }; attrMenuPos.value = { x: pos.x + 10, y: pos.y + 5 }; attrMenuVisible.value = true; }
function handleWheel(e) { e.evt.preventDefault(); const old = scale.value.x; const factor = e.evt.deltaY > 0 ? 0.9 : 1.1; scale.value = { x: old * factor, y: old * factor }; }
function onUpdateClassSize({ id, width, height }) { const cls = props.classes.find(c => c.id === id); if (cls) { cls.width = width; cls.height = height; } }
function isEditing(id) { return editingClassId.value === id; }
</script>

<style scoped>
.canvas-container { background-color: #eee; width: 100%; height: 100%; position: relative; }
.text-input { position: absolute; font-size: 16px; border: 1px solid #333; padding: 4px; z-index: 10; }
.context-menu { position: absolute; background-color: #fff; border: 1px solid #333; padding: 4px; z-index: 9999; }
.context-menu button { display: block; width: 120px; padding: 5px; cursor: pointer; background-color: #1976d2; color: #fff; border: none; text-align: left; margin-bottom: 2px; }
.context-menu button:last-child { background-color: #f44336; }
.context-menu button:hover { opacity: 0.85; }
</style>