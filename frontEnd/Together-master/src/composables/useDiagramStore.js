import { ref } from 'vue';

const classes = ref([]);
const relationships = ref([]);
const history = ref([]);
const historyIndex = ref(-1);

export function useDiagramStore() {
  function updateClassPosition(id, x, y) {
    const cls = classes.value.find(c => c.id === id);
    if (cls) {
      cls.x = x;
      cls.y = y;
    }
  }

  function updateClassText({ id, region, index, newText }) {
    const cls = classes.value.find(c => c.id === id);
    if (!cls) return;
    if (region === 'name') {
      cls.name = newText;
    } else if (region === 'attributes') {
      cls.attributes[index] = newText;
    } else if (region === 'methods') {
      cls.methods[index] = newText;
    }
  }

  function addItemToClass(id, region) {
    const cls = classes.value.find(c => c.id === id);
    if (!cls) return;
    if (region === 'attributes') {
      cls.attributes.push('- field: Type');
    } else if (region === 'methods') {
      cls.methods.push('+ method(): void');
    }
  }

  function deleteClass(id) {
    classes.value = classes.value.filter(c => c.id !== id);
    relationships.value = relationships.value.filter(r => r.fromId !== id && r.toId !== id);
  }

  function deleteRelationship(rel) {
    relationships.value = relationships.value.filter(r =>
      !(r.fromId === rel.fromId && r.toId === rel.toId && r.fromDirection === rel.fromDirection && r.toDirection === rel.toDirection)
    );
  }

  function saveHistory(clsCopy, relCopy) {
    const snapshot = {
      classes: JSON.parse(JSON.stringify(clsCopy)),
      relationships: JSON.parse(JSON.stringify(relCopy))
    };
    history.value = history.value.slice(0, historyIndex.value + 1);
    history.value.push(snapshot);
    historyIndex.value++;
  }

  function undo() {
    if (historyIndex.value > 0) {
      historyIndex.value--;
      restoreFromHistory();
    }
  }

  function redo() {
    if (historyIndex.value < history.value.length - 1) {
      historyIndex.value++;
      restoreFromHistory();
    }
  }

  function restoreFromHistory() {
    const snapshot = history.value[historyIndex.value];
    classes.value = JSON.parse(JSON.stringify(snapshot.classes));
    relationships.value = JSON.parse(JSON.stringify(snapshot.relationships));
  }

  function exportToJSON() {
    return JSON.stringify({ classes: classes.value, relationships: relationships.value }, null, 2);
  }

  function importFromJSON(json) {
    try {
      const data = JSON.parse(json);
      classes.value = data.classes || [];
      relationships.value = data.relationships || [];
    } catch (e) {
      alert('불러오기에 실패했습니다. JSON 형식을 확인해주세요.');
    }
  }

  function clearDiagram() {
    classes.value = [];
    relationships.value = [];
    history.value = [];
    historyIndex.value = -1;
  }

  return {
    classes,
    relationships,
    updateClassPosition,
    updateClassText,
    addItemToClass,
    deleteClass,
    deleteRelationship,
    saveHistory,
    undo,
    redo,
    exportToJSON,
    importFromJSON,
    clearDiagram
  };
}