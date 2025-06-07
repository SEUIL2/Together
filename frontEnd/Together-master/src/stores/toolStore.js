// toolStore.js
import { defineStore } from 'pinia'

export const useToolStore = defineStore('tool', {
  state: () => ({
    selectedTool: null, // 선택된 툴 항목 { type, subtype } 구조
  }),

  actions: {
    /**
     * 도형 또는 관계선 선택
     * @param {Object} tool 예: { type: 'box', subtype: 'class' }
     */
    setSelectedTool(tool) {
      this.selectedTool = tool
    },

    /**
     * 선택된 툴 초기화
     */
    resetTool() {
      this.selectedTool = null
    }
  }
})
