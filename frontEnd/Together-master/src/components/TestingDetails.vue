<template>
  <div 
      class="test-table-container"
      @contextmenu.prevent="handleRightClick"
      style="position: relative"
  >
    <div class="nav-buttons">
      <button
          v-for="(tab, idx) in testTabs"
          :key="tab.type"
          :class="['nav-btn', { active: selectedIndex === idx }]"
          @click="selectTab(idx)"
      >
        {{ tab.name }}
      </button>
    </div>

    <div class="table-header">
      <div class="title-area">
        <!-- <h2>{{ currentTab.name }}</h2>
        <p class="table-subtitle">{{ currentConfig.subtitle }}</p> -->
      </div>
    </div>

    <div class="master-detail-layout">
      <!-- Master Pane (Left) -->
      <div class="master-pane">
        <div class="master-pane-header">
          <div class="filter-controls">
            <input type="text" v-model="searchQuery" placeholder="테스트 ID로 검색..." class="search-input" />
            <div class="status-filter">
              <button :class="{ active: statusFilter === 'all' }" @click="statusFilter = 'all'">전체</button>
              <button :class="{ active: statusFilter === 'in-progress' }" @click="statusFilter = 'in-progress'">진행 중</button>
              <button :class="{ active: statusFilter === 'completed' }" @click="statusFilter = 'completed'">완료</button>
            </div>
          </div>
          <button
              v-if="!isReadOnly"
              class="add-row-btn"
              :disabled="currentTab.loading"
              @click="addRow(currentTab.type)"
          >
            + 새 테스트 추가
          </button>
        </div>

        <div v-if="currentTab.loading" class="state-cell">
          데이터를 불러오는 중입니다...
        </div>
        <div v-else-if="!filteredRows.length" class="state-cell">
          아직 등록된 테스트가 없습니다.
        </div>
        <ul v-else class="master-list">
          <li
              v-for="row in filteredRows"
              :key="row.id"
              :class="{ active: selectedRowId === row.id }"
              class="master-item"
              @click="selectedRowId = row.id"
          >
            <div class="master-item-content">
              <span class="master-item-id">{{ row.testId || 'ID 없음' }}</span>
              <p class="master-item-desc">{{ row.caseDesc || row.scenario || '설명 없음' }}</p>
            </div>
            <div class="master-item-status">
              <span :class="['status-chip', row.completed ? 'status-done' : 'status-progress']">
                {{ row.completed ? '완료' : '진행 중' }}
              </span>
              <button
                  v-if="!isReadOnly"
                  class="delete-btn"
                  title="삭제"
                  @click.stop="deleteRow(currentTab.type, row.id)"
              >
                🗑️
              </button>
            </div>
          </li>
        </ul>
      </div>

      <!-- Detail Pane (Right) -->
      <div class="detail-pane">
        <div v-if="!selectedRow" class="detail-empty-state">
          <div class="empty-inner">
            <span class="empty-icon">←</span>
            <p>왼쪽 목록에서<br/>테스트를 선택하세요.</p>
          </div>
        </div>
        <div v-else class="detail-form">
          <div
              v-for="field in currentConfig.fields"
              :key="field.key"
              class="form-group"
          >
            <div class="form-label-wrapper">
              <label :for="`field-${field.key}`">{{ field.label }}</label>
              <InfoTooltip v-if="field.description" :text="field.description" />
            </div>
            <div class="form-control">
              <template v-if="field.type === 'input'">
                <input
                    :id="`field-${field.key}`"
                    v-model="selectedRow[field.key]"
                    type="text"
                    :placeholder="field.placeholder || ''"
                    :disabled="isReadOnly"
                    @blur="saveRow(currentTab.type, selectedRow)"
                />
              </template>
              <template v-else-if="field.type === 'textarea'">
                <textarea
                    :id="`field-${field.key}`"
                    v-model="selectedRow[field.key]"
                    :rows="field.rows || 3"
                    :placeholder="field.placeholder || ''"
                    :disabled="isReadOnly"
                    @blur="saveRow(currentTab.type, selectedRow)"
                ></textarea>
              </template>
              <template v-else-if="field.type === 'checkbox'">
                <div class="checkbox-wrapper">
                  <input
                      :id="`field-${field.key}`"
                      v-model="selectedRow.completed"
                      type="checkbox"
                      :disabled="isReadOnly"
                      @change="toggleCompleted(currentTab.type, selectedRow)"
                  />
                  <span :class="['status-chip', selectedRow.completed ? 'status-done' : 'status-progress']">
                    {{ selectedRow.completed ? '완료' : '진행 중' }}
                  </span>
                </div>
              </template>
              <template v-else-if="field.type === 'datetime'">
                <span class="readonly-text">{{ formatDateTime(selectedRow[field.key]) }}</span>
              </template>
              <template v-else-if="field.type === 'readonly'">
                <span class="readonly-text">{{ selectedRow[field.key] || '-' }}</span>
              </template>
            </div>
          </div>
          <p v-if="!isReadOnly" class="table-hint">
            입력 후 포커스를 벗어나면 자동으로 저장되며, 완료 체크는 즉시 반영됩니다.
          </p>
        </div>
      </div>
    </div>

    <div
        v-for="fb in feedbacks"
        :key="fb.feedbackId"
        class="feedback-marker"
        :style="{ top: fb.y + 'px', left: fb.x + 'px', position: 'absolute' }"
        @click="selectedFeedback = fb"
    >
      📌
    </div>

    <FeedbackPopup
        v-if="selectedFeedback"
        :fb="selectedFeedback"
        :readonly="true"
        @read="handleReadFeedback"
        @close="selectedFeedback = null"
    />

    <FeedbackInput
        v-if="showFeedbackInput"
        :x="feedbackPosition.x"
        :y="feedbackPosition.y"
        :page="`test-${currentTab.type.toLowerCase()}`"
        :readonly="true"
        :projectId="resolvedProjectId"
        @close="showFeedbackInput = false"
        @submitted="() => { showFeedbackInput = false; loadFeedbacks() }"
    />

    <ContextMenu
      v-if="showContextMenu"
      :x="feedbackPosition.x"
      :y="feedbackPosition.y"
      :visible="showContextMenu"
      @select="handleMenuSelect"
      @close="showContextMenu = false"
    />

  </div>
</template>

<script setup>
import { ref, reactive, computed, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import api from '@/api'
import FeedbackPopup from '@/components/feedback/FeedbackPopup.vue'
import ContextMenu from '@/components/feedback/ContextMenu.vue'
import FeedbackInput from '@/components/feedback/FeedbackInput.vue'
import { useFeedback } from '@/composables/useFeedback'
import InfoTooltip from '@/components/InfoTooltip.vue';

const props = defineProps({
  projectId: Number,
  readonly: {
    type: Boolean,
    default: false,
  },
})

const route = useRoute()
const router = useRouter()
const resolvedProjectId = computed(() => {
  if (props.projectId != null) {
    return Number(props.projectId)
  }
  const routeId = route.params.projectId
  return routeId != null ? Number(routeId) : null
})
const isReadOnly = computed(() => !!props.readonly)

const tabConfigs = {
  UNIT: {
    name: '단위 테스트',
    basePath: '/api/test-rows/unit',
    subtitle: '각 기능 단위별 테스트 케이스를 관리하세요.',
    editableKeys: [
      'testId',
      'methodName',
      'caseDesc',
      'inputs',
      'expectedResult',
      'actualResult',
      'caseType',
      'linkedIntegrationId',
      'completed',
    ],
    createDefaults: {
      testId: '',
      methodName: '',
      caseDesc: '',
      inputs: '',
      expectedResult: '',
      actualResult: '',
      caseType: '',
      linkedIntegrationId: '',
      completed: false,
    },
    fields: [
      { key: 'testId', label: '테스트 ID', type: 'input', placeholder: 'UT_001', description: '각 테스트 케이스를 식별하는 고유 ID입니다. (예: UT_001)' },
      {
        key: 'methodName',
        label: '메서드명',
        type: 'input',
        placeholder: 'OrderService.calculatePrice',
        description: '테스트하려는 클래스와 메서드의 이름을 명시합니다. (예: OrderService.calculatePrice)'
      },
      { key: 'caseDesc', label: '케이스 설명', type: 'input', description: '어떤 상황을 테스트하는지에 대한 간결한 설명입니다.' },
      { key: 'inputs', label: '입력 / 조건', type: 'input', description: '테스트를 실행하기 위해 필요한 입력값이나 사전 조건을 명시합니다.' },
      { key: 'expectedResult', label: '기대 결과', type: 'input', description: '테스트가 성공했을 때 예상되는 결과값 또는 시스템의 상태입니다.' },
      { key: 'actualResult', label: '실제 결과', type: 'input', description: '테스트를 실행한 후 실제로 나타난 결과입니다.' },
      { key: 'caseType', label: '유형', type: 'input', placeholder: '정상 / 예외', description: '정상적인 경우를 테스트하는지, 예외 상황을 테스트하는지 구분합니다.' },
      {
        key: 'linkedIntegrationId',
        label: '연결된 통합 ID',
        type: 'input',
        placeholder: 'SIT_001',
        description: '이 단위 테스트와 관련된 통합 테스트가 있다면 해당 ID를 기입합니다.'
      },
      { key: 'completed', label: '완료 여부', type: 'checkbox', description: '테스트 케이스의 진행 상태를 나타냅니다.' },
      { key: 'authorName', label: '작성자', type: 'readonly',  },
      { key: 'createdAt', label: '작성일', type: 'datetime',  },
      { key: 'updatedAt', label: '수정일', type: 'datetime',  },
    ],
  },
  INTEGRATION: {
    name: '통합 테스트',
    basePath: '/api/test-rows/integration',
    subtitle: '시나리오 기반 통합 테스트 결과를 기록하세요.',
    editableKeys: ['testId', 'moduleName', 'scenario', 'expected', 'result', 'note', 'completed'],
    createDefaults: {
      testId: '',
      moduleName: '',
      scenario: '',
      expected: '',
      result: '',
      note: '',
      completed: false,
    },
    fields: [
      { key: 'testId', label: '테스트 ID', type: 'input', placeholder: 'SIT_001', description: '각 테스트 시나리오를 식별하는 고유 ID입니다. (예: SIT_001)' },
      { key: 'moduleName', label: '모듈명', type: 'input', placeholder: '주문 + 결제', description: '테스트 대상이 되는 시스템의 모듈 또는 기능 범위를 명시합니다.' },
      { key: 'scenario', label: '시나리오', type: 'textarea', rows: 2, description: '사용자 관점에서 수행되는 테스트의 전체적인 흐름이나 절차를 설명합니다.' },
      { key: 'expected', label: '기대 결과', type: 'textarea', rows: 2, description: '시나리오가 성공적으로 완료되었을 때 예상되는 최종 결과입니다.' },
      { key: 'result', label: '실제 결과', type: 'textarea', rows: 2, description: '시나리오를 실행한 후 실제로 나타난 결과입니다.' },
      { key: 'note', label: '비고', type: 'textarea', rows: 2, description: '테스트와 관련된 추가 정보나 특이사항을 기록합니다.' },
      { key: 'completed', label: '완료 여부', type: 'checkbox', description: '테스트 시나리오의 진행 상태를 나타냅니다.' },
      { key: 'authorName', label: '작성자', type: 'readonly',  },
      { key: 'createdAt', label: '작성일', type: 'datetime',  },
      { key: 'updatedAt', label: '수정일', type: 'datetime', },
    ],
  },
}

const testTabs = reactive([
  { type: 'UNIT', name: tabConfigs.UNIT.name, rows: [], loading: false },
  { type: 'INTEGRATION', name: tabConfigs.INTEGRATION.name, rows: [], loading: false },
])

const selectedIndex = ref(0)
const selectedRowId = ref(null)

const searchQuery = ref('');
const statusFilter = ref('all'); // 'all', 'in-progress', 'completed'

// URL 쿼리에서 substep을 확인하여 초기 탭 설정
watch(
  () => route.query.substep,
  (substep) => {
    if (substep === 'unit') {
      selectedIndex.value = 0
    } else if (substep === 'integration') {
      selectedIndex.value = 1
    }
  },
  { immediate: true }
)

const currentTab = computed(() => testTabs[selectedIndex.value])
const currentConfig = computed(() => tabConfigs[currentTab.value.type])
const selectedRow = computed(() => {
  if (!selectedRowId.value) return null
  return currentTab.value.rows.find(row => row.id === selectedRowId.value)
})

const filteredRows = computed(() => {
  let rows = currentTab.value.rows;

  // 1. Filter by search query
  if (searchQuery.value) {
    const lowerCaseQuery = searchQuery.value.toLowerCase();
    rows = rows.filter(row =>
      row.testId && row.testId.toLowerCase().includes(lowerCaseQuery)
    );
  }

  // 2. Filter by status
  if (statusFilter.value === 'in-progress') {
    rows = rows.filter(row => !row.completed);
  } else if (statusFilter.value === 'completed') {
    rows = rows.filter(row => row.completed);
  }

  return rows;
});



const feedbacks = ref([])
const selectedFeedback = ref(null)
const showFeedbackInput = ref(false)
const showContextMenu = ref(false)
const feedbackPosition = ref({ x: 0, y: 0 })
const { markFeedbackAsRead } = useFeedback()

function selectTab(idx) {
  selectedIndex.value = idx
  searchQuery.value = ''; // 탭 전환 시 검색어 초기화
  statusFilter.value = 'all'; // 탭 전환 시 필터 초기화
  selectedRowId.value = null // 탭 전환 시 선택 초기화
  const substep = idx === 0 ? 'unit' : 'integration'
  router.push({
    query: { ...route.query, substep }
  })
}

function handleRightClick(e) {
  const rect = e.currentTarget.getBoundingClientRect()
  feedbackPosition.value = {
    x: e.clientX - rect.left + e.currentTarget.scrollLeft,
    y: e.clientY - rect.top + e.currentTarget.scrollTop,
  }
  showContextMenu.value = true
}

function handleMenuSelect(action) {
  if (action === 'add-feedback') {
    showFeedbackInput.value = true
  }
  showContextMenu.value = false
}

function handleReadFeedback(id) {
  markFeedbackAsRead(id)
  feedbacks.value = feedbacks.value.filter(fb => fb.feedbackId !== id)
  selectedFeedback.value = null
}

function getProjectParams() {
  return resolvedProjectId.value ? { projectId: resolvedProjectId.value } : {}
}

function mapResponse(type, raw) {
  if (type === 'UNIT') {
    return {
      id: raw.id,
      testId: raw.testId ?? '',
      methodName: raw.methodName ?? '',
      caseDesc: raw.caseDesc ?? '',
      inputs: raw.inputs ?? '',
      expectedResult: raw.expectedResult ?? '',
      actualResult: raw.actualResult ?? '',
      caseType: raw.caseType ?? '',
      linkedIntegrationId: raw.linkedIntegrationId ?? '',
      completed: !!raw.completed,
      authorName: raw.authorName ?? '',
      createdAt: raw.createdAt ?? null,
      updatedAt: raw.updatedAt ?? null,
    }
  }
  return {
    id: raw.id,
    testId: raw.testId ?? '',
    moduleName: raw.moduleName ?? '',
    scenario: raw.scenario ?? '',
    expected: raw.expected ?? '',
    result: raw.result ?? '',
    note: raw.note ?? '',
    completed: !!raw.completed,
    authorName: raw.authorName ?? '',
    createdAt: raw.createdAt ?? null,
    updatedAt: raw.updatedAt ?? null,
  }
}

function getTabByType(type) {
  return testTabs.find(tab => tab.type === type)
}

function getConfigByType(type) {
  return tabConfigs[type]
}

async function fetchRowsForTab(tab) {
  const config = getConfigByType(tab.type)
  tab.loading = true
  try {
    const { data } = await api.get(`${config.basePath}/project`, {
      params: getProjectParams(),
    })
    tab.rows = data.map(row => mapResponse(tab.type, row))
    if (tab.rows.length > 0 && !selectedRowId.value) {
      selectedRowId.value = tab.rows[0].id
    }
  } catch (err) {
    console.error('❌ 테스트 행 불러오기 실패:', err)
    tab.rows = []
  } finally {
    tab.loading = false
  }
}

async function loadFeedbacks() {
  if (!resolvedProjectId.value) return
  const pageIdentifier = `test-${currentTab.value.type.toLowerCase()}`;
  try {
    const { data } = await api.get('/feedbacks/project', {
      params: { page: pageIdentifier, projectId: resolvedProjectId.value },
      headers: { Authorization: localStorage.getItem('authHeader') },
      withCredentials: true,
    })
    feedbacks.value = data.filter(fb => !fb.isRead)
  } catch (err) {
    console.error('❌ 피드백 불러오기 실패:', err)
  }
}

async function refreshTab(type) {
  const tab = getTabByType(type)
  if (tab) {
    await fetchRowsForTab(tab)
  }
}

async function addRow(type) {
  if (isReadOnly.value) return
  const config = getConfigByType(type)
  try {
    const { data } = await api.post(`${config.basePath}/create`, config.createDefaults, {
      params: getProjectParams(),
    })
    const tab = getTabByType(type)
    if (tab) {
      tab.rows = [mapResponse(type, data), ...tab.rows]
      selectedRowId.value = data.id // 새로 추가된 행을 선택
    }
  } catch (err) {
    console.error('❌ 테스트 행 추가 실패:', err)
    alert('새 테스트를 추가하지 못했습니다. 다시 시도해주세요.')
  }
}

async function saveRow(type, row) {
  if (isReadOnly.value || !row.id) return
  const config = getConfigByType(type)
  const payload = {}
  config.editableKeys.forEach(key => {
    payload[key] = row[key] ?? ''
  })
  try {
    const { data } = await api.put(`${config.basePath}/${row.id}`, payload, {
      params: getProjectParams(),
    })
    Object.assign(row, mapResponse(type, data))
  } catch (err) {
    console.error('❌ 테스트 행 저장 실패:', err)
    alert('저장에 실패했습니다. 잠시 후 다시 시도해주세요.')
    await refreshTab(type)
  }
}

async function toggleCompleted(type, row) {
  if (isReadOnly.value || !row.id) return
  const config = getConfigByType(type)
  try {
    const { data } = await api.patch(`${config.basePath}/${row.id}/toggle`, null, {
      params: getProjectParams(),
    })
    Object.assign(row, mapResponse(type, data))
  } catch (err) {
    row.completed = !row.completed
    console.error('❌ 완료 여부 변경 실패:', err)
    alert('완료 여부를 변경하지 못했습니다.')
  }
}

async function deleteRow(type, rowId) {
  if (isReadOnly.value || !rowId) return
  if (!confirm('이 테스트 행을 삭제하시겠습니까?')) return
  const config = getConfigByType(type)
  try {
    await api.delete(`${config.basePath}/${rowId}`, {
      params: getProjectParams(),
    })
    const tab = getTabByType(type)
    if (tab) {
      tab.rows = tab.rows.filter(row => row.id !== rowId)
      selectedRowId.value = null // 삭제 후 선택 해제
    }
  } catch (err) {
    console.error('❌ 테스트 행 삭제 실패:', err)
    alert('삭제에 실패했습니다. 다시 시도해주세요.')
  }
}

function formatDateTime(value) {
  if (!value) return '-'
  const date = new Date(value)
  if (Number.isNaN(date.getTime())) {
    return String(value).replace('T', ' ')
  }
  return date.toLocaleString('ko-KR', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit',
  })
}

watch(
    resolvedProjectId,
    async newVal => {
      if (!newVal) return
      await Promise.all(testTabs.map(tab => fetchRowsForTab(tab)))
      await loadFeedbacks()
    },
    { immediate: true }
)
</script>
<style scoped>
.test-table-container {
  padding: 12px;
  background-color: #ffffff;
  height: calc(100vh - 120px);
  font-family: 'Segoe UI', 'Apple SD Gothic Neo', sans-serif;
  color: #1f2937;
  border-radius: 16px;
  display: flex;
  flex-direction: column;
}

.nav-buttons {
  display: flex;
  gap: 12px;
  margin-bottom: 12px;
  flex-shrink: 0;
}

.nav-btn {
  padding: 8px 16px;
  font-weight: 600;
  border: 1px solid #dbeafe;
  border-radius: 8px;
  cursor: pointer;
  background-color: #eef2ff;
  color: #3b82f6;
  transition: all 0.2s ease;
}

.nav-btn:hover {
  background-color: #dbeafe;
  color: #1e40af;
}

.nav-btn.active {
  background-color: #3b82f6;
  color: #fff;
  border-color: #3b82f6;
}

.table-header {
  margin-bottom: 16px;
  flex-shrink: 0;
}

.title-area {
  display: flex;
  align-items: baseline;
  gap: 12px;
}

.title-area h2 {
  margin: 0;
}

.table-header h2 {
  margin: 0;
  font-size: 28px;
  font-weight: 700;
}

.table-subtitle {
  margin: 6px 0 0;
  color: #64748b;
  font-size: 14px;
}

.add-row-btn {
  padding: 8px 16px;
  border: none;
  background-color: #495057;
  color: #fff;
  border-radius: 10px;
  font-weight: 600;
  cursor: pointer;
  box-shadow: 0 2px 8px rgba(59, 130, 246, 0.2);
  transition: transform 0.2s ease, box-shadow 0.2s ease;
  width: 100%;
}

.add-row-btn:disabled {
  opacity: 0.6;
  cursor: default;
  box-shadow: none;
}

.add-row-btn:not(:disabled):hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(59, 130, 246, 0.3);
}

.master-detail-layout {
  display: flex;
  gap: 24px;
  flex-grow: 1;
  min-height: 0;
}

.master-pane {
  width: 35%;
  min-width: 300px;
  background-color: #fff;
  border-radius: 16px;
  border: 1px solid #e5e7eb;
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

.master-pane-header {
  padding: 12px;
  border-bottom: 1px solid #e5e7eb;
  display: grid;
  gap: 12px;
}

.filter-controls {
  display: grid;
  grid-template-columns: 1fr;
  gap: 8px;
  align-items: center;
}

.search-input {
  width: 100%;
  padding: 8px 12px;
  border: 1px solid #d1d5db;
  border-radius: 8px;
  font-size: 13px;
}

.status-filter {
  display: flex;
  width: 100%;
  border-radius: 8px;
  overflow: hidden;
  background-color: #f1f5f9;
}

.status-filter button {
  flex: 1;
  padding: 6px 8px;
  border: none;
  background-color: transparent;
  cursor: pointer;
  font-size: 12px;
  transition: background-color 0.2s;
}

.status-filter button.active {
  background-color: #3b82f6;
  color: white;
}
.master-list {
  list-style: none;
  padding: 8px;
  margin: 0;
  overflow-y: auto;
}

.master-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12px;
  border-radius: 10px;
  cursor: pointer;
  transition: background-color 0.2s ease;
  border-bottom: 1px solid #f3f4f6;
}

.master-item:hover {
  background-color: #f9fafb;
}

.master-item.active {
  background-color: #eef2ff;
  font-weight: 600;
}

.master-item-content {
  display: flex;
  flex-direction: column;
  gap: 4px;
  overflow: hidden;
}

.master-item-id {
  font-size: 14px;
  color: #111827;
}

.master-item-desc {
  font-size: 13px;
  color: #6b7280;
  margin: 0;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.master-item-status {
  display: flex;
  align-items: center;
  gap: 8px;
  flex-shrink: 0;
}

.detail-pane {
  width: 65%;
  background-color: #fff;
  border-radius: 16px;
  border: 1px solid #e5e7eb;
  overflow-y: auto;
  padding: 24px;
}

.detail-empty-state {
  display: flex;
  justify-content: center;
  align-items: center;
  height: 100%;
  text-align: center;
  color: #9ca3af;
}
.empty-inner { display: flex; flex-direction: column; align-items: center; gap: 16px; }
.empty-icon { font-size: 3rem; color: #d1d5db; }
.empty-inner p { font-size: 1.1rem; line-height: 1.6; }

.detail-form {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.form-group {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.form-label-wrapper {
  display: flex;
  align-items: center;
}

.form-group label {
  font-weight: 600;
  font-size: 14px;
  color: #374151;
}



.form-control textarea,
.form-control input[type='text'] {
  width: 100%;
  font-size: 13px;
  padding: 8px 10px;
  border: 1px solid #cbd5f5;
  border-radius: 8px;
  resize: vertical;
  transition: border-color 0.2s ease, box-shadow 0.2s ease;
  background-color: #fff;
}

.form-control textarea:focus,
.form-control input[type='text']:focus {
  border-color: #2563eb;
  box-shadow: 0 0 0 3px rgba(37, 99, 235, 0.15);
  outline: none;
}

.form-control textarea:disabled,
.form-control input[type='text']:disabled {
  background-color: #f1f5f9;
  cursor: not-allowed;
}

.checkbox-wrapper {
  display: flex;
  align-items: center;
  gap: 10px;
}

.checkbox-wrapper input[type='checkbox'] {
  width: 18px;
  height: 18px;
  accent-color: #2563eb;
}

.status-chip {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  min-width: 70px;
  padding: 4px 10px;
  border-radius: 999px;
  font-size: 12px;
  font-weight: 600;
}

.status-done {
  background-color: rgba(34, 197, 94, 0.15);
  color: #15803d;
}

.status-progress {
  background-color: rgba(59, 130, 246, 0.15);
  color: #1d4ed8;
}

.datetime-text,
.readonly-text {
  font-size: 13px;
  color: #475569;
  padding: 8px 10px;
  display: block;
}

.delete-btn {
  border: none;
  background: none;
  cursor: pointer;
  font-size: 18px;
  color: #ef4444;
  transition: opacity 0.2s ease, transform 0.2s ease, color 0.2s ease;
}

.delete-btn:hover {
  color: #dc2626;
  transform: scale(1.1);
}

.state-cell {
  text-align: center;
  padding: 40px 0;
  color: #64748b;
  font-size: 14px;
}

.table-hint {
  margin-top: 12px;
  font-size: 13px;
  color: #6b7280;
}

.feedback-marker {
  cursor: pointer;
  font-size: 18px;
  transition: transform 0.2s ease;
}

.feedback-marker:hover {
  transform: scale(1.1);
}
</style>
