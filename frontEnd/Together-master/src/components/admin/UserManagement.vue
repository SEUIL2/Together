<template>
  <div>
    <h2>전체 가입 사용자 목록</h2>
    <div class="toolbar">
      <input type="text" placeholder="사용자 아이디 또는 이름으로 검색..." v-model="searchQuery" @input="filterUsers" />
    </div>
    <table class="data-table">
      <thead>
      <tr>
        <th>아이디</th>
        <th>이름</th>
        <th>이메일</th>
        <th>가입일</th>
        <th>역할</th>
        <th>작업</th>
      </tr>
      </thead>
      <tbody>
      <tr v-for="user in filteredUsers" :key="user.id">
        <td>{{ user.loginId }}</td>
        <td>{{ user.name }}</td>
        <td>{{ user.email }}</td>
        <td>{{ user.joinDate }}</td>
        <td>{{ user.role }}</td>
        <td><button class="btn-sm btn-danger" @click="deleteUser(user.id)">삭제</button></td>
      </tr>
      </tbody>
    </table>
  </div>
</template>

<script>
export default {
  name: 'UserManagement',
  data() {
    return {
      searchQuery: '',
      users: [
        { id: 1, loginId: 'dev_park', name: '박서준', email: 'sjpark@example.com', joinDate: '2025-09-12', role: 'STUDENT' },
        { id: 2, loginId: 'designer_kim', name: '김지민', email: 'jmkim@example.com', joinDate: '2025-09-12', role: 'STUDENT' },
        { id: 3, loginId: 'plan_lee', name: '이수빈', email: 'subin.lee@example.com', joinDate: '2025-09-11', role: 'STUDENT' },
        { id: 4, loginId: 'choi_jh', name: '최정훈', email: 'jh.choi@example.com', joinDate: '2025-09-11', role: 'STUDENT' },
        { id: 5, loginId: 'yoon_sm', name: '윤세미', email: 'sem.yoon@example.com', joinDate: '2025-09-10', role: 'STUDENT' },
        { id: 6, loginId: 'professor_kang', name: '강현우', email: 'hw.kang@university.ac.kr', joinDate: '2025-09-10', role: 'PROFESSOR' },
        { id: 7, loginId: 'hong_gd', name: '홍길동', email: 'gd.hong@example.com', joinDate: '2025-09-09', role: 'STUDENT' },
        { id: 8, loginId: 'song_hj', name: '송혜교', email: 'hjsong@example.com', joinDate: '2025-09-09', role: 'STUDENT' },
        { id: 9, loginId: 'im_sy', name: '임서연', email: 'sy.im@example.com', joinDate: '2025-09-08', role: 'STUDENT' },
        { id: 10, loginId: 'hwang_mj', name: '황민준', email: 'minjun.hwang@example.com', joinDate: '2025-09-08', role: 'STUDENT' },
        { id: 11, loginId: 'tester_jung', name: '정다솜', email: 'ds.jung@example.com', joinDate: '2025-09-07', role: 'STUDENT' },
        { id: 12, loginId: 'ahn_sh', name: '안성훈', email: 'sh.ahn@example.com', joinDate: '2025-09-06', role: 'STUDENT' },
        { id: 13, loginId: 'baek_yh', name: '백유하', email: 'yha.baek@example.com', joinDate: '2025-09-06', role: 'STUDENT' },
        { id: 14, loginId: 'seo_hj', name: '서현진', email: 'hj.seo@example.com', joinDate: '2025-09-05', role: 'STUDENT' },
        { id: 15, loginId: 'ryu_jw', name: '류정우', email: 'jw.ryu@example.com', joinDate: '2025-09-05', role: 'STUDENT' },
        { id: 16, loginId: 'professor_yoo', name: '유재석', email: 'js.yoo@university.ac.kr', joinDate: '2025-09-04', role: 'PROFESSOR' },
        { id: 17, loginId: 'moon_ch', name: '문채원', email: 'ch.moon@example.com', joinDate: '2025-09-04', role: 'STUDENT' },
        { id: 18, loginId: 'kwon_jy', name: '권지용', email: 'jy.kwon@example.com', joinDate: '2025-09-03', role: 'STUDENT' },
        { id: 19, loginId: 'na_sj', name: '나선재', email: 'sj.na@example.com', joinDate: '2025-09-03', role: 'STUDENT' },
        { id: 20, loginId: 'han_yj', name: '한예준', email: 'yj.han@example.com', joinDate: '2025-09-02', role: 'STUDENT' },
        { id: 21, loginId: 'oh_es', name: '오은서', email: 'es.oh@example.com', joinDate: '2025-09-02', role: 'STUDENT' },
        { id: 22, loginId: 'jang_wh', name: '장원호', email: 'wh.jang@example.com', joinDate: '2025-09-01', role: 'STUDENT' },
        { id: 23, loginId: 'jo_mh', name: '조민희', email: 'mh.jo@example.com', joinDate: '2025-09-01', role: 'STUDENT' },
        { id: 24, loginId: 'shin_eh', name: '신은하', email: 'eh.shin@example.com', joinDate: '2025-08-31', role: 'STUDENT' },
        { id: 25, loginId: 'cha_dw', name: '차도원', email: 'dw.cha@example.com', joinDate: '2025-08-30', role: 'STUDENT' },
        { id: 26, loginId: 'pyo_jy', name: '표지영', email: 'jy.pyo@example.com', joinDate: '2025-08-29', role: 'STUDENT' },
        { id: 27, loginId: 'sun_dh', name: '선동혁', email: 'dh.sun@example.com', joinDate: '2025-08-28', role: 'STUDENT' },
        { id: 28, loginId: 'kang_sol', name: '강솔', email: 'sol.kang@example.com', joinDate: '2025-08-27', role: 'STUDENT' },
        { id: 29, loginId: 'lim_js', name: '임지섭', email: 'js.lim@example.com', joinDate: '2025-08-26', role: 'STUDENT' },
        { id: 30, loginId: 'professor_ha', name: '하동훈', email: 'dh.ha@university.ac.kr', joinDate: '2025-08-25', role: 'PROFESSOR' },
        { id: 31, loginId: 'yoo_ih', name: '유인하', email: 'ih.yoo@example.com', joinDate: '2025-08-24', role: 'STUDENT' },
        { id: 32, loginId: 'jung_tk', name: '정태경', email: 'tk.jung@example.com', joinDate: '2025-08-23', role: 'STUDENT' },
        { id: 33, loginId: 'bae_ch', name: '배철현', email: 'ch.bae@example.com', joinDate: '2025-08-22', role: 'STUDENT' },
        { id: 34, loginId: 'ki_sy', name: '기서영', email: 'sy.ki@example.com', joinDate: '2025-08-21', role: 'STUDENT' },
      ],
      filteredUsers: []
    };
  },
  created() {
    this.filteredUsers = this.users;
  },
  methods: {
    filterUsers() {
      if (!this.searchQuery) {
        this.filteredUsers = this.users;
        return;
      }
      this.filteredUsers = this.users.filter(u =>
          u.loginId.includes(this.searchQuery) || u.name.includes(this.searchQuery)
      );
    },
    deleteUser(userId) {
      alert(`(데모) ID: ${userId} 사용자를 삭제합니다.`);
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