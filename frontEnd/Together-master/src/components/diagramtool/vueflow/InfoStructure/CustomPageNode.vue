<template>
  <div class="page-node-wrapper">
    
    <Handle type="target" :position="Position.Top" id="t-t" class="handle top" />
    <Handle type="source" :position="Position.Top" id="t-s" class="handle top" />
    
    <Handle type="target" :position="Position.Bottom" id="b-t" class="handle bottom" />
    <Handle type="source" :position="Position.Bottom" id="b-s" class="handle bottom" />

    <Handle type="target" :position="Position.Left" id="l-t" class="handle left" />
    <Handle type="source" :position="Position.Left" id="l-s" class="handle left" />

    <Handle type="target" :position="Position.Right" id="r-t" class="handle right" />
    <Handle type="source" :position="Position.Right" id="r-s" class="handle right" />


    <div class="page-node-container">
      <div class="node-main-content">
        <div class="node-header" @dblclick.stop.prevent="startEditing":style="{ backgroundColor: data.headerColor || '#718096' }">
          <div v-if="!isEditing" class="node-label">
            {{ data.label || '페이지' }}
          </div>
          <input v-else
                 ref="inputRef"
                 v-model="data.label"
                 class="node-label-input" 
                 @blur="stopEditing" 
                 @keydown.enter="stopEditing" 
                 @keydown.stop 
                 @mousedown.stop />
        </div>
        <div class="node-list">
          <div class="list-item" v-for="(item, index) in data.items || []" :key="index">
            
            <input 
              v-if="editingItemIndex === index"
              ref="itemInputRef"
              v-model="props.data.items[index]" 
              class="item-text-input"
              @blur="stopEditItem"
              @keydown.enter="stopEditItem"
              @keydown.stop 
              @mousedown.stop
            />
            <span 
              v-else 
              class="item-text" 
              @dblclick.stop.prevent="startEditItem(index)"
            >
              {{ item }}
            </span>
            
            <button class="delete-item-btn" @click="deleteItem(index)">✕</button>
          </div>

          <button class="add-item-btn" @mousedown.stop @mouseup.stop @click.stop="addItem">
            + 항목 추가
          </button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
// <script> 영역은 변경사항 없습니다.
import { ref, nextTick } from 'vue'
import { Handle, Position } from '@vue-flow/core'

const props = defineProps({
  data: {
    type: Object,
    required: true,
    default: () => ({ label: '페이지', items: [] })
  },
})

const isEditing = ref(false)
const inputRef = ref(null)

const editingItemIndex = ref(null) 
const itemInputRef = ref(null)

const startEditing = () => {
  isEditing.value = true
  nextTick(() => {
    if (inputRef.value) {
      inputRef.value.focus()
      inputRef.value.select()
    }
  })
}

const stopEditing = () => {
  isEditing.value = false
  if (!props.data.label.trim()) {
    props.data.label = '페이지'
  }
}

const addItem = () => {
  if (!Array.isArray(props.data.items)) {
    props.data.items = [];
  }
  props.data.items.push('새 항목');
};

const deleteItem = (index) => {
  if (props.data.items) {
    props.data.items.splice(index, 1);
  }
};

const startEditItem = (index) => {
  editingItemIndex.value = index; 
  nextTick(() => {
    nextTick(() => {
       if (itemInputRef.value && itemInputRef.value.length > 0) {
        itemInputRef.value[0].focus();
        itemInputRef.value[0].select();
      } else if (itemInputRef.value) {
        itemInputRef.value.focus();
        itemInputRef.value.select();
      }
    });
  });
}

const stopEditItem = () => {
  if (editingItemIndex.value !== null && props.data.items) {
    if (!props.data.items[editingItemIndex.value]?.trim()) {
      props.data.items[editingItemIndex.value] = '내용 없음'; 
    }
  }
  editingItemIndex.value = null;
}
</script>

<style scoped>
/* <style> 영역의 핸들(.handle) 관련 부분이 수정되었습니다. */
.page-node-wrapper {
  position: relative;
}

.page-node-container {
  min-width: 250px;
  border-radius: 8px;
  overflow: hidden;
  box-shadow: 0 4px 12px rgba(0,0,0,0.08);
}

.node-main-content {
  position: relative; 
  border: 1px solid #e2e8f0;
  background: #ffffff;
  display: flex;
  flex-direction: column;
  border-radius: 8px;
  overflow: hidden;
}


.node-header {
  
  padding: 10px 15px;
  text-align: center;
  border-bottom: 1px solid #a0aec0;
}

.node-label {
  font-weight: 600;
  color: #ffffff;
  font-size: 16px;
}

.node-label-input {
  width: calc(100% - 20px);
  padding: 5px;
  font-size: 16px;
  font-weight: 600;
  text-align: center;
  
  background-color: transparent;
  color: #ffffff;
  border: none;
  border-bottom: 2px solid #ffffff;
  border-radius: 0;
  outline: none;
  box-shadow: none;
}


.node-list {
  display: flex;
  flex-direction: column;
}

.list-item {
  display: flex;
  align-items: center;
  padding: 10px 15px;
  border-bottom: 1px solid #e2e8f0;
  background: #ffffff;
  position: relative; /* [수정] 삭제 버튼의 기준점이 됩니다. */
}

.list-item:last-child {
  border-bottom: none;
}

.item-text {
  color: #2d3748;
  font-size: 15px;
  flex: 1; 
  word-break: break-all;
  cursor: text;
  text-align: center;
  padding-right: 25px
}

.item-text-input {
  flex: 1;
  padding-right: 25px;
  padding: 4px 6px;
  font-size: 15px;
  color: #2d3748;
  
  background-color: transparent;
  border: none;
  border-bottom: 2px solid #a0aec0; /* 밑줄 스타일 */
  border-radius: 0;
  outline: none;
  box-shadow: none;
}


/* === [수정됨] 핸들 스타일 === */
.handle {
  width: 12px;
  height: 12px;
  background: #cbd5e1;
  border: 2px solid white;
  border-radius: 50%;
  opacity: 0;
  transition: opacity 0.2s ease-in-out;
  box-shadow: 0 0 0 1.5px #718096;
  z-index: 10;
}

/* 4개의 위치 지정자만 사용하여
  같은 클래스(.top, .bottom 등)를 가진 핸들들이
  정확히 같은 위치에 겹치도록 합니다.
*/
.handle.top { 
  transform: translateY(-50%); 
}
.handle.bottom { 
  transform: translateY(50%); 
}
.handle.left { 
  transform: translateX(-50%); 
}
.handle.right { 
  transform: translateX(50%); 
}
/* ======================== */


.page-node-wrapper:hover .handle {
  opacity: 1;
}

.add-item-btn {
  width: 100%;
  padding: 8px;
  background-color: #f0f4f8;
  border: none;
  border-top: 1px solid #e2e8f0;
  color: #4a5568;
  font-weight: 600;
  cursor: pointer;
  transition: background-color 0.2s;
}
.add-item-btn:hover {
  background-color: #e2e8f0;
}

.delete-item-btn {
  background: none;
  border: none;
  color: #e53e3e;
  cursor: pointer;
  font-size: 1.2em;
  position: absolute;
  right: 10px; /* 오른쪽에서 10px */
  top: 50%;
  transform: translateY(-50%); /* 세로 중앙 정렬 */
  padding: 0 5px;
  opacity: 0;
  transition: opacity 0.2s ease-in-out;
}
.list-item:hover .delete-item-btn {
  opacity: 1;
}
</style>