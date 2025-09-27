<template>
  <div class="modal-overlay" @click.self="$emit('close')">
    <div class="modal-content">
      <div class="modal-header">
        <h2>카테고리 관리</h2>
        <button class="close-btn" @click="$emit('close')">✕</button>
      </div>
      <div class="category-manager-body">
        <!-- 새 카테고리 추가 -->
        <div class="add-category-form">
          <input type="text" v-model="newCategoryName" placeholder="새 카테고리 이름" @keydown.enter="addCategory" />
          <button @click="addCategory" :disabled="!newCategoryName.trim()">추가</button>
        </div>

        <!-- 카테고리 목록 -->
        <ul class="category-list">
          <li v-for="category in categories" :key="category.id" class="category-item">
            <template v-if="editingCategoryId === category.id">
              <input v-model="editingCategoryName" class="edit-input" @keydown.enter="saveCategory(category.id)" @blur="cancelEdit" />
              <button @click="saveCategory(category.id)" class="action-btn save-btn">저장</button>
              <button @click="cancelEdit" class="action-btn cancel-btn">취소</button>
            </template>
            <template v-else>
              <span class="category-name">{{ category.name }}</span>
              <div class="actions">
                <button @click="startEdit(category)" class="action-btn">수정</button>
                <button @click="deleteCategory(category.id)" class="action-btn delete-btn">삭제</button>
              </div>
            </template>
          </li>
        </ul>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue';
import axios from 'axios';

const emit = defineEmits(['close']);

const categories = ref([]);
const newCategoryName = ref('');
const editingCategoryId = ref(null);
const editingCategoryName = ref('');

const axiosConfig = {
  headers: { Authorization: localStorage.getItem('authHeader') },
  withCredentials: true,
};

const fetchCategories = async () => {
  try {
    const { data } = await axios.get('/feedbacks/categories', axiosConfig);
    categories.value = data;
  } catch (error) {
    console.error('카테고리 목록 로딩 실패:', error);
    alert('카테고리 목록을 불러오는 데 실패했습니다.');
  }
};

const addCategory = async () => {
  if (!newCategoryName.value.trim()) return;
  try {
    await axios.post('/feedbacks/categories', { name: newCategoryName.value }, axiosConfig);
    newCategoryName.value = '';
    await fetchCategories();
  } catch (error) {
    console.error('카테고리 추가 실패:', error);
    alert('카테고리 추가에 실패했습니다.');
  }
};

const deleteCategory = async (id) => {
  if (!confirm('정말로 이 카테고리를 삭제하시겠습니까? 관련된 모든 피드백의 카테고리가 초기화될 수 있습니다.')) return;
  try {
    await axios.delete(`/feedbacks/categories/${id}`, axiosConfig);
    await fetchCategories();
  } catch (error) {
    console.error('카테고리 삭제 실패:', error);
    alert('카테고리 삭제에 실패했습니다.');
  }
};

const startEdit = (category) => {
  editingCategoryId.value = category.id;
  editingCategoryName.value = category.name;
};

const cancelEdit = () => {
  editingCategoryId.value = null;
  editingCategoryName.value = '';
};

const saveCategory = async (id) => {
  if (!editingCategoryName.value.trim()) return;
  try {
    await axios.put(`/feedbacks/categories/${id}`, { name: editingCategoryName.value }, axiosConfig);
    cancelEdit();
    await fetchCategories();
  } catch (error) {
    console.error('카테고리 수정 실패:', error);
    alert('카테고리 수정에 실패했습니다.');
  }
};

onMounted(fetchCategories);
</script>

<style scoped>
.modal-overlay { position: fixed; inset: 0; background: rgba(0,0,0,0.5); display: flex; justify-content: center; align-items: center; z-index: 1001; }
.modal-content { background: #fff; padding: 24px; border-radius: 12px; width: 90%; max-width: 500px; box-shadow: 0 5px 15px rgba(0,0,0,0.3); }
.modal-header { display: flex; justify-content: space-between; align-items: center; border-bottom: 1px solid #eee; padding-bottom: 16px; margin-bottom: 16px; }
.modal-header h2 { margin: 0; font-size: 20px; }
.close-btn { background: none; border: none; font-size: 24px; cursor: pointer; }

.add-category-form { display: flex; gap: 8px; margin-bottom: 20px; }
.add-category-form input { flex-grow: 1; padding: 8px 12px; border: 1px solid #ccc; border-radius: 6px; }
.add-category-form button { padding: 8px 16px; background-color: #3f8efc; color: white; border: none; border-radius: 6px; cursor: pointer; }
.add-category-form button:disabled { background-color: #ccc; }

.category-list { list-style: none; padding: 0; margin: 0; max-height: 40vh; overflow-y: auto; }
.category-item { display: flex; align-items: center; justify-content: space-between; padding: 12px 8px; border-bottom: 1px solid #f0f0f0; }
.category-name { font-weight: 500; }
.edit-input { padding: 6px 10px; border: 1px solid #3f8efc; border-radius: 4px; flex-grow: 1; margin-right: 8px; }
.actions { display: flex; gap: 8px; }
.action-btn { background: none; border: 1px solid #ccc; border-radius: 4px; padding: 4px 8px; font-size: 12px; cursor: pointer; }
.action-btn.delete-btn { border-color: #e53935; color: #e53935; }
.action-btn.save-btn { border-color: #3f8efc; color: #3f8efc; }
</style>