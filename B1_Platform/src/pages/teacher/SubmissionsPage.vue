<script setup lang="ts">
import { ref, computed, onMounted } from "vue"
import { ElMessage } from "element-plus"
import { FileText, Download } from "lucide-vue-next"
import { useTeacherStore } from "@/stores/useTeacherStore"
import * as teacherApi from "@/api/modules/teacher"
import type { IManualDeduction, IAttachment } from "@/types/teacher"
import BaseButton from "@/components/base/BaseButton.vue"
import LoadingState from "@/components/common/LoadingState.vue"
import ErrorState from "@/components/common/ErrorState.vue"

const store = useTeacherStore()
const loadError = ref("")
const currentIndex = ref(-1)
const teacherComment = ref("")
const manualDeductions = ref<IManualDeduction[]>([])
const manualPoints = ref(0)
const manualReason = ref("")
const showAdjust = ref(false)
const adjustIdx = ref(-1)
const adjPoints = ref(0)
const adjReason = ref("")
const codeContent = ref("")
const codeLoading = ref(false)

const currentSub = computed(() => {
  if (currentIndex.value < 0 || currentIndex.value >= store.pendingSubmissions.length) return null
  return store.pendingSubmissions[currentIndex.value]
})

const isCodeSubmission = computed(() => currentSub.value?.submissionType === "code")

const attachments = computed<IAttachment[]>(() => currentSub.value?.attachments ?? [])

const previewable = computed(() => {
  if (!attachments.value.length) return null
  const a = attachments.value[0]
  const ext = (a.fileName ?? "").split(".").pop()?.toLowerCase() ?? ""
  const type = (a.fileType ?? "").toLowerCase()
  if (["png", "jpg", "jpeg", "gif", "webp", "svg"].includes(ext) || type.startsWith("image/")) return "image"
  if (ext === "pdf" || type === "application/pdf") return "pdf"
  return null
})

const quickTags = ["代码规范良好", "功能实现完整", "文档需完善", "创新性强", "答辩表现优秀", "需要继续优化"]

const finalScore = computed(() => {
  if (!store.currentDiagnosis) return 100
  let total = 0
  store.currentDiagnosis.deductions.forEach((d) => {
    if (!d.overridden) total += d.adjustedDeduct !== undefined ? d.adjustedDeduct : d.suggestDeduct
  })
  manualDeductions.value.forEach((md) => { total += md.points })
  return Math.max(0, 100 - total)
})

const scoreRingClass = computed(() => {
  const s = finalScore.value
  if (s >= 85) return "ring-excellent"
  if (s >= 70) return "ring-good"
  if (s >= 60) return "ring-fair"
  return "ring-poor"
})

const gradeLabel = computed(() => {
  const s = finalScore.value
  if (s >= 85) return "优秀"
  if (s >= 70) return "良好"
  if (s >= 60) return "及格"
  return "不及格"
})

function formatFileSize(bytes?: number) {
  if (!bytes) return "-"
  if (bytes < 1024) return `${bytes} B`
  if (bytes < 1024 * 1024) return `${(bytes / 1024).toFixed(1)} KB`
  return `${(bytes / (1024 * 1024)).toFixed(1)} MB`
}

async function selectSubmission(idx: number) {
  currentIndex.value = idx
  const sub = store.pendingSubmissions[idx]
  if (!sub) return
  teacherComment.value = ""
  manualDeductions.value = []
  codeContent.value = ""

  // For code submissions, fetch detail (attachments) and then the code text
  if (sub.submissionType === "code") {
    codeLoading.value = true
    try {
      const detail = await teacherApi.fetchSubmissionDetail(sub.submissionId) as unknown as {
        attachments: Array<{ downloadUrl: string; fileName: string }>
      }
      // Also update the store's attachments for this submission
      const idx2 = store.pendingSubmissions.findIndex((s) => s.submissionId === sub.submissionId)
      if (idx2 >= 0 && detail?.attachments) {
        store.pendingSubmissions[idx2].attachments = detail.attachments.map((a) => ({
          fileId: String(a.fileId ?? ""),
          fileName: String(a.fileName ?? ""),
          fileSize: Number(a.fileSize ?? 0),
          fileType: String(a.fileType ?? ""),
          downloadUrl: String(a.downloadUrl ?? ""),
        }))
      }
      if (detail?.attachments?.length > 0 && detail.attachments[0].downloadUrl) {
        const resp = await fetch(detail.attachments[0].downloadUrl)
        codeContent.value = await resp.text()
      }
    } catch { /* code preview unavailable */ }
    finally { codeLoading.value = false }
  } else {
    store.fetchSubmissionDetail(sub.submissionId)
  }

  try { await store.fetchAIDiagnosis(sub.submissionId) }
  catch { ElMessage.error("加载AI诊断失败") }
}

function nextSubmission() {
  if (currentIndex.value < store.pendingSubmissions.length - 1) {
    selectSubmission(currentIndex.value + 1)
  }
}

function prevSubmission() {
  if (currentIndex.value > 0) selectSubmission(currentIndex.value - 1)
}

function handleDownload(attachment: IAttachment) {
  if (attachment.downloadUrl) {
    window.open(attachment.downloadUrl, "_blank")
  }
}

function insertTag(tag: string) {
  teacherComment.value = teacherComment.value + (teacherComment.value ? "；" : "") + tag
}

function addManual() {
  if (!manualPoints.value || !manualReason.value.trim()) { ElMessage.warning("请填写分值和原因"); return }
  manualDeductions.value.push({ points: manualPoints.value, reason: manualReason.value.trim() })
  manualPoints.value = 0; manualReason.value = ""
}

function removeManual(idx: number) { manualDeductions.value.splice(idx, 1) }

function acceptDeduction(idx: number) {
  if (!store.currentDiagnosis) return
  store.currentDiagnosis.deductions[idx].overridden = true
  store.currentDiagnosis.deductions[idx].overrideAction = "accept"
}

function rejectDeduction(idx: number) {
  if (!store.currentDiagnosis) return
  store.currentDiagnosis.deductions[idx].overridden = true
  store.currentDiagnosis.deductions[idx].overrideAction = "reject"
}

function openAdjust(idx: number) {
  if (!store.currentDiagnosis) return
  const d = store.currentDiagnosis.deductions[idx]
  adjustIdx.value = idx; adjPoints.value = d.adjustedDeduct || d.suggestDeduct
  adjReason.value = d.overrideReason || ""; showAdjust.value = true
}

function confirmAdjust() {
  if (!store.currentDiagnosis) return
  if (!adjReason.value.trim()) { ElMessage.warning("请填写调整理由"); return }
  const d = store.currentDiagnosis.deductions[adjustIdx.value]
  d.overridden = true; d.overrideAction = "adjust"
  d.adjustedDeduct = adjPoints.value; d.overrideReason = adjReason.value
  showAdjust.value = false; ElMessage.success("扣分已调整")
}

async function handlePublish() {
  if (!currentSub.value) return
  const final = parseFloat(finalScore.value.toFixed(1))
  const comment = teacherComment.value.trim()
  if (!window.confirm(`确认发布？\n总分: ${final} 分\n评语: ${comment || "无"}`)) return
  try {
    await store.publishReview(currentSub.value.submissionId, {
      status: "PUBLISHED",
      finalScore: final, comment,
      manualDeductions: manualDeductions.value,
      deductionOverrides: {},
    })
    ElMessage.success(`发布成功！总分 ${final} 分`)
    if (store.pendingSubmissions.length > 0) {
      currentIndex.value = Math.min(currentIndex.value, store.pendingSubmissions.length - 1)
      if (currentIndex.value >= 0) {
        teacherComment.value = ""; manualDeductions.value = []
        await selectSubmission(currentIndex.value)
      }
    } else { currentIndex.value = -1 }
  } catch (e: unknown) { ElMessage.error((e as Error)?.message || "发布失败") }
}

async function handleReject() {
  if (!currentSub.value) return
  const comment = teacherComment.value.trim()
  if (!comment) { ElMessage.warning("打回时请填写打回原因"); return }
  if (!window.confirm(`确认打回该提交？\n原因: ${comment}`)) return
  try {
    await store.publishReview(currentSub.value.submissionId, {
      status: "REJECTED",
      finalScore: 0, comment,
      manualDeductions: [],
      deductionOverrides: {},
    })
    ElMessage.warning("已打回")
    if (store.pendingSubmissions.length > 0) {
      currentIndex.value = Math.min(currentIndex.value, store.pendingSubmissions.length - 1)
      if (currentIndex.value >= 0) {
        teacherComment.value = ""; manualDeductions.value = []
        await selectSubmission(currentIndex.value)
      }
    } else { currentIndex.value = -1 }
  } catch (e: unknown) { ElMessage.error((e as Error)?.message || "操作失败") }
}

function codeLines() {
  if (codeContent.value) return codeContent.value.split("\n")
  return []
}

function isIssueLine(ln: number) {
  if (!store.currentDiagnosis) return false
  return store.currentDiagnosis.aiIssueLines.includes(ln)
}

async function initPage() {
  loadError.value = ""
  try { await store.fetchPendingSubmissions() }
  catch (e: unknown) { loadError.value = (e as Error)?.message || "加载失败" }
}

onMounted(() => { initPage() })
</script>

<template>
  <div class="review-root">
    <LoadingState v-if="store.submissionsLoading" text="加载待审核列表..." />
    <ErrorState v-else-if="loadError" :message="loadError" @retry="initPage" />

    <template v-else>
      <!-- Breadcrumb -->
      <div class="review-topbar">
        <span class="breadcrumb">审核工作台</span>
      </div>

      <!-- Three-panel area -->
      <div class="review-wb">
        <!-- LEFT: Student list -->
        <div class="review-left">
          <div class="panel-title">待审核提交 ({{ store.pendingSubmissions.length }})</div>
          <div class="student-list">
            <div
              v-for="(sub, idx) in store.pendingSubmissions"
              :key="sub.submissionId"
              :class="['student-item', { 'student-item--active': idx === currentIndex }]"
              @click="selectSubmission(idx)"
            >
              <div class="si-name">{{ sub.studentName }}</div>
              <div class="si-task">{{ sub.taskName }}</div>
              <div class="si-meta">
                <span :class="['si-type-tag', sub.submissionType === 'code' ? 'si-type-tag--code' : 'si-type-tag--file']">
                  {{ sub.submissionType === "code" ? "代码" : "文件" }}
                </span>
                <span class="si-time">{{ sub.submittedAt?.substring(0, 16).replace('T', ' ') }}</span>
              </div>
            </div>
          </div>
          <div v-if="store.pendingSubmissions.length === 0" class="empty-hint">暂无待审核提交</div>
        </div>

        <!-- CENTER: Preview -->
        <div class="review-center">
          <div class="panel-title" v-if="currentSub">
            {{ currentSub.taskName }} · {{ currentSub.studentName }}
          </div>
          <div v-else class="panel-title">请选择学生</div>

          <!-- Code preview -->
          <div v-if="!currentSub" class="empty-hint">← 从左侧选择学生预览提交内容</div>
          <template v-else-if="isCodeSubmission">
            <div v-if="codeLoading" class="empty-hint">正在加载代码...</div>
            <pre v-else class="code-preview"><div
              v-for="(line, i) in codeLines()"
              :key="i"
              :class="['code-line', { 'code-line--issue': isIssueLine(i) }]"
            ><span class="code-ln">{{ i + 1 }}</span><span class="code-txt">{{ line }}</span></div></pre>
          </template>

          <!-- Image preview -->
          <div v-else-if="previewable === 'image'" class="preview-image">
            <img :src="attachments[0].downloadUrl" :alt="attachments[0].fileName" style="max-width:100%;max-height:100%;object-fit:contain;" />
          </div>

          <!-- PDF preview -->
          <div v-else-if="previewable === 'pdf'" class="preview-pdf">
            <iframe :src="attachments[0].downloadUrl" width="100%" height="100%" style="border:none;" />
          </div>

          <!-- File preview (non-previewable) -->
          <div v-else class="file-preview">
            <div class="file-preview__icon"><FileText :size="48" /></div>
            <div v-if="attachments.length > 0" class="file-preview__name">{{ attachments[0].fileName }}</div>
            <div v-else class="file-preview__name">未知文件</div>
            <div v-if="attachments.length > 0" class="file-preview__size">{{ formatFileSize(attachments[0].fileSize) }}</div>
            <div class="file-preview__hint">该文件暂不支持在线预览，请下载后查看</div>
            <template v-if="attachments.length > 0">
              <BaseButton v-for="(att, i) in attachments" :key="i" type="primary" size="small" class="file-preview__btn" @click="handleDownload(att)">
                <Download :size="14" />
                <span style="margin-left: 4px">下载 {{ att.fileName }}</span>
              </BaseButton>
            </template>
          </div>
        </div>

        <!-- RIGHT: AI diagnosis + scoring -->
        <div class="review-right">
          <div v-if="!store.currentDiagnosis" class="empty-hint">请先选择学生</div>
          <template v-else>
            <!-- Score ring -->
            <div class="score-header">
              <div :class="['score-ring', scoreRingClass]">{{ store.currentDiagnosis.aiScore.toFixed(1) }}</div>
              <div>
                <div class="grade-label">{{ gradeLabel }}</div>
                <div class="grade-meta">共 {{ store.currentDiagnosis.totalDeductions }} 个问题 · 扣分 -{{ store.currentDiagnosis.totalDeductScore }}</div>
              </div>
            </div>

            <!-- Deduction items -->
            <div class="deductions">
              <div
                v-for="(d, i) in store.currentDiagnosis.deductions"
                :key="i"
                :class="['deduction-item', { 'deduction-item--rejected': d.overridden && d.overrideAction === 'reject' }]"
              >
                <div class="ded-header">
                  <span :class="['ded-tag', `ded-tag--${(d.agentType || 'CODE').toLowerCase()}`]">{{ d.agentType || 'CODE' }}</span>
                  <span class="ded-type">{{ d.issueType }}</span>
                  <span class="ded-score">-{{ d.adjustedDeduct !== undefined ? d.adjustedDeduct : d.suggestDeduct }}</span>
                </div>
                <div class="ded-reason">{{ d.reason }}</div>
                <div class="ded-file">{{ d.filePath }}:{{ d.lineNumber }} · 置信度 {{ (d.confidence * 100).toFixed(0) }}%</div>
                <div class="ded-actions">
                  <button :class="['ded-btn', { 'ded-btn--active': d.overridden && d.overrideAction === 'accept' }]" @click="acceptDeduction(i)">采纳</button>
                  <button :class="['ded-btn', { 'ded-btn--active': d.overridden && d.overrideAction === 'reject' }]" @click="rejectDeduction(i)">驳回</button>
                  <button class="ded-btn" @click="openAdjust(i)">调整</button>
                </div>
              </div>
            </div>
          </template>

          <!-- Teacher scoring -->
          <div class="score-zone">
            <div class="score-zone__title">教师评语</div>
            <textarea v-model="teacherComment" class="score-zone__textarea" placeholder="输入评语..." />
            <div class="quick-tags">
              <span v-for="tag in quickTags" :key="tag" class="quick-tag" @click="insertTag(tag)">{{ tag }}</span>
            </div>
            <div class="manual-row">
              <span class="manual-label">手动扣分</span>
              <input v-model.number="manualPoints" class="manual-points" type="number" placeholder="分值" min="1" max="100" />
              <input v-model="manualReason" class="manual-reason" placeholder="扣分原因" />
              <button class="manual-add" @click="addManual">+</button>
            </div>
            <div v-for="(md, i) in manualDeductions" :key="i" class="manual-item">
              <span class="manual-item__pts">-{{ md.points }}</span>
              <span class="manual-item__reason">{{ md.reason }}</span>
              <span class="manual-item__remove" @click="removeManual(i)">×</span>
            </div>
          </div>

          <!-- Final score -->
          <div class="final-score">
            最终得分: <span :class="['fs-val', scoreRingClass]">{{ finalScore.toFixed(1) }}</span>
          </div>
        </div>
      </div>

      <!-- Footer -->
      <div v-if="currentSub" class="review-footer">
        <div class="footer-nav">
          <BaseButton :disabled="currentIndex <= 0" @click="prevSubmission">← 上一个</BaseButton>
          <BaseButton :disabled="currentIndex >= store.pendingSubmissions.length - 1" @click="nextSubmission">下一个 →</BaseButton>
        </div>
        <div class="footer-actions">
          <BaseButton type="danger" @click="handleReject">打回</BaseButton>
          <BaseButton type="primary" @click="handlePublish">确认发布</BaseButton>
        </div>
      </div>
    </template>

    <!-- Adjust modal -->
    <teleport to="body">
      <div v-if="showAdjust" class="adj-overlay" @click.self="showAdjust = false">
        <div class="adj-modal">
          <div class="adj-modal__title">调整扣分</div>
          <div class="form-group"><label class="form-label">调整分值</label><input v-model.number="adjPoints" class="form-input" type="number" min="0" max="100" step="0.5" /></div>
          <div class="form-group"><label class="form-label">调整理由</label><textarea v-model="adjReason" class="form-textarea" rows="2" placeholder="请输入调整理由" /></div>
          <div class="adj-modal__btns">
            <BaseButton @click="showAdjust = false">取消</BaseButton>
            <BaseButton type="primary" @click="confirmAdjust">确认调整</BaseButton>
          </div>
        </div>
      </div>
    </teleport>
  </div>
</template>

<style scoped>
.review-root {
  display: flex;
  flex-direction: column;
  flex: 1;
  min-height: 0;
}

/* Top bar */
.review-topbar {
  padding: 12px 20px 0;
  flex-shrink: 0;
}

.breadcrumb {
  font-size: 13px;
  color: var(--color-text-secondary, #64748b);
}

/* === Three-panel === */
.review-wb {
  display: flex;
  flex: 1;
  min-height: 0;
}

/* LEFT */
.review-left {
  width: 280px;
  border-right: 1px solid var(--color-border, #e2e8f0);
  overflow-y: auto;
  padding: 16px;
  flex-shrink: 0;
  background: var(--color-card, #fff);
}

/* CENTER */
.review-center {
  flex: 1;
  overflow-y: auto;
  border-right: 1px solid var(--color-border, #e2e8f0);
  display: flex;
  flex-direction: column;
}

/* RIGHT */
.review-right {
  width: 380px;
  overflow-y: auto;
  padding: 16px;
  flex-shrink: 0;
  background: var(--color-card, #fff);
}

.panel-title { font-weight: 600; font-size: 13px; margin-bottom: 12px; padding: 0 4px; color: var(--color-text-primary, #1e293b); }
.empty-hint { color: var(--color-text-placeholder, #94a3b8); text-align: center; padding: 80px 0; font-size: 13px; }

/* Student list */
.student-list { display: flex; flex-direction: column; gap: 2px; }
.student-item { padding: 12px; border-radius: var(--radius-sm, 4px); cursor: pointer; transition: background 0.1s; border-left: 3px solid transparent; }
.student-item:hover { background: var(--color-bg, #f8fafc); }
.student-item--active { background: var(--color-primary-light, #eff6ff); border-left-color: var(--color-primary, #3b82f6); }
.si-name { font-size: 14px; font-weight: 500; color: var(--color-text-primary, #1e293b); }
.si-task { font-size: 12px; color: var(--color-text-placeholder, #94a3b8); margin-top: 2px; }
.si-meta { display: flex; align-items: center; gap: 8px; margin-top: 4px; }
.si-type-tag { font-size: 10px; font-weight: 600; padding: 1px 6px; border-radius: 3px; }
.si-type-tag--code { background: var(--color-primary-light, #eff6ff); color: var(--color-primary, #3b82f6); }
.si-type-tag--file { background: var(--color-success-light, #ecfdf5); color: #047857; }
.si-time { font-size: 11px; color: var(--color-text-placeholder, #94a3b8); font-family: var(--font-mono); }

/* Code preview */
.code-preview {
  font-family: var(--font-mono); font-size: 13px; line-height: 1.7;
  white-space: pre-wrap; background: #1e293b; color: #e2e8f0;
  padding: 16px; margin: 0; flex: 1; overflow: auto;
}
.code-line { display: flex; }
.code-line--issue { background: rgba(239, 68, 68, 0.12); }
.code-ln { width: 36px; text-align: right; color: #64748b; padding-right: 12px; user-select: none; flex-shrink: 0; }
.code-line--issue .code-ln::after { content: " ⚡"; font-size: 10px; }
.code-txt { flex: 1; }

/* Image / PDF preview */
.preview-image {
  flex: 1; display: flex; align-items: center; justify-content: center;
  background: #f1f5f9; padding: 16px; overflow: auto;
}
.preview-pdf {
  flex: 1; display: flex; min-height: 0;
}

/* File preview */
.file-preview {
  flex: 1; display: flex; flex-direction: column; align-items: center;
  justify-content: center; padding: 40px; gap: 12px;
}
.file-preview__icon { color: var(--color-text-placeholder, #94a3b8); opacity: 0.5; }
.file-preview__name { font-size: 16px; font-weight: 600; color: var(--color-text-primary, #1e293b); }
.file-preview__size { font-family: var(--font-mono); font-size: 13px; color: var(--color-text-secondary, #64748b); }
.file-preview__hint { font-size: 13px; color: var(--color-text-placeholder, #94a3b8); }
.file-preview__btn { margin-top: 8px; }

/* Score ring */
.score-header { display: flex; align-items: center; gap: 14px; margin-bottom: 16px; }
.score-ring { width: 56px; height: 56px; border-radius: 50%; display: flex; align-items: center; justify-content: center; font-family: var(--font-mono); font-size: 16px; font-weight: 700; flex-shrink: 0; }
.ring-excellent { background: #d1fae5; color: #065f46; border: 3px solid var(--color-success, #10b981); }
.ring-good { background: #eff6ff; color: #1d4ed8; border: 3px solid var(--color-primary, #3b82f6); }
.ring-fair { background: #fef3c7; color: #92400e; border: 3px solid var(--color-warning, #f59e0b); }
.ring-poor { background: #fee2e2; color: #991b1b; border: 3px solid var(--color-danger, #ef4444); }
.grade-label { font-size: 13px; font-weight: 500; color: var(--color-text-primary, #1e293b); }
.grade-meta { font-size: 11px; color: var(--color-text-placeholder, #94a3b8); }

/* Deductions */
.deductions { margin-bottom: 16px; }
.deduction-item { padding: 10px 12px; border: 1px solid var(--color-border, #e2e8f0); border-radius: var(--radius-sm, 4px); margin-bottom: 6px; background: var(--color-card, #fff); }
.deduction-item--rejected { opacity: 0.5; background: var(--color-bg, #f8fafc); }
.deduction-item--rejected .ded-reason { text-decoration: line-through; }
.ded-header { display: flex; align-items: center; gap: 6px; margin-bottom: 4px; }
.ded-tag { font-size: 10px; font-weight: 600; padding: 1px 6px; border-radius: 3px; text-transform: uppercase; }
.ded-tag--code { background: var(--color-primary-light, #eff6ff); color: var(--color-primary, #3b82f6); }
.ded-tag--doc { background: var(--color-success-light, #ecfdf5); color: #047857; }
.ded-tag--req { background: var(--color-warning-light, #fffbeb); color: #b45309; }
.ded-type { font-size: 12px; font-weight: 500; flex: 1; color: var(--color-text-primary, #1e293b); }
.ded-score { font-family: var(--font-mono); font-size: 13px; font-weight: 600; color: var(--color-danger, #ef4444); margin-left: auto; }
.ded-reason { font-size: 12px; color: var(--color-text-secondary, #64748b); line-height: 1.4; }
.ded-file { font-size: 11px; color: var(--color-text-placeholder, #94a3b8); font-family: var(--font-mono); margin-top: 2px; }
.ded-actions { display: flex; gap: 4px; margin-top: 6px; }
.ded-btn { font-size: 11px; padding: 2px 8px; border-radius: 3px; border: 1px solid var(--color-border, #e2e8f0); background: var(--color-bg, #f8fafc); cursor: pointer; color: var(--color-text-secondary, #64748b); }
.ded-btn:hover { background: #f1f5f9; }
.ded-btn--active { background: #d1fae5; border-color: #10b981; color: #047857; }

/* Score zone */
.score-zone { background: var(--color-bg, #f8fafc); border: 1px solid var(--color-border, #e2e8f0); border-radius: var(--radius-md, 8px); padding: 16px; margin-top: 0; }
.score-zone__title { font-size: 13px; font-weight: 600; margin-bottom: 8px; color: var(--color-text-primary, #1e293b); }
.score-zone__textarea { width: 100%; min-height: 80px; resize: vertical; padding: 8px 12px; font-size: 13px; font-family: inherit; border: 1px solid var(--color-border, #e2e8f0); border-radius: var(--radius-sm, 4px); background: var(--color-card, #fff); color: var(--color-text-primary, #1e293b); outline: none; box-sizing: border-box; }
.score-zone__textarea:focus { border-color: var(--color-primary, #3b82f6); }
.quick-tags { display: flex; flex-wrap: wrap; gap: 4px; margin-top: 8px; }
.quick-tag { font-size: 11px; padding: 2px 8px; border-radius: 12px; border: 1px solid var(--color-border, #e2e8f0); background: var(--color-card, #fff); cursor: pointer; color: var(--color-text-secondary, #64748b); transition: all var(--transition-fast, 150ms); }
.quick-tag:hover { background: var(--color-primary-light, #eff6ff); border-color: var(--color-primary, #3b82f6); color: var(--color-primary, #3b82f6); }
.manual-row { display: flex; gap: 6px; align-items: center; margin-top: 10px; }
.manual-label { font-size: 12px; color: var(--color-text-placeholder, #94a3b8); white-space: nowrap; }
.manual-points { width: 64px; padding: 5px 8px; font-size: 12px; border: 1px solid var(--color-border, #e2e8f0); border-radius: var(--radius-sm, 4px); background: var(--color-card, #fff); outline: none; box-sizing: border-box; }
.manual-reason { flex: 1; padding: 5px 8px; font-size: 12px; border: 1px solid var(--color-border, #e2e8f0); border-radius: var(--radius-sm, 4px); background: var(--color-card, #fff); outline: none; box-sizing: border-box; }
.manual-add { padding: 5px 10px; font-size: 13px; border: 1px solid var(--color-border, #e2e8f0); background: var(--color-bg, #f8fafc); border-radius: var(--radius-sm, 4px); cursor: pointer; color: var(--color-text-secondary, #64748b); }
.manual-item { display: flex; align-items: center; gap: 8px; padding: 6px 0; border-bottom: 1px solid #f1f5f9; font-size: 12px; }
.manual-item__pts { font-family: var(--font-mono); color: var(--color-danger, #ef4444); }
.manual-item__reason { flex: 1; color: var(--color-text-secondary, #64748b); }
.manual-item__remove { color: var(--color-danger, #ef4444); cursor: pointer; font-size: 14px; }

/* Final score */
.final-score { text-align: center; margin-top: 16px; font-size: 12px; color: var(--color-text-placeholder, #94a3b8); }
.fs-val { font-family: var(--font-mono); font-size: 28px; font-weight: 700; }

/* Footer */
.review-footer { border-top: 1px solid var(--color-border, #e2e8f0); padding: 12px 20px; display: flex; justify-content: space-between; align-items: center; flex-shrink: 0; background: var(--color-card, #fff); }
.footer-nav { display: flex; gap: 8px; }
.footer-actions { display: flex; gap: 8px; }

/* Adjust modal */
.adj-overlay { position: fixed; inset: 0; background: rgba(15, 23, 42, 0.4); z-index: 300; display: flex; align-items: center; justify-content: center; }
.adj-modal { background: var(--color-card, #fff); border-radius: var(--radius-lg, 12px); box-shadow: var(--shadow-xl); width: 400px; padding: 24px; }
.adj-modal__title { font-size: 15px; font-weight: 600; margin-bottom: 16px; color: var(--color-text-primary, #1e293b); }
.adj-modal__btns { display: flex; justify-content: flex-end; gap: 8px; margin-top: 16px; }
.form-group { margin-bottom: 16px; }
.form-label { display: block; font-size: 13px; font-weight: 500; color: var(--color-text-secondary, #64748b); margin-bottom: 5px; }
.form-input { width: 100%; padding: 8px 12px; font-size: 14px; border: 1px solid var(--color-border, #e2e8f0); border-radius: var(--radius-sm, 4px); background: var(--color-card, #fff); color: var(--color-text-primary, #1e293b); outline: none; box-sizing: border-box; }
.form-input:focus { border-color: var(--color-primary, #3b82f6); }
.form-textarea { width: 100%; padding: 8px 12px; font-size: 14px; border: 1px solid var(--color-border, #e2e8f0); border-radius: var(--radius-sm, 4px); background: var(--color-card, #fff); color: var(--color-text-primary, #1e293b); resize: vertical; outline: none; font-family: inherit; box-sizing: border-box; }
.form-textarea:focus { border-color: var(--color-primary, #3b82f6); }
</style>
