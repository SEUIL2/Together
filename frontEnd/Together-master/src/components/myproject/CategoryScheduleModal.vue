<template>
  <div class="modal-overlay" @click.self="$emit('close')">
    <div class="modal-content">
      <div class="modal-header">
        <h3>{{ categoryName }} 일정</h3>
        <input type="text" v-model="searchTerm" @keydown.enter="goToFirstResult" placeholder="작업 검색 후 Enter" class="search-input" />
        <button class="close-btn" @click="$emit('close')">×</button>
      </div>
      <div class="modal-body">
        <div class="calendar-wrapper">
          <FullCalendar ref="fullCalendar" :options="calendarOptions" />
        </div>
        <div class="task-list-wrapper">
          <div class="task-list-header">
            <h4>작업 목록 ({{ filteredEvents.length }}개)</h4>
            <button v-if="!readonly" @click="startCreatingNewTask" class="new-task-btn">+</button>
          </div>
          <ul class="task-list">
            <li v-for="event in filteredEvents" :key="event.id" @click="goToDate(event.start)" class="task-list-item">
              <div class="task-title">{{ event.title }}</div>
              <div class="task-meta">👤 {{ event.extendedProps.assignee }}</div>
            </li>
          </ul>
        </div>
      </div>
    </div>

    <!-- 작업 상세 정보 모달 -->
    <div v-if="showDetailModal" class="modal-overlay detail-modal-overlay" @click.self="showDetailModal = false">
      <div class="detail-modal-content">
        <div class="form-group">
          <label>작업명</label>
          <input type="text" v-model="editingEvent.title" />
        </div>
        <div class="form-group">
          <label>카테고리</label>
          <select v-model="editingEvent.category">
            <option disabled value="">카테고리 선택</option>
            <option v-for="(label, key) in categories" :key="key" :value="key">{{ label }}</option>
          </select>
        </div>
        <div class="form-group">
          <label>담당자</label>
          <select v-model="editingEvent.assignee">
            <option value="">미지정</option>
            <option v-for="member in teamMembersWithColor" :key="member.userId" :value="member.userName">{{ member.userName }}</option>
          </select>
        </div>
        <div class="form-group-row">
          <div class="form-group">
            <label>시작일</label>
            <input type="date" v-model="editingEvent.start" />
          </div>
          <div class="form-group">
            <label>종료일</label>
            <input type="date" v-model="editingEvent.end" />
          </div>
        </div>
        <div class="detail-actions">
          <button @click="cancelEdit" class="detail-cancel-btn">취소</button>
          <button v-if="editingEvent.id" @click="deleteTask" class="detail-delete-btn">삭제</button>
          <button @click="saveChanges" class="detail-save-btn">저장</button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue';
import FullCalendar from '@fullcalendar/vue3';
import dayGridPlugin from '@fullcalendar/daygrid';
import interactionPlugin from '@fullcalendar/interaction';
import api from '@/api';

const props = defineProps({
  projectId: { type: [String, Number], required: true },
  category: { type: String, required: true },
  readonly: { type: Boolean, default: true }
});

const emit = defineEmits(['close']);

const categories = {
  PLANNING: '기획',
  DESIGN: '설계',
  DEVELOPMENT: '개발',
  TEST: '테스트',
};

const categoryName = computed(() => categories[props.category] || '프로젝트');
const allEvents = ref([]);
const searchTerm = ref('');
const fullCalendar = ref(null);
const showDetailModal = ref(false);
const selectedEvent = ref(null);
const isEditingDetail = ref(false);
const editingEvent = ref({});
const teamMembersWithColor = ref([]);

const filteredEvents = computed(() => {
  if (!searchTerm.value) {
    return allEvents.value;
  }
  const lowerCaseSearch = searchTerm.value.toLowerCase();
  return allEvents.value.filter(event =>
    event.title.toLowerCase().includes(lowerCaseSearch)
  );
});

const calendarOptions = computed(() => ({
  plugins: [dayGridPlugin, interactionPlugin],
  initialView: 'dayGridMonth',
  headerToolbar: {
    left: 'prev,next today',
    center: 'title',
    right: ''
  },
  events: filteredEvents.value,
  locale: 'ko',
  dayMaxEvents: true, // 이벤트가 많을 때 '더보기' 링크 표시
  eventDisplay: 'block',
  height: '100%',
  editable: !props.readonly, // readonly가 아닐 때만 수정 가능
  eventClick: handleEventClick,
  eventDrop: handleEventChange,
  eventResize: handleEventChange,
}));

function handleEventClick(clickInfo) {
  // "+more" 팝업에서 이벤트를 클릭했을 때, 해당 팝업을 닫습니다.
  clickInfo.jsEvent.target.closest('.fc-popover')?.remove();

  if (props.readonly) return;

  selectedEvent.value = clickInfo.event;
  startEditing();
}

// YYYY-MM-DD 형식으로 변환하는 헬퍼 함수 (시간대 문제 방지)
const toYYYYMMDD = (d) => {
  const date = new Date(d);
  const year = date.getFullYear();
  const month = (date.getMonth() + 1).toString().padStart(2, '0');
  const day = date.getDate().toString().padStart(2, '0');
  return `${year}-${month}-${day}`;
};

async function handleEventChange(changeInfo) {
  if (props.readonly) return;

  const { id, start, end } = changeInfo.event;

  // FullCalendar의 end는 exclusive이므로, inclusive한 날짜로 변경하기 위해 하루를 뺍니다.
  const inclusiveEndDate = new Date(end.getTime() - (24 * 60 * 60 * 1000));

  const payload = {
    startDate: toYYYYMMDD(start),
    endDate: toYYYYMMDD(inclusiveEndDate),
  };

  try {
    await api.patch(`/work-tasks/${id}/schedule`, payload);
    // 변경 성공 후, 데이터 동기화를 위해 전체 작업을 다시 불러옵니다.
    await fetchTasks();
  } catch (err) {
    console.error('일정 업데이트 실패:', err);
    alert('일정 업데이트에 실패했습니다. 원래 위치로 되돌립니다.');
    changeInfo.revert(); // API 업데이트 실패 시, 캘린더 이벤트를 원래 위치로 되돌립니다.
  }
}

function startEditing() {
  isEditingDetail.value = true;
  showDetailModal.value = true;
  const event = selectedEvent.value;
  // FullCalendar의 end는 exclusive이므로, 표시 및 수정을 위해 하루를 빼줍니다.
  const inclusiveEndDate = new Date(event.end.getTime() - (24 * 60 * 60 * 1000));

  editingEvent.value = {
    id: event.id,
    title: event.title,
    start: toYYYYMMDD(event.start),
    end: toYYYYMMDD(inclusiveEndDate),
    assignee: event.extendedProps.assignee,
    category: event.extendedProps.category,
  };
}

function cancelEdit() {
  isEditingDetail.value = false;
  showDetailModal.value = false;
}

function startCreatingNewTask() {
  editingEvent.value = {
    id: null, // id가 null이면 '생성' 모드
    title: '',
    start: toYYYYMMDD(new Date()),
    end: toYYYYMMDD(new Date()),
    assignee: '',
    category: props.category, // 현재 모달의 카테고리를 기본값으로 설정
  };
  isEditingDetail.value = true;
  showDetailModal.value = true;
}


async function saveChanges() {
  const member = teamMembersWithColor.value.find(m => m.userName === editingEvent.value.assignee);
  const assignedUserId = member ? member.userId : null;

  const payload = {
    title: editingEvent.value.title,
    startDate: editingEvent.value.start,
    endDate: editingEvent.value.end,
    assignedUserId: assignedUserId,
    category: editingEvent.value.category,
  };

  try {
    if (editingEvent.value.id) {
      // 작업 수정
      await api.patch(`/work-tasks/${editingEvent.value.id}`, payload);
      alert('작업이 성공적으로 수정되었습니다.');
    } else {
      // 새 작업 생성
      await api.post('/work-tasks', { ...payload, status: 'PENDING' });
      alert('작업이 성공적으로 생성되었습니다.');
    }

    isEditingDetail.value = false;
    showDetailModal.value = false;
    // 변경된 내용을 달력에 즉시 반영하기 위해 데이터를 다시 불러옵니다.
    await fetchTasks();
  } catch (err) {
    console.error('작업 저장 실패:', err);
    alert('작업 저장 중 오류가 발생했습니다.');
  }
}

async function deleteTask() {
  if (!confirm(`'${editingEvent.value.title}' 작업을 정말 삭제하시겠습니까?\n이 작업은 영구적으로 삭제됩니다.`)) {
    return;
  }

  try {
    await api.delete(`/work-tasks/${editingEvent.value.id}`);
    alert('작업이 삭제되었습니다.');
    showDetailModal.value = false;
    await fetchTasks(); // 달력 새로고침
  } catch (err) {
    console.error('작업 삭제 실패:', err);
    alert('작업 삭제에 실패했습니다.');
  }
}

const goToFirstResult = () => {
  if (filteredEvents.value.length > 0) {
    const firstEvent = filteredEvents.value[0];
    const eventStartDate = firstEvent.start;

    if (fullCalendar.value) {
      const calendarApi = fullCalendar.value.getApi();
      calendarApi.gotoDate(eventStartDate);
    }
  }
};

const goToDate = (date) => {
  if (fullCalendar.value) {
    const calendarApi = fullCalendar.value.getApi();
    calendarApi.gotoDate(date);
  }
};

async function fetchTeamMembers() {
  try {
    const { data } = await api.get('/projects/members/students', { params: { projectId: props.projectId } });
    teamMembersWithColor.value = data;
  } catch (e) {
    console.error('팀원 정보 가져오기 실패', e);
  }
}

async function fetchTasks() {
  try {
    const res = await api.get('/work-tasks/project', { params: { projectId: props.projectId } });
    const filteredTasks = res.data.filter(task => task.category === props.category);

    allEvents.value = filteredTasks.map(task => {
      const member = teamMembersWithColor.value.find(m => m.userName === task.assignedUserName);
      const eventColor = member ? member.userColor : '#868e96'; // 담당자가 있으면 해당 색상, 없으면 회색

      const start = new Date(task.startDate);
      const end = new Date(task.endDate);
      const duration = Math.ceil((end.getTime() - start.getTime()) / (1000 * 60 * 60 * 24)) + 1;

      return {
        id: task.id,
        title: task.title,
        start: task.startDate,
        end: new Date(end.getTime() + 24 * 60 * 60 * 1000).toISOString().split('T')[0],
        startStr: new Date(task.startDate).toLocaleDateString('ko-KR'),
        endStr: end.toLocaleDateString('ko-KR'),
        backgroundColor: eventColor,
        borderColor: eventColor,
        extendedProps: {
          category: task.category,
          dbEndDate: task.endDate, // DB 원본 종료일 저장
          assignee: task.assignedUserName || '미지정',
          duration: duration,
        }
      }
    });
  } catch (e) {
    console.error('작업 불러오기 실패', e);
  }
}

onMounted(async () => {
  await fetchTeamMembers();
  await fetchTasks();
});

</script>

<style scoped>
.modal-overlay {
  position: fixed;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background-color: rgba(0, 0, 0, 0.6);
  display: flex;
  justify-content: center;
  align-items: center;
  z-index: 1000;
}

.modal-content {
  background: #fff;
  padding: 20px;
  border-radius: 12px;
  width: 90%;
  max-width: 1000px;
  height: 80vh;
  display: flex;
  flex-direction: column;
  box-shadow: 0 5px 15px rgba(0,0,0,0.3);
}

.modal-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 16px;
  border-bottom: 1px solid #eee;
  padding-bottom: 10px;
  margin-bottom: 20px;
}

.modal-header h3 {
  flex-shrink: 0;
  margin: 0;
  font-size: 20px;
}

.search-input {
  flex-grow: 1;
  padding: 8px 12px;
  border: 1px solid #ddd;
  border-radius: 8px;
  font-size: 14px;
}
.search-input:focus {
  outline: none;
  border-color: #3f8efc;
  box-shadow: 0 0 0 2px rgba(63, 142, 252, 0.2);
}

.close-btn {
  background: none;
  border: none;
  font-size: 24px;
  cursor: pointer;
}

.modal-body {
  flex-grow: 1;
  display: flex;
  gap: 20px;
  position: relative;
  overflow: hidden;
}

.calendar-wrapper {
  flex: 3;
  min-width: 0;
}

.task-list-wrapper {
  flex: 1;
  border-left: 1px solid #eee;
  padding-left: 20px;
  display: flex;
  flex-direction: column;
}

/* FullCalendar 스타일 오버라이드 */
:deep(.fc) {
  height: 100%;
}

.task-list-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin: 0 0 10px 0;
}

.task-list-header h4 {
  font-size: 16px;
  font-weight: 600;
  color: #333;
  margin: 0;
}

.new-task-btn {
  background-color: #3f8efc;
  color: white;
  border: none;
  border-radius: 50%;
  width: 24px;
  height: 24px;
  font-size: 18px;
  line-height: 24px;
  cursor: pointer;
}

.task-list {
  list-style: none;
  padding: 0;
  margin: 0;
  overflow-y: auto;
  flex-grow: 1;
}

.task-list-item {
  padding: 10px;
  border-radius: 6px;
  cursor: pointer;
  transition: background-color 0.2s;
  border-bottom: 1px solid #f0f0f0;
}

.task-list-item:hover {
  background-color: #f7f8fc;
}

.task-title {
  font-size: 14px;
  font-weight: 500;
  color: #343a40;
  margin-bottom: 4px;
}

.task-meta {
  font-size: 12px;
  color: #6c757d;
}


.detail-modal-overlay {
  background-color: rgba(0, 0, 0, 0.2); /* 상세 모달은 배경을 약간만 어둡게 */
  z-index: 1001; /* 메인 모달보다 위에 표시 */
}

.detail-modal-content {
  background: #fff;
  padding: 24px;
  border-radius: 10px;
  width: 90%;
  max-width: 400px;
  box-shadow: 0 4px 15px rgba(0,0,0,0.2);
}

.detail-modal-content h4 {
  margin-top: 0;
  margin-bottom: 16px;
  font-size: 18px;
  font-weight: 600;
}

.detail-modal-content ul {
  list-style: none;
  padding: 0;
  margin: 0 0 24px 0;
  font-size: 15px;
}

.detail-modal-content li {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 8px 0;
  border-bottom: 1px solid #f0f0f0;
}
.detail-modal-content li:last-child {
  border-bottom: none;
}

.form-group {
  margin-bottom: 16px;
}
.form-group label {
  display: block;
  font-weight: 600;
  font-size: 14px;
  margin-bottom: 6px;
  color: #555;
}
.form-group input, .form-group select {
  width: 100%;
  padding: 10px;
  border: 1px solid #ccc;
  border-radius: 6px;
  font-size: 15px;
}
.form-group-row {
  display: flex;
  gap: 16px;
}

.detail-modal-content strong {
  color: #555;
  width: 70px;
}

.detail-actions {
  display: flex;
  justify-content: flex-end;
  gap: 10px;
  margin-top: 24px;
}

.detail-close-btn, .detail-edit-btn, .detail-save-btn, .detail-cancel-btn, .detail-delete-btn {
  padding: 10px;
  border: none;
  border-radius: 8px;
  cursor: pointer;
  font-weight: 600;
  flex-grow: 1;
}

.detail-delete-btn {
  background-color: #dc3545;
  color: white;
}
.detail-close-btn, .detail-save-btn {
  background-color: #3f8efc;
  color: white;
}
.detail-edit-btn, .detail-cancel-btn {
  background-color: #e9ecef;
  color: #495057;
}

.category-badge {
  font-size: 12px;
  padding: 3px 8px;
  border-radius: 12px;
  color: white;
}
.category-PLANNING { background-color: #ffaeae; }
.category-DESIGN { background-color: #f39c12; }
.category-DEVELOPMENT { background-color: #2ecc71; }
.category-TEST { background-color: #9b59b6; }
</style>