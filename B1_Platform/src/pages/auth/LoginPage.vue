<script setup lang="ts">
import { ref, reactive, onMounted, onUnmounted } from "vue"
import { useRouter, useRoute } from "vue-router"
import { useUserStore } from "@/stores/useUserStore"
import { ElMessage } from "element-plus"
import { BookOpen } from "lucide-vue-next"
import BaseInput from "@/components/base/BaseInput.vue"
import BaseButton from "@/components/base/BaseButton.vue"

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()

const REMEMBER_KEY = "b1_remember_login"

interface RememberData {
  username: string
  password: string
  checked: boolean
}

const formRef = ref()
const loading = ref(false)
const isOffline = ref(!window.navigator.onLine)

const form = reactive({
  username: "",
  password: "",
  remember: false,
})

const rules = {
  username: [
    { required: true, message: "请输入用户名", trigger: "blur" },
    { min: 3, message: "用户名至少3个字符", trigger: "blur" },
  ],
  password: [
    { required: true, message: "请输入密码", trigger: "blur" },
  ],
}

const ERROR_MESSAGES: Record<number, string> = {
  2004: "用户名或密码错误",
  2005: "账号已被锁定，请联系管理员",
  2006: "账号已被禁用，请联系管理员",
}

function handleOnline() {
  isOffline.value = false
}

function handleOffline() {
  isOffline.value = true
}

// 自动填充记住的凭据
function loadRemembered() {
  try {
    const raw = localStorage.getItem(REMEMBER_KEY)
    if (!raw) return
    const data: RememberData = JSON.parse(raw)
    if (data.checked) {
      form.username = data.username
      form.password = window.atob(data.password)
      form.remember = true
    }
  } catch {
    // ignore parse errors
  }
}

// 已登录用户自动跳转
function checkAlreadyLoggedIn() {
  if (userStore.isLoggedIn) {
    router.replace(userStore.homePath)
  }
}

onMounted(() => {
  checkAlreadyLoggedIn()
  loadRemembered()
  window.addEventListener("online", handleOnline)
  window.addEventListener("offline", handleOffline)
})

onUnmounted(() => {
  window.removeEventListener("online", handleOnline)
  window.removeEventListener("offline", handleOffline)
})

function getErrorMessage(err: unknown): string {
  if (err instanceof Error) {
    // 尝试从错误消息中提取 code
    const msg = err.message
    for (const [codeStr, label] of Object.entries(ERROR_MESSAGES)) {
      if (msg.includes(codeStr)) return label
    }
    return msg
  }
  return "登录失败，请重试"
}

async function handleLogin() {
  if (loading.value) return

  const valid = await formRef.value?.validate().catch(() => false)
  if (!valid) return

  loading.value = true
  try {
    await userStore.login({
      username: form.username,
      password: form.password,
    })

    saveRemember()

    ElMessage.success("登录成功")
    setTimeout(() => {
      const redirect = (route.query.redirect as string) || userStore.homePath
      router.push(redirect)
    }, 1500)
  } catch (err: unknown) {
    ElMessage.error(getErrorMessage(err))
    form.password = ""
  } finally {
    loading.value = false
  }
}

function saveRemember() {
  if (form.remember) {
    const data: RememberData = {
      username: form.username,
      password: window.btoa(form.password),
      checked: true,
    }
    localStorage.setItem(REMEMBER_KEY, JSON.stringify(data))
  } else {
    localStorage.removeItem(REMEMBER_KEY)
  }
}

function onEnterKey() {
  handleLogin()
}
</script>

<template>
  <div class="login-page">
    <!-- 离线提示 -->
    <transition name="fade">
      <div v-if="isOffline" class="login-page__offline-banner">
        网络连接已断开，请检查网络
      </div>
    </transition>

    <div class="login-page__card">
      <!-- 品牌区 -->
      <div class="login-page__brand">
        <div class="login-page__logo">
          <BookOpen :size="48" />
        </div>
        <h1 class="login-page__title">B1 实训平台</h1>
        <p class="login-page__subtitle">软件实训教学检查评价与报表系统</p>
      </div>

      <!-- 表单区 -->
      <el-form
        ref="formRef"
        :model="form"
        :rules="rules"
        class="login-page__form"
        @keyup.enter="onEnterKey"
      >
        <el-form-item prop="username">
          <BaseInput
            v-model="form.username"
            placeholder="用户名"
            prefix-icon="User"
            :disabled="loading"
            size="large"
          />
        </el-form-item>

        <el-form-item prop="password">
          <BaseInput
            v-model="form.password"
            type="password"
            placeholder="密码"
            prefix-icon="Lock"
            show-password
            :disabled="loading"
            size="large"
          />
        </el-form-item>

        <div class="login-page__options">
          <el-checkbox v-model="form.remember" :disabled="loading">
            记住密码
          </el-checkbox>
          <el-tooltip content="请联系管理员重置密码" placement="top">
            <span class="login-page__forgot">忘记密码？</span>
          </el-tooltip>
        </div>

        <BaseButton
          type="primary"
          size="large"
          :loading="loading"
          :disabled="loading"
          block
          @click="handleLogin"
        >
          {{ loading ? "正在登录..." : "登 录" }}
        </BaseButton>
      </el-form>

      <!-- 底部 -->
      <div class="login-page__footer">
        <span>第十五届中国软件杯 · B1 赛题</span>
        <span>© 2026 B1 Platform</span>
      </div>
    </div>
  </div>
</template>

<style scoped>
.login-page {
  display: flex;
  align-items: center;
  justify-content: center;
  min-height: 100vh;
  background: var(--color-bg, #f8fafc);
  padding: var(--spacing-lg, 24px);
  position: relative;
}

.login-page__offline-banner {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  z-index: 1000;
  padding: var(--spacing-sm, 8px) var(--spacing-lg, 24px);
  background: var(--color-warning, #f59e0b);
  color: #fff;
  font-size: var(--font-size-sm, 14px);
  text-align: center;
}

.login-page__card {
  width: 420px;
  background: var(--color-card, #fff);
  border-radius: var(--radius-xl, 16px);
  box-shadow: var(--shadow-xl, 0 20px 25px -5px rgba(0, 0, 0, 0.1));
  padding: var(--spacing-2xl, 48px);
}

.login-page__brand {
  text-align: center;
  margin-bottom: var(--spacing-xl, 32px);
}

.login-page__logo {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 72px;
  height: 72px;
  border-radius: var(--radius-lg, 12px);
  background: var(--color-primary, #3b82f6);
  color: #fff;
  margin-bottom: var(--spacing-md, 16px);
}

.login-page__title {
  font-size: var(--font-size-xl, 20px);
  font-weight: 700;
  color: var(--color-text-primary, #1e293b);
  margin: 0 0 var(--spacing-xs, 4px) 0;
  letter-spacing: 0.5px;
}

.login-page__subtitle {
  font-size: var(--font-size-sm, 14px);
  color: var(--color-text-secondary, #64748b);
  margin: 0;
}

.login-page__form {
  margin-top: var(--spacing-lg, 24px);
}

.login-page__options {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: var(--spacing-lg, 24px);
}

.login-page__forgot {
  font-size: var(--font-size-sm, 14px);
  color: var(--color-primary, #3b82f6);
  cursor: pointer;
}

.login-page__forgot:hover {
  color: var(--color-primary-hover, #2563eb);
}

.login-page__footer {
  margin-top: var(--spacing-xl, 32px);
  text-align: center;
  display: flex;
  flex-direction: column;
  gap: var(--spacing-xs, 4px);
  font-size: var(--font-size-xs, 12px);
  color: var(--color-text-placeholder, #94a3b8);
}

.fade-enter-active,
.fade-leave-active {
  transition: opacity var(--transition-fast, 150ms ease);
}

.fade-enter-from,
.fade-leave-to {
  opacity: 0;
}
</style>
