<template>
  <div
    class="context-menu"
    :style="{
      top: (y -80) + 'px',
      left: (x -150) + 'px',
      position: 'absolute',
      zIndex: 99,
      background: '#fafdff',
      border: '1.5px solid #b2b7be',
      borderRadius: '14px',
      padding: '22px 18px 14px 18px',
      boxShadow: '0 6px 32px #0002',
      minWidth: '230px',
      fontFamily: 'Pretendard, Arial, sans-serif'
    }"
    @click.stop
  >
    <div class="dropdown-row">
      <div class="dropdown-label">시작점 <span class="dropdown-sub">(바깥)</span></div>
      <select class="symbol-select" v-model="fromOuter" @change="updateSymbol('fromOuter', fromOuter)">
        <option v-for="opt in symbolOptions" :key="'fo'+opt.value" :value="opt.value">
          {{ opt.label }}
        </option>
      </select>
    </div>
    <div class="dropdown-row">
      <div class="dropdown-label">시작점 <span class="dropdown-sub">(안쪽)</span></div>
      <select class="symbol-select" v-model="fromInner" @change="updateSymbol('fromInner', fromInner)">
        <option v-for="opt in symbolOptions" :key="'fi'+opt.value" :value="opt.value">
          {{ opt.label }}
        </option>
      </select>
    </div>
    <div class="dropdown-row">
      <div class="dropdown-label">끝점 <span class="dropdown-sub">(안쪽)</span></div>
      <select class="symbol-select" v-model="toInner" @change="updateSymbol('toInner', toInner)">
        <option v-for="opt in symbolOptions" :key="'ti'+opt.value" :value="opt.value">
          {{ opt.label }}
        </option>
      </select>
    </div>
    <div class="dropdown-row">
      <div class="dropdown-label">끝점 <span class="dropdown-sub">(바깥)</span></div>
      <select class="symbol-select" v-model="toOuter" @change="updateSymbol('toOuter', toOuter)">
        <option v-for="opt in symbolOptions" :key="'to'+opt.value" :value="opt.value">
          {{ opt.label }}
        </option>
      </select>
    </div>
    <div class="symbol-preview-row">
      <span class="preview-label">미리보기:</span>
      <img v-for="s in [fromOuter, fromInner, toInner, toOuter]" :key="s" :src="getSymbolImg(s)" height="22" style="margin-right:4px;" />
    </div>
    <div class="menu-bottom">
      <button class="delete-btn" @click="$emit('delete')">🗑️ 선 삭제</button>
      <button class="close-btn" @click="$emit('close')">닫기</button>
    </div>
  </div>
</template>

<script setup>
import { ref, watch } from 'vue'
import crowsfoot from '@/assets/erd-symbols/crowsfoot.svg'
import bar from '@/assets/erd-symbols/bar.svg'
import circle from '@/assets/erd-symbols/circle.svg'
import arrow from '@/assets/erd-symbols/arrow.svg'

const props = defineProps({
  x: Number,
  y: Number,
  initFromOuter: String,
  initFromInner: String,
  initToInner: String,
  initToOuter: String,
})
const emit = defineEmits(['update-symbol', 'close', 'delete'])

const symbolOptions = [
  { value: 'bar', label: 'Bar (1)', img: bar },
  { value: 'crowsfoot', label: "Crow's Foot (N)", img: crowsfoot },
  { value: 'circle', label: 'Circle (0)', img: circle },
  { value: 'arrow', label: 'Arrow', img: arrow },
]

const fromOuter = ref(props.initFromOuter)
const fromInner = ref(props.initFromInner)
const toInner = ref(props.initToInner)
const toOuter = ref(props.initToOuter)

// 변화시 부모에 알림
function updateSymbol(which, value) {
  emit('update-symbol', { which, value })
}
function getSymbolImg(sym) {
  return symbolOptions.find(opt => opt.value === sym)?.img
}

// prop 바뀌면 값 반영
watch(() => props.initFromOuter, v => fromOuter.value = v)
watch(() => props.initFromInner, v => fromInner.value = v)
watch(() => props.initToInner, v => toInner.value = v)
watch(() => props.initToOuter, v => toOuter.value = v)
</script>

<style scoped>
.dropdown-row {
  display: flex;
  align-items: center;
  margin-bottom: 12px;
}
.dropdown-label {
  width: 68px;
  font-size: 15px;
  color: #263746;
  font-weight: 500;
  letter-spacing: -0.5px;
}
.dropdown-sub {
  font-size: 12px;
  color: #888;
  font-weight: 400;
}
.symbol-select {
  flex: 1;
  font-size: 15px;
  border: 1px solid #b9bec7;
  border-radius: 6px;
  padding: 2px 6px;
  background: #f9fbff;
  margin-left: 8px;
  outline: none;
}
.symbol-preview-row {
  display: flex;
  align-items: center;
  margin: 16px 0 4px 0;
}
.preview-label {
  color: #5570ff;
  font-size: 13px;
  margin-right: 8px;
  font-weight: 500;
}
.menu-bottom {
  display: flex;
  justify-content: flex-end;
  align-items: center;
  gap: 10px;
  margin-top: 18px;
}
.close-btn, .delete-btn {
  border: none;
  border-radius: 6px;
  padding: 5px 14px;
  font-size: 15px;
  cursor: pointer;
  transition: background 0.16s;
}
.close-btn {
  background: #eeeeff;
  color: #333;
}
.delete-btn {
  background: #ffeaea;
  color: #ca3247;
  font-weight: 500;
}
.close-btn:hover {
  background: #d5dfff;
}
.delete-btn:hover {
  background: #ffd0d7;
}
</style>
