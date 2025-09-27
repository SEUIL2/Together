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
          @click="selectedIndex = idx"
      >
        {{ tab.name }}
      </button>
    </div>

    <div class="table-header">
      <div>
        <h2>{{ currentTab.name }}</h2>
        <p class="table-subtitle">{{ currentConfig.subtitle }}</p>
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

    <div class="table-wrapper">
      <table class="test-table">
        <thead>
        <tr>
          <th class="action-header"></th>
          <th v-for="field in currentConfig.fields" :key="field.key">
            {{ field.label }}
          </th>
        </tr>
        </thead>
        <tbody>
        <tr v-if="currentTab.loading">
          <td :colspan="columnCount" class="state-cell">
            데이터를 불러오는 중입니다...
          </td>
        </tr>
        <tr v-else-if="!currentTab.rows.length">
          <td :colspan="columnCount" class="state-cell">
            아직 등록된 테스트가 없습니다. "새 테스트 추가" 버튼으로 첫 테스트를 작성해보세요.
          </td>
        </tr>
        <tr
            v-else
            v-for="row in currentTab.rows"
            :key="row.id"
            class="table-row"
            @mouseover="hoveredRow = row.id"
            @mouseleave="hoveredRow = null"
        >
          <td class="delete-cell">
            <button
                v-if="!isReadOnly"
                class="delete-btn"
                title="삭제"
                @click="deleteRow(currentTab.type, row.id)"
            >
              🗑️
            </button>
          </td>
          <td
              v-for="field in currentConfig.fields"
              :key="field.key"
              :class="['cell', `cell-${field.type}`]"
          >
            <template v-if="field.type === 'input'">
              <input
                  v-model="row[field.key]"
                  type="text"
                  :placeholder="field.placeholder || ''"
                  :disabled="isReadOnly"
                  @blur="saveRow(currentTab.type, row)"
              />
            </template>
            <template v-else-if="field.type === 'textarea'">
                <textarea
                    v-model="row[field.key]"
                    :rows="field.rows || 2"
                    :placeholder="field.placeholder || ''"
                    :disabled="isReadOnly"
                    @blur="saveRow(currentTab.type, row)"
                ></textarea>
            </template>
            <template v-else-if="field.type === 'checkbox'">
              <div class="checkbox-wrapper">
                <input
                    v-model="row.completed"
                    type="checkbox"
                    :disabled="isReadOnly"
                    @change="toggleCompleted(currentTab.type, row)"
                />
                <span
                    :class="['status-chip', row.completed ? 'status-done' : 'status-progress']"
                >
                    {{ row.completed ? '완료' : '진행 중' }}
                  </span>
              </div>
            </template>
            <template v-else-if="field.type === 'datetime'">
              <span class="datetime-text">{{ formatDateTime(row[field.key]) }}</span>
            </template>
            <template v-else-if="field.type === 'readonly'">
              <span class="readonly-text">{{ row[field.key] || '-' }}</span>
            </template>
          </td>
        </tr>
        </tbody>
      </table>
    </div>

    <p v-if="!isReadOnly" class="table-hint">
      입력 후 포커스를 벗어나면 자동으로 저장되며, 완료 체크는 즉시 반영됩니다.
    </p>

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
import { useRoute } from 'vue-router'
import axios from '@/utils/axiosInstance'
import FeedbackPopup from '@/components/feedback/FeedbackPopup.vue'
import ContextMenu from '@/components/feedback/ContextMenu.vue'
import FeedbackInput from '@/components/feedback/FeedbackInput.vue'
import { useFeedback } from '@/composables/useFeedback'

const props = defineProps({
  projectId: Number,
  readonly: {
    type: Boolean,
    default: false,
  },
})

const route = useRoute()
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
      { key: 'testId', label: '테스트 ID', type: 'input', placeholder: 'UT_001' },
      {
        key: 'methodName',
        label: '메서드명',
        type: 'input',
        placeholder: 'OrderService.calculatePrice',
      },
      { key: 'caseDesc', label: '케이스 설명', type: 'input' },
      { key: 'inputs', label: '입력 / 조건', type: 'input' },
      { key: 'expectedResult', label: '기대 결과', type: 'input' },
      { key: 'actualResult', label: '실제 결과', type: 'input' },
      { key: 'caseType', label: '유형', type: 'input', placeholder: '정상 / 예외' },
      {
        key: 'linkedIntegrationId',
        label: '연결된 통합 ID',
        type: 'input',
        placeholder: 'SIT_001',
      },
      { key: 'completed', label: '완료 여부', type: 'checkbox' },
      { key: 'authorName', label: '작성자', type: 'readonly' },
      { key: 'createdAt', label: '작성일', type: 'datetime' },
      { key: 'updatedAt', label: '수정일', type: 'datetime' },
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
      { key: 'testId', label: '테스트 ID', type: 'input', placeholder: 'SIT_001' },
      { key: 'moduleName', label: '모듈명', type: 'input', placeholder: '주문 + 결제' },
      { key: 'scenario', label: '시나리오', type: 'textarea', rows: 2 },
      { key: 'expected', label: '기대 결과', type: 'textarea', rows: 2 },
      { key: 'result', label: '실제 결과', type: 'textarea', rows: 2 },
      { key: 'note', label: '비고', type: 'textarea', rows: 2 },
      { key: 'completed', label: '완료 여부', type: 'checkbox' },
      { key: 'authorName', label: '작성자', type: 'readonly' },
      { key: 'createdAt', label: '작성일', type: 'datetime' },
      { key: 'updatedAt', label: '수정일', type: 'datetime' },
    ],
  },
}

const testTabs = reactive([
  { type: 'UNIT', name: tabConfigs.UNIT.name, rows: [], loading: false },
  { type: 'INTEGRATION', name: tabConfigs.INTEGRATION.name, rows: [], loading: false },
])

const selectedIndex = ref(0)
const hoveredRow = ref(null)
const currentTab = computed(() => testTabs[selectedIndex.value])
const currentConfig = computed(() => tabConfigs[currentTab.value.type])
const columnCount = computed(() => currentConfig.value.fields.length + 1)

const feedbacks = ref([])
const selectedFeedback = ref(null)
const showFeedbackInput = ref(false)
const showContextMenu = ref(false)
const feedbackPosition = ref({ x: 0, y: 0 })
const { markFeedbackAsRead } = useFeedback()

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
    const { data } = await axios.get(`${config.basePath}/project`, {
      params: getProjectParams(),
    })
    tab.rows = data.map(row => mapResponse(tab.type, row))
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
    const { data } = await axios.get('/feedbacks/project', {
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
    const { data } = await axios.post(`${config.basePath}/create`, config.createDefaults, {
      params: getProjectParams(),
    })
    const tab = getTabByType(type)
    if (tab) {
      tab.rows = [mapResponse(type, data), ...tab.rows]
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
    const { data } = await axios.put(`${config.basePath}/${row.id}`, payload, {
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
    const { data } = await axios.patch(`${config.basePath}/${row.id}/toggle`, null, {
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
    await axios.delete(`${config.basePath}/${rowId}`, {
      params: getProjectParams(),
    })
    const tab = getTabByType(type)
    if (tab) {
      tab.rows = tab.rows.filter(row => row.id !== rowId)
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
  padding: 32px;
  background-color: #f7f9fc;
  min-height: calc(100vh - 120px);
  font-family: 'Segoe UI', 'Apple SD Gothic Neo', sans-serif;
  color: #1f2937;
  border-radius: 16px;
  box-shadow: 0 10px 30px rgba(15, 23, 42, 0.08);
}

.nav-buttons {
  display: flex;
  gap: 12px;
  margin-bottom: 24px;
}

.nav-btn {
  padding: 10px 18px;
  font-weight: 600;
  border: 1px solid #cbd5f5;
  border-radius: 10px;
  cursor: pointer;
  background-color: #eef2ff;
  color: #1e40af;
  transition: background-color 0.2s ease, color 0.2s ease, box-shadow 0.2s ease;
}

.nav-btn:hover {
  background-color: #dbeafe;
  box-shadow: 0 6px 14px rgba(59, 130, 246, 0.2);
}

.nav-btn.active {
  background-color: #2563eb;
  color: #fff;
  border-color: #1d4ed8;
  box-shadow: 0 8px 18px rgba(37, 99, 235, 0.35);
}

.table-header {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 16px;
  margin-bottom: 16px;
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
  padding: 10px 18px;
  border: none;
  background: linear-gradient(135deg, #2563eb, #4f46e5);
  color: #fff;
  border-radius: 10px;
  font-weight: 600;
  cursor: pointer;
  box-shadow: 0 10px 20px rgba(79, 70, 229, 0.25);
  transition: transform 0.2s ease, box-shadow 0.2s ease;
}

.add-row-btn:disabled {
  opacity: 0.6;
  cursor: default;
  box-shadow: none;
}

.add-row-btn:not(:disabled):hover {
  transform: translateY(-2px);
  box-shadow: 0 14px 24px rgba(79, 70, 229, 0.35);
}

.table-wrapper {
  overflow-x: auto;
  background-color: #fff;
  border-radius: 16px;
  border: 1px solid #e5e7eb;
}

.test-table {
  width: auto; /* 테이블 너비를 내용에 따라 자동으로 조절 */
  border-collapse: separate;
  border-spacing: 0;
}

.test-table thead th {
  position: sticky;
  top: 0;
  background-color: #eff6ff;
  color: #1e3a8a;
  padding: 14px 16px;
  font-size: 13px;
  font-weight: 700;
  text-align: left;
  border-bottom: 1px solid #cbd5f5;
  white-space: nowrap; /* 헤더 텍스트 줄바꿈 방지 */
  z-index: 1;
}

.test-table tbody td {
  border-bottom: 1px solid #e2e8f0;
  padding: 12px 16px;
  vertical-align: top;
  background-color: #fff;
  white-space: nowrap; /* 셀 내용 줄바꿈 방지 */
}

.table-row:hover td {
  background-color: #f8fafc;
}

.cell textarea,
.cell input[type='text'] {
  width: 100%;
  min-width: 120px; /* 모든 입력 필드에 최소 너비(120px)를 지정합니다. */
  font-size: 13px;
  padding: 8px 10px;
  border: 1px solid #cbd5f5;
  border-radius: 8px;
  resize: vertical;
  transition: border-color 0.2s ease, box-shadow 0.2s ease;
  background-color: #fff;
}

.cell textarea:focus,
.cell input[type='text']:focus {
  border-color: #2563eb;
  box-shadow: 0 0 0 3px rgba(37, 99, 235, 0.15);
  outline: none;
}

.cell textarea:disabled,
.cell input[type='text']:disabled {
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
}

.delete-cell {
  width: 50px;
  text-align: center;
  vertical-align: middle; /* 아이콘을 세로 중앙에 위치시키기 위함 */
}

.delete-btn {
  border: none;
  background: none;
  cursor: pointer;
  font-size: 18px;
  color: #ef4444;
  /* 기본적으로 투명하게 만들어 보이지 않게 처리 */
  opacity: 0;
  transition: opacity 0.2s ease, transform 0.2s ease, color 0.2s ease;
}

/* 행에 마우스를 올렸을 때 휴지통 아이콘이 보이도록 변경 */
.table-row:hover .delete-btn {
  opacity: 1;
}

.delete-btn:hover {
  color: #dc2626;
  transform: scale(1.1);
}

.action-header {
  width: 40px;
}

.state-cell {
  text-align: center;
  padding: 40px 0;
  color: #64748b;
  font-size: 14px;
  background-color: #fff;
}

.table-hint {
  margin-top: 18px;
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