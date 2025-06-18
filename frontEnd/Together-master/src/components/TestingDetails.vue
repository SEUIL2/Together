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
        <th>테스트 항목명</th>
        <th>설명</th>
        <th>작성자</th>
        <th>작성일</th>
        <th>완료 여부</th>
      </tr>
      </thead>
      <tbody>
      <tr v-for="(row, index) in currentTab.rows" :key="row.rowId || index">
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
        <td><input type="checkbox" v-model="row.completed" @change="toggleCompleted(currentTab.type, row)" /></td>
      </tr>
      </tbody>
    </table>

    <button @click="addRow(currentTab.type)">+</button>


  </div>
</template>

<script setup>
import { ref, reactive, onMounted, computed } from 'vue'
import axios from '@/utils/axiosInstance'

const testTabs = reactive([
  { name: '단위 테스트', type: 'UNIT', rows: [] },
  { name: '통합 테스트', type: 'INTEGRATION', rows: [] }
])
const selectedIndex = ref(0)
const teamMembers = ref([])

const currentTab = computed(() => testTabs[selectedIndex.value])

function formatDate(dateStr) {
  return new Date(dateStr).toLocaleDateString()
}

async function fetchRows() {
  for (const tab of testTabs) {
    const { data } = await axios.get('/api/test-rows/list', {
      params: { tableType: tab.type }
    })
    tab.rows = data
  }
}

async function fetchTeamMembers() {
  const { data } = await axios.get('/api/project-members')
  teamMembers.value = data
}

async function addRow(tableType) {
  const { data } = await axios.post('/api/test-rows/create', null, {
    params: {
      tableType,
      itemName: '',
      description: ''
    }
  })
  const tab = testTabs.find(t => t.type === tableType)
  tab.rows.push(data)
}

async function saveRow(tableType, row) {
  if (!row.rowId) return
  await axios.put(`/api/test-rows/update/${row.rowId}`, null, {
    params: {
      itemName: row.itemName,
      description: row.description,
      completed: row.completed
    }
  })
}

async function toggleCompleted(tableType, row) {
  const { data } = await axios.patch(`/api/test-rows/toggle/${row.rowId}`)
  row.completed = data.completed
}

onMounted(() => {
  fetchRows()
  fetchTeamMembers()
})
</script>

<style scoped>
.test-table-container {
  padding: 20px;
}
.nav-buttons {
  display: flex;
  gap: 10px;
  margin-bottom: 15px;
}
.nav-btn {
  padding: 8px 16px;
  border: 1px solid #ccc;
  background: white;
  cursor: pointer;
  border-radius: 6px;
}
.nav-btn.active {
  background-color: #4a90e2;
  color: white;
  border-color: #4a90e2;
}
.test-table {
  width: 100%;
  border-collapse: collapse;
}
.test-table th,
.test-table td {
  border: 1px solid #ccc;
  padding: 10px;
}
.guide {
  margin-top: 15px;
  font-size: 14px;
  color: #555;
}
</style>
