<template>
  <div class="test-table-container">
    <h2>통합 테스트</h2>
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
      <tr v-for="(row, index) in rows" :key="row.rowId || index">
        <td><input v-model="row.itemName" @blur="saveRow(row)" /></td>
        <td><input v-model="row.description" @blur="saveRow(row)" /></td>
        <td>
          <select v-model="row.authorId" @change="saveRow(row)">
            <option v-for="user in teamMembers" :key="user.userId" :value="user.userId">
              {{ user.userName }}
            </option>
          </select>
        </td>
        <td>{{ formatDate(row.createdAt) }}</td>
        <td><input type="checkbox" v-model="row.completed" @change="toggleCompleted(row)" /></td>
      </tr>
      </tbody>
    </table>

    <button @click="addRow">+</button>

    <div class="guide">
      <p>작성일은 줄 생성 시 자동 첨가</p>
      <p>작성자는 드롭다운으로 팀원 선택</p>
      <p>자동저장(추천)</p>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import api from '@/api'

const rows = ref([])
const teamMembers = ref([])
const tableType = 'INTEGRATION' // 단위테스트면 'UNIT'

async function fetchRows() {
  const { data } = await api.get('/api/test-rows/list', {
    params: { tableType }
  })
  rows.value = data
}

async function fetchTeamMembers() {
  const { data } = await api.get('/api/project-members')
  teamMembers.value = data.filter(member => member.role === 'STUDENT')
}

function formatDate(dateStr) {
  return new Date(dateStr).toLocaleDateString()
}

async function addRow() {
  const { data } = await api.post('/api/test-rows/create', null, {
    params: {
      tableType,
      itemName: '',
      description: ''
    }
  })
  rows.value.push(data)
}

async function saveRow(row) {
  if (!row.rowId) return
  await api.put(`/api/test-rows/update/${row.rowId}`, null, {
    params: {
      itemName: row.itemName,
      description: row.description,
      completed: row.completed
    }
  })
}

async function toggleCompleted(row) {
  const { data } = await api.patch(`/api/test-rows/toggle/${row.rowId}`)
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
.test-table {
  width: 100%;
  border-collapse: collapse;
}
.test-table th, .test-table td {
  border: 1px solid #ccc;
  padding: 10px;
}
.guide {
  margin-top: 15px;
  font-size: 14px;
  color: #555;
}
</style>
