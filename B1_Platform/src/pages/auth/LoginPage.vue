<script setup lang="ts">
import { ref, reactive, computed, onMounted, onUnmounted } from "vue"
import { useRouter, useRoute } from "vue-router"
import { useUserStore } from "@/stores/useUserStore"
import { ElMessage } from "element-plus"
import {
  GraduationCap, Zap, ShieldCheck, TrendingUp,
  UserCheck, BookOpenCheck, Settings,
  ChevronLeft, Lock,
  LogIn, School, QrCode,
  Smartphone, MessageSquare, CheckCircle, Check,
} from "lucide-vue-next"
import type { UserRole } from "@/constants/role"
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

// ---- Step state ----
const currentStep = ref(1)
const selectedRole = ref<string | null>(null)

const roleNames: Record<string, string> = {
  student: "实训学生",
  teacher: "授课教师",
  admin: "系统管理员",
}

const roleColors: Record<string, string> = {
  student: "role--student",
  teacher: "role--teacher",
  admin: "role--admin",
}

// ---- Form state ----
const formRef = ref()
const loading = ref(false)
const isOffline = ref(!window.navigator.onLine)
const showPassword = ref(false)
const loginSuccess = ref(false)

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

// ---- Forgot password state ----
const phoneInput = ref("")
const smsInput = ref("")
const newPwdInput = ref("")
const smsCountdown = ref(0)
let smsTimer: ReturnType<typeof setInterval> | null = null

const ERROR_MESSAGES: Record<number, string> = {
  2004: "用户名或密码错误",
  2005: "账号已被锁定，请联系管理员",
  2006: "账号已被禁用，请联系管理员",
}

// ---- Computed ----
const stepDots = computed(() => [1, 2, 3].map((n) => n <= currentStep.value))
const stepLabel = computed(() => {
  const labels: Record<number, string> = { 1: "选择角色", 2: "账号登录", 3: "找回密码" }
  return labels[currentStep.value]
})

const passwordStrength = computed(() => {
  const pwd = newPwdInput.value
  let strength = 0
  if (pwd.length >= 8) strength++
  if (/[a-z]/.test(pwd) && /[A-Z]/.test(pwd)) strength++
  if (/\d/.test(pwd)) strength++
  if (/[^a-zA-Z0-9]/.test(pwd)) strength++
  return strength
})

const strengthColor = computed(() => {
  const colors = ["#ef4444", "#f97316", "#f59e0b", "#22c55e"]
  return passwordStrength.value > 0 ? colors[passwordStrength.value - 1] : "#e2e8f0"
})

const strengthLabel = computed(() => {
  const labels = ["弱", "中等", "较强", "强"]
  return passwordStrength.value > 0 ? `密码强度：${labels[passwordStrength.value - 1]}` : "密码强度"
})

// ---- Role selection ----
function selectRole(role: string) {
  selectedRole.value = role
}

function goStep(step: number) {
  currentStep.value = step
}

// ---- Network ----
function handleOnline() { isOffline.value = false }
function handleOffline() { isOffline.value = true }

// ---- Remember ----
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
  } catch { /* ignore */ }
}

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
  if (smsTimer) clearInterval(smsTimer)
})

// ---- Login ----
function getErrorMessage(err: unknown): string {
  if (err instanceof Error) {
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

    // Set active role from login page selection
    if (selectedRole.value) {
      userStore.setActiveRole(selectedRole.value as UserRole)
    }

    saveRemember()
    loginSuccess.value = true

    setTimeout(() => {
      const redirect = (route.query.redirect as string) || userStore.homePath
      router.push(redirect)
    }, 2000)
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

// ---- SMS ----
function sendSms() {
  const phone = phoneInput.value.trim()
  if (!phone || phone.length !== 11) {
    ElMessage.warning("请输入正确的手机号")
    return
  }
  smsCountdown.value = 60
  smsTimer = setInterval(() => {
    smsCountdown.value--
    if (smsCountdown.value <= 0) {
      if (smsTimer) clearInterval(smsTimer)
    }
  }, 1000)
  ElMessage.success("验证码已发送")
}

// ---- Reset password ----
function resetPassword() {
  const sms = smsInput.value.trim()
  const pwd = newPwdInput.value.trim()
  if (!sms || !pwd) {
    ElMessage.warning("请填写完整信息")
    return
  }
  ElMessage.success("密码重置成功！请使用新密码登录。")
  goStep(2)
}
</script>

<template>
  <div class="login-bg">
    <!-- Decorative circles -->
    <div class="deco-circle deco-circle--1" />
    <div class="deco-circle deco-circle--2" />
    <div class="deco-circle deco-circle--3" />
    <div class="deco-circle deco-circle--4" />

    <!-- Offline banner -->
    <transition name="fade">
      <div v-if="isOffline" class="offline-banner">
        网络连接已断开，请检查网络
      </div>
    </transition>

    <div class="login-wrapper">
      <!-- ====== Left brand area ====== -->
      <div class="brand-area">
        <div class="brand-header">
          <div class="brand-logo">
            <GraduationCap :size="24" />
          </div>
          <div>
            <div class="brand-name">软件实训教学平台</div>
            <div class="brand-sub">Training Management System</div>
          </div>
        </div>
        <h1 class="brand-title">
          智能化实训<br>教学全流程管理
        </h1>
        <p class="brand-desc">
          AI 辅助评价 · 进度实时监控 · 能力成长跟踪<br>
          覆盖课程管理、成果评价与教学分析的完整闭环
        </p>
        <div class="brand-features">
          <div class="brand-feature">
            <div class="feature-icon"><Zap :size="14" /></div>
            <span>AI 智能分析，批改效率提升 60%</span>
          </div>
          <div class="brand-feature">
            <div class="feature-icon"><ShieldCheck :size="14" /></div>
            <span>标准化评价体系，消除评分偏差</span>
          </div>
          <div class="brand-feature">
            <div class="feature-icon"><TrendingUp :size="14" /></div>
            <span>能力成长可视化，跨学期数据追踪</span>
          </div>
        </div>
        <div class="brand-stats">
          <div class="brand-stat">
            <div class="brand-stat-val">200+</div>
            <div class="brand-stat-label">合作院校</div>
          </div>
          <div class="brand-stat">
            <div class="brand-stat-val">50万+</div>
            <div class="brand-stat-label">在校学生</div>
          </div>
          <div class="brand-stat">
            <div class="brand-stat-val">98%</div>
            <div class="brand-stat-label">教师满意度</div>
          </div>
        </div>
      </div>

      <!-- ====== Right card ====== -->
      <div class="login-card-wrap">
        <div class="login-card">
          <!-- Step indicator -->
          <div class="step-dots">
            <div
              v-for="dot in [1, 2, 3]"
              :key="dot"
              class="step-dot"
              :class="{ 'step-dot--active': dot <= currentStep }"
            />
            <span class="step-label">{{ stepLabel }}</span>
          </div>

          <!-- ====== Step 1: Select role ====== -->
          <div v-show="currentStep === 1" class="step-panel">
            <h2 class="step-title">欢迎回来</h2>
            <p class="step-desc">请选择您的身份继续登录</p>

            <div class="role-grid">
              <div
                v-for="role in ['student', 'teacher', 'admin']"
                :key="role"
                :class="['role-card', roleColors[role], { 'role-card--selected': selectedRole === role }]"
                @click="selectRole(role)"
              >
                <div class="role-icon-wrap">
                  <UserCheck v-if="role === 'student'" :size="24" />
                  <BookOpenCheck v-else-if="role === 'teacher'" :size="24" />
                  <Settings v-else :size="24" />
                </div>
                <div class="role-name">{{ roleNames[role] }}</div>
                <div class="role-desc">
                  {{ role === 'student' ? '查看任务·提交成果' : role === 'teacher' ? '管理实训·审核评分' : '用户管理·系统运维' }}
                </div>
              </div>
            </div>

            <BaseButton
              type="primary"
              size="large"
              block
              :disabled="!selectedRole"
              @click="goStep(2)"
            >
              下一步
            </BaseButton>

            <div class="contact-hint">
              首次使用？<span class="contact-link">联系管理员开通账号</span>
            </div>
          </div>

          <!-- ====== Step 2: Credentials ====== -->
          <div v-show="currentStep === 2" class="step-panel">
            <div class="step-back">
              <button class="back-btn" @click="goStep(1)">
                <ChevronLeft :size="16" />
              </button>
              <h2 class="step-title">账号登录</h2>
            </div>

            <div v-if="selectedRole" class="role-hint">
              <span :class="['role-badge', roleColors[selectedRole]]">
                {{ roleNames[selectedRole] }}
              </span>
              <span>身份已选择</span>
            </div>

            <el-form
              ref="formRef"
              :model="form"
              :rules="rules"
              @keyup.enter="handleLogin"
            >
              <el-form-item prop="username">
                <BaseInput
                  v-model="form.username"
                  placeholder="请输入工号或学号"
                  prefix-icon="User"
                  size="large"
                  :disabled="loading"
                />
              </el-form-item>

              <el-form-item prop="password">
                <BaseInput
                  v-model="form.password"
                  placeholder="请输入登录密码"
                  prefix-icon="Lock"
                  :type="showPassword ? 'text' : 'password'"
                  :show-password="true"
                  size="large"
                  :disabled="loading"
                />
              </el-form-item>

              <div class="form-options">
                <el-checkbox v-model="form.remember" :disabled="loading">
                  7天内免登录
                </el-checkbox>
                <span class="forgot-link" @click="goStep(3)">忘记密码？</span>
              </div>

              <BaseButton
                type="primary"
                size="large"
                block
                :loading="loading"
                :disabled="loading"
                @click="handleLogin"
              >
                <LogIn :size="16" />
                <span style="margin-left: 6px">{{ loading ? "登录中..." : "登录" }}</span>
              </BaseButton>
            </el-form>

            <!-- Other login methods -->
            <div class="divider">
              <span class="divider-text">其他方式</span>
            </div>
            <div class="other-methods">
              <button class="other-btn">
                <School :size="16" />
                <span>校园统一认证</span>
              </button>
              <button class="other-btn">
                <QrCode :size="16" />
                <span>扫码登录</span>
              </button>
            </div>
          </div>

          <!-- ====== Step 3: Forgot password ====== -->
          <div v-show="currentStep === 3" class="step-panel">
            <div class="step-back">
              <button class="back-btn" @click="goStep(2)">
                <ChevronLeft :size="16" />
              </button>
              <h2 class="step-title">找回密码</h2>
            </div>
            <p class="step-desc step-desc--indent">通过绑定手机号验证身份，重置登录密码</p>

            <div class="form-group">
              <label class="form-label">绑定手机号</label>
              <div class="input-wrap">
                <Smartphone :size="16" class="input-icon" />
                <input
                  v-model="phoneInput"
                  class="input-field"
                  type="tel"
                  placeholder="请输入绑定的手机号"
                  maxlength="11"
                >
              </div>
            </div>

            <div class="form-group">
              <label class="form-label">短信验证码</label>
              <div class="sms-row">
                <div class="input-wrap sms-input-wrap">
                  <MessageSquare :size="16" class="input-icon" />
                  <input
                    v-model="smsInput"
                    class="input-field"
                    type="text"
                    placeholder="6位验证码"
                    maxlength="6"
                  >
                </div>
                <button
                  class="sms-btn"
                  :disabled="smsCountdown > 0"
                  @click="sendSms"
                >
                  {{ smsCountdown > 0 ? `${smsCountdown}s 后重发` : "发送验证码" }}
                </button>
              </div>
            </div>

            <div class="form-group">
              <label class="form-label">设置新密码</label>
              <div class="input-wrap">
                <Lock :size="16" class="input-icon" />
                <input
                  v-model="newPwdInput"
                  class="input-field"
                  type="password"
                  placeholder="8-20位，含字母和数字"
                >
              </div>
            </div>

            <!-- Password strength -->
            <div class="strength-bars">
              <div v-for="i in 4" :key="i" class="strength-bar" :style="{
                background: i <= passwordStrength ? strengthColor : '#e2e8f0'
              }" />
            </div>
            <div class="strength-label" :style="{ color: passwordStrength > 0 ? strengthColor : '#94a3b8' }">
              {{ strengthLabel }}
            </div>

            <BaseButton
              type="primary"
              size="large"
              block
              @click="resetPassword"
              style="margin-top: 16px"
            >
              <CheckCircle :size="16" />
              <span style="margin-left: 6px">重置密码</span>
            </BaseButton>
          </div>
        </div>

        <!-- Success overlay -->
        <transition name="fade">
          <div v-if="loginSuccess" class="success-overlay">
            <div class="success-icon">
              <Check :size="32" />
            </div>
            <div class="success-title">登录成功</div>
            <div class="success-msg">
              正在跳转至{{ selectedRole ? roleNames[selectedRole] : '' }}工作台...
            </div>
            <div class="success-bar">
              <div class="success-bar-fill" />
            </div>
          </div>
        </transition>

        <!-- Footer -->
        <div class="login-footer">
          © 2026 软件实训教学平台 · v2.0.0 · 技术支持：教学信息中心
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
/* ====== Background ====== */
.login-bg {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 16px;
  position: relative;
  overflow: hidden;
  background: linear-gradient(135deg, oklch(0.25 0.08 260) 0%, oklch(0.18 0.06 280) 50%, oklch(0.22 0.10 300) 100%);
}

/* Decorative circles */
.deco-circle {
  position: absolute;
  border-radius: 50%;
  background: rgba(255, 255, 255, 0.05);
  border: 1px solid rgba(255, 255, 255, 0.08);
}
.deco-circle--1 { width: 384px; height: 384px; top: -96px; left: -96px; }
.deco-circle--2 { width: 256px; height: 256px; top: 33%; right: -64px; }
.deco-circle--3 { width: 192px; height: 192px; bottom: 64px; left: 25%; }
.deco-circle--4 { width: 320px; height: 320px; bottom: -80px; right: -80px; }

.offline-banner {
  position: fixed;
  top: 0; left: 0; right: 0;
  z-index: 1000;
  padding: 8px 24px;
  background: var(--color-warning, #f59e0b);
  color: #fff;
  font-size: 13px;
  text-align: center;
}

/* ====== Layout ====== */
.login-wrapper {
  display: flex;
  width: 100%;
  max-width: 1050px;
  gap: 48px;
  align-items: center;
  position: relative;
  z-index: 1;
}

/* ====== Brand area ====== */
.brand-area {
  flex: 1;
  max-width: 520px;
  color: #fff;
  display: none;
}

@media (min-width: 1024px) {
  .brand-area { display: block; }
}

.brand-header {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 40px;
}

.brand-logo {
  width: 48px;
  height: 48px;
  background: rgba(255, 255, 255, 0.15);
  border-radius: 16px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #fff;
}

.brand-name {
  font-weight: 700;
  font-size: 20px;
  line-height: 1.3;
}

.brand-sub {
  font-size: 13px;
  color: rgba(255, 255, 255, 0.55);
  margin-top: 1px;
}

.brand-title {
  font-size: 36px;
  font-weight: 800;
  margin-bottom: 16px;
  line-height: 1.25;
}

.brand-desc {
  color: rgba(255, 255, 255, 0.65);
  font-size: 15px;
  margin-bottom: 40px;
  line-height: 1.7;
}

.brand-features {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.brand-feature {
  display: flex;
  align-items: center;
  gap: 12px;
  color: rgba(255, 255, 255, 0.75);
  font-size: 13px;
}

.feature-icon {
  width: 28px;
  height: 28px;
  border-radius: 8px;
  background: rgba(255, 255, 255, 0.1);
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
  color: #fff;
}

.brand-stats {
  display: flex;
  gap: 32px;
  margin-top: 48px;
  padding-top: 32px;
  border-top: 1px solid rgba(255, 255, 255, 0.12);
}

.brand-stat-val {
  font-size: 24px;
  font-weight: 700;
}

.brand-stat-label {
  font-size: 11px;
  color: rgba(255, 255, 255, 0.45);
  margin-top: 2px;
}

/* ====== Card wrap ====== */
.login-card-wrap {
  width: 100%;
  max-width: 420px;
  position: relative;
}

.login-card {
  background: rgba(255, 255, 255, 0.97);
  backdrop-filter: blur(20px);
  border: 1px solid rgba(255, 255, 255, 0.3);
  border-radius: 16px;
  box-shadow: 0 24px 64px rgba(0, 0, 0, 0.2);
  padding: 28px 32px 32px;
  position: relative;
}

.login-footer {
  text-align: center;
  margin-top: 20px;
  font-size: 11px;
  color: rgba(255, 255, 255, 0.35);
}

/* ====== Step dots ====== */
.step-dots {
  display: flex;
  align-items: center;
  gap: 6px;
  margin-bottom: 24px;
}

.step-dot {
  width: 8px;
  height: 8px;
  border-radius: 50%;
  background: #e2e8f0;
  transition: all 300ms;
}

.step-dot--active {
  background: var(--color-primary, #3b82f6);
}

.step-label {
  font-size: 11px;
  color: #94a3b8;
  margin-left: 4px;
}

/* ====== Step panels ====== */
.step-panel {
  animation: fadeSlideIn 250ms ease;
}

@keyframes fadeSlideIn {
  from { opacity: 0; transform: translateY(8px); }
  to { opacity: 1; transform: translateY(0); }
}

.step-back {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 4px;
}

.back-btn {
  width: 28px;
  height: 28px;
  border-radius: 8px;
  border: none;
  background: transparent;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #64748b;
  transition: background 150ms;
}
.back-btn:hover { background: #f1f5f9; }

.step-title {
  font-size: 24px;
  font-weight: 700;
  color: #1e293b;
}

.step-desc {
  font-size: 13px;
  color: #64748b;
  margin-bottom: 24px;
}

.step-desc--indent {
  margin-left: 36px;
}

/* ====== Role cards ====== */
.role-grid {
  display: grid;
  grid-template-columns: 1fr 1fr 1fr;
  gap: 10px;
  margin-bottom: 24px;
}

.role-card {
  border: 2px solid #e2e8f0;
  border-radius: 12px;
  padding: 16px 12px;
  text-align: center;
  cursor: pointer;
  transition: all 200ms;
}

.role-card:hover {
  border-color: var(--color-primary, #3b82f6);
  background: var(--color-primary-light, #eff6ff);
  transform: translateY(-2px);
}

.role-icon-wrap {
  width: 48px;
  height: 48px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  margin: 0 auto 10px;
  transition: all 200ms;
}

.role--student .role-icon-wrap { background: #dcfce7; color: #16a34a; }
.role--teacher .role-icon-wrap { background: #eef2ff; color: #4f46e5; }
.role--admin   .role-icon-wrap { background: #f1f5f9; color: #475569; }

.role-name {
  font-weight: 600;
  font-size: 13px;
  color: #334155;
}

.role-desc {
  font-size: 11px;
  color: #94a3b8;
  margin-top: 2px;
}

/* Selected state */
.role-card--selected {
  border-color: var(--color-primary, #3b82f6);
  background: var(--color-primary-light, #eff6ff);
  box-shadow: 0 0 0 3px rgba(59, 130, 246, 0.15);
}
.role-card--selected .role-icon-wrap { background: var(--color-primary, #3b82f6); color: #fff; }

.contact-hint {
  text-align: center;
  margin-top: 16px;
  font-size: 12px;
  color: #94a3b8;
}
.contact-link {
  color: var(--color-primary, #3b82f6);
  cursor: pointer;
}
.contact-link:hover { text-decoration: underline; }

/* Role hint in step 2 */
.role-hint {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 13px;
  color: #64748b;
  margin-bottom: 20px;
  margin-left: 36px;
}

.role-badge {
  padding: 2px 8px;
  border-radius: 100px;
  font-size: 11px;
  font-weight: 500;
}

.role--student.role-badge { background: #dcfce7; color: #166534; }
.role--teacher.role-badge { background: #eef2ff; color: #4f46e5; }
.role--admin.role-badge   { background: #f1f5f9; color: #475569; }

/* ====== Input fields ====== */
.input-wrap {
  position: relative;
  width: 100%;
}

.input-icon {
  position: absolute;
  left: 12px;
  top: 50%;
  transform: translateY(-50%);
  color: #94a3b8;
  pointer-events: none;
  z-index: 1;
}

.input-field {
  width: 100%;
  height: 44px;
  padding: 0 12px 0 36px;
  font-size: 14px;
  border: 1px solid #e2e8f0;
  border-radius: 12px;
  background: #fff;
  color: #1e293b;
  outline: none;
  transition: border-color 150ms, box-shadow 150ms;
  box-sizing: border-box;
}

.input-field:focus {
  border-color: var(--color-primary, #3b82f6);
  box-shadow: 0 0 0 3px rgba(59, 130, 246, 0.1);
}

.input-field::placeholder { color: #94a3b8; }
.input-field:disabled { background: #f8fafc; cursor: not-allowed; }

.pwd-toggle {
  position: absolute;
  right: 12px;
  top: 50%;
  transform: translateY(-50%);
  background: none;
  border: none;
  cursor: pointer;
  color: #94a3b8;
  padding: 0;
  z-index: 1;
}
.pwd-toggle:hover { color: #64748b; }

/* ====== Form options ====== */
.form-options {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 24px;
}

.forgot-link {
  font-size: 13px;
  color: var(--color-primary, #3b82f6);
  cursor: pointer;
}
.forgot-link:hover { text-decoration: underline; }

/* ====== Divider ====== */
.divider {
  display: flex;
  align-items: center;
  gap: 12px;
  margin: 20px 0;
}

.divider::before,
.divider::after {
  content: "";
  flex: 1;
  height: 1px;
  background: #f1f5f9;
}

.divider-text {
  font-size: 12px;
  color: #94a3b8;
}

/* ====== Other methods ====== */
.other-methods {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 8px;
}

.other-btn {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  padding: 10px;
  border: 1px solid #e2e8f0;
  border-radius: 12px;
  background: transparent;
  cursor: pointer;
  font-size: 13px;
  color: #64748b;
  transition: background 150ms;
}
.other-btn:hover { background: #f8fafc; }

/* ====== SMS ====== */
.sms-row {
  display: flex;
  gap: 8px;
}
.sms-input-wrap { flex: 1; }
.sms-btn {
  flex-shrink: 0;
  padding: 0 16px;
  height: 44px;
  border-radius: 12px;
  font-size: 13px;
  font-weight: 500;
  border: 1px solid #c7d2fe;
  background: transparent;
  color: var(--color-primary, #3b82f6);
  cursor: pointer;
  white-space: nowrap;
  transition: background 150ms;
}
.sms-btn:hover:not(:disabled) { background: var(--color-primary-light, #eff6ff); }
.sms-btn:disabled { opacity: 0.5; cursor: not-allowed; }

/* ====== Form group ====== */
.form-group {
  margin-bottom: 16px;
}
.form-label {
  display: block;
  font-size: 13px;
  font-weight: 500;
  color: #475569;
  margin-bottom: 6px;
}

/* ====== Password strength ====== */
.strength-bars {
  display: flex;
  gap: 4px;
  margin-bottom: 4px;
}
.strength-bar {
  height: 4px;
  border-radius: 2px;
  flex: 1;
  background: #e2e8f0;
  transition: background 300ms;
}
.strength-label {
  font-size: 12px;
  color: #94a3b8;
  margin-bottom: 8px;
}

/* ====== Success overlay ====== */
.success-overlay {
  position: absolute;
  inset: 0;
  background: rgba(255, 255, 255, 0.95);
  backdrop-filter: blur(8px);
  border-radius: 16px;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  z-index: 10;
}

.success-icon {
  width: 64px;
  height: 64px;
  border-radius: 50%;
  background: #dcfce7;
  color: #16a34a;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-bottom: 16px;
}

.success-title {
  font-size: 20px;
  font-weight: 700;
  color: #1e293b;
  margin-bottom: 4px;
}

.success-msg {
  font-size: 13px;
  color: #64748b;
  margin-bottom: 16px;
}

.success-bar {
  width: 192px;
  height: 6px;
  border-radius: 3px;
  background: #f1f5f9;
  overflow: hidden;
}

.success-bar-fill {
  height: 100%;
  border-radius: 3px;
  background: var(--color-primary, #3b82f6);
  animation: progressFill 2s linear forwards;
}

@keyframes progressFill {
  from { width: 0; }
  to { width: 100%; }
}

/* ====== Transitions ====== */
.fade-enter-active,
.fade-leave-active {
  transition: opacity 150ms ease;
}
.fade-enter-from,
.fade-leave-to {
  opacity: 0;
}
</style>
