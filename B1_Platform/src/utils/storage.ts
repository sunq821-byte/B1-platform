/**
 * sessionStorage 封装
 * Token 存储在 sessionStorage，关闭浏览器即失效
 */
const PREFIX = "b1_"

export const storage = {
  get<T = string>(key: string): T | null {
    try {
      const raw = sessionStorage.getItem(PREFIX + key)
      if (raw === null) return null
      return JSON.parse(raw) as T
    } catch {
      return null
    }
  },

  set(key: string, value: unknown): void {
    try {
      sessionStorage.setItem(PREFIX + key, JSON.stringify(value))
    } catch {
      console.warn("[storage] Failed to set item:", key)
    }
  },

  remove(key: string): void {
    try {
      sessionStorage.removeItem(PREFIX + key)
    } catch {
      console.warn("[storage] Failed to remove item:", key)
    }
  },

  clear(): void {
    try {
      const keysToRemove: string[] = []
      for (let i = 0; i < sessionStorage.length; i++) {
        const key = sessionStorage.key(i)
        if (key?.startsWith(PREFIX)) {
          keysToRemove.push(key)
        }
      }
      keysToRemove.forEach((k) => sessionStorage.removeItem(k))
    } catch {
      console.warn("[storage] Failed to clear items")
    }
  },
}
