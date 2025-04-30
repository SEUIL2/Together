// ✅ 클래스 다이어그램 저장 및 불러오기 유틸 (Vue 컴포넌트에서 사용 가능)

import axios from 'axios';

// 저장 함수
export async function saveClassDiagram(classes, relationships) {
  const form = new FormData();
  form.append("type", "class-diagram");
  form.append("json", JSON.stringify({ classes, relationships }));

  try {
    const res = await axios.post("/design/upload", form, {
      headers: { "Content-Type": "multipart/form-data" },
    });
    console.log("✅ 다이어그램 저장 성공", res.data);
    return res.data;
  } catch (err) {
    console.error("❌ 다이어그램 저장 실패", err);
    throw err;
  }
}

// 불러오기 함수
export async function loadClassDiagram() {
    try {
        const res = await axios.get("/design/all");
        console.log("📦 전체 디자인 응답:", res.data);
        console.log("👉 classDiagram 데이터:", res.data.classDiagram);
        console.log("🧾 classDiagram.json 필드:", res.data.classDiagram?.json);
        
  
      const diagramData = res.data.classDiagram?.json; // ✅ 여기서 json 읽어야 함
  
      if (diagramData) {
        console.log("✅ classDiagram.json 내용:", diagramData);
        const parsed = JSON.parse(diagramData);
        return {
          classes: parsed.classes || [],
          relationships: parsed.relationships || [],
        };
      } else {
        console.warn("⚠️ 불러올 JSON 데이터가 없음 (classDiagram.json 비어있음)");
        return { classes: [], relationships: [] };
      }
    } catch (err) {
      console.error("❌ 다이어그램 불러오기 실패", err);
      return { classes: [], relationships: [] };
    }
  }
  
