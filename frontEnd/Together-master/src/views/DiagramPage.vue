<template>
  <div class="diagram-page">
    <Toolbar
      @add-class="handleAddClass"
      @export-json="exportToJson"
      @import-json="importFromJson"
      @save-diagram="saveDiagram"
    />
    <CanvasArea
      :classes="classes"
      :relationships="relationships"
      @update-position="updatePosition"
      @update-text="updateText"
      @add-item="addItem"
      @delete-class="deleteClass"
      @delete-relationship="deleteRelationship"
      @add-relationship="addRelationship"
      @update-relationship="updateRelationship"
    />
  </div>
</template>

<script setup>
import { ref, onMounted, watch } from 'vue';
import Toolbar from '@/components/classdiagram/Toolbar.vue';
import CanvasArea from '@/components/classdiagram/CanvasArea.vue';
import { loadClassDiagram, saveClassDiagram } from '@/utils/classDiagramApi';
import { debounce } from 'lodash';

const classes = ref([]);
const relationships = ref([]);

onMounted(async () => {
  try {
    const data = await loadClassDiagram();
    console.log('📥 서버 응답 데이터:', data);

    // ✅ 여기 수정
    if (data.classes && data.relationships) {
      classes.value = data.classes;
      relationships.value = data.relationships;
      console.log('✅ 다이어그램 불러오기 성공:', data);
    } else {
      console.log('⚠️ 불러올 JSON 데이터가 없음');
    }
  } catch (err) {
    console.warn('❌ 다이어그램 불러오기 실패:', err);
  }
});

async function saveDiagram() {
  console.log('💾 수동 저장 요청됨');
  await saveClassDiagram(classes.value, relationships.value);
}

const autoSave = debounce(() => {
  console.log('🌀 자동 저장 실행됨');
  console.log('🧩 저장할 클래스:', classes.value);
  console.log('🔗 저장할 관계선:', relationships.value);
  saveClassDiagram(classes.value, relationships.value);
}, 500);

watch([classes, relationships], autoSave, { deep: true });

function handleAddClass() {
  const id = 'class-' + Date.now();
  const newClass = {
    id,
    name: 'NewClass',
    x: 100,
    y: 100,
    attributes: [],
    methods: []
  };
  classes.value.push(newClass);
  console.log('➕ 클래스 추가됨:', newClass);
}

function exportToJson() {
  const data = {
    classes: classes.value,
    relationships: relationships.value
  };
  const blob = new Blob([JSON.stringify(data)], { type: 'application/json' });
  const url = URL.createObjectURL(blob);
  const link = document.createElement('a');
  link.href = url;
  link.download = 'diagram.json';
  link.click();
  URL.revokeObjectURL(url);
  console.log('📤 JSON 내보내기 완료:', data);
}

function importFromJson(e) {
  const file = e.target.files[0];
  if (!file) return;
  const reader = new FileReader();
  reader.onload = (event) => {
    const result = JSON.parse(event.target.result);
    classes.value = result.classes || [];
    relationships.value = result.relationships || [];
    console.log('📥 JSON 불러오기 완료:', result);
  };
  reader.readAsText(file);
}

function updatePosition({ id, x, y }) {
  const target = classes.value.find(c => c.id === id);
  if (target) {
    target.x = x;
    target.y = y;
    console.log(`📍 클래스 위치 변경: ${id} → (${x}, ${y})`);
  }
}

function updateText({ id, region, index, newText }) {
  const target = classes.value.find(c => c.id === id);
  if (!target) return;
  if (region === 'name') {
    console.log(`✏️ 클래스명 수정: ${target.name} → ${newText}`);
    target.name = newText;
  } else if (region === 'attributes') {
    console.log(`✏️ 속성 수정 [${index}]: ${target.attributes[index]} → ${newText}`);
    target.attributes[index] = newText;
  } else if (region === 'methods') {
    console.log(`✏️ 메서드 수정 [${index}]: ${target.methods[index]} → ${newText}`);
    target.methods[index] = newText;
  }
}

function addItem({ id, region }) {
  const target = classes.value.find(c => c.id === id);
  if (!target) return;
  if (region === 'attributes') {
    target.attributes.push('newAttribute');
    console.log(`➕ 속성 추가 (${id})`);
  } else if (region === 'methods') {
    target.methods.push('newMethod');
    console.log(`➕ 메서드 추가 (${id})`);
  }
}

function deleteClass(id) {
  console.log(`🗑️ 클래스 삭제: ${id}`);
  classes.value = classes.value.filter(c => c.id !== id);
  relationships.value = relationships.value.filter(r => r.fromId !== id && r.toId !== id);
}

function deleteRelationship(rel) {
  console.log(`🗑️ 관계선 삭제: ${rel.id}`);
  relationships.value = relationships.value.filter(r => r.id !== rel.id);
}

function addRelationship(rel) {
  const newRel = { ...rel, id: 'rel-' + Date.now() };
  relationships.value.push(newRel);
  console.log('🔗 관계선 추가됨:', newRel);
}

function updateRelationship(updatedRel) {
  const index = relationships.value.findIndex(r => r.id === updatedRel.id);
  if (index !== -1) {
    relationships.value.splice(index, 1, updatedRel);
    console.log('🔁 관계선 수정됨:', updatedRel);
  }
}
</script>

