<template>
  <div class="export-page-container">
    <div class="export-content card">
      <div class="selection-area">
        <div class="global-actions">
          <h2 class="selection-title">📄 PDF 문서 추출</h2>
          <div>
            <button class="global-btn" @click="selectAll">전체 선택</button>
            <button class="global-btn deselect" @click="deselectAll">전체 해제</button>
          </div>
        </div>
        <div class="transfer-list-container">
          <!-- 왼쪽: 선택 가능한 항목 -->
          <div class="panel available-panel">
            <h3 class="panel-title">선택 가능 항목</h3>
            <div class="panel-content">
              <div v-for="category in categories" :key="`avail-${category.key}`" class="category-group">
                <h4 class="category-title" @click="selectAllInCategory(category.key)" title="해당 카테고리 전체 선택">
                  <span class="category-icon">
                    <svg xmlns="http://www.w3.org/2000/svg" width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M22 19a2 2 0 0 1-2 2H4a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h5l2 3h9a2 2 0 0 1 2 2z"></path></svg>
                  </span>
                  <span>{{ category.name }}</span>
                </h4>
                <ul class="item-list">
                  <li v-for="item in availableItemsFor(category.key)" :key="item.key" @click="selectItem(category.key, item.name)" class="item">
                    {{ item.name }}
                  </li>
                  <li v-if="availableItemsFor(category.key).length === 0 && category.items.length > 0" class="empty-item">모든 항목이 선택됨</li>
                  <li v-if="category.items.length === 0" class="empty-item">항목 없음</li>
                </ul>
              </div>
            </div>
          </div>

          <!-- 중앙 화살표 아이콘 -->
          <div class="transfer-actions">
            <svg xmlns="http://www.w3.org/2000/svg" width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><line x1="5" y1="12" x2="19" y2="12"></line><polyline points="12 5 19 12 12 19"></polyline></svg>
          </div>

          <!-- 오른쪽: 선택된 항목 -->
          <div class="panel selected-panel">
            <h3 class="panel-title">선택된 항목</h3>
            <div class="panel-content">
              <div v-for="category in categories" :key="`selected-${category.key}`" class="category-group">
                 <h4 class="category-title" @click="deselectAllInCategory(category.key)" title="해당 카테고리 전체 해제">
                   <span class="category-icon selected-icon">
                     <svg xmlns="http://www.w3.org/2000/svg" width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M22 19a2 2 0 0 1-2 2H4a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h5l2 3h9a2 2 0 0 1 2 2z"></path><line x1="9" y1="14" x2="15" y2="14"></line></svg>
                   </span>
                   <span>{{ category.name }}</span>
                 </h4>
                 <ul class="item-list selected-list">
                   <li v-for="item in selectedItemsFor(category.key)" :key="item.key" @click="deselectItem(category.key, item.name)" class="item selected">
                     {{ item.name }}
                   </li>
                   <li v-if="selectedItemsFor(category.key).length === 0" class="empty-item">선택된 항목 없음</li>
                 </ul>
              </div>
            </div>
          </div>
        </div>
      </div>

      <div class="actions-area">
        <button class="export-btn" @click="exportPdf" :disabled="isExportButtonDisabled || isLoading">
          <span v-if="isLoading">
            <svg class="spinner" viewBox="0 0 50 50">
              <circle class="path" cx="25" cy="25" r="20" fill="none" stroke-width="5"></circle>
            </svg>
            생성 중...
          </span>
          <span v-else>PDF로 추출하기</span>
        </button>
        <p v-if="isExportButtonDisabled" class="disabled-message">추출할 항목을 1개 이상 선택해주세요.</p>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, computed } from 'vue';
import { useRoute } from 'vue-router';
import api from '@/api';

const route = useRoute();
const isLoading = ref(false);

const categories = reactive([
  {
    name: '기획',
    key: '기획',
    items: [
      { name: '프로젝트 동기', key: 'motivation' },
      { name: '프로젝트 목표', key: 'goal' },
      { name: '요구사항 정의', key: 'requirement' },
      { name: '정보구조도', key: 'infostructure' },
      { name: '스토리보드', key: 'storyboard' },
    ],
  },
  {
    name: '설계',
    key: '설계',
    items: [
      { name: '유스케이스 다이어그램', key: 'usecase' },
      { name: '클래스 다이어그램', key: 'classDiagram' },
      { name: '시퀀스 다이어그램', key: 'sequence' },
      { name: 'UI 디자인', key: 'ui' },
      { name: 'ERD', key: 'erd' },
      { name: '테이블 명세', key: 'table' },
      { name: '시스템 아키텍쳐', key: 'architecture' },
    ],
  },
  {
    name: '개발',
    key: '개발',
    items: [
      { name: '개발 환경설정', key: 'env' },
      { name: '기능별 개발 순서', key: 'devOrder' },
      { name: '커밋 메세지 규칙', key: 'commitRule' },
      { name: '폴더 구조 및 파일 규칙', key: 'folder' },
    ],
  },
  {
    name: '테스트', key: '테스트', items: [{ name: '단위테스트', key: 'unit' }, { name: '통합테스트', key: 'integration' }],
  },
]);

const selectedItems = reactive({
  '기획': [],
  '설계': [],
  '개발': [],
  '테스트': [],
});

const availableItemsFor = (categoryKey) => {
  const category = categories.find(c => c.key === categoryKey);
  if (!category) return [];
  const selected = selectedItems[categoryKey];
  return category.items.filter(item => !selected.includes(item.name));
};

const selectedItemsFor = (categoryKey) => {
  const category = categories.find(c => c.key === categoryKey);
  if (!category) return [];
  const selected = selectedItems[categoryKey];
  // 순서 유지를 위해 category.items 기준으로 필터링
  return category.items.filter(item => selected.includes(item.name));
};

const selectItem = (categoryKey, itemName) => {
  if (!selectedItems[categoryKey].includes(itemName)) {
    selectedItems[categoryKey].push(itemName);
  }
};

const deselectItem = (categoryKey, itemName) => {
  const index = selectedItems[categoryKey].indexOf(itemName);
  if (index > -1) {
    selectedItems[categoryKey].splice(index, 1);
  }
};

const selectAllInCategory = (categoryKey) => {
  const available = availableItemsFor(categoryKey);
  available.forEach(item => selectItem(categoryKey, item.name));
};

const deselectAllInCategory = (categoryKey) => {
  selectedItems[categoryKey] = [];
};

const selectAll = () => {
  categories.forEach(category => {
    selectedItems[category.key] = category.items.map(item => item.name);
  });
};

const deselectAll = () => {
  for (const key in selectedItems) {
    selectedItems[key] = [];
  }
};

const isExportButtonDisabled = computed(() => {
  return Object.values(selectedItems).every(items => items.length === 0);
});

const exportPdf = async () => {
  if (isExportButtonDisabled.value) {
    alert('추출할 항목을 선택해주세요.');
    return;
  }
  isLoading.value = true;

  const projectId = route.params.projectId;
  const payload = {};
  for (const key in selectedItems) {
    if (selectedItems[key].length > 0) {
      payload[key] = selectedItems[key];
    }
  }

  try {

    const response = await api.post(`/export/pdf`, payload, {
      params: { projectId },
      headers: { Authorization: localStorage.getItem('authHeader') },
      withCredentials: true,
      responseType: 'blob',
    });

    const url = window.URL.createObjectURL(new Blob([response.data]));
    const link = document.createElement('a');
    link.href = url;

    // Extract filename from content-disposition header
    const contentDisposition = response.headers['content-disposition'];
    let filename = `project_${projectId}_export.pdf`;
    if (contentDisposition) {
      const filenameMatch = contentDisposition.match(/filename\*?=([^;]+)/);
      if (filenameMatch && filenameMatch[1]) {
        const encodedFilename = filenameMatch[1].replace(/['"]/g, '').replace('UTF-8', '');
        filename = decodeURIComponent(encodedFilename);
      }
    }
    
    link.setAttribute('download', filename);
    document.body.appendChild(link);
    link.click();
    link.remove();
    window.URL.revokeObjectURL(url);
  } catch (error) {
    console.error('PDF 추출 실패:', error);
    alert('PDF 문서를 생성하는 중 오류가 발생했습니다.');
  } finally {
    isLoading.value = false;
  }
};
</script>

<style scoped>
.export-page-container { padding: 40px; background-color: #f7f8fc; min-height: calc(100vh - 61px); }
.card { background: #fff; border-radius: 16px; padding: 32px; box-shadow: 0 8px 32px rgba(0,0,0,0.06); }
.global-actions {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24px;
}
.selection-title { font-size: 24px; font-weight: 700; color: #343a40; margin: 0; }
.global-btn {
  padding: 8px 16px;
  font-size: 14px;
  font-weight: 600;
  border-radius: 8px;
  border: none;
  background-color: #f1f3f5;
  color: #495057;
  cursor: pointer;
  margin-left: 8px;
  transition: all 0.2s;
}
.global-btn:hover { background-color: #e9ecef; }
.global-btn.deselect { background-color: #fff5f5; color: #e03131; }
.global-btn.deselect:hover { background-color: #ffe3e3; }

.transfer-list-container { display: grid; grid-template-columns: 1fr auto 1fr; gap: 24px; align-items: stretch; }
.transfer-actions { display: flex; flex-direction: column; justify-content: center; align-items: center; color: #ced4da; }

.panel { background-color: #fff; border: 1px solid #e9ecef; border-radius: 12px; display: flex; flex-direction: column; }
.panel-title { font-size: 16px; font-weight: 600; padding: 16px; margin: 0; color: #495057; border-bottom: 1px solid #e9ecef; }
.panel-content { padding: 0 16px 16px; overflow-y: auto; flex-grow: 1; min-height: 450px; }

.category-group { margin-bottom: 20px; }
.category-title {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 15px;
  font-weight: 600;
  margin-bottom: 12px;
  color: #343a40;
  padding: 8px;
  border-radius: 6px;
  cursor: pointer;
  transition: background-color 0.2s;
}
.category-title:hover { background-color: #f8f9fa; }
.category-icon { color: #868e96; transition: color 0.2s; }
.category-title:hover .category-icon { color: #3f8efc; }

.item-list { list-style: none; padding: 0; margin: 0; }
.item-list .item {
  padding: 10px 12px;
  border-radius: 6px;
  font-size: 15px;
  color: #495057;
  cursor: pointer;
  transition: background-color 0.2s, color 0.2s;
  margin-bottom: 6px;
  border: 1px solid #f1f3f5;
  background-color: #f8f9fa;
}
.available-panel .item:hover { background-color: #f1f3f5; }
.selected-list .item {
  background-color: #eef6ff;
  color: #3f8efc;
  font-weight: 500;
  border-color: #dbeafe;
}
.selected-list .item:hover { background-color: #dbeafe; border-color: #c4dbfd; }

.item-list .empty-item { color: #adb5bd; font-style: italic; padding: 10px 12px; font-size: 14px; text-align: center; }

.actions-area { margin-top: 32px; padding-top: 24px; border-top: 1px solid #e9ecef; text-align: center; }
.export-btn { background-color: #3f8efc; color: white; border: none; padding: 14px 28px; font-size: 16px; font-weight: 600; border-radius: 8px; cursor: pointer; transition: all 0.2s; min-width: 180px; display: inline-flex; align-items: center; justify-content: center; gap: 8px; }
.export-btn:hover:not(:disabled) { background-color: #3578e5; transform: translateY(-2px); box-shadow: 0 4px 12px rgba(0,0,0,0.1); }
.export-btn:disabled { background-color: #adb5bd; cursor: not-allowed; }
.disabled-message { color: #868e96; font-size: 14px; margin-top: 12px; }
.spinner { animation: rotate 2s linear infinite; width: 20px; height: 20px; }
.spinner .path { stroke: #fff; stroke-linecap: round; animation: dash 1.5s ease-in-out infinite; }
@keyframes rotate { 100% { transform: rotate(360deg); } }
@keyframes dash { 0% { stroke-dasharray: 1, 150; stroke-dashoffset: 0; } 50% { stroke-dasharray: 90, 150; stroke-dashoffset: -35; } 100% { stroke-dasharray: 90, 150; stroke-dashoffset: -124; } }
</style>