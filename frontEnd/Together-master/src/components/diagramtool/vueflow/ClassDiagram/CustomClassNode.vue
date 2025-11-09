<template>
  <div class="class-node">
    <div class="header" @dblclick="startEditing('name')">
      <div v-if="data.stereotype" class="stereotype" data-type="name">
        &lt;&lt;{{ data.stereotype }}&gt;&gt;
      </div>
      <!-- 이름 편집 -->
      <strong v-if="!editingState.name" class="class-name" data-type="name">{{ data.name }}</strong>
      <input
        v-else
        ref="nameInputRef"
        v-model="data.name"
        class="edit-input header-input"
        @blur="stopEditing('name')"
        @keydown.enter="stopEditing('name')"
        @keydown.stop
      />
    </div>

    <div class="content attributes">
      <!-- 속성 목록 -->
      <div 
        v-for="(attr, index) in data.attributes || []" 
        :key="index" 
        class="attribute-item"
        @dblclick="startEditing('attribute', index)"
      >
        <span v-if="editingState.attributeIndex !== index">{{ attr }}</span>
        <input
          v-else
          :ref="el => itemInputRefs[index] = el"
          v-model="data.attributes[index]"
          class="edit-input"
          @blur="stopEditing('attribute')"
          @keydown.enter="stopEditing('attribute')"
          @keydown.stop
        />
        <button class="delete-item-btn" @click="deleteItem('attribute', index)">✕</button>
      </div>
      <div v-if="!data.attributes || data.attributes.length === 0" class="empty-placeholder"></div>
      
      <button class="add-btn" data-type="add-attribute" title="속성 추가">+</button>
    </div>

    <div class="content methods">
      <!-- 메서드 목록 -->
      <div 
        v-for="(method, index) in data.methods || []" 
        :key="index" 
        class="method-item"
        @dblclick="startEditing('method', index)"
      >
        <span v-if="editingState.methodIndex !== index">{{ method }}</span>
        <input
          v-else
          :ref="el => itemInputRefs[index] = el"
          v-model="data.methods[index]"
          class="edit-input"
          @blur="stopEditing('method')"
          @keydown.enter="stopEditing('method')"
          @keydown.stop
        />
        <button class="delete-item-btn" @click="deleteItem('method', index)">✕</button>
      </div>
      <div v-if="!data.methods || data.methods.length === 0" class="empty-placeholder"></div>

      <button class="add-btn" data-type="add-method" title="메서드 추가">+</button>
    </div>

    <Handle id="left" type="source" :position="Position.Left" />
    <Handle id="left" type="target" :position="Position.Left" />
    <Handle id="right" type="source" :position="Position.Right" />
    <Handle id="right" type="target" :position="Position.Right" />
    <Handle id="top" type="source" :position="Position.Top" />
    <Handle id="top" type="target" :position="Position.Top" />
    <Handle id="bottom" type="source" :position="Position.Bottom" />
    <Handle id="bottom" type="target" :position="Position.Bottom" />
  </div>
</template>

<script setup>
import { Handle, Position } from '@vue-flow/core';
import { defineProps, defineEmits, ref, reactive, nextTick } from 'vue'

const emit = defineEmits([]); // [수정] 더 이상 add-item 이벤트를 사용하지 않음
const props = defineProps({
  data: {
    type: Object,
    required: true,
    default: () => ({
      stereotype: '',
      name: 'ClassName',
      attributes: [],
      methods: []
    })
  },
})

const editingState = reactive({
  name: false,
  attributeIndex: null,
  methodIndex: null,
});

const nameInputRef = ref(null);
const itemInputRefs = ref([]);

function startEditing(type, index = null) {
  if (type === 'name') {
    editingState.name = true;
    nextTick(() => nameInputRef.value?.focus());
  } else if (type === 'attribute') {
    editingState.attributeIndex = index;
    nextTick(() => itemInputRefs.value[index]?.focus());
  } else if (type === 'method') {
    editingState.methodIndex = index;
    nextTick(() => itemInputRefs.value[index]?.focus());
  }
}

function stopEditing(type) {
  if (type === 'name') {
    editingState.name = false;
  } else if (type === 'attribute') {
    editingState.attributeIndex = null;
  } else if (type === 'method') {
    editingState.methodIndex = null;
  }
}

function deleteItem(type, index) {
  if (type === 'attribute' && props.data.attributes) {
    props.data.attributes.splice(index, 1);
  } else if (type === 'method' && props.data.methods) {
    props.data.methods.splice(index, 1);
  }
  // 부모에게 변경사항을 알릴 필요 없이,
  // Vue의 반응성 시스템이 props 객체 내부 배열의 변경을 감지하고 화면을 업데이트합니다.
}

</script>

<style scoped>
.class-node {
  min-width: 180px;
  border: 1px solid #000;
  background: #ffffff;
  font-family: Arial, sans-serif;
  font-size: 13px;
  box-shadow: none;
  text-align: left;
}

.class-node .header {
  padding: 8px 12px;
  text-align: center;
  background: #fdfdfd;
  border-bottom: 1px solid #000;
  cursor: grab; /* 헤더를 잡고 드래그 */
}

.class-node .stereotype {
  font-size: 11px;
  color: #555;
  margin-bottom: 2px;
  font-style: italic;
  pointer-events: none; /* 클릭 이벤트가 부모로 전달되도록 */
}

.class-node .class-name {
  font-size: 14px;
  color: #000;
  font-weight: bold;
  pointer-events: none; /* 클릭 이벤트가 부모로 전달되도록 */
}

.class-node .content {
  padding: 5px 12px;
  min-height: 0; /* [수정] 최소 높이를 0으로 변경 */
  display: flex;
  flex-direction: column;
  justify-content: center;
  position: relative; /* 추가 버튼 위치 기준 */
  padding-bottom: 22px; /* [수정] 추가 버튼을 위한 공간 조정 */
}

.class-node .attributes {
  border-bottom: 1px solid #000;
}

.attribute-item,
.method-item {
  font-size: 13px;
  color: #333;
  padding: 2px 20px 2px 0; /* [수정] 삭제 버튼을 위한 오른쪽 여백 추가 */
  cursor: text;
  position: relative; /* 삭제 버튼 위치 기준 */
}

.edit-input {
  width: 100%;
  padding: 1px 2px;
  border: 1px solid transparent;
  border-radius: 3px;
  font-family: inherit;
  font-size: inherit;
  color: inherit;
  background-color: transparent;
  box-sizing: border-box;
}
.edit-input:focus {
  border: 1px solid #3b82f6; /* 포커스 시에만 테두리 표시 */
  background-color: transparent; /* [수정] 포커스 시에도 배경을 투명하게 유지 */
}
.header-input {
  text-align: center;
}
.empty-placeholder {
  height: 0; /* [수정] 항목이 없을 때 차지하는 높이를 0으로 변경 */
}

/* [추가] '추가' 버튼 스타일 */
.add-btn {
  position: absolute;
  bottom: 2px;
  right: 5px;
  width: 20px;
  height: 20px;
  border-radius: 50%;
  border: 1px solid #888;
  background: #f9f9f9;
  color: #555;
  font-weight: bold;
  font-size: 14px;
  line-height: 18px;
  text-align: center;
  cursor: pointer;
  z-index: 10; /* 다른 요소들 위에 버튼이 오도록 z-index 추가 */
  transition: all 0.1s ease;
}
.add-btn:hover {
  background: #e0e0e0;
  border-color: #000;
  color: #000;
}

.delete-item-btn {
  position: absolute;
  right: 0; /* [수정] 오른쪽 끝에 위치하도록 조정 */
  top: 50%;
  transform: translateY(-50%);
  background: none;
  border: none;
  color: #e53e3e;
  cursor: pointer;
  font-size: 1.1em;
  line-height: 1;
  padding: 2px 4px;
  border-radius: 4px;
  opacity: 0;
  transition: opacity 0.2s ease-in-out;
}
.attribute-item:hover .delete-item-btn,
.method-item:hover .delete-item-btn {
  opacity: 1;
}

/* Vue Flow 핸들 스타일 수정 */
.vue-flow__handle {
  width: 10px;
  height: 10px;
  background: #2c3e50;
  border: 1px solid #fff;
  opacity: 0; /* 기본적으로 핸들을 숨김 */
  transition: opacity 0.2s ease-in-out; /* 부드러운 효과 */
}
.class-node:hover .vue-flow__handle {
  opacity: 1; /* 노드에 마우스를 올렸을 때 핸들을 표시 */
}
</style>