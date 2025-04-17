<template>
  <div class="diagram-page">
    <Toolbar
      @add-class="handleAddClass"
      @export-json="exportToJson"
      @import-json="importFromJson"
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
import { ref } from 'vue';
import Toolbar from '@/components/classdiagram/Toolbar.vue';
import CanvasArea from '@/components/classdiagram/CanvasArea.vue';

const classes = ref([]);
const relationships = ref([]);

function handleAddClass() {
  const id = 'class-' + Date.now();
  classes.value.push({
    id,
    name: 'NewClass',
    x: 100,
    y: 100,
    attributes: [],
    methods: []
  });
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
}

function importFromJson(e) {
  const file = e.target.files[0];
  if (!file) return;
  const reader = new FileReader();
  reader.onload = (event) => {
    const result = JSON.parse(event.target.result);
    classes.value = result.classes || [];
    relationships.value = result.relationships || [];
  };
  reader.readAsText(file);
}

function updatePosition({ id, x, y }) {
  const target = classes.value.find(c => c.id === id);
  if (target) {
    target.x = x;
    target.y = y;
  }
}

function updateText({ id, region, index, newText }) {
  const target = classes.value.find(c => c.id === id);
  if (!target) return;
  if (region === 'name') target.name = newText;
  else if (region === 'attributes') target.attributes[index] = newText;
  else if (region === 'methods') target.methods[index] = newText;
}

function addItem({ id, region }) {
  const target = classes.value.find(c => c.id === id);
  if (!target) return;
  if (region === 'attributes') target.attributes.push('newAttribute');
  else if (region === 'methods') target.methods.push('newMethod');
}

function deleteClass(id) {
  classes.value = classes.value.filter(c => c.id !== id);
  relationships.value = relationships.value.filter(r => r.fromId !== id && r.toId !== id);
}

function deleteRelationship(rel) {
  relationships.value = relationships.value.filter(r => r.id !== rel.id);
}

function addRelationship(rel) {
  relationships.value.push({
    ...rel,
    id: 'rel-' + Date.now()
  });
}

function updateRelationship(updatedRel) {
  const index = relationships.value.findIndex(r => r.id === updatedRel.id);
  if (index !== -1) {
    relationships.value.splice(index, 1, updatedRel);
    console.log('[UPDATE] 관계선 반영됨:', updatedRel);
  }
}
</script>