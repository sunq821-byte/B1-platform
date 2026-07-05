<script setup lang="ts">
import { onMounted } from "vue"
import { useRouter } from "vue-router"
import { useTeacherStore } from "@/stores/useTeacherStore"
import { useUserStore } from "@/stores/useUserStore"
import PageHeader from "@/components/layout/PageHeader.vue"
import BarChart from "@/components/chart/BarChart.vue"

const store = useTeacherStore()
const userStore = useUserStore()
const router = useRouter()

onMounted(() => {
  store.fetchDashboard()
})

function goToSubmissions() {
  router.push("/teacher/submissions")
}
</script>

<template>
  <div>
    <PageHeader
      title="工作台"
      :subtitle="`欢迎回来，${userStore.userName || '教师'}`"
    />

    <template v-if="store.dashboardData">
      <div class="stats-row">
        <div class="stat-card">
          <div class="stat-label">学生总数</div>
          <div class="stat-value">{{ store.dashboardData.stats.totalStudents }}</div>
          <div class="stat-meta">{{ store.dashboardData.stats.classCount }} 个班级</div>
        </div>
        <div class="stat-card">
          <div class="stat-label">待审核</div>
          <div class="stat-value">{{ store.dashboardData.stats.pendingCount }}</div>
          <div class="stat-meta">需要评阅的提交</div>
        </div>
        <div class="stat-card">
          <div class="stat-label">已评阅</div>
          <div class="stat-value">{{ store.dashboardData.stats.reviewedCount }}</div>
          <div class="stat-meta">完成审核</div>
        </div>
        <div class="stat-card">
          <div class="stat-label">提交率</div>
          <div class="stat-value">{{ store.dashboardData.stats.submissionRate }}%</div>
          <div class="stat-meta">全局提交比例</div>
        </div>
      </div>

      <div class="content-grid mt-24">
        <div class="section">
          <div class="section-header">
            <h2 class="section-title">待审核提交</h2>
            <a class="section-action" @click="goToSubmissions">查看全部 →</a>
          </div>
          <div class="item-list">
            <template v-if="store.dashboardData.pendingReviews.length > 0">
              <a
                v-for="item in store.dashboardData.pendingReviews"
                :key="item.submissionId"
                class="item-row"
                style="text-decoration: none; color: inherit;"
                @click="goToSubmissions"
              >
                <span class="item-id">{{ item.submissionId }}</span>
                <span class="item-name">{{ item.studentName }} - {{ item.taskName }}</span>
                <span class="item-meta">{{ item.submittedAt.substring(0, 10) }}</span>
                <span class="badge" :class="item.status === 'submitted' ? 'badge--warning' : 'badge--info'">
                  {{ item.status === 'submitted' ? '已提交' : '审核中' }}
                </span>
              </a>
            </template>
            <div v-else class="empty-state">
              <div class="empty-state-text">暂无待审核提交</div>
            </div>
          </div>
        </div>

        <div class="chart-wrap lg">
          <BarChart
            title="班级提交率"
            :x-axis="store.dashboardData.submitRateByClass.classNames"
            :series-data="[{ name: '提交率(%)', data: store.dashboardData.submitRateByClass.values }]"
            height="300px"
          />
        </div>
      </div>
    </template>
  </div>
</template>
