<template>
  <div class="test-table-container">
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

    <h2>{{ currentTab.name }}</h2>

    <table class="test-table">
      <thead>
      <tr>
        <th></th> <!-- 삭제 열 -->
        <th>테스트 항목명</th>
        <th>설명</th>
        <th>작성자</th>
        <th>작성일</th>
        <th>완료 여부</th>
      </tr>
      </thead>

      <tbody>
      <tr
          v-for="(row, index) in currentTab.rows"
          :key="row.rowId || index"
          class="table-row"
          @mouseover="hoveredRow = row.rowId"
          @mouseleave="hoveredRow = null"
      >
        <!-- 삭제 아이콘 셀 -->
        <td class="delete-cell">
          <button
              v-if="hoveredRow === row.rowId"
              class="delete-btn"
              @click="deleteRow(row.rowId)"
              title="삭제"
          >
            🗑️
          </button>
        </td>

        <!-- 나머지 셀 -->
        <td><input v-model="row.itemName" @blur="saveRow(currentTab.type, row)" /></td>
        <td><input v-model="row.description" @blur="saveRow(currentTab.type, row)" /></td>
        <td>
          <select v-model="row.authorId" @change="saveRow(currentTab.type, row)">
            <option v-for="user in teamMembers" :key="user.userId" :value="user.userId">
              {{ user.userName }}
            </option>
          </select>
        </td>
        <td>{{ formatDate(row.createdAt) }}</td>
        <td>
          <input
              type="checkbox"
              v-model="row.completed"
              @change="toggleCompleted(currentTab.type, row)"
          />
        </td>
      </tr>
      </tbody>

    </table>

    <button @click="addRow(currentTab.type)">+</button>


  </div>
</template>

<script setup>
import { ref, reactive, onMounted, computed } from 'vue'
import { useRoute } from 'vue-router'
import axios from '@/utils/axiosInstance'

const props = defineProps({ projectId: Number })
const route = useRoute()
const resolvedProjectId = computed(() => props.projectId || Number(route.params.projectId))

const testTabs = reactive([
  { name: '단위 테스트', type: 'UNIT', rows: [] },
  { name: '통합 테스트', type: 'INTEGRATION', rows: [] }
])
const selectedIndex = ref(0)
const teamMembers = ref([])

const currentTab = computed(() => testTabs[selectedIndex.value])

const hoveredRow = ref(null)

async function deleteRow(rowId) {
  if (!confirm('이 항목을 삭제하시겠습니까?')) return
  await axios.delete(`/api/test-rows/delete/${rowId}`)
  currentTab.value.rows = currentTab.value.rows.filter(r => r.rowId !== rowId)
}


function formatDate(dateStr) {
  return new Date(dateStr).toLocaleDateString()
}

function mapRow(raw) {
  const member = teamMembers.value.find(m => m.userName === raw.authorName)
  return {
    rowId: raw.id,
    itemName: raw.itemName,
    description: raw.description,
    authorId: member ? member.userId : null,
    createdAt: raw.createdDate,
    completed: raw.completed,
  }
}

async function fetchRows() {
  for (const tab of testTabs) {
    const { data } = await axios.get('/api/test-rows/list', {
      params: {
        tableType: tab.type,
        ...(resolvedProjectId.value ? { projectId: resolvedProjectId.value } : {})
      }
    })
    tab.rows = data.map(mapRow)
  }
}

async function fetchTeamMembers() {
  const { data } = await axios.get('/projects/members', {
    params: resolvedProjectId.value ? { projectId: resolvedProjectId.value } : {}
  })
  teamMembers.value = data
}

async function addRow(tableType) {
  const { data } = await axios.post('/api/test-rows/create', null, {
    params: {
      tableType,
      itemName: '',
      description: '',
      ...(resolvedProjectId.value ? { projectId: resolvedProjectId.value } : {})
    }
  })
  const tab = testTabs.find(t => t.type === tableType)
  tab.rows.push(mapRow(data))
}

async function saveRow(tableType, row) {
  if (!row.rowId) return
  await axios.put(`/api/test-rows/update/${row.rowId}`, null, {
    params: {
      itemName: row.itemName,
      description: row.description,
      completed: row.completed,
      ...(resolvedProjectId.value ? { projectId: resolvedProjectId.value } : {})
    }
  })
}

async function toggleCompleted(tableType, row) {
  const { data } = await axios.patch(`/api/test-rows/toggle/${row.rowId}`, null, {
    params: resolvedProjectId.value ? { projectId: resolvedProjectId.value } : {}
  })
  row.completed = data.completed
}

onMounted(async () => {
  await fetchTeamMembers()
  await fetchRows()
})
</script>

<style scoped>
.test-table-container {
  padding: 24px;
  background-color: #ffffff;
  min-height: 100vh;
  font-family: 'Segoe UI', sans-serif;
  color: #333;
}

/* 탭 버튼 */
.nav-buttons {
  display: flex;
  gap: 10px;
  margin-bottom: 20px;
}

.nav-btn {
  padding: 8px 16px;
  font-weight: 600;
  border: 1px solid #ccc;
  border-radius: 8px; /* 둥글게 */
  cursor: pointer;
  background-color: #f0f0f0;
  color: #333;
  transition: background-color 0.2s;
}

.nav-btn:hover {
  background-color: #e0e0e0;
}

.nav-btn.active {
  background-color: #4a90e2;
  color: white;
  border-color: #4a90e2;
}

/* 테이블 */
.test-table {
  width: 100%;
  border-collapse: collapse;
  border: 1px solid #ccc;
  font-size: 14px;
  background-color: white;
}

.test-table th,
.test-table td {
  border: 1px solid #ccc;
  padding: 10px;
  text-align: left;
}

.test-table tbody tr:hover {
  background-color: #f9f9f9;
}

/* 입력창 & 드롭다운 */
.test-table input[type="text"],
.test-table select {
  width: 100%;
  padding: 6px 8px;
  font-size: 14px;
  border: 1px solid #bbb;
  border-radius: 0;
  background-color: #fff;
  outline: none;
}

.test-table input[type="text"]:focus,
.test-table select:focus {
  border-color: #4a90e2;
  box-shadow: none;
}

/* 체크박스 */
.test-table input[type="checkbox"] {
  width: 16px;
  height: 16px;
  accent-color: #4a90e2;
}

/* + 버튼 (아래쪽에 여유 공간 포함) */
.test-table-container > button:last-child {
  margin-top: 30px;
  padding: 10px 20px;
  background-color: #4a90e2;
  color: white;
  border: none;
  border-radius: 8px; /* 둥글게 */
  font-weight: 600;
  cursor: pointer;
  transition: background-color 0.2s ease;
}

.test-table-container > button:last-child {
  margin-top: 24px;
  padding: 4px 8px; /* 아주 얇고 작게 */
  background-color: #4a90e2;
  color: white;
  border: none;
  border-radius: 6px; /* 살짝 둥글게 */
  font-size: 18px; /* 기호가 깔끔하게 보이도록 */
  font-weight: bold;
  line-height: 1;
  cursor: pointer;
  transition: background-color 0.2s ease, transform 0.1s ease;
}

.test-table-container > button:last-child:hover {
  background-color: #357ab7;
  transform: translateY(-1px);
}


.test-table-container > button:last-child:hover {
  background-color: #357ab7;
  transform: translateY(-1px);
}

/* 삭제 아이콘 열 */
.delete-cell {
  width: 40px;
  text-align: center;
}

/* 아이콘 기본 숨김 */
.delete-btn {
  background: none;
  border: none;
  cursor: pointer;
  font-size: 16px;
  color: #e74c3c;
  transition: color 0.2s;
}

.delete-btn:hover {
  color: #c0392b;
}


</style>
