<template>
    <section class="detail-section">
      <div class="timeline horizontal">
        <div class="timeline-item" v-for="(item, index) in designItems" :key="index">
          <div class="circle" :class="{ active: item.completed }"></div>
          <p class="timeline-text">{{ item.name }}</p>
        </div>
      </div>
      <div class="detail-inputs">
        <div class="detail-box" v-for="(item, index) in designItems" :key="index">
          <h3 class="detail-title">{{ item.name }}</h3>
          <textarea
            class="detail-textarea"
            v-model="item.content"
            :placeholder="item.placeholder"
          ></textarea>
        </div>
      </div>
    </section>
  </template>
  
  <script setup>
  import { ref } from 'vue'
  
  const designItems = ref([
    { name: "유스케이스 다이어그램", completed: false, content: "", placeholder: "유스케이스 다이어그램 내용을 작성하세요." },
    { name: "클래스 다이어그램", completed: false, content: "", placeholder: "클래스 다이어그램 내용을 작성하세요." },
    { name: "시퀀스 다이어그램", completed: false, content: "", placeholder: "시퀀스 다이어그램 내용을 작성하세요." },
    { name: "UI 디자인", completed: false, content: "", placeholder: "UI 디자인에 대해 작성하세요." },
    { name: "ER 다이어그램", completed: false, content: "", placeholder: "ER 다이어그램 내용을 작성하세요." },
    { name: "테이블 명세", completed: false, content: "", placeholder: "테이블 명세를 작성하세요." },
    { name: "시스템 아키텍쳐", completed: false, content: "", placeholder: "시스템 아키텍쳐를 작성하세요." },
    { name: "프로젝트 일정 계획", completed: false, content: "", placeholder: "프로젝트 일정 계획을 작성하세요." },
  ])
  </script>
  
  <style scoped>
  /* DesignDetails의 스타일은 PlanningDetails와 동일 */
  .detail-section {
    background: white;
    padding: 15px;
    margin-bottom: 20px;
    border-radius: 10px;
    box-shadow: 0 4px 10px rgba(0,0,0,0.1);
  }
  .timeline.horizontal {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 20px;
  }
  .timeline-item {
    display: flex;
    flex-direction: column;
    align-items: center;
  }
  .circle {
    width: 12px;
    height: 12px;
    border-radius: 50%;
    background: #ddd;
    margin-bottom: 5px;
  }
  .circle.active {
    background: #3f8efc;
  }
  .timeline-text {
    font-size: 0.8rem;
  }
  .detail-inputs {
    display: flex;
    flex-direction: column;
    gap: 20px;
  }
  .detail-box {
    background: #f9f9f9;
    border: 1px solid #ddd;
    border-radius: 5px;
    padding: 10px;
  }
  .detail-title {
    margin: 0 0 8px 0;
    font-size: 1rem;
    color: #333;
  }
  .detail-textarea {
    width: 100%;
    min-height: 60px;
    border: 1px solid #ccc;
    border-radius: 4px;
    padding: 6px;
    resize: vertical;
  }
  .detail-textarea:focus {
    outline: none;
    border-color: #3f8efc;
  }
  </style>
  