<script setup lang="ts">
import { ref, computed, onMounted } from "vue"
import { ElMessage } from "element-plus"
import { useTeacherStore } from "@/stores/useTeacherStore"
import PageHeader from "@/components/layout/PageHeader.vue"
import BaseButton from "@/components/base/BaseButton.vue"
import BaseModal from "@/components/base/BaseModal.vue"
import LoadingState from "@/components/common/LoadingState.vue"
import ErrorState from "@/components/common/ErrorState.vue"

const store = useTeacherStore()
const loadError = ref("")
const filterClass = ref("all")
const searchTerm = ref("")
const fileInput = ref<HTMLInputElement>()

const classes = computed(() => {
  const set = new Set(store.students.map((s) => s.className))
  return Array.from(set).sort()
})

const filteredStudents = computed(() => {
  return store.students.filter((s) => {
    if (filterClass.value !== "all" && s.className !== filterClass.value) return false
    if (searchTerm.value && !s.userId.includes(searchTerm.value) && !s.realName.includes(searchTerm.value)) return false
    return true
  })
})

const showDetail = ref(false)
const detailLoading = ref(false)

function scoreColor(score: number | null) {
  if (score == null) return ""
  if (score >= 80) return "clr-good"
  if (score >= 60) return "clr-warn"
  return "clr-danger"
}

async function openDetail(userId: string) {
  detailLoading.value = true
  try {
    await store.fetchStudentDetail(userId)
    showDetail.value = true
  } catch { /* ignore */ }
  finally { detailLoading.value = false }
}

function handleImportClick() {
  fileInput.value?.click()
}

function parseCSVLine(line: string): string[] {
  const result: string[] = []
  let current = ""
  let inQuotes = false
  for (let i = 0; i < line.length; i++) {
    const ch = line[i]
    if (inQuotes) {
      if (ch === '"') {
        if (i + 1 < line.length && line[i + 1] === '"') { current += '"'; i++ }
        else inQuotes = false
      } else current += ch
    } else {
      if (ch === '"') inQuotes = true
      else if (ch === ",") { result.push(current.trim()); current = "" }
      else current += ch
    }
  }
  result.push(current.trim())
  return result
}

async function handleImportFile(e: Event) {
  const input = e.target as HTMLInputElement
  const file = input.files?.[0]
  if (!file) return

  try {
    const text = await file.text()
    const lines = text.split(/\r?\n/).filter((l) => l.trim())
    if (lines.length < 2) { ElMessage.warning("文件格式不正确：至少需要标题行和一条数据"); return }

    const headers = parseCSVLine(lines[0]).map((h) => h.toLowerCase())
    const idIdx = headers.findIndex((h) => h.includes("学号") || h === "studentid")
    const nameIdx = headers.findIndex((h) => h.includes("姓名") || h === "name")
    const classIdx = headers.findIndex((h) => h.includes("班级") || h === "classname" || h === "class")
    const emailIdx = headers.findIndex((h) => h.includes("邮箱") || h === "email")

    if (idIdx < 0 || nameIdx < 0) { ElMessage.warning("文件缺少必要列：学号、姓名"); return }

    let successCount = 0
    let skipCount = 0
    const existingIds = new Set(store.students.map((s) => s.studentId))

    for (let i = 1; i < lines.length; i++) {
      const cols = parseCSVLine(lines[i])
      const sid = cols[idIdx]
      if (existingIds.has(sid)) { skipCount++; continue }
      const importedName = cols[nameIdx] || ''
      store.students.push({
        userId: `IMP${Date.now()}${i}`,
        studentId: sid,
        name: importedName,
        realName: importedName,
        className: cols[classIdx] || "",
        email: cols[emailIdx] || "",
        phone: '',
        completedCount: 0,
        submissionCount: 0,
        avgScore: null,
      })
      existingIds.add(sid)
      successCount++
    }

    ElMessage.success(`导入完成：成功 ${successCount} 条${skipCount > 0 ? `，跳过重复 ${skipCount} 条` : ""}`)
  } catch {
    ElMessage.error("文件解析失败，请检查文件格式")
  } finally {
    input.value = ""
  }
}

function handleExport() {
  const rows = filteredStudents.value
  if (rows.length === 0) { ElMessage.warning("当前列表无数据可导出"); return }

  const BOM = "﻿"
  const header = "学号,姓名,班级,邮箱,完成数,均分"
  const lines = rows.map((s) =>
    `${s.userId},${s.realName},${s.className},${s.email},${s.submissionCount},${s.avgScore != null ? s.avgScore.toFixed(1) : ""}`,
  )
  const csv = BOM + header + "\n" + lines.join("\n")
  const blob = new Blob([csv], { type: "text/csv;charset=utf-8;" })
  const url = URL.createObjectURL(blob)

  const a = document.createElement("a")
  a.href = url
  a.download = `学生名单_${new Date().toISOString().substring(0, 10)}.csv`
  a.click()
  URL.revokeObjectURL(url)

  ElMessage.success(`已导出 ${rows.length} 条学生数据`)
}

async function initPage() {
  loadError.value = ""
  try { await store.fetchStudents() }
  catch (e: unknown) { loadError.value = (e as Error)?.message || "加载失败" }
}

onMounted(() => { initPage() })
</script>

<template>
  <div class="page">
    <PageHeader title="学生管理" :subtitle="`共 ${filteredStudents.length} 名学生`" />

    <LoadingState v-if="store.studentsLoading" />
    <ErrorState v-else-if="loadError" :message="loadError" @retry="initPage" />

    <template v-else>
      <div class="filter-bar">
        <select v-model="filterClass" class="form-select filter-select">
          <option value="all">全部班级</option>
          <option v-for="c in classes" :key="c" :value="c">{{ c }}</option>
        </select>
        <input v-model="searchTerm" class="form-input filter-input" placeholder="搜索学号或姓名..." />
        <div class="filter-actions">
          <input ref="fileInput" type="file" accept=".csv,.xlsx,.xls" class="file-input-hidden" @change="handleImportFile" />
          <BaseButton size="small" @click="handleImportClick">📥 导入学生</BaseButton>
          <BaseButton size="small" @click="handleExport">📤 导出名单</BaseButton>
        </div>
      </div>

      <div class="data-table">
        <table>
          <thead>
            <tr><th>学号</th><th>姓名</th><th>班级</th><th>邮箱</th><th>完成数</th><th>均分</th><th>操作</th></tr>
          </thead>
          <tbody>
            <tr v-for="s in filteredStudents" :key="s.userId">
              <td><span class="mono-sm">{{ s.userId }}</span></td>
              <td>{{ s.realName }}</td>
              <td>{{ s.className || '-' }}</td>
              <td><span class="email">{{ s.email || '-' }}</span></td>
              <td>{{ s.submissionCount }}</td>
              <td>
                <span :class="['mono-score', scoreColor(s.avgScore)]">
                  {{ s.avgScore != null ? s.avgScore.toFixed(1) : '-' }}
                </span>
              </td>
              <td>
                <BaseButton size="small" @click="openDetail(s.userId)">详情</BaseButton>
              </td>
            </tr>
          </tbody>
        </table>
      </div>

      <BaseModal v-model="showDetail" :title="store.currentStudent ? store.currentStudent.realName + ' - 详情' : '学生详情'" width="640px" @confirm="showDetail = false" :confirm-text="'关闭'">
        <LoadingState v-if="detailLoading" />
        <template v-else-if="store.currentStudent">
          <div class="detail-grid">
            <div><span class="dl">学号</span><br>{{ store.currentStudent.userId }}</div>
            <div><span class="dl">姓名</span><br>{{ store.currentStudent.realName }}</div>
            <div><span class="dl">班级</span><br>{{ store.currentStudent.className || '-' }}</div>
            <div><span class="dl">邮箱</span><br>{{ store.currentStudent.email || '-' }}</div>
            <div><span class="dl">已完成</span><br>{{ store.currentStudent.submissionCount }}</div>
            <div>
              <span class="dl">均分</span><br>
              <span class="detail-avg">{{ store.currentStudent.avgScore != null ? store.currentStudent.avgScore.toFixed(1) : '-' }}</span>
            </div>
          </div>

          <div v-if="store.currentStudent.grades.length > 0" class="grade-section">
            <h4 class="grade-title">成绩记录</h4>
            <div v-for="g in store.currentStudent.grades" :key="g.taskName" class="grade-row">
              <span class="grade-name">{{ g.taskName }}</span>
              <span :class="['grade-score', scoreColor(g.score)]">{{ g.score.toFixed(1) }}</span>
              <span class="grade-date">{{ g.reviewedAt ? g.reviewedAt.substring(0, 10) : '-' }}</span>
            </div>
          </div>
        </template>
      </BaseModal>
    </template>
  </div>
</template>

<style scoped>
.page { /* width/centering handled by AppLayout */ }

.filter-bar { display: flex; align-items: center; gap: 12px; margin-bottom: 16px; flex-wrap: wrap; }
.filter-select { width: 160px; }
.filter-input { width: 200px; }
.filter-actions { margin-left: auto; display: flex; gap: 8px; }
.file-input-hidden { display: none; }

.form-select, .form-input {
  padding: 8px 12px; font-size: 14px;
  border: 1px solid var(--color-border, #e2e8f0); border-radius: var(--radius-sm, 4px);
  background: var(--color-card, #fff); color: var(--color-text-primary, #1e293b); outline: none;
  box-sizing: border-box;
}
.form-select:focus, .form-input:focus { border-color: var(--color-primary, #3b82f6); }

.data-table {
  width: 100%; background: var(--color-card, #fff);
  border: 1px solid var(--color-border, #e2e8f0); border-radius: var(--radius-md, 8px); overflow: hidden;
}
.data-table table { width: 100%; border-collapse: collapse; }
.data-table th { text-align: left; padding: 10px 16px; font-size: 12px; font-weight: 600; color: var(--color-text-placeholder, #94a3b8); text-transform: uppercase; letter-spacing: 0.03em; background: #f8fafc; border-bottom: 1px solid var(--color-border, #e2e8f0); }
.data-table td { padding: 11px 16px; font-size: 14px; border-bottom: 1px solid #f1f5f9; }
.data-table tbody tr:last-child td { border-bottom: none; }
.data-table tbody tr:hover td { background: #f8fafc; }

.mono-sm { font-family: var(--font-mono); font-size: 12px; }
.mono-score { font-family: var(--font-mono); font-weight: 600; }
.email { color: var(--color-text-placeholder, #94a3b8); font-size: 12px; }

.clr-good { color: var(--color-success, #10b981); }
.clr-warn { color: var(--color-warning, #f59e0b); }
.clr-danger { color: var(--color-danger, #ef4444); }

.detail-grid { display: grid; grid-template-columns: 1fr 1fr; gap: 12px; font-size: 13px; }
.dl { color: var(--color-text-placeholder, #94a3b8); }
.detail-avg { font-family: var(--font-mono); font-size: 18px; font-weight: 600; }

.grade-section { margin-top: 16px; }
.grade-title { font-size: 13px; font-weight: 500; margin-bottom: 8px; color: var(--color-text-primary, #1e293b); }
.grade-row { display: flex; align-items: center; gap: 10px; padding: 6px 0; border-bottom: 1px solid #f1f5f9; }
.grade-name { flex: 1; font-size: 13px; }
.grade-score { font-family: var(--font-mono); font-weight: 600; min-width: 48px; }
.grade-date { font-size: 11px; color: var(--color-text-placeholder, #94a3b8); }
</style>
