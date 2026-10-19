import { onMounted, onBeforeUnmount } from "vue"

type RefreshFn = () => void | Promise<void>

/**
 * 学情/报表页自动刷新：挂载拉取 + 页面重新可见时刷新 + 轻量轮询。
 */
export function useReportRefresh(refresh: RefreshFn, intervalMs = 30_000) {
  let timer: ReturnType<typeof setInterval> | null = null
  let refreshing = false

  async function runRefresh() {
    if (refreshing) return
    refreshing = true
    try {
      await refresh()
    } finally {
      refreshing = false
    }
  }

  function onVisibilityChange() {
    if (document.visibilityState === "visible") {
      void runRefresh()
    }
  }

  onMounted(() => {
    void runRefresh()
    document.addEventListener("visibilitychange", onVisibilityChange)
    timer = setInterval(() => {
      if (document.visibilityState === "visible") {
        void runRefresh()
      }
    }, intervalMs)
  })

  onBeforeUnmount(() => {
    document.removeEventListener("visibilitychange", onVisibilityChange)
    if (timer) clearInterval(timer)
  })

  return { refresh: runRefresh }
}
