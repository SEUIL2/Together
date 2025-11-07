<template>
  <div class="main-container">
    <header class="top-nav" role="navigation" aria-label="주요 메뉴">
      <div class="brand" @click="goHome" role="button" tabindex="0" aria-label="TOGETHER 홈으로 이동">
        <span class="logo-dot" aria-hidden="true"></span>
        <span class="logo-text">TOGETHER</span>
      </div>
      <div class="nav-right">
        <div v-if="displayName" class="user-pill" title="로그인 사용자">
          <div class="avatar" aria-hidden="true">{{ initials }}</div>
          <span class="username">{{ displayName }}</span>
        </div>
      </div>
    </header>

    <main class="hero" role="main">
      <div class="hero-inner glass">
        <p class="eyebrow">졸업작품을 위한 든든한 동반자</p>
        <h1 class="headline">TOGETHER</h1>
        <p class="tagline">팀 협업, 일정, 회의, 다이어그램까지 한 곳에서. 이제는 시작이 더 쉬워집니다.</p>
        <div class="cta-row">
          <button class="btn cta" aria-label="10초 만에 프로젝트 생성 시작" @click="noop">
            10초만에 프로젝트 생성하기
            <span class="shine" aria-hidden="true"></span>
          </button>
        </div>
        <div class="trust">
          <span class="dot" aria-hidden="true"></span>
          이미 수많은 팀이 함께하고 있어요
        </div>
      </div>
    </main>

    <footer class="foot" role="contentinfo">
      © {{ new Date().getFullYear() }} TOGETHER • Better Projects, Together
    </footer>
  </div>
  
</template>

<script>
import { ref, onMounted, computed } from 'vue'
import { useRouter } from 'vue-router'
import api from '@/api'

export default {
  name: 'MainPage2',
  setup() {
    const router = useRouter();
    const currentUser = ref(null);

    onMounted(async () => {
      try {
        const response = await api.get('/auth/me');
        currentUser.value = response.data;
        console.log('로그인된 사용자 정보:', response.data);
      } catch (error) {
        console.warn('로그인된 사용자 정보 없음:', error?.message || error);
      }
    });

    const displayName = computed(() => {
      const u = currentUser.value;
      if (!u) return null;
      return (
        u.name || u.nickname || u.username || u.displayName || u.email || '사용자'
      );
    });

    const initials = computed(() => {
      if (!displayName.value) return '';
      return displayName.value
        .toString()
        .trim()
        .split(/\s+/)
        .map(p => p[0])
        .slice(0, 2)
        .join('')
        .toUpperCase();
    });

  const goToCreate = () => router.push('/CreateProject');
  const noop = () => { /* 클릭 무효: 향후 활성화 가능 */ };
    const goHome = () => router.push('/');

  return { goToCreate, goHome, currentUser, displayName, initials, noop };
  }
}
</script>

<style scoped>
/* 색상/타이포 변수 (컴포넌트 범위) */
.main-container {
  /* 라이트 기본값 (흰색 & 밝은 파란색 메인) */
  --bg-1: #ffffff;
  --bg-2: #ffffff;
  --card: rgba(255, 255, 255, 0.72);
  --card-border: rgba(15, 23, 42, 0.06);
  --card-highlight: rgba(15, 23, 42, 0.08);
  --text-1: #0f172a;  /* slate-900 */
  --text-2: #475569;  /* slate-600 */
  --accent-1: #3b82f6; /* 브랜드 블루(고정) */
  --accent-2: #60a5fa; /* 라이트 블루 */
  --accent-3: #93c5fd; /* 소프트 블루 */
  --ok: #0ea5e9;      /* 시그널 블루 */

  min-height: 100vh;
  width: 100%;
  color: var(--text-1);
  background:
    radial-gradient(1200px 600px at 80% -10%, rgba(59, 130, 246, 0.10), transparent 60%),
    radial-gradient(900px 500px at 10% 10%, rgba(147, 197, 253, 0.08), transparent 60%),
    linear-gradient(180deg, var(--bg-1), var(--bg-2));
  position: relative;
  display: flex;
  flex-direction: column;
  overflow: clip;
}

/* 배경 장식 (부드러운 그라디언트 블럽) */
.main-container::before,
.main-container::after {
  content: '';
  position: absolute;
  inset: auto;
  width: 56vmin;
  height: 56vmin;
  border-radius: 50%;
  filter: blur(60px);
  opacity: 0.5;
  pointer-events: none;
  z-index: 0;
}
.main-container::before {
  background: radial-gradient(circle at 30% 30%, color-mix(in oklab, var(--accent-3) 60%, transparent), transparent 60%);
  top: -8vmin; left: -8vmin;
  animation: floatA 14s ease-in-out infinite;
}
.main-container::after {
  background: radial-gradient(circle at 60% 60%, color-mix(in oklab, var(--accent-2) 60%, transparent), transparent 60%);
  bottom: -10vmin; right: -6vmin;
  animation: floatB 18s ease-in-out infinite;
}

/* 상단 네비게이션 */
.top-nav {
  position: relative;
  z-index: 2;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 18px clamp(16px, 4vw, 40px);
  backdrop-filter: blur(8px) saturate(120%);
}
.brand {
  display: inline-flex;
  align-items: center;
  gap: 10px;
  font-weight: 800;
  letter-spacing: 0.4px;
  cursor: pointer;
}
.logo-dot {
  width: 12px;
  height: 12px;
  border-radius: 50%;
  background: linear-gradient(135deg, var(--accent-1), var(--accent-2));
  box-shadow: 0 0 10px rgba(59, 130, 246, 0.55);
}
.logo-text { font-size: 1.05rem; }

.nav-right { display: flex; align-items: center; gap: 10px; }
.user-pill {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  padding: 6px 10px;
  border: 1px solid var(--card-border);
  background: rgba(255, 255, 255, 0.04);
  border-radius: 999px;
}
.avatar {
  width: 24px; height: 24px;
  display: grid; place-items: center;
  border-radius: 50%;
  background: linear-gradient(135deg, var(--accent-1), var(--accent-3));
  font-size: 12px; font-weight: 700;
}
.username { color: var(--text-2); font-size: 0.9rem; }

/* 히어로 섹션 */
.hero {
  position: relative;
  z-index: 1;
  display: grid;
  place-items: center;
  padding: clamp(32px, 6vw, 80px) clamp(16px, 4vw, 40px);
  flex: 1 1 auto;
}
.hero-inner {
  width: min(1100px, 92vw);
  text-align: center;
  padding: clamp(24px, 4vw, 48px);
  border-radius: 20px;
  border: 1px solid var(--card-border);
  box-shadow: 0 10px 30px rgba(2, 6, 23, 0.06), inset 0 1px 0 var(--card-highlight);
}
.glass { background: var(--card); backdrop-filter: blur(14px) saturate(140%); }

.eyebrow {
  color: var(--text-2);
  font-weight: 600;
  letter-spacing: 0.12em;
  text-transform: uppercase;
  margin-bottom: 10px;
}
.headline {
  font-size: clamp(42px, 9vw, 92px);
  line-height: 0.95;
  font-weight: 900;
  letter-spacing: -0.02em;
  background: linear-gradient(180deg, #3b82f6, #60a5fa 55%, #93c5fd);
  -webkit-background-clip: text;
  background-clip: text;
  color: transparent;
  margin: 4px 0 12px;
  text-shadow: 0 8px 28px rgba(59, 130, 246, 0.20);
}
.tagline {
  color: var(--text-2);
  font-size: clamp(14px, 2.4vw, 18px);
  margin: 0 auto 24px;
  max-width: 760px;
}

.cta-row { display: inline-flex; gap: 12px; align-items: center; justify-content: center; }

.btn {
  position: relative;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  gap: 10px;
  padding: 14px 18px;
  border-radius: 12px;
  border: 1px solid transparent;
  font-weight: 700;
  letter-spacing: 0.2px;
  transition: transform 200ms ease, box-shadow 200ms ease, background 200ms ease, border-color 200ms ease;
  text-decoration: none;
}

.btn.small { padding: 10px 14px; font-size: 0.95rem; }

.btn.cta {
  background: linear-gradient(135deg, var(--accent-1), var(--accent-2));
  color: #ffffff;
  border-color: color-mix(in oklab, black 6%, transparent);
  box-shadow: 0 10px 24px rgba(59, 130, 246, 0.30), 0 1px 0 rgba(255, 255, 255, 0.45) inset;
}
.btn.cta:hover { transform: translateY(-2px); box-shadow: 0 14px 36px rgba(59, 130, 246, 0.40); }
.btn.cta:active { transform: translateY(0); }

/* (비활성 스타일 제거됨: CTA는 시각적으로 정상, 클릭은 noop) */

.btn.ghost {
  background: #ffffff;
  border-color: #e2e8f0;
  color: var(--text-1);
}
.btn.ghost:hover { background: #f8fafc; border-color: #cbd5e1; }

.btn .shine {
  position: absolute;
  inset: 0;
  border-radius: inherit;
  background: linear-gradient(120deg, transparent 20%, rgba(255, 255, 255, 0.35) 35%, transparent 50%);
  transform: translateX(-120%);
  animation: shine 3.2s ease-in-out infinite;
  pointer-events: none;
}

.trust { margin-top: 32px; color: var(--text-2); font-size: 0.95rem; display: inline-flex; align-items: center; gap: 8px; }
.trust .dot { width: 8px; height: 8px; margin-left: 15px;border-radius: 50%; background: #3b82f6; box-shadow: 0 0 8px rgba(59, 130, 246, 0.5); }

/* 푸터 */
.foot { color: var(--text-2); text-align: center; padding: 18px clamp(16px, 4vw, 40px); font-size: 0.9rem; }

/* 모션 */
@keyframes floatA {
  0%, 100% { transform: translate3d(0, 0, 0) scale(1); }
  50% { transform: translate3d(4vmin, 2vmin, 0) scale(1.05); }
}
@keyframes floatB {
  0%, 100% { transform: translate3d(0, 0, 0) scale(1); }
  50% { transform: translate3d(-3vmin, -2vmin, 0) scale(0.98); }
}
@keyframes shine {
  0% { transform: translateX(-120%); }
  80% { transform: translateX(120%); }
  100% { transform: translateX(120%); }
}

/* 반응형 */
@media (max-width: 640px) {
  .nav-right .username { display: none; }
  .btn { width: 100%; }
  .cta-row { flex-direction: column; width: 100%; }
}

/* 접근성: 모션 최소화 선호 시 */
@media (prefers-reduced-motion: reduce) {
  .main-container::before,
  .main-container::after,
  .btn .shine { animation: none !important; }
}

</style>
