// utils/erdExportUtils.js

export function exportToSQL(entities, relationships) {
    let sql = ''
  
    // 테이블 생성
    entities.forEach((entity) => {
      sql += `CREATE TABLE ${entity.name} (\n`
  
      entity.fields.forEach((field, index) => {
        const parts = [`  ${field.name}`, field.type.toUpperCase()]
        if (field.isPK) parts.push('PRIMARY KEY')
        if (field.isFK) parts.push('-- FOREIGN KEY') // 외래키는 아래에서 처리
        sql += parts.join(' ') + (index < entity.fields.length - 1 ? ',' : '') + '\n'
      })
  
      sql += ');\n\n'
    })
  
    // 관계선 기반 FOREIGN KEY 제약조건
    relationships.forEach((rel) => {
      const fromEntity = entities.find(e => e.id === rel.fromEntityId)
      const toEntity = entities.find(e => e.id === rel.toEntityId)
  
      if (fromEntity && toEntity) {
        sql += `ALTER TABLE ${fromEntity.name}\n`
        sql += `  ADD CONSTRAINT fk_${fromEntity.name}_${rel.fromFieldName}\n`
        sql += `  FOREIGN KEY (${rel.fromFieldName})\n`
        sql += `  REFERENCES ${toEntity.name}(${rel.toFieldName});\n\n`
      }
    })
  
    return sql
  }
  