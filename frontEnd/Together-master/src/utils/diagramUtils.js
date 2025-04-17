export function getTextWidth(text, font = '14px Arial') {
    const canvas = document.createElement('canvas');
    const context = canvas.getContext('2d');
    context.font = font;
    const metrics = context.measureText(text);
    return metrics.width;
  }
  // utils/diagramUtils.js

export const relationshipTypes = [
    { key: 'inheritance', label: '상속', icon: '▲' },
    { key: 'implementation', label: '구현', icon: '▲--' },
    { key: 'dependency', label: '의존', icon: '- - >' },
    { key: 'association', label: '연관', icon: '—' },
    { key: 'directedAssociation', label: '직접연관', icon: '—>' },
    { key: 'aggregation', label: '집합', icon: '◇' },
    { key: 'composition', label: '합성', icon: '◆' },
  ];
  