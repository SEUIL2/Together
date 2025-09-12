<template>
  <div>
    <h2>사용자에게 알림 전송</h2>
    <div class="notification-layout">
      <div class="widget form-widget">
        <div class="form-group">
          <label for="userId">사용자 아이디</label>
          <input type="text" id="userId" v-model="targetUser" placeholder="오른쪽 목록에서 선택하세요" />
        </div>
        <div class="form-group">
          <label for="message">알림 내용</label>
          <textarea id="message" v-model="message" rows="4" placeholder="전송할 메시지를 입력하세요..."></textarea>
        </div>
        <button class="btn-primary" @click="sendNotification">알림 보내기</button>
      </div>

      <div class="widget user-list-widget">
        <h3>사용자 목록</h3>
        <ul class="user-list">
          <li v-for="user in users" :key="user.id" @click="selectUser(user)">
            <strong>{{ user.name }}</strong> ({{ user.loginId }})
          </li>
        </ul>
      </div>
    </div>
  </div>
</template>

<script>
export default {
  name: 'NotificationSender',
  data() {
    return {
      targetUser: '',
      message: '',
      // 사용자 목록 데이터 (UserManagement.vue와 동일하게 유지)
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
        // ... 필요하면 더 많은 사용자 추가
      ]
    };
  },
  methods: {
    selectUser(user) {
      this.targetUser = user.loginId;
    },
    sendNotification() {
      if (!this.targetUser || !this.message) {
        alert('사용자 아이디와 내용을 모두 입력해주세요.');
        return;
      }
      alert(`(데모) '${this.targetUser}'에게 알림을 전송했습니다:\n${this.message}`);
      this.targetUser = '';
      this.message = '';
    }
  }
};
</script>

<style scoped>
/* [추가] 레이아웃 스타일 */
.notification-layout {
  display: grid;
  grid-template-columns: 2fr 1fr; /* 2:1 비율로 분할 */
  gap: 30px;
}

.widget {
  background: #fff;
  padding: 30px;
  border-radius: 8px;
  box-shadow: 0 2px 4px rgba(0,0,0,0.1);
}
.form-group {
  margin-bottom: 20px;
}
.form-group label {
  display: block;
  margin-bottom: 5px;
  font-weight: bold;
}
.form-group input, .form-group textarea {
  width: 100%;
  padding: 10px;
  border: 1px solid #ccc;
  border-radius: 4px;
  box-sizing: border-box; /* 패딩이 너비에 포함되도록 설정 */
}
.btn-primary {
  padding: 10px 20px;
  background-color: #007bff;
  color: white;
  border: none;
  border-radius: 4px;
  cursor: pointer;
}

/* [추가] 사용자 목록 스타일 */
.user-list-widget h3 {
  margin-top: 0;
}
.user-list {
  list-style: none;
  padding: 0;
  max-height: 220px; /* 목록 최대 높이 지정 */
  overflow-y: auto; /* 내용이 많아지면 스크롤 생성 */
}
.user-list li {
  padding: 10px;
  border-bottom: 1px solid #eee;
  cursor: pointer;
  transition: background-color 0.2s;
}
.user-list li:hover {
  background-color: #f5f5f5;
}
.user-list li:last-child {
  border-bottom: none;
}
</style>