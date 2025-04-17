// src/utils/relationshipUtils.js

/**
 * 관계선 시작점과 끝점을 계산하는 함수
 */
export function getConnectorPoints(rel, classes) {
  const from = classes.find(cls => cls.id === rel.fromId);
  const to = classes.find(cls => cls.id === rel.toId);

  if (!from || !to) return [0, 0, 0, 0];

  const fromAnchor = getAnchorPoint(from, rel.fromDirection);
  const toAnchor = getAnchorPoint(to, rel.toDirection);

  return [fromAnchor.x, fromAnchor.y, toAnchor.x, toAnchor.y];
}

/**
 * Anchor 점의 정확한 위치 계산 (박스 외곽에서 시작하도록 보정)
 */
function getAnchorPoint(box, direction) {
  const width = box.width ?? 200;
  const height = box.height ?? 80;

  switch (direction) {
    case 'top':
      return { x: box.x + width / 2, y: box.y };  // 박스의 상단 외곽
    case 'bottom':
      return { x: box.x + width / 2, y: box.y + height };  // 박스의 하단 외곽
    case 'left':
      return { x: box.x, y: box.y + height / 2 };  // 박스의 좌측 외곽
    case 'right':
      return { x: box.x + width, y: box.y + height / 2 };  // 박스의 우측 외곽
    default:
      return { x: box.x + width / 2, y: box.y + height / 2 };  // 중앙 (기본값)
  }
}

/**
 * 화살표의 위치와 회전값 계산
 */
export function getArrowheadProps(pos, rel, classes) {
  const points = getConnectorPoints(rel, classes);
  if (points.length !== 4) return { x: 0, y: 0, rotation: 0 };

  const [x1, y1, x2, y2] = points;
  const x = pos === 'from' ? x1 : x2;
  const y = pos === 'from' ? y1 : y2;
  const dx = pos === 'from' ? x2 - x1 : x1 - x2;
  const dy = pos === 'from' ? y2 - y1 : y1 - y2;
  const rotation = (Math.atan2(dy, dx) * 180) / Math.PI;

  return { x, y, rotation };
}
