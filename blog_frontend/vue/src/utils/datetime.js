/**
 * 时间格式化工具：统一处理后端 LocalDateTime 的两种序列化形态
 * （数组 [y,m,d,h,mi,s] 或 ISO/带空格字符串），避免各页面各自解析。
 */

function pad(n) {
  return String(n).padStart(2, '0')
}

/** 把数组或字符串解析成 { y, m, d, h, i }；解析不出返回 null */
export function parseParts(value) {
  if (value == null) {
    return null
  }
  if (Array.isArray(value)) {
    return {
      y: value[0],
      m: value[1],
      d: value[2],
      h: value[3] || 0,
      i: value[4] || 0
    }
  }
  if (typeof value === 'string') {
    const s = value.trim()
    const full = s.match(/^(\d{4})-(\d{1,2})-(\d{1,2})[T ](\d{1,2}):(\d{1,2})/)
    if (full) {
      return {
        y: +full[1], m: +full[2], d: +full[3],
        h: +full[4], i: +full[5]
      }
    }
    const dateOnly = s.match(/^(\d{4})-(\d{1,2})-(\d{1,2})/)
    if (dateOnly) {
      return { y: +dateOnly[1], m: +dateOnly[2], d: +dateOnly[3], h: 0, i: 0 }
    }
  }
  return null
}

/** YYYY-MM-DD */
export function formatDate(value) {
  const p = parseParts(value)
  return p ? `${p.y}-${pad(p.m)}-${pad(p.d)}` : ''
}

/** YYYY-MM-DD HH:mm */
export function formatDateTime(value) {
  const p = parseParts(value)
  return p ? `${p.y}-${pad(p.m)}-${pad(p.d)} ${pad(p.h)}:${pad(p.i)}` : ''
}

/** MM-DD */
export function formatMonthDay(value) {
  const p = parseParts(value)
  return p ? `${pad(p.m)}-${pad(p.d)}` : ''
}
