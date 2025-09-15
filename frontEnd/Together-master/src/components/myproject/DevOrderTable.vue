<template>
  <div class="dev-order-container">
    <table class="dev-order-table">
      <thead>
        <tr>
          <th style="width: 8%">순서</th>
          <th style="width: 20%">기능 이름</th>
          <th style="width: 12%">중요도</th>
          <th>기능 설명</th>
          <th style="width: 8%">완료</th>
          <th style="width: 5%"></th>
        </tr>
      </thead>
      <tbody>
        <tr v-for="(row, index) in rows" :key="row.id">
          <td><input type="number" v-model.number="row.devOrder" @blur="updateDevOrderItem(row)" /></td>
          <td><input type="text" v-model="row.featureName" @blur="updateDevOrderItem(row)" /></td>
          <td>
            <select v-model="row.priority" @change="updateDevOrderItem(row)">
              <option v-for="p in priorities" :key="p" :value="p">{{ p }}</option>
            </select>
          </td>
          <td><input type="text" v-model="row.featureDescription" @blur="updateDevOrderItem(row)" /></td>
          <td class="text-center">
            <input type="checkbox" v-model="row.completed" @change="toggleDevOrderItemStatus(row)" />
          </td>
          <td class="text-center">
            <button class="delete-row-btn" @click="deleteDevOrderItem(row.id, index)">&times;</button>
          </td>
        </tr>
      </tbody>
    </table>
    <button class="add-row-btn" @click="addDevOrderItem">새 기능 추가</button>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue';
import axios from 'axios';

const props = defineProps({
  projectId: {
    type: Number,
    required: true,
  },
});

const rows = ref([]);
const priorities = ['상', '중', '하'];

async function fetchDevOrderItems() {
  try {
    const { data } = await axios.get('/dev-order-items', {
      params: { projectId: props.projectId },
      headers: { Authorization: localStorage.getItem('authHeader') },
      withCredentials: true,
    });
    rows.value = data.sort((a, b) => a.devOrder - b.devOrder);
  } catch (err) {
    console.error('기능 순서 목록 로딩 실패:', err);
  }
}

async function addDevOrderItem() {
  const newRow = {
    featureOrder: rows.value.length + 1,
    featureName: '',
    priority: '중',
    description: '',
    completed: false,
  };
  try {
    const { data } = await axios.post('/dev-order-items', newRow, {
      params: { projectId: props.projectId },
      headers: { Authorization: localStorage.getItem('authHeader') },
      withCredentials: true,
    });
    rows.value.push(data);
  } catch (err) {
    console.error('기능 추가 실패:', err);
    alert('기능 추가에 실패했습니다.');
  }
}

async function updateDevOrderItem(row) {
  try {
    const requestDto = {
      featureName: row.featureName,
      featureOrder: row.devOrder,
      priority: row.priority,
      description: row.featureDescription,
      completed: row.completed,
    };
    await axios.put(`/dev-order-items/${row.id}`, requestDto, {
      headers: { Authorization: localStorage.getItem('authHeader') },
      withCredentials: true,
    });
  } catch (err) {
    console.error('기능 업데이트 실패:', err);
  }
}

async function toggleDevOrderItemStatus(row) {
  await axios.patch(`/dev-order-items/${row.id}/status`, { completed: row.completed }, {
    headers: { Authorization: localStorage.getItem('authHeader') },
    withCredentials: true,
  });
}

async function deleteDevOrderItem(itemId, index) {
  try {
    await axios.delete(`/dev-order-items/${itemId}`, {
      headers: { Authorization: localStorage.getItem('authHeader') },
      withCredentials: true,
    });
    rows.value.splice(index, 1);
  } catch (err) {
    console.error('기능 삭제 실패:', err);
  }
}

onMounted(() => {
  fetchDevOrderItems();
});
</script>

<style scoped>
.dev-order-container {
  margin-top: 16px;
}
.dev-order-table {
  width: 100%;
  border-collapse: collapse;
  font-size: 14px;
}
.dev-order-table th, .dev-order-table td {
  border: 1px solid #e0e0e0;
  padding: 8px 12px;
  text-align: left;
  vertical-align: middle;
}
.dev-order-table th {
  background-color: #f7f9fc;
  font-weight: 600;
}
.dev-order-table input[type="text"],
.dev-order-table input[type="number"],
.dev-order-table select {
  width: 100%;
  border: 1px solid transparent;
  padding: 6px;
  border-radius: 4px;
  background-color: transparent;
  transition: border-color 0.2s, background-color 0.2s;
}
.dev-order-table input:focus, .dev-order-table select:focus {
  outline: none;
  border-color: #4a90e2;
  background-color: #fff;
}
.dev-order-table input[type="checkbox"] {
  width: 16px;
  height: 16px;
}
.text-center {
  text-align: center;
}
.delete-row-btn {
  background: none;
  border: none;
  color: #e53935;
  cursor: pointer;
  font-size: 18px;
  font-weight: bold;
}
.add-row-btn {
  margin-top: 12px;
  padding: 8px 16px;
  border-radius: 6px;
  border: 1px solid #4a90e2;
  background-color: #eef6ff;
  color: #4a90e2;
  cursor: pointer;
  font-weight: 600;
}
</style>