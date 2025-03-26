<template>
  <div>
    <div id="spread-host">
      <gc-spread-sheets
          hostClass="spreadHost"
          @workbookInitialized="initSpread"
      ></gc-spread-sheets>
    </div>
    <!-- 컨트롤 영역 -->
    <div class="controls">
      <button @click="saveWorkbook">Save Workbook</button>
      <button @click="triggerFileLoad">Load Workbook</button>
      <!-- 파일 선택 input (숨김 처리) -->
      <input type="file" ref="fileInput" style="display: none;" @change="loadWorkbook" accept=".json" />
    </div>
  </div>
</template>

<script>
export default {
  name: "App",
  data() {
    return {
      spread: null
    };
  },
  methods: {
    initSpread(spread) {
      this.spread = spread;
      // 예시: 첫 번째 셀에 기본 텍스트 설정
      const sheet = this.spread.getActiveSheet();
      sheet.setValue(0, 0, "Hello, enter text here");
      // 격자선 색상을 연한 회색으로 설정 (기본은 진한 검정색)
      sheet.options.gridlineColor = "#d3d3d3";
    },
    saveWorkbook() {
      if (this.spread) {
        // 현재 워크북 상태를 JSON 객체로 변환 후 문자열화
        const json = this.spread.toJSON();
        const jsonString = JSON.stringify(json, null, 2);
        console.log("Workbook saved:", jsonString);

        // Blob 생성 후 임시 링크를 만들어 파일 다운로드 실행
        const blob = new Blob([jsonString], {type: "application/json"});
        const url = URL.createObjectURL(blob);
        const a = document.createElement("a");
        a.href = url;
        a.download = "workbook.json";
        document.body.appendChild(a);
        a.click();
        document.body.removeChild(a);
        URL.revokeObjectURL(url);
      }
    },
    triggerFileLoad() {
      // 파일 선택 input 클릭 트리거
      this.$refs.fileInput.click();
    },
    loadWorkbook(event) {
      const file = event.target.files[0];
      if (!file) return;

      const reader = new FileReader();
      reader.onload = (e) => {
        try {
          const fileContent = e.target.result;
          const json = JSON.parse(fileContent);
          this.spread.fromJSON(json);
          console.log("Workbook loaded successfully");
        } catch (e) {
          console.error("Error parsing JSON:", e);
        }
      };
      reader.readAsText(file);

      // 같은 파일을 다시 불러올 수 있도록 input 초기화
      event.target.value = "";
    }
  }
};
</script>

<style>
.spreadHost {
  width: 100%;
  height: 600px;
  border: 1px solid #ccc;
}

.controls {
  margin: 10px 0;
}

.controls button {
  margin-right: 10px;
  padding: 8px 12px;
  font-size: 1rem;
  background-color: #3f8efc;
  color: #fff;
  border: none;
  border-radius: 4px;
  cursor: pointer;
}

.controls button:hover {
  background-color: #3077e5;
}
</style>
