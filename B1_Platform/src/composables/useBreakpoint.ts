import { computed, onMounted, onUnmounted, ref } from "vue"

/** 与 design-tokens `--bp-*` 对齐（max-width 用 bp-1） */
const MQ = {
  sm: "(max-width: 767.98px)",
  mdDown: "(max-width: 1023.98px)",
  lgUp: "(min-width: 1280px)",
} as const

function match(query: string): boolean {
  if (typeof window === "undefined" || !window.matchMedia) return false
  return window.matchMedia(query).matches
}

/**
 * 响应式断点（UI Design System §6.8）
 * - isXs: &lt;768
 * - isMdDown: &lt;1024（手机+平板，壳层用汉堡）
 * - isLgUp: ≥1280
 */
export function useBreakpoint() {
  const isXs = ref(match(MQ.sm))
  const isMdDown = ref(match(MQ.mdDown))
  const isLgUp = ref(match(MQ.lgUp))

  let mqs: MediaQueryList[] = []
  const sync = () => {
    isXs.value = match(MQ.sm)
    isMdDown.value = match(MQ.mdDown)
    isLgUp.value = match(MQ.lgUp)
  }

  onMounted(() => {
    sync()
    mqs = Object.values(MQ).map((q) => window.matchMedia(q))
    mqs.forEach((mq) => mq.addEventListener("change", sync))
  })

  onUnmounted(() => {
    mqs.forEach((mq) => mq.removeEventListener("change", sync))
    mqs = []
  })

  const isSmUp = computed(() => !isXs.value)

  return { isXs, isMdDown, isLgUp, isSmUp }
}
