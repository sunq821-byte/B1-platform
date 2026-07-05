<script setup lang="ts">
import { ref, computed, watch, nextTick } from "vue"

const props = withDefaults(defineProps<{
  modelValue: string
  language?: string
  disabled?: boolean
}>(), {
  language: "java",
  disabled: false,
})

const emit = defineEmits<{
  "update:modelValue": [value: string]
  "update:language": [value: string]
}>()

const LANG_MAP: Record<string, string> = {
  java: "Main.java",
  python: "main.py",
  cpp: "main.cpp",
  javascript: "main.js",
  go: "main.go",
}

const textareaRef = ref<HTMLTextAreaElement | null>(null)
const lineNumbersRef = ref<HTMLDivElement | null>(null)
const lineCount = ref(1)
const charCount = ref(0)

const fileName = computed(() => LANG_MAP[props.language] || "main.txt")

function updateStats() {
  const v = props.modelValue
  lineCount.value = v ? v.split("\n").length : 1
  charCount.value = v ? v.length : 0
}

function syncScroll() {
  if (lineNumbersRef.value && textareaRef.value) {
    lineNumbersRef.value.scrollTop = textareaRef.value.scrollTop
  }
}

function onInput(e: Event) {
  const val = (e.target as HTMLTextAreaElement).value
  emit("update:modelValue", val)
}

function onKeydown(e: KeyboardEvent) {
  if (e.key === "Tab") {
    e.preventDefault()
    const ta = textareaRef.value
    if (!ta) return
    const start = ta.selectionStart
    const end = ta.selectionEnd
    const v = props.modelValue
    const newVal = v.substring(0, start) + "    " + v.substring(end)
    emit("update:modelValue", newVal)
    nextTick(() => {
      ta.selectionStart = ta.selectionEnd = start + 4
    })
  }
}

function onLanguageChange(e: Event) {
  const lang = (e.target as HTMLSelectElement).value
  emit("update:language", lang)
  emit("update:modelValue", "")
}

watch(() => props.modelValue, updateStats, { immediate: true })
</script>

<template>
  <div class="code-editor-wrapper">
    <div class="ce-toolbar">
      <select
        class="ce-lang-select"
        :value="language"
        :disabled="disabled"
        @change="onLanguageChange"
      >
        <option value="java">Java</option>
        <option value="python">Python</option>
        <option value="cpp">C++</option>
        <option value="javascript">JavaScript</option>
        <option value="go">Go</option>
      </select>
      <span class="ce-file-name">{{ fileName }}</span>
    </div>
    <div class="ce-body">
      <div ref="lineNumbersRef" class="ce-lines">
        <div v-for="n in lineCount" :key="n">{{ n }}</div>
      </div>
      <textarea
        ref="textareaRef"
        class="ce-textarea"
        :value="modelValue"
        :disabled="disabled"
        placeholder="// 在此编写代码..."
        spellcheck="false"
        @input="onInput"
        @scroll="syncScroll"
        @keydown="onKeydown"
      />
    </div>
    <div class="ce-footer">
      <span>行数: {{ lineCount }}</span>
      <span>字符数: {{ charCount }}</span>
    </div>
  </div>
</template>

<style scoped>
.code-editor-wrapper {
  border: 1px solid #e2e8f0;
  border-radius: 8px;
  overflow: hidden;
}

.ce-toolbar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 8px 14px;
  background: #252526;
}

.ce-lang-select {
  background: #3c3c3c;
  color: #ccc;
  border: 1px solid #555;
  border-radius: 4px;
  padding: 4px 10px;
  font-size: 12px;
  cursor: pointer;
  outline: none;
}

.ce-lang-select:focus {
  border-color: #007acc;
}

.ce-file-name {
  font-size: 12px;
  color: #ccc;
  font-family: "JetBrains Mono", "SF Mono", monospace;
}

.ce-body {
  display: flex;
  background: #1e1e1e;
  min-height: 320px;
  max-height: 480px;
  overflow: hidden;
}

.ce-lines {
  width: 48px;
  padding: 12px 0;
  text-align: right;
  color: #858585;
  font-family: "JetBrains Mono", "SF Mono", monospace;
  font-size: 13px;
  line-height: 1.6;
  user-select: none;
  overflow: hidden;
  border-right: 1px solid #3e3e42;
  flex-shrink: 0;
}

.ce-lines div {
  padding-right: 12px;
}

.ce-textarea {
  flex: 1;
  background: transparent;
  color: #d4d4d4;
  border: none;
  padding: 12px 16px;
  font-family: "JetBrains Mono", "SF Mono", monospace;
  font-size: 13px;
  line-height: 1.6;
  resize: none;
  outline: none;
  white-space: pre;
  overflow: auto;
  tab-size: 4;
}

.ce-textarea::placeholder {
  color: #6a6a6a;
}

.ce-footer {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 6px 14px;
  background: #252526;
  border-top: 1px solid #3e3e42;
  font-size: 12px;
  color: #858585;
  font-family: "JetBrains Mono", "SF Mono", monospace;
}
</style>
