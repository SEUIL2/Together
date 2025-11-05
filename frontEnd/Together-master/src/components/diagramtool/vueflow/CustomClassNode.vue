<template>
  <div class="class-node">
    <div class="header">
      <strong>{{ data.name }}</strong>
    </div>
    
    <div class="content attributes">
      <div v-for="attr in data.attributes" :key="attr">{{ attr }}</div>
    </div>
    
    <div class="content methods">
      <div v-for="method in data.methods" :key="method">{{ method }}()</div>
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
import { Handle, Position } from '@vue-flow/core'
import { defineProps } from 'vue'

// 노드 데이터 (name, attributes, methods)를 props로 받습니다.
const props = defineProps({
  data: {
    type: Object,
    required: true,
  },
})
</script>

<style>
.class-node {
  width: 200px;
  border: 1px solid #333;
  border-radius: 4px;
  background: #fefefe;
  font-family: monospace;
  font-size: 12px;
  box-shadow: 0 4px 6px rgba(0,0,0,0.1);
}

.class-node .header {
  padding: 8px;
  text-align: center;
  background: #eee;
  border-bottom: 1px solid #ccc;
  border-top-left-radius: 4px;
  border-top-right-radius: 4px;
}

.class-node .content {
  padding: 8px;
}

.class-node .attributes {
  border-bottom: 1px solid #ccc;
}

.class-node .methods {
  padding-bottom: 8px;
}
</style>