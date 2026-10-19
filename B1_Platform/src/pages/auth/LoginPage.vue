<script setup lang="ts">
import { ref, reactive, computed, onMounted, onUnmounted } from "vue"
import { useRouter, useRoute } from "vue-router"
import { useUserStore } from "@/stores/useUserStore"
import { ElMessage } from "element-plus"
import {
  User,
  Key,
  Eye,
  EyeOff,
  ChevronLeft,
  Smartphone,
  MessageSquare,
  CheckCircle,
  Check,
  Lock,
} from "lucide-vue-next"
import { useBreakpoint } from "@/composables/useBreakpoint"

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()
const { isMdDown } = useBreakpoint()

const REMEMBER_KEY = "b1_remember_login"

interface RememberData {
  username: string
  password: string
  checked: boolean
}

const currentStep = ref(2)
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
    { required: true, message: "请输入工号或学号", trigger: "blur" },
    { min: 3, message: "账号至少3个字符", trigger: "blur" },
  ],
  password: [
    { required: true, message: "请输入密码", trigger: "blur" },
  ],
}

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
  const colors = ["#FFB4AB", "#FFD591", "#FFF1B8", "#B7EB8F"]
  return passwordStrength.value > 0 ? colors[passwordStrength.value - 1] : "rgba(255,255,255,0.25)"
})

const strengthLabel = computed(() => {
  const labels = ["弱", "中等", "较强", "强"]
  return passwordStrength.value > 0 ? `密码强度：${labels[passwordStrength.value - 1]}` : "密码强度"
})

function handleOnline() { isOffline.value = false }
function handleOffline() { isOffline.value = true }

function goStep(step: number) {
  currentStep.value = step
}

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

function sendSms() {
  const phone = phoneInput.value.trim()
  if (!phone || phone.length !== 11) {
    ElMessage.warning("请输入正确的手机号")
    return
  }
  smsCountdown.value = 60
  smsTimer = setInterval(() => {
    smsCountdown.value--
    if (smsCountdown.value <= 0 && smsTimer) {
      clearInterval(smsTimer)
    }
  }, 1000)
  ElMessage.success("验证码已发送")
}

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
  <div class="login-page">
    <transition name="fade">
      <div v-if="isOffline" class="offline-banner">
        网络连接已断开，请检查网络
      </div>
    </transition>

    <main class="login-page__main" :class="{ 'login-page__main--compact': isMdDown }">
      <div class="login-shell" :class="{ 'login-shell--form-only': isMdDown }">
        <section v-if="!isMdDown" class="login-shell__visual" aria-hidden="true">
          <img
            class="login-shell__image"
            src="/login-left.jpg"
            alt=""
          >
        </section>

        <section class="login-shell__form">
          <div class="login-card">
            <div class="login-card__brand">
              <img class="login-card__logo" src="/logo.svg" alt="衡迹" />
            </div>

            <div v-show="currentStep === 2" class="step-panel">
              <h2 class="login-card__title">登录</h2>
              <p class="login-card__desc">
                输入工号或学号，系统将自动进入学生、教师或管理员工作台。
              </p>

            <el-form
              ref="formRef"
              :model="form"
              :rules="rules"
              class="login-form"
              label-position="top"
              @keyup.enter="handleLogin"
            >
              <el-form-item label="工号或学号" prop="username" class="login-field">
                <div class="login-field__wrap">
                  <User :size="18" class="login-field__icon" />
                  <input
                    v-model="form.username"
                    class="login-field__input"
                    type="text"
                    placeholder="请输入工号或学号"
                    autocomplete="username"
                    :disabled="loading"
                  >
                </div>
              </el-form-item>

              <el-form-item label="密码" prop="password" class="login-field">
                <div class="login-field__wrap">
                  <Key :size="18" class="login-field__icon" />
                  <input
                    v-model="form.password"
                    class="login-field__input login-field__input--password"
                    :type="showPassword ? 'text' : 'password'"
                    placeholder="请输入登录密码"
                    autocomplete="current-password"
                    :disabled="loading"
                  >
                  <button
                    type="button"
                    class="login-field__toggle"
                    :aria-label="showPassword ? '隐藏密码' : '显示密码'"
                    @click="showPassword = !showPassword"
                  >
                    <EyeOff v-if="showPassword" :size="18" />
                    <Eye v-else :size="18" />
                  </button>
                </div>
              </el-form-item>

              <div class="form-options">
                <el-checkbox v-model="form.remember" :disabled="loading">
                  7天内免登录
                </el-checkbox>
                <button type="button" class="link-btn" @click="goStep(3)">
                  忘记密码？
                </button>
              </div>

              <button
                type="button"
                class="login-submit"
                :disabled="loading"
                @click="handleLogin"
              >
                {{ loading ? "登录中..." : "登录" }}
              </button>
            </el-form>

            <p class="login-card__help">
              需要帮助？请联系教学信息中心
            </p>
          </div>

          <div v-show="currentStep === 3" class="step-panel">
            <div class="step-back">
              <button type="button" class="back-btn" @click="goStep(2)">
                <ChevronLeft :size="18" />
              </button>
              <h2 class="login-card__title login-card__title--inline">找回密码</h2>
            </div>
            <p class="login-card__desc">
              通过绑定手机号验证身份，重置登录密码
            </p>

            <div class="form-group">
              <label class="login-field__label">绑定手机号</label>
              <div class="login-field__wrap">
                <Smartphone :size="18" class="login-field__icon" />
                <input
                  v-model="phoneInput"
                  class="login-field__input"
                  type="tel"
                  placeholder="请输入绑定的手机号"
                  maxlength="11"
                >
              </div>
            </div>

            <div class="form-group">
              <label class="login-field__label">短信验证码</label>
              <div class="sms-row">
                <div class="login-field__wrap sms-input-wrap">
                  <MessageSquare :size="18" class="login-field__icon" />
                  <input
                    v-model="smsInput"
                    class="login-field__input"
                    type="text"
                    placeholder="6位验证码"
                    maxlength="6"
                  >
                </div>
                <button
                  type="button"
                  class="sms-btn"
                  :disabled="smsCountdown > 0"
                  @click="sendSms"
                >
                  {{ smsCountdown > 0 ? `${smsCountdown}s 后重发` : "发送验证码" }}
                </button>
              </div>
            </div>

            <div class="form-group">
              <label class="login-field__label">设置新密码</label>
              <div class="login-field__wrap">
                <Lock :size="18" class="login-field__icon" />
                <input
                  v-model="newPwdInput"
                  class="login-field__input"
                  type="password"
                  placeholder="8-20位，含字母和数字"
                >
              </div>
            </div>

            <div class="strength-bars">
              <div
                v-for="i in 4"
                :key="i"
                class="strength-bar"
                :style="{ background: i <= passwordStrength ? strengthColor : 'rgba(255,255,255,0.25)' }"
              />
            </div>
            <div
              class="strength-label"
              :style="{ color: passwordStrength > 0 ? strengthColor : 'rgba(255,255,255,0.65)' }"
            >
              {{ strengthLabel }}
            </div>

            <button type="button" class="login-submit login-submit--spaced" @click="resetPassword">
              <CheckCircle :size="18" />
              <span>重置密码</span>
            </button>
          </div>

          <transition name="fade">
            <div v-if="loginSuccess" class="success-overlay">
              <div class="success-icon">
                <Check :size="32" />
              </div>
              <div class="success-title">登录成功</div>
              <div class="success-msg">正在跳转至工作台...</div>
              <div class="success-bar">
                <div class="success-bar-fill" />
              </div>
            </div>
          </transition>
          </div>
        </section>
      </div>
    </main>
  </div>
</template>

<style scoped>
.login-page {
  --login-blue: #2748c4;
  --login-input-bg: rgba(255, 255, 255, 0.14);
  --login-input-bg-focus: rgba(255, 255, 255, 0.2);
  min-height: 100vh;
  background: #f5f7fa;
  display: flex;
  flex-direction: column;
  position: relative;
}

.offline-banner {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  z-index: 1000;
  padding: 8px 24px;
  background: var(--color-warning, #FAAD14);
  color: #fff;
  font-size: 13px;
  text-align: center;
}

.login-page__main {
  flex: 1;
  width: 100%;
  max-width: 980px;
  margin: 0 auto;
  padding: 24px 20px 40px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.login-page__main--compact {
  max-width: 440px;
  padding: 20px 16px 28px;
  align-items: stretch;
}

.login-shell {
  width: 100%;
  display: grid;
  grid-template-columns: 1fr;
  border-radius: 28px;
  overflow: hidden;
  box-shadow: 0 24px 56px rgba(39, 72, 196, 0.14);
  background: #ffffff;
}

.login-shell--form-only {
  background: transparent;
  box-shadow: none;
  border-radius: 0;
  overflow: visible;
}

@media (min-width: 1024px) {
  .login-page__main {
    padding: 48px 24px 64px;
    max-width: 1040px;
  }

  .login-shell {
    grid-template-columns: 1.08fr 440px;
    min-height: 620px;
  }
}

.login-shell__visual {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 12px;
  padding: 24px 8px 20px 28px;
  background: #ffffff;
  min-height: 620px;
}

.login-shell__image {
  flex: 1;
  width: 108%;
  height: auto;
  max-height: 580px;
  margin-left: -4%;
  object-fit: contain;
  object-position: center;
  user-select: none;
  pointer-events: none;
}

.login-shell__form {
  position: relative;
  z-index: 2;
  display: flex;
  flex-direction: column;
}

@media (min-width: 1024px) {
  .login-shell__form {
    margin-left: -52px;
    padding-left: 0;
  }
}

.login-card {
  position: relative;
  flex: 1;
  background: var(--login-blue);
  border-radius: 28px;
  padding: 32px 32px 28px;
  overflow: hidden;
}

.login-shell--form-only .login-card {
  min-height: calc(100vh - 48px);
  display: flex;
  flex-direction: column;
  justify-content: center;
  box-shadow: 0 16px 40px rgba(39, 72, 196, 0.18);
  padding: 36px 24px 32px;
}

@media (min-width: 1024px) {
  .login-card {
    border-top-left-radius: 48px;
    border-bottom-left-radius: 108px;
    min-height: 620px;
    box-shadow: -12px 0 32px rgba(39, 72, 196, 0.1);
  }
}

.login-card__brand {
  display: flex;
  align-items: center;
  justify-content: center;
  margin-bottom: 28px;
}

.login-card__logo {
  width: 52px;
  height: 52px;
  object-fit: contain;
}

.login-card__title {
  margin: 0 0 10px;
  font-size: 36px;
  font-weight: 800;
  color: #ffffff;
  letter-spacing: -0.03em;
  line-height: 1.1;
}

.login-card__title--inline {
  margin-bottom: 0;
}

.login-card__desc {
  margin: 0 0 32px;
  font-size: 13px;
  line-height: 1.65;
  color: rgba(255, 255, 255, 0.78);
  max-width: 340px;
}

.login-form {
  display: block;
}

.login-form :deep(.el-form-item) {
  margin-bottom: 18px;
}

.login-form :deep(.el-form-item__label) {
  display: block;
  width: 100% !important;
  padding: 0;
  margin-bottom: 8px;
  font-size: 12px;
  font-weight: 400;
  color: rgba(255, 255, 255, 0.92);
  letter-spacing: 0.01em;
  line-height: 1.4;
  text-align: left;
}

.login-form :deep(.el-form-item__content) {
  display: block;
  width: 100%;
  line-height: 1;
}

.login-form :deep(.el-form-item__error) {
  color: #ffe7e6;
  font-size: 12px;
  padding-top: 6px;
}

.login-field__wrap {
  position: relative;
  width: 100%;
}

.login-field__label,
.form-group .login-field__label {
  display: block;
  margin-bottom: 8px;
  font-size: 12px;
  font-weight: 400;
  color: rgba(255, 255, 255, 0.92);
  letter-spacing: 0.01em;
}

.login-field__icon {
  position: absolute;
  left: 16px;
  top: 50%;
  transform: translateY(-50%);
  color: rgba(255, 255, 255, 0.72);
  pointer-events: none;
}

.login-field__input {
  width: 100%;
  height: 48px;
  padding: 0 16px 0 48px;
  border: none;
  border-radius: 10px;
  background: var(--login-input-bg);
  color: #ffffff;
  font-size: 14px;
  outline: none;
  transition: background 150ms ease;
  box-sizing: border-box;
}

.login-field__input--password {
  padding-right: 48px;
}

.login-field__input::placeholder {
  color: rgba(255, 255, 255, 0.42);
}

.login-field__input:focus {
  background: var(--login-input-bg-focus);
}

.login-field__input:-webkit-autofill,
.login-field__input:-webkit-autofill:hover,
.login-field__input:-webkit-autofill:focus {
  -webkit-box-shadow: 0 0 0 1000px rgba(255, 255, 255, 0.14) inset;
  -webkit-text-fill-color: #ffffff;
  caret-color: #ffffff;
  transition: background-color 9999s ease-out 0s;
}

.login-field__input:disabled {
  opacity: 0.65;
  cursor: not-allowed;
}

.login-field__toggle {
  position: absolute;
  right: 14px;
  top: 50%;
  transform: translateY(-50%);
  border: none;
  background: transparent;
  color: rgba(255, 255, 255, 0.55);
  cursor: pointer;
  padding: 0;
  display: flex;
  align-items: center;
  justify-content: center;
}

.login-field__toggle:hover {
  color: #ffffff;
}

.form-options {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin: 4px 0 28px;
}

.form-options :deep(.el-checkbox) {
  height: auto;
}

.form-options :deep(.el-checkbox__label) {
  color: rgba(255, 255, 255, 0.9);
  font-size: 13px;
  padding-left: 8px;
}

.form-options :deep(.el-checkbox__inner) {
  width: 16px;
  height: 16px;
  background: transparent;
  border-color: rgba(255, 255, 255, 0.55);
  border-radius: 4px;
}

.form-options :deep(.el-checkbox__input.is-checked .el-checkbox__inner) {
  background: #ffffff;
  border-color: #ffffff;
}

.form-options :deep(.el-checkbox__input.is-checked .el-checkbox__inner::after) {
  border-color: var(--login-blue);
}

.link-btn {
  border: none;
  background: transparent;
  padding: 0;
  font-size: 13px;
  color: rgba(255, 255, 255, 0.88);
  cursor: pointer;
  text-decoration: none;
}

.link-btn:hover {
  color: #ffffff;
  text-decoration: underline;
  text-underline-offset: 2px;
}

.login-submit {
  width: 100%;
  height: 48px;
  border: none;
  border-radius: 999px;
  background: #ffffff;
  color: var(--login-blue);
  font-size: 15px;
  font-weight: 700;
  cursor: pointer;
  transition: transform 150ms ease, box-shadow 150ms ease, opacity 150ms ease;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  margin-top: 0;
}

.login-submit:hover:not(:disabled) {
  box-shadow: 0 10px 24px rgba(0, 0, 0, 0.12);
}

.login-submit:active:not(:disabled) {
  transform: translateY(1px);
}

.login-submit:disabled {
  opacity: 0.72;
  cursor: not-allowed;
}

.login-submit--spaced {
  margin-top: 16px;
}

.login-card__help {
  margin: 24px 0 0;
  text-align: center;
  font-size: 12px;
  color: rgba(255, 255, 255, 0.72);
}

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
  margin-bottom: 8px;
}

.back-btn {
  width: 32px;
  height: 32px;
  border: 1px solid rgba(255, 255, 255, 0.25);
  border-radius: 10px;
  background: rgba(255, 255, 255, 0.12);
  color: #ffffff;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: background 150ms ease;
}

.back-btn:hover {
  background: rgba(255, 255, 255, 0.2);
}

.form-group {
  margin-bottom: 16px;
}

.sms-row {
  display: flex;
  gap: 8px;
}

.sms-input-wrap {
  flex: 1;
}

.sms-btn {
  flex-shrink: 0;
  height: 48px;
  padding: 0 16px;
  border-radius: 12px;
  border: 1px solid rgba(255, 255, 255, 0.35);
  background: rgba(255, 255, 255, 0.12);
  color: #ffffff;
  font-size: 13px;
  font-weight: 600;
  cursor: pointer;
  white-space: nowrap;
  transition: background 150ms ease;
}

.sms-btn:hover:not(:disabled) {
  background: rgba(255, 255, 255, 0.2);
}

.sms-btn:disabled {
  opacity: 0.55;
  cursor: not-allowed;
}

.strength-bars {
  display: flex;
  gap: 4px;
  margin-bottom: 4px;
}

.strength-bar {
  height: 4px;
  border-radius: 2px;
  flex: 1;
  transition: background 300ms ease;
}

.strength-label {
  font-size: 12px;
  margin-bottom: 8px;
}

.success-overlay {
  position: absolute;
  inset: 0;
  background: rgba(39, 72, 196, 0.96);
  backdrop-filter: blur(8px);
  border-radius: 28px;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  z-index: 10;
  padding: 24px;
}

@media (min-width: 1024px) {
  .success-overlay {
    border-top-left-radius: 48px;
    border-bottom-left-radius: 108px;
  }
}

.success-icon {
  width: 64px;
  height: 64px;
  border-radius: 50%;
  background: rgba(255, 255, 255, 0.18);
  color: #ffffff;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-bottom: 16px;
}

.success-title {
  font-size: 20px;
  font-weight: 700;
  color: #ffffff;
  margin-bottom: 4px;
}

.success-msg {
  font-size: 13px;
  color: rgba(255, 255, 255, 0.82);
  margin-bottom: 16px;
}

.success-bar {
  width: 192px;
  height: 6px;
  border-radius: 3px;
  background: rgba(255, 255, 255, 0.22);
  overflow: hidden;
}

.success-bar-fill {
  height: 100%;
  border-radius: 3px;
  background: #ffffff;
  animation: progressFill 2s linear forwards;
}

@keyframes progressFill {
  from { width: 0; }
  to { width: 100%; }
}

.fade-enter-active,
.fade-leave-active {
  transition: opacity 150ms ease;
}

.fade-enter-from,
.fade-leave-to {
  opacity: 0;
}
</style>
