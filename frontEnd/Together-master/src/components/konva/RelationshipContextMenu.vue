<template>
  <div
    v-if="rel"
    class="context-menu"
    :style="{ top: y + 'px', left: x + 'px' }"
    @click.stop
  >
    <h4>관계선 설정</h4>

    <label>시작 도형</label>
    <select v-model="localRel.fromType">
      <option value="none">없음</option>
      <option value="arrow">화살표</option>
      <option value="triangle">삼각형</option>
      <option value="empty_diamond">빈 다이아몬드</option>
      <option value="filled_diamond">꽉찬 다이아몬드</option>
    </select>

    <div v-if="localRel.fromType === 'filled_diamond'">
      <label>시작 도형 채움 여부</label>
      <select v-model="localRel.fromFilled">
        <option :value="false">빈</option>
        <option :value="true">채움</option>
      </select>
    </div>

    <label>선 종류</label>
    <select v-model="localRel.lineStyle">
      <option value="solid">실선</option>
      <option value="dashed">점선</option>
    </select>

    <label>끝 도형</label>
    <select v-model="localRel.toType">
      <option value="none">없음</option>
      <option value="arrow">화살표</option>
      <option value="triangle">삼각형</option>
      <option value="empty_diamond">빈 다이아몬드</option>
      <option value="filled_diamond">꽉찬 다이아몬드</option>
    </select>

    <div v-if="localRel.toType === 'filled_diamond'">
      <label>끝 도형 채움 여부</label>
      <select v-model="localRel.toFilled">
        <option :value="false">빈</option>
        <option :value="true">채움</option>
      </select>
    </div>

    <button class="delete-btn" @click="$emit('delete')">🗑 삭제</button>
  </div>
</template>

<script setup>
import { reactive, watch, toRefs } from 'vue'

const props = defineProps({
  rel: Object,   // 부모로부터 받은 rel 객체
  x: Number,
  y: Number
})
const emit = defineEmits(['update', 'delete'])

// rel에서 필요한 프로퍼티만 뽑아 로컬 복사
const localRel = reactive({
  fromType: props.rel.fromType,
  toType:   props.rel.toType,
  fromFilled: props.rel.fromFilled ?? false,
  toFilled:   props.rel.toFilled   ?? false,
  lineStyle:  props.rel.lineStyle,
  bendStyle:  props.rel.bendStyle  ?? 'one'
})

// localRel이 변경될 때마다 부모로 전체 rel 업데이트 전송
watch(localRel, (newVal) => {
  emit('update', {
    ...props.rel,       // 기존 rel 데이터
    fromType:   newVal.fromType,
    toType:     newVal.toType,
    fromFilled: newVal.fromFilled,
    toFilled:   newVal.toFilled,
    lineStyle:  newVal.lineStyle,
    bendStyle:  newVal.bendStyle
  })
}, { deep: true })

</script>


<style scoped>
.context-menu {
  position: absolute;
  background: #ffffff;
  border: 1px solid #ccc;
  padding: 10px;
  border-radius: 6px;
  width: 200px;
  box-shadow: 0 2px 10px rgba(0,0,0,0.15);
  z-index: 999;
}

.context-menu h4 {
  margin: 0 0 10px;
  font-size: 14px;
  color: #2c3e50;
}

.context-menu label {
  font-weight: bold;
  font-size: 13px;
  display: block;
  margin: 6px 0 2px;
}

.context-menu select {
  width: 100%;
  padding: 4px;
  margin-bottom: 6px;
}

.delete-btn {
  background: #e74c3c;
  color: white;
  border: none;
  padding: 6px 12px;
  border-radius: 4px;
  cursor: pointer;
  width: 100%;
  margin-top: 6px;
}
</style>
