<template>
    <div class="diagram-page">
      <!-- 화면 위에 겹쳐 뜨는 툴바 -->
      <div class="toolbar-overlay">
        <Toolbar
          @create-class="handleCreateClass"
          @toggle-relationship-mode="toggleRelationshipMode"
          :relationshipMode="relationshipMode"
        />
      </div>
  
      <!-- 다이어그램 캔버스 (화면 전체) -->
      <div class="main-content">
        <CanvasArea 
          :classes="classes"
          :relationships="relationships"
          :relationshipMode="relationshipMode"
          @update-position="updatePosition"
          @update-text="updateText"
          @delete-class="deleteClass"
          @add-item="addItem"
          @box-selected="onBoxSelected"
          @delete-relationship="deleteRelationship"
        />
      </div>
    </div>
  </template>
  
  <script>
  import Toolbar from "@/components/classdiagram/Toolbar.vue";
  import CanvasArea from "@/components/classdiagram/CanvasArea.vue";
  
  export default {
    name: "DiagramPage",
    components: { Toolbar, CanvasArea },
    data() {
      return {
        classes: [],
        relationships: [],
        relationshipMode: false,
        firstSelectedBoxId: null
      };
    },
    methods: {
      handleCreateClass() {
        const newId = "Class_" + Date.now();
        this.classes.push({
          id: newId,
          name: "클래스명",
          attributes: ["속성"],
          methods: ["메서드"],
          x: 200,
          y: 50
        });
      },
      updatePosition({ id, x, y }) {
        const cls = this.classes.find(c => c.id === id);
        if (cls) {
          cls.x = x;
          cls.y = y;
        }
      },
      updateText({ id, region, index, newText }) {
        const cls = this.classes.find(c => c.id === id);
        if (!cls) return;
        if (region === "name") {
          cls.name = newText;
        } else if (region === "attributes" && index != null) {
          if (index === cls.attributes.length) {
            cls.attributes.push(newText);
          } else {
            cls.attributes.splice(index, 1, newText);
          }
        } else if (region === "methods" && index != null) {
          if (index === cls.methods.length) {
            cls.methods.push(newText);
          } else {
            cls.methods.splice(index, 1, newText);
          }
        }
      },
      addItem({ id, region }) {
        const cls = this.classes.find(c => c.id === id);
        if (!cls) return;
        if (region === "attributes") {
          cls.attributes.push("새로운 속성");
        } else if (region === "methods") {
          cls.methods.push("새로운 메서드");
        }
      },
      deleteClass(id) {
        this.classes = this.classes.filter(c => c.id !== id);
        this.relationships = this.relationships.filter(
          rel => rel.fromId !== id && rel.toId !== id
        );
      },
      toggleRelationshipMode() {
        this.relationshipMode = !this.relationshipMode;
        this.firstSelectedBoxId = null;
      },
      onBoxSelected(boxId) {
        if (!this.relationshipMode) return;
        if (!this.firstSelectedBoxId) {
          this.firstSelectedBoxId = boxId;
        } else if (this.firstSelectedBoxId !== boxId) {
          this.relationships.push({
            fromId: this.firstSelectedBoxId,
            toId: boxId,
            type: "association"
          });
          this.relationshipMode = false;
          this.firstSelectedBoxId = null;
        }
      },
      deleteRelationship(rel) {
        this.relationships = this.relationships.filter(r => r !== rel);
      }
    }
  };
  </script>
  
  <style scoped>
  .diagram-page {
    width: 100vw;
    height: 100vh;
    position: relative;
    background: #eee; /* 전체 페이지 배경을 #eee로 */
    overflow: hidden; /* 필요 시 스크롤을 숨길 수도 있음 */
  }
  
  .toolbar-overlay {
    position: absolute;
    top: 20px;
    left: 20px;
    z-index: 10;
    /* 필요하다면 배경, 테두리 등 스타일 추가 가능 */
  }
  
  .main-content {
    width: 100%;
    height: 100%;
    /* CanvasArea가 차지할 영역 */
  }
  </style>
  