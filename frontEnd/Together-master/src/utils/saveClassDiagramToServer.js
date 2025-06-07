import axios from 'axios'

export async function saveClassDiagramToServer(classBoxes, relationships) {
  const formData = new FormData()
  formData.append('type', 'class-diagram')
  formData.append('json', JSON.stringify({ classes: classBoxes, relationships }))

  try {
    const res = await axios.put('/design/update', formData, {
      headers: {
        'Content-Type': 'multipart/form-data'
      }
    })
    console.log('✅ 서버 자동 저장 성공:', res.data)
  } catch (err) {
    console.warn('❌ 서버 자동 저장 실패:', err)
  }
}
