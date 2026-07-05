import { defineStore } from "pinia"
import { ref, watch } from "vue"

export interface BreadcrumbItem {
  title: string
  path?: string
}

function loadPersisted<T>(key: string, fallback: T): T {
  try {
    const raw = localStorage.getItem(key)
    return raw ? (JSON.parse(raw) as T) : fallback
  } catch {
    return fallback
  }
}

export const useAppStore = defineStore("app", () => {
  // --- State ---
  const currentTheme = ref(loadPersisted("b1_theme", "light"))
  const isOffline = ref(false)
  const globalLoading = ref(false)
  const pageTitle = ref("")
  const breadcrumbs = ref<BreadcrumbItem[]>([])
  const openedTabs = ref<string[]>([])
  const cachedPages = ref<string[]>([])

  // --- Persistence ---
  watch(currentTheme, (v) => localStorage.setItem("b1_theme", JSON.stringify(v)))

  // --- Actions ---
  function setTheme(theme: string): void {
    currentTheme.value = theme
  }

  function setOffline(value: boolean): void {
    isOffline.value = value
  }

  function setGlobalLoading(value: boolean): void {
    globalLoading.value = value
  }

  function setPageTitle(title: string): void {
    pageTitle.value = title
    document.title = title ? `${title} - B1 实训平台` : "B1 实训平台"
  }

  function setBreadcrumbs(items: BreadcrumbItem[]): void {
    breadcrumbs.value = items
  }

  function addOpenedTab(path: string): void {
    if (!openedTabs.value.includes(path)) {
      openedTabs.value.push(path)
    }
  }

  function removeOpenedTab(path: string): void {
    const idx = openedTabs.value.indexOf(path)
    if (idx > -1) openedTabs.value.splice(idx, 1)
  }

  function closeAllTabs(): void {
    openedTabs.value = []
  }

  function addCachedPage(name: string): void {
    if (!cachedPages.value.includes(name)) {
      if (cachedPages.value.length >= 10) cachedPages.value.shift()
      cachedPages.value.push(name)
    }
  }

  function removeCachedPage(name: string): void {
    const idx = cachedPages.value.indexOf(name)
    if (idx > -1) cachedPages.value.splice(idx, 1)
  }

  return {
    currentTheme,
    isOffline,
    globalLoading,
    pageTitle,
    breadcrumbs,
    openedTabs,
    cachedPages,
    setTheme,
    setOffline,
    setGlobalLoading,
    setPageTitle,
    setBreadcrumbs,
    addOpenedTab,
    removeOpenedTab,
    closeAllTabs,
    addCachedPage,
    removeCachedPage,
  }
})
