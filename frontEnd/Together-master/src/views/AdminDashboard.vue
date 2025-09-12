<template>
  <div class="admin-layout">
    <aside class="admin-sidebar">
      <h1>Together Admin</h1>
      <nav>
        <a @click="activeTab = 'dashboard'" :class="{ active: activeTab === 'dashboard' }">대시보드</a>
        <a @click="activeTab = 'users'" :class="{ active: activeTab === 'users' }">사용자 관리</a>
        <a @click="activeTab = 'projects'" :class="{ active: activeTab === 'projects' }">프로젝트 관리</a>
        <a @click="activeTab = 'notifications'" :class="{ active: activeTab === 'notifications' }">알림 관리</a>
      </nav>
    </aside>
    <main class="admin-main-content">
      <div v-if="activeTab === 'dashboard'">
        <DashboardHome />
      </div>
      <div v-if="activeTab === 'users'">
        <UserManagement />
      </div>
      <div v-if="activeTab === 'projects'">
        <ProjectManagement />
      </div>
      <div v-if="activeTab === 'notifications'">
        <NotificationSender />
      </div>
    </main>
  </div>
</template>

<script>
import DashboardHome from '../components/admin/DashboardHome.vue';
import UserManagement from '../components/admin/UserManagement.vue';
import ProjectManagement from '../components/admin/ProjectManagement.vue';
import NotificationSender from '../components/admin/NotificationSender.vue';

export default {
  name: 'AdminDashboard',
  components: {
    DashboardHome,
    UserManagement,
    ProjectManagement,
    NotificationSender
  },
  data() {
    return {
      activeTab: 'dashboard' // 기본으로 보여줄 탭
    };
  }
};
</script>

<style scoped>
.admin-layout {
  display: flex;
  height: 100vh;
  background-color: #f0f2f5;
}
.admin-sidebar {
  width: 220px;
  background-color: #333;
  color: #fff;
  padding: 20px;
  height: 100%;
}
.admin-sidebar h1 {
  font-size: 24px;
  text-align: center;
  margin-bottom: 30px;
}
.admin-sidebar nav a {
  display: block;
  color: #ddd;
  padding: 12px 15px;
  text-decoration: none;
  border-radius: 4px;
  margin-bottom: 10px;
  cursor: pointer;
  transition: background-color 0.3s;
}
.admin-sidebar nav a:hover, .admin-sidebar nav a.active {
  background-color: #555;
  color: #fff;
}
.admin-main-content {
  flex-grow: 1;
  padding: 30px;
  overflow-y: auto;
}
</style>