<template>
  <div>
    <h2>전체 프로젝트 목록</h2>
    <div class="toolbar">
      <input type="text" placeholder="프로젝트명 또는 팀장명으로 검색..." v-model="searchQuery" @input="filterProjects" />
    </div>
    <table class="data-table">
      <thead>
      <tr>
        <th>ID</th>
        <th>프로젝트명</th>
        <th>팀장</th>
        <th>생성일</th>
        <th>멤버 수</th>
        <th>작업</th>
      </tr>
      </thead>
      <tbody>
      <tr v-for="project in filteredProjects" :key="project.id">
        <td>{{ project.id }}</td>
        <td>{{ project.name }}</td>
        <td>{{ project.leader }}</td>
        <td>{{ project.createDate }}</td>
        <td>{{ project.members }}</td>
        <td><button class="btn-sm btn-danger" @click="deleteProject(project.id)">삭제</button></td>
      </tr>
      </tbody>
    </table>
  </div>
</template>

<script>
export default {
  name: 'ProjectManagement',
  data() {
    return {
      searchQuery: '',
      projects: [
        { id: 101, name: 'AI 기반 스마트 캠퍼스 안내 시스템', leader: '박서준', createDate: '2025-08-01', members: 5 },
        { id: 102, name: '실시간 온라인 협업 화이트보드', leader: '김지민', createDate: '2025-08-15', members: 5 },
        { id: 103, name: '메타버스 기반 가상 도서관', leader: '최정훈', createDate: '2025-09-01', members: 5 },
        { id: 104, name: '블록체인 기반 중고거래 플랫폼', leader: '홍길동', createDate: '2025-08-20', members: 5 },
        { id: 105, name: '반려동물 산책 경로 추천 앱', leader: '송혜교', createDate: '2025-09-05', members: 5 },
        { id: 106, name: '음성인식 회의록 자동 작성 서비스', leader: '황민준', createDate: '2025-08-28', members: 4 },
        { id: 107, name: 'AR을 활용한 실내 인테리어 앱', leader: '정다솜', createDate: '2025-09-10', members: 5 },
      ],
      filteredProjects: []
    };
  },
  created() {
    this.filteredProjects = this.projects;
  },
  methods: {
    filterProjects() {
      if (!this.searchQuery) {
        this.filteredProjects = this.projects;
        return;
      }
      this.filteredProjects = this.projects.filter(p =>
          p.name.includes(this.searchQuery) || p.leader.includes(this.searchQuery)
      );
    },
    deleteProject(projectId) {
      alert(`(데모) Project ID: ${projectId} 프로젝트를 삭제합니다.`);
    }
  }
};
</script>

<style scoped>
.toolbar { margin-bottom: 20px; }
.toolbar input { padding: 8px; width: 300px; border: 1px solid #ccc; border-radius: 4px; }
.data-table { width: 100%; border-collapse: collapse; background: #fff; }
.data-table th, .data-table td { border: 1px solid #ddd; padding: 12px; text-align: left; }
.data-table th { background-color: #f8f9fa; }
.btn-sm { padding: 4px 8px; font-size: 12px; border-radius: 4px; cursor: pointer; border: none; }
.btn-danger { background-color: #dc3545; color: white; }
</style>