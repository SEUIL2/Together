<template>
    <div class="canvas-container" ref="container" @click="hideContextMenu">
      <v-stage :config="stageConfig">
        <v-layer>
          <!-- 박스들을 그리는 Group -->
          <v-group
            v-for="cls in classes"
            :key="cls.id"
            :config="getGroupConfig(cls)"
            @dragmove="onDragMove($event, cls)"
            @contextmenu="onRightClick($event, cls)"
            @click="onGroupClick(cls)"
          >
            <!-- 박스 배경 -->
            <v-rect :config="getRectConfig(cls)" />
  
            <!-- [1] 상단 영역: 클래스명 (더블 클릭 편집) -->
            <v-text
              :config="getClassNameTextConfig(cls)"
              @dblclick="(evt) => startEdit(cls, 'name', null, evt)"
            />
            <v-line :config="getSeparatorConfig(headerHeight)" />
  
            <!-- [2] 중간 영역: attributes -->
            <v-text
              v-for="(attr, i) in cls.attributes"
              :key="'attr-' + i"
              :config="getAttributeTextConfig(cls, attr, i)"
              @dblclick="(evt) => startEdit(cls, 'attributes', i, evt)"
            />
            <!-- + 버튼: attributes (중앙 정렬) -->
            <v-text
              :config="getAttributePlusButtonConfig(cls)"
              @click="(evt) => { evt.evt.stopPropagation(); handlePlus('attributes', cls); }"
            />
            <v-line :config="getSeparatorConfig(headerHeight + cls.attributes.length * lineHeight + attributePlusButtonHeight)" />
  
            <!-- [3] 하단 영역: methods -->
            <v-text
              v-for="(mth, j) in cls.methods"
              :key="'mth-' + j"
              :config="getMethodTextConfig(cls, mth, j)"
              @dblclick="(evt) => startEdit(cls, 'methods', j, evt)"
            />
            <!-- + 버튼: methods (중앙 정렬) -->
            <v-text
              :config="getMethodPlusButtonConfig(cls)"
              @click="(evt) => { evt.evt.stopPropagation(); handlePlus('methods', cls); }"
            />
          </v-group>
        </v-layer>
        
        <!-- 관계선을 그리는 Layer -->
        <v-layer>
          <v-arrow
            v-for="(rel, index) in relationships"
            :key="rel.fromId + '-' + rel.toId + '-' + index"
            :config="getArrowConfig(rel)"
            @click="(evt) => onArrowClick(evt, rel)"
          />
        </v-layer>
      </v-stage>
    
      <!-- 편집용 HTML input (더블 클릭 또는 + 버튼 후) -->
      <input
        v-if="editingClassId !== null"
        ref="textInput"
        class="text-input"
        v-model="editingText"
        :style="{ top: editingPos.y + 'px', left: editingPos.x + 'px' }"
        @blur="finishEdit"
        @keyup.enter="finishEdit"
      />
    
      <!-- 관계선 컨텍스트 메뉴 (삭제) -->
      <div
        v-if="arrowContextMenuVisible"
        class="context-menu"
        :style="{ top: arrowContextMenuPos.y + 'px', left: arrowContextMenuPos.x + 'px' }"
      >
        <button @click="deleteCurrentRelationship">삭제</button>
      </div>
    
      <!-- 박스 컨텍스트 메뉴 (삭제) -->
      <div
        v-if="contextMenuVisible"
        class="context-menu"
        :style="{ top: contextMenuPos.y + 'px', left: contextMenuPos.x + 'px' }"
      >
        <button @click="deleteCurrentClass">삭제</button>
      </div>
    </div>
  </template>
  
  <script>
  export default {
    name: "CanvasArea",
    props: {
      classes: { type: Array, default: () => [] },
      relationships: { type: Array, default: () => [] },
      relationshipMode: { type: Boolean, default: false } // 새 prop: 관계 추가 모드
    },
    data() {
      return {
        stageWidth: 2000,
        stageHeight: 2000,
        headerHeight: 30,
        lineHeight: 20,
        attributePlusButtonHeight: 20,
        methodPlusButtonHeight: 20,
        bottomMargin: 10,
    
        // 편집 상태
        editingClassId: null,
        editingRegion: null, // "name", "attributes", "methods"
        editingIndex: null,
        editingText: "",
        editingPos: { x: 0, y: 0 },
    
        // 박스 컨텍스트 메뉴
        contextMenuVisible: false,
        contextMenuPos: { x: 0, y: 0 },
        contextClassId: null,
    
        // 관계선 컨텍스트 메뉴
        arrowContextMenuVisible: false,
        arrowContextMenuPos: { x: 0, y: 0 },
        currentRelationship: null
      };
    },
    computed: {
      stageConfig() {
        return { width: this.stageWidth, height: this.stageHeight };
      }
    },
    methods: {
      getTotalHeight(cls) {
        const attributes = cls.attributes || [];
        const methods = cls.methods || [];
        return (
          this.headerHeight +
          attributes.length * this.lineHeight +
          this.attributePlusButtonHeight +
          methods.length * this.lineHeight +
          this.methodPlusButtonHeight +
          this.bottomMargin
        );
      },
      getGroupConfig(cls) {
        return {
          x: cls.x,
          y: cls.y,
          draggable: this.editingClassId !== cls.id, // 편집 중이면 드래그 불가
          id: cls.id,
          listening: true
        };
      },
      onDragMove(e, cls) {
        this.$emit("update-position", {
          id: cls.id,
          x: e.target.x(),
          y: e.target.y()
        });
      },
      getRectConfig(cls) {
        return {
          width: 200,
          height: this.getTotalHeight(cls),
          fill: "#fff",
          stroke: "#000",
          strokeWidth: 1,
          listening: true
        };
      },
      getClassNameTextConfig(cls) {
        return {
          text: cls.name,
          x: 5,
          y: 5,
          width: 190,
          fontSize: 20,
          fontFamily: "Arial",
          fill: "#000",
          listening: true
        };
      },
      getSeparatorConfig(y) {
        return {
          points: [0, y, 200, y],
          stroke: "#000",
          strokeWidth: 1,
          listening: false
        };
      },
      getAttributeTextConfig(cls, attr, i) {
        let y = this.headerHeight + 5 + i * this.lineHeight;
        return {
          text: attr,
          x: 5,
          y: y,
          width: 190,
          fontSize: 16,
          fontFamily: "Arial",
          fill: "#000",
          listening: true
        };
      },
      // 속성 + 버튼: 중앙 정렬 (x= (200-20)/2 = 90)
      getAttributePlusButtonConfig(cls) {
        let y = this.headerHeight + cls.attributes.length * this.lineHeight + 5;
        return {
          text: "+",
          x: 90,
          y: y,
          fontSize: 18,
          fontFamily: "Arial",
          fill: "#4caf50",
          width: 20,
          align: "center",
          cursor: "pointer",
          listening: true
        };
      },
      getMethodTextConfig(cls, mth, j) {
        let startY =
          this.headerHeight +
          cls.attributes.length * this.lineHeight +
          this.attributePlusButtonHeight;
        let y = startY + 5 + j * this.lineHeight;
        return {
          text: mth,
          x: 5,
          y: y,
          width: 190,
          fontSize: 16,
          fontFamily: "Arial",
          fill: "#000",
          listening: true
        };
      },
      // 메서드 + 버튼: 중앙 정렬 (x=90)
      getMethodPlusButtonConfig(cls) {
        let startY =
          this.headerHeight +
          cls.attributes.length * this.lineHeight +
          this.attributePlusButtonHeight;
        let y = startY + cls.methods.length * this.lineHeight + 5;
        return {
          text: "+",
          x: 90,
          y: y,
          fontSize: 18,
          fontFamily: "Arial",
          fill: "#4caf50",
          width: 20,
          align: "center",
          cursor: "pointer",
          listening: true
        };
      },
      // Arrow 설정: 두 박스의 중앙 혹은 원하는 위치 계산
      getArrowConfig(rel) {
        const fromClass = this.classes.find((c) => c.id === rel.fromId);
        const toClass = this.classes.find((c) => c.id === rel.toId);
        if (!fromClass || !toClass) return { points: [] };
        const fromX = fromClass.x + 100; // 중앙 (200/2)
        const fromY = fromClass.y + this.getTotalHeight(fromClass);
        const toX = toClass.x + 100;
        const toY = toClass.y;
        return {
          points: [fromX, fromY, toX, toY],
          stroke: "#000",
          strokeWidth: 2,
          pointerLength: 10,
          pointerWidth: 10,
          fill: "#000",
          listening: true
        };
      },
      // 박스 오른쪽 클릭 컨텍스트 메뉴 (삭제)
      onRightClick(e, cls) {
        e.evt.preventDefault();
        const mouseX = e.evt.clientX;
        const mouseY = e.evt.clientY;
        this.contextMenuPos = { x: mouseX, y: mouseY };
        this.contextClassId = cls.id;
        this.contextMenuVisible = true;
      },
      hideContextMenu() {
        this.contextMenuVisible = false;
        this.contextClassId = null;
        this.arrowContextMenuVisible = false;
        this.currentRelationship = null;
      },
      deleteCurrentClass() {
        if (this.contextClassId) {
          this.$emit("delete-class", this.contextClassId);
        }
        this.hideContextMenu();
      },
      // Arrow 클릭 시 관계 컨텍스트 메뉴 표시 (삭제)
      onArrowClick(e, rel) {
        e.evt.stopPropagation();
        const mouseX = e.evt.clientX;
        const mouseY = e.evt.clientY;
        this.arrowContextMenuPos = { x: mouseX, y: mouseY };
        this.currentRelationship = rel;
        this.arrowContextMenuVisible = true;
      },
      deleteCurrentRelationship() {
        if (this.currentRelationship) {
          this.$emit("delete-relationship", this.currentRelationship);
        }
        this.hideContextMenu();
      },
      // 텍스트 편집 시작 (더블 클릭 또는 + 버튼 후)
      // region: "name", "attributes", "methods"; index: 해당 배열의 인덱스 (클래스명은 null)
      startEdit(cls, region, index, event) {
        if (event && event.evt) {
          event.evt.stopPropagation();
        }
        this.editingClassId = cls.id;
        this.editingRegion = region;
        this.editingIndex = index;
        let currentText = "";
        if (region === "name") {
          currentText = cls.name;
        } else if (region === "attributes") {
          currentText = cls.attributes[index];
        } else if (region === "methods") {
          currentText = cls.methods[index];
        }
        this.editingText = currentText;
        let offsetX = cls.x + 10;
        let offsetY = 0;
        if (region === "name") {
          offsetY = cls.y + 5;
        } else if (region === "attributes") {
          offsetY = cls.y + this.headerHeight + 5 + index * this.lineHeight;
        } else if (region === "methods") {
          let startY =
            cls.y +
            this.headerHeight +
            cls.attributes.length * this.lineHeight +
            this.attributePlusButtonHeight;
          offsetY = startY + 5 + index * this.lineHeight;
        }
        this.editingPos = { x: offsetX, y: offsetY };
        this.$nextTick(() => {
          this.$refs.textInput && this.$refs.textInput.focus();
        });
      },
      finishEdit() {
        if (this.editingClassId) {
          this.$emit("update-text", {
            id: this.editingClassId,
            region: this.editingRegion,
            index: this.editingIndex,
            newText: this.editingText
          });
        }
        this.editingClassId = null;
        this.editingRegion = null;
        this.editingIndex = null;
        this.editingText = "";
      },
      handlePlus(region, cls) {
        if (region === "attributes") {
          this.$emit("add-item", { id: cls.id, region: "attributes" });
        } else if (region === "methods") {
          this.$emit("add-item", { id: cls.id, region: "methods" });
        }
      },
      // 관계 추가 모드: 박스를 클릭하면 emit("box-selected", cls.id)
      onGroupClick(cls) {
        if (this.relationshipMode) {
          this.$emit("box-selected", cls.id);
        }
      },
      // 새로 추가: v-group에 @click="onGroupClick(cls)"를 설정해줍니다.
      onGroupClick(cls) {
        if (this.relationshipMode) {
          this.$emit("box-selected", cls.id);
        }
      }
    }
  };
  </script>
  
  <style scoped>
  .canvas-container {
    
    background-color: #eee;
    width: 800px;
    height: 600px;
    position: relative;
  }
  .text-input {
    position: absolute;
    font-size: 16px;
    font-family: Arial, sans-serif;
    border: 1px solid #333;
    padding: 4px;
    z-index: 10;
  }
  .context-menu {
    position: absolute;
    background-color: #fff;
    border: 1px solid #333;
    padding: 4px;
    z-index: 999;
  }
  .context-menu button {
    display: block;
    width: 80px;
    padding: 5px;
    cursor: pointer;
    background-color: #f44336;
    color: #fff;
    border: none;
    text-align: left;
  }
  .context-menu button:hover {
    background-color: #e53935;
  }
  </style>
  