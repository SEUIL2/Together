<template>
  <div class="help-page">
    <!-- 제목 -->
    <h1 class="page-title">📘 졸업작품 FAQ</h1>
    <p class="page-subtitle">졸업작품을 하며 자주 겪는 고민과 문제를 해결해보세요.</p>

    <!-- 검색창 -->
    <div class="search-wrapper">
      <input
        v-model="searchQuery"
        class="search-input"
        type="text"
        placeholder="궁금한 내용을 검색해보세요 (예: 팀 갈등, 보고서 작성)..."
      />
    </div>

    <!-- 카테고리 필터 -->
    <div class="categories">
      <button
        v-for="cat in categories"
        :key="cat"
        @click="selectedCategory = cat"
        :class="{ active: selectedCategory === cat }"
        class="category-button"
      >
        {{ cat }}
      </button>
    </div>

    <!-- FAQ 목록 -->
    <div class="faq-list">
      <FaqItem
        v-for="(faq, i) in filteredFaqs"
        :key="i"
        :faq="faq"
      />
      <p v-if="filteredFaqs.length === 0" class="no-result">
        검색 결과가 없습니다. 다른 키워드로 검색해보세요!
      </p>
    </div>
  </div>
</template>

<script setup>
import { ref, computed } from 'vue'
import FaqItem from './FaqItem.vue'
import faqData from './faqData'

const searchQuery = ref('')
const selectedCategory = ref('전체')

const categories = ['전체', ...new Set(faqData.map(f => f.category))]

const filteredFaqs = computed(() => {
  return faqData.filter(faq => {
    const matchesCategory =
      selectedCategory.value === '전체' || faq.category === selectedCategory.value
    const matchesQuery =
      faq.question.includes(searchQuery.value) || faq.answer.includes(searchQuery.value)
    return matchesCategory && matchesQuery
  })
})
</script>

<style scoped>
.help-page {
  max-width: 860px;
  margin: 40px auto;
  padding: 20px;
  font-family: 'Segoe UI', sans-serif;
  color: #333;
}

.page-title {
  font-size: 28px;
  font-weight: bold;
  margin-bottom: 4px;
  text-align: center;
  color: #222;
}

.page-subtitle {
  font-size: 15px;
  text-align: center;
  color: #777;
  margin-bottom: 30px;
}

/* 검색창 */
.search-wrapper {
  display: flex;
  justify-content: center;
  margin-bottom: 24px;
}

.search-input {
  width: 100%;
  max-width: 600px;
  padding: 12px 16px;
  font-size: 15px;
  border: 1px solid #ccc;
  border-radius: 10px;
  outline: none;
  transition: border-color 0.3s ease;
}

.search-input:focus {
  border-color: #0088ff;
}

/* 카테고리 필터 */
.categories {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
  justify-content: center;
  margin-bottom: 32px;
}

.category-button {
  padding: 8px 16px;
  border: 1px solid #ddd;
  border-radius: 20px;
  background-color: #f9f9f9;
  color: #555;
  font-size: 14px;
  cursor: pointer;
  transition: all 0.3s ease;
}

.category-button:hover {
  background-color: #eee;
}

.category-button.active {
  background-color: #0088ff;
  color: white;
  border-color: #0088ff;
}

/* FAQ 목록 */
.faq-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.no-result {
  text-align: center;
  color: #888;
  margin-top: 40px;
  font-size: 16px;
}
</style>
