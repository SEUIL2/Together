// src/utils/id.js

let counter = 0

export function generateId() {
  return 'id-' + counter++
}
