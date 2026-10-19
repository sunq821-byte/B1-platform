<script setup lang="ts">
import { ref, onMounted, computed } from "vue"
import { useRouter } from "vue-router"
import { useAdminStore } from "@/stores/useAdminStore"
import { useUserStore } from "@/stores/useUserStore"
import {
  ArrowRight, Users, School, Activity, Settings, Search,
} from "lucide-vue-next"
import LineChart from "@/components/chart/LineChart.vue"
import LoadingState from "@/components/common/LoadingState.vue"
import ErrorState from "@/components/common/ErrorState.vue"

const store = useAdminStore()
const userStore = useUserStore()
const router = useRouter()
const loadError = ref("")
const quickQuery = ref("")

const today = computed(() => {
  const d = new Date()
  return `${d.getFullYear()}-${String(d.getMonth() + 1).padStart(2, "0")}-${String(d.getDate()).padStart(2, "0")}`
})

const data = computed(() => store.dashboardData)
const stats = computed(() => data.value?.stats)
const runtime = computed(() => data.value?.runtimeSummary)
const trend = computed(() => data.value?.activityTrend)
const priority = computed(() => data.value?.priorityAlert)

const storageRate = computed(() => {
  const s = stats.value
  if (!s || !s.storageQuotaGb) return 0
  return Math.min(100, Math.round((s.storageUsedGb / s.storageQuotaGb) * 100))
})

const serviceHealthRate = computed(() => {
  const s = stats.value
  if (!s || !s.totalServices) return 0
  return Math.min(100, Math.round((s.healthyServices / s.totalServices) * 100))
})

const roleTotal = computed(() => {
  const s = stats.value
  if (!s) return 0
  return s.studentCount + s.teacherCount + s.adminCount
})

const roleBubbles = computed(() => {
  const s = stats.value
  const total = roleTotal.value || 1
  if (!s) return []
  return [
    { key: "student", label: "学生", pct: Math.round((s.studentCount / total) * 100), cls: "b1" },
    { key: "teacher", label: "教师", pct: Math.round((s.teacherCount / total) * 100), cls: "b2" },
    { key: "admin", label: "管理", pct: Math.round((s.adminCount / total) * 100), cls: "b3" },
  ]
})

const recentEvents = computed(() => (data.value?.recentLogs ?? []).slice(0, 5))

function deltaTone(v: number): "up" | "down" | "flat" {
  if (v > 0) return "up"
  if (v < 0) return "down"
  return "flat"
}

function deltaLabel(v: number): string {
  if (v > 0) return `↑ ${v}%`
  if (v < 0) return `↓ ${Math.abs(v)}%`
  return "→ 0%"
}

async function initPage() {
  loadError.value = ""
  try {
    await store.fetchDashboard()
  } catch (e: unknown) {
    loadError.value = (e as Error)?.message || "加载系统总览失败"
  }
}

function goTo(path: string) {
  router.push(path)
}

function relativeTime(iso: string): string {
  const diff = Date.now() - new Date(iso).getTime()
  const mins = Math.floor(diff / 60000)
  if (Number.isNaN(mins)) return iso
  if (mins < 1) return "刚刚"
  if (mins < 60) return `${mins}m`
  const hours = Math.floor(mins / 60)
  if (hours < 24) return `${hours}h`
  return `${Math.floor(hours / 24)}d`
}

function logBadge(type: string): { text: string; cls: string } {
  if (type === "error") return { text: "错误", cls: "danger" }
  if (type === "warning") return { text: "告警", cls: "warn" }
  if (type === "success") return { text: "成功", cls: "ok" }
  return { text: "信息", cls: "ai" }
}

function onQuickSearch() {
  const q = quickQuery.value.trim()
  if (!q) {
    goTo("/admin/organization?tab=users")
    return
  }
  goTo("/admin/organization?tab=users")
}

onMounted(() => { initPage() })
</script>

<template>
  <div class="dash">
    <div v-if="store.dashboardLoading" class="dash-state">
      <LoadingState text="加载系统总览..." />
    </div>
    <div v-else-if="loadError" class="dash-state">
      <ErrorState :message="loadError" @retry="initPage" />
    </div>

    <template v-else-if="data && stats">
      <!-- Row 1: Welcome + pills + KPIs -->
      <section class="hero">
        <article class="card welcome">
          <h1>Welcome in, {{ userStore.userName || "管理员" }}</h1>
          <p>{{ today }} · 管理员工作台 · 关注账号规模、服务健康与审计告警</p>
        </article>

        <article class="card pills">
          <div class="pill">
            <div class="pill-top"><span>账号启用率</span><b>{{ stats.accountEnableRate }}%</b></div>
            <div class="track"><i :style="{ width: `${Math.min(100, stats.accountEnableRate)}%`, background: 'var(--color-primary, #1677FF)' }" /></div>
          </div>
          <div class="pill">
            <div class="pill-top"><span>服务健康</span><b>{{ stats.healthyServices }}/{{ stats.totalServices || 0 }}</b></div>
            <div class="track"><i :style="{ width: `${serviceHealthRate}%`, background: 'var(--color-success, #00B96B)' }" /></div>
          </div>
          <div class="pill">
            <div class="pill-top"><span>存储占用</span><b>{{ storageRate }}%</b></div>
            <div class="track"><i :style="{ width: `${storageRate}%`, background: 'var(--color-warning, #FAAD14)' }" /></div>
          </div>
          <div class="pill">
            <div class="pill-top"><span>AI 今日配额</span><b>{{ stats.aiQuotaRate }}%</b></div>
            <div class="track"><i :style="{ width: `${Math.min(100, stats.aiQuotaRate)}%`, background: 'var(--color-ai, #722ED1)' }" /></div>
          </div>
        </article>

        <article class="card kpis">
          <div class="kpi">
            <span class="kpi-badge" :class="deltaTone(stats.alertDeltaPct)">{{ deltaLabel(stats.alertDeltaPct) }}</span>
            <div class="kpi-val">{{ stats.alertCount24h }}</div>
            <div class="kpi-label">近 24h 告警</div>
          </div>
          <div class="kpi">
            <span class="kpi-badge" :class="deltaTone(stats.aiCallsDeltaPct)">{{ deltaLabel(stats.aiCallsDeltaPct) }}</span>
            <div class="kpi-val">{{ stats.aiCallsToday }}</div>
            <div class="kpi-label">今日 AI 调用</div>
          </div>
          <div class="kpi">
            <span class="kpi-badge" :class="deltaTone(stats.completionDeltaPct)">{{ deltaLabel(stats.completionDeltaPct) }}</span>
            <div class="kpi-val">{{ stats.completionRate }}%</div>
            <div class="kpi-label">提交完成率</div>
          </div>
        </article>
      </section>

      <!-- Row 2: Runtime | Trend | Priority -->
      <section class="mid">
        <article class="card">
          <div class="head">
            <h3>运行摘要</h3>
            <span class="muted">本小时</span>
          </div>
          <div class="summary-row">
            <div class="summary-left"><i class="dot" style="background:#1677FF" />正常服务</div>
            <div class="summary-val">{{ runtime?.healthy ?? 0 }}</div>
          </div>
          <div class="summary-row">
            <div class="summary-left"><i class="dot" style="background:#FAAD14" />降级 / 告警</div>
            <div class="summary-val">{{ runtime?.degraded ?? 0 }}</div>
          </div>
          <div class="summary-row">
            <div class="summary-left"><i class="dot" style="background:#FF4D4F" />不可用</div>
            <div class="summary-val">{{ runtime?.down ?? 0 }}</div>
          </div>
          <div class="summary-row">
            <div class="summary-left"><i class="dot" style="background:#722ED1" />AI 队列积压</div>
            <div class="summary-val">{{ runtime?.aiQueue ?? 0 }}</div>
          </div>
        </article>

        <article class="card chart-card">
          <div class="head">
            <h3>平台活跃趋势</h3>
            <div class="seg" aria-label="时间范围">
              <span>30 天</span>
              <span class="on">7 天</span>
              <span>24 小时</span>
            </div>
          </div>
          <LineChart
            :x-axis="trend?.xAxis ?? []"
            :series-data="[
              { name: '日活用户', data: trend?.activeUsers ?? [], color: '#1677FF' },
              { name: 'AI 调用', data: trend?.aiCalls ?? [], color: '#00B96B' },
            ]"
            height="240px"
          />
        </article>

        <article v-if="priority" class="card alert-card">
          <div>
            <div class="head"><h3>{{ priority.title || '优先处理' }}</h3></div>
            <p>{{ priority.summary }}</p>
            <div class="alert-tags">
              <span class="tag">Priority · {{ priority.priority }}</span>
              <span v-if="priority.source" class="tag">Source · {{ priority.source }}</span>
            </div>
          </div>
          <button type="button" class="alert-cta" @click="goTo(priority.actionPath || '/admin/monitor')">
            前往运行监控 →
          </button>
        </article>
        <article v-else class="card alert-card alert-card--ok">
          <div>
            <div class="head"><h3>优先处理</h3></div>
            <p>当前无高优先级告警。可继续查看运行监控，确认各依赖服务处于健康状态。</p>
          </div>
          <button type="button" class="alert-cta" @click="goTo('/admin/monitor')">
            查看运行监控 →
          </button>
        </article>
      </section>

      <!-- Row 3: Events | Roles | Shortcuts -->
      <section class="bottom">
        <article class="card">
          <div class="head">
            <h3>最近运维事件</h3>
            <button type="button" class="link-all" @click="goTo('/admin/logs')">
              查看全部日志 <ArrowRight :size="14" />
            </button>
          </div>
          <div
            v-for="(l, i) in recentEvents"
            :key="i"
            class="incident"
          >
            <div class="incident-top">
              <span class="badge" :class="logBadge(l.type).cls">{{ logBadge(l.type).text }}</span>
              <span class="id">{{ relativeTime(l.createdAt) }}</span>
            </div>
            <p>{{ l.message }}{{ l.detail ? `：${l.detail}` : '' }}</p>
            <div class="incident-foot">
              <span>{{ l.operator || '系统' }}</span>
              <span v-if="l.ip">{{ l.ip }}</span>
            </div>
          </div>
          <div v-if="!recentEvents.length" class="empty">暂无运维事件</div>
        </article>

        <article class="card">
          <div class="head"><h3>角色构成</h3></div>
          <div class="stat-bubbles" aria-label="角色占比">
            <div
              v-for="b in roleBubbles"
              :key="b.key"
              class="bubble"
              :class="b.cls"
            >
              {{ b.label }}<br />{{ b.pct }}%
            </div>
          </div>
          <div class="role-meta">
            总账号 <b>{{ roleTotal }}</b>
            · 学生 {{ stats.studentCount }}
            · 教师 {{ stats.teacherCount }}
            · 管理 {{ stats.adminCount }}
          </div>
        </article>

        <article class="card">
          <div class="head"><h3>运维快捷台</h3></div>
          <p class="help-intro">Hi, {{ userStore.userName || "管理员" }}。常用运维入口：</p>
          <div class="help-grid">
            <button type="button" class="help-item" @click="goTo('/admin/organization?tab=users')">
              <Users :size="14" /> 新建用户
              <span>账号开通</span>
            </button>
            <button type="button" class="help-item" @click="goTo('/admin/classes')">
              <School :size="14" /> 班级管理
              <span>组织维护</span>
            </button>
            <button type="button" class="help-item" @click="goTo('/admin/monitor')">
              <Activity :size="14" /> 运行监控
              <span>资源与服务</span>
            </button>
            <button type="button" class="help-item" @click="goTo('/admin/system')">
              <Settings :size="14" /> 系统配置
              <span>维护模式</span>
            </button>
          </div>
          <form class="ask" @submit.prevent="onQuickSearch">
            <Search :size="14" />
            <input v-model="quickQuery" type="search" placeholder="搜索用户 / 班级 / 日志…" />
            <button type="submit">Enter</button>
          </form>
        </article>
      </section>
    </template>
  </div>
</template>

<style scoped>
.dash-state { padding: 48px 0; }

.hero, .mid, .bottom {
  display: grid;
  gap: 16px;
  margin-bottom: 16px;
}
.hero { grid-template-columns: 1.2fr 1.5fr 1fr; }
.mid { grid-template-columns: 0.85fr 1.55fr 0.95fr; }
.bottom { grid-template-columns: 1.35fr 0.9fr 1fr; }

.card {
  background: var(--color-card, #fff);
  border: 1px solid var(--color-border, #E5E7EB);
  border-radius: 18px;
  padding: 18px;
  box-shadow: var(--shadow-card, 0 1px 2px rgba(15, 23, 42, 0.04), 0 8px 24px rgba(15, 23, 42, 0.04));
}

.welcome h1 {
  margin: 0;
  font-size: 28px;
  font-weight: 800;
  letter-spacing: -0.03em;
  color: var(--color-navy, #0B3A5C);
  line-height: 1.2;
}
.welcome p {
  margin: 8px 0 0;
  font-size: 13px;
  color: var(--color-text-secondary, #6B7280);
}

.pills {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 10px;
}
.pill {
  border: 1px solid var(--color-border, #E5E7EB);
  border-radius: 999px;
  padding: 10px 12px;
  background: #F8FAFC;
  display: grid;
  gap: 6px;
}
.pill-top {
  display: flex;
  justify-content: space-between;
  font-size: 11px;
  color: var(--color-text-secondary, #6B7280);
}
.pill-top b {
  color: var(--color-text-primary, #1F2937);
  font-family: var(--font-mono);
}
.track {
  height: 6px;
  border-radius: 999px;
  background: #E5E7EB;
  overflow: hidden;
}
.track i {
  display: block;
  height: 100%;
  border-radius: 999px;
}

.kpis {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 10px;
  align-content: center;
}
.kpi { text-align: left; padding: 8px 4px; }
.kpi-badge {
  display: inline-flex;
  align-items: center;
  font-size: 11px;
  font-weight: 650;
  padding: 3px 8px;
  border-radius: 999px;
  margin-bottom: 8px;
}
.kpi-badge.up { background: #E6F7EF; color: #009A57; }
.kpi-badge.down { background: #FFF1F0; color: #FF4D4F; }
.kpi-badge.flat { background: #F3F4F6; color: #6B7280; }
.kpi-val {
  font-family: var(--font-mono);
  font-size: 28px;
  font-weight: 750;
  letter-spacing: -0.04em;
  line-height: 1;
  color: var(--color-text-primary, #1F2937);
}
.kpi-label {
  margin-top: 6px;
  font-size: 12px;
  color: var(--color-text-secondary, #6B7280);
}

.head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 8px;
  margin-bottom: 14px;
}
.head h3 {
  margin: 0;
  font-size: 16px;
  font-weight: 800;
  letter-spacing: -0.02em;
  color: var(--color-text-primary, #1F2937);
}
.muted { font-size: 12px; color: var(--color-text-secondary, #6B7280); }

.summary-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 12px 0;
  border-bottom: 1px solid #F1F5F9;
  font-size: 13px;
}
.summary-row:last-child { border-bottom: 0; }
.summary-left {
  display: flex;
  align-items: center;
  gap: 8px;
  color: var(--color-text-secondary, #6B7280);
}
.dot {
  width: 8px;
  height: 8px;
  border-radius: 50%;
  display: inline-block;
  flex-shrink: 0;
}
.summary-val {
  font-family: var(--font-mono);
  font-weight: 700;
}

.seg {
  display: inline-flex;
  border: 1px solid var(--color-border, #E5E7EB);
  border-radius: 10px;
  overflow: hidden;
  font-size: 11px;
}
.seg span {
  padding: 6px 8px;
  color: var(--color-text-secondary, #6B7280);
  background: #fff;
  border-right: 1px solid var(--color-border, #E5E7EB);
}
.seg span:last-child { border-right: 0; }
.seg span.on {
  background: #E6F4FF;
  color: var(--color-primary, #1677FF);
  font-weight: 650;
}

.chart-card :deep(.chart-card) {
  background: transparent;
  padding: 0;
  box-shadow: none;
  border-radius: 0;
}
.chart-card :deep(.chart-card__title) { display: none; }

.alert-card {
  background: linear-gradient(145deg, #0B3A5C 0%, #1677FF 100%);
  color: #fff;
  border: none;
  display: flex;
  flex-direction: column;
  justify-content: space-between;
  min-height: 280px;
}
.alert-card--ok {
  background: linear-gradient(145deg, #0B3A5C 0%, #0958D9 55%, #1677FF 100%);
}
.alert-card .head h3 { color: #fff; }
.alert-card p {
  margin: 0;
  font-size: 13px;
  line-height: 1.65;
  color: rgba(255, 255, 255, 0.88);
}
.alert-tags {
  display: flex;
  gap: 8px;
  flex-wrap: wrap;
  margin-top: 16px;
}
.tag {
  font-size: 11px;
  font-weight: 650;
  padding: 6px 10px;
  border-radius: 999px;
  background: rgba(255, 255, 255, 0.16);
  border: 1px solid rgba(255, 255, 255, 0.22);
}
.alert-cta {
  margin-top: 18px;
  align-self: flex-start;
  border: none;
  background: #fff;
  color: var(--color-navy, #0B3A5C);
  font-weight: 700;
  font-size: 13px;
  padding: 10px 14px;
  border-radius: 10px;
  cursor: pointer;
}
.alert-cta:hover { opacity: 0.92; }

.link-all {
  display: inline-flex;
  align-items: center;
  gap: 4px;
  border: none;
  background: transparent;
  color: var(--color-primary, #1677FF);
  font-size: 12px;
  cursor: pointer;
  padding: 0;
}

.incident {
  border: 1px solid var(--color-border, #E5E7EB);
  border-radius: 14px;
  padding: 12px;
  background: #F8FAFC;
  margin-bottom: 10px;
}
.incident:last-child { margin-bottom: 0; }
.incident-top {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 8px;
}
.badge {
  font-size: 10px;
  font-weight: 700;
  padding: 3px 8px;
  border-radius: 999px;
}
.badge.warn { background: #FFF7E6; color: #D48806; }
.badge.ok { background: #E6F7EF; color: #009A57; }
.badge.ai { background: #F9F0FF; color: #722ED1; }
.badge.danger { background: #FFF1F0; color: #FF4D4F; }
.id {
  font-family: var(--font-mono);
  font-size: 11px;
  color: var(--color-text-secondary, #6B7280);
}
.incident p {
  margin: 0;
  font-size: 13px;
  color: var(--color-text-primary, #1F2937);
  line-height: 1.5;
}
.incident-foot {
  margin-top: 10px;
  display: flex;
  gap: 10px;
  font-size: 11px;
  color: var(--color-text-secondary, #6B7280);
  font-family: var(--font-mono);
}

.stat-bubbles {
  height: 200px;
  position: relative;
  border-radius: 14px;
  background: #F8FAFC;
  border: 1px dashed var(--color-border, #E5E7EB);
  overflow: hidden;
}
.bubble {
  position: absolute;
  border-radius: 50%;
  display: grid;
  place-items: center;
  color: #fff;
  font-size: 12px;
  font-weight: 700;
  text-align: center;
  line-height: 1.3;
  padding: 8px;
}
.b1 { width: 120px; height: 120px; left: 28px; top: 36px; background: #1677FF; }
.b2 { width: 88px; height: 88px; right: 36px; top: 24px; background: #00B96B; }
.b3 { width: 72px; height: 72px; right: 58px; bottom: 24px; background: #0B3A5C; }
.role-meta {
  margin-top: 12px;
  font-size: 12px;
  color: var(--color-text-secondary, #6B7280);
}
.role-meta b {
  font-family: var(--font-mono);
  color: var(--color-text-primary, #1F2937);
}

.help-intro {
  margin: 0;
  font-size: 13px;
  color: var(--color-text-secondary, #6B7280);
}
.help-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 10px;
  margin: 12px 0 14px;
}
.help-item {
  border: 1px solid var(--color-border, #E5E7EB);
  border-radius: 12px;
  padding: 12px;
  background: #F8FAFC;
  font-size: 12px;
  color: var(--color-text-primary, #1F2937);
  font-weight: 600;
  min-height: 64px;
  text-align: left;
  cursor: pointer;
  font-family: inherit;
  display: flex;
  flex-direction: column;
  gap: 2px;
  align-items: flex-start;
}
.help-item span {
  display: block;
  margin-top: 2px;
  font-weight: 400;
  color: var(--color-text-secondary, #6B7280);
  font-size: 11px;
}
.help-item:hover {
  background: #fff;
  box-shadow: var(--shadow-md, 0 4px 12px rgba(15, 23, 42, 0.08));
}

.ask {
  display: flex;
  align-items: center;
  gap: 8px;
  border: 1px solid var(--color-border, #E5E7EB);
  border-radius: 12px;
  padding: 10px 12px;
  background: #fff;
  color: var(--color-text-secondary, #6B7280);
}
.ask input {
  flex: 1;
  border: none;
  outline: none;
  background: transparent;
  font: inherit;
  font-size: 12px;
  color: var(--color-text-primary, #1F2937);
  min-width: 0;
}
.ask button {
  border: none;
  background: transparent;
  color: var(--color-primary, #1677FF);
  font-size: 11px;
  font-weight: 700;
  cursor: pointer;
  padding: 0;
}

.empty {
  text-align: center;
  padding: 28px 0;
  font-size: 13px;
  color: var(--color-text-placeholder, #9CA3AF);
}

@media (max-width: 1023.98px) {
  .hero, .mid, .bottom { grid-template-columns: 1fr; }
  .kpis { grid-template-columns: repeat(3, 1fr); }
}
@media (max-width: 767.98px) {
  .welcome h1 { font-size: 22px; }
  .pills { grid-template-columns: 1fr 1fr; }
  .kpis { grid-template-columns: 1fr 1fr; gap: 10px; }
  .help-grid { grid-template-columns: 1fr; }
  .alert-card { min-height: auto; }
  .hero, .mid, .bottom { gap: 12px; min-width: 0; overflow: hidden; }
}
</style>
