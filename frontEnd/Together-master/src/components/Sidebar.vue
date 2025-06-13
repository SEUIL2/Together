<!-- <template>
    <aside class="sidebar">
      <h3 class="sidebar-title"></h3>
      <ul class="menu">
        <li class="menu-item" :class="{ active: currentMenu === '기획' }">
          <button class="menu-title" @click="toggleMenu('기획')">기획</button>
          <ul v-if="currentMenu === '기획'" class="submenu">
            <li>프로젝트 동기</li>
            <li>프로젝트 목표</li>
            <li>요구사항 정의</li>
            <li>정보구조도</li>
            <li>스토리보드</li>
          </ul>
        </li>
        <li class="menu-item" :class="{ active: currentMenu === '설계' }">
          <button class="menu-title" @click="toggleMenu('설계')">설계</button>
        </li>
        <li class="menu-item" :class="{ active: currentMenu === '개발' }">
          <button class="menu-title" @click="toggleMenu('개발')">개발</button>
        </li>
        <li class="menu-item" :class="{ active: currentMenu === '테스트' }">
          <button class="menu-title" @click="toggleMenu('테스트')">테스트</button>
        </li>
      </ul>
    </aside>
  </template>
  
  <script setup>
  import { ref } from 'vue'
  
  const currentMenu = ref('')
  
  function toggleMenu(menuName) {
    currentMenu.value = currentMenu.value === menuName ? '' : menuName
  }
  </script>
  
  <style scoped>
  /* 좌측 사이드바 (고정) */
  .sidebar {
  position: fixed;
  top: 60px;
  margin-top: 20px;
  left: 0;
  bottom: 20px;
  width: 220px;
  padding: 20px;
  background-color: #fafafa;
  border-right: 1px solid #ddd;
  box-shadow: 2px 0 8px rgba(0, 0, 0, 0.1);
  border-top-right-radius: 10px;
  border-bottom-right-radius: 10px;
}
  
  /* 사이드바 제목 */
  .sidebar-title {
    font-size: 1.2rem;
    font-weight: bold;
    margin-bottom: 15px;
    text-align: center;
    color: #3f8efc;
  }
  
  /* 메뉴 리스트 */
  .menu {
    list-style: none;
    padding: 0;
    margin: 0;
  }
  
  /* 메뉴 아이템 */
  .menu-item {
    margin-bottom: 10px;
  }
  
  /* 메뉴 버튼 */
  .menu-title {
    width: 100%;
    padding: 10px;
    background-color: #eee;
    border: none;
    border-radius: 5px;
    text-align: left;
    cursor: pointer;
    transition: background 0.3s, transform 0.3s;
  }
  
  .menu-title:hover {
    background-color: #d4d4d4;
    transform: translateX(2px);
  }
  
  /* 활성화된 메뉴 스타일 */
  .menu-item.active .menu-title {
    background-color: #3f8efc;
    color: #fff;
  }
  
  /* 서브메뉴 */
  .submenu {
    list-style: none;
    padding-left: 15px;
    margin-top: 5px;
  }
  
  .submenu li {
    margin-bottom: 5px;
    font-size: 0.9rem;
    cursor: pointer;
    transition: color 0.3s;
  }
  
  .submenu li:hover {
    color: #3f8efc;
  }
  </style>
   -->