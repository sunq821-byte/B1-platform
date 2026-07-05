<script setup lang="ts">
import { ref, reactive, onMounted, onUnmounted } from "vue"
import { useUserStore } from "@/stores/useUserStore"
import { updateProfile, changePassword, uploadAvatar } from "@/api/modules/user"
import { ElMessage } from "element-plus"
import { Camera } from "lucide-vue-next"
import PageHeader from "@/components/layout/PageHeader.vue"
import BaseInput from "@/components/base/BaseInput.vue"
import BaseButton from "@/components/base/BaseButton.vue"
import { LoadingState, ErrorState } from "@/components/common"

const userStore = useUserStore()
const userInfo = userStore.userInfo

const isEditing = ref(false)
const loading = ref(true)
const loadError = ref(false)
const profileSubmitting = ref(false)
const passwordSubmitting = ref(false)
const avatarUploading = ref(false)
const isOnline = ref(window.navigator.onLine)

const profileForm = reactive({ realName: "", email: "", phone: "" })
const passwordForm = reactive({ oldPassword: "", newPassword: "", confirmPassword: "" })

function handleOnline() { isOnline.value = true }
function handleOffline() { isOnline.value = false }

function avatarChar(name?: string) { return (name ?? "?").charAt(0).toUpperCase() }

function resetProfileForm() {
  const info = userStore.userInfo
  profileForm.realName = info?.realName ?? ""
  profileForm.email = info?.email ?? ""
  profileForm.phone = info?.phone ?? ""
}

async function loadUserInfo() {
  loading.value = true; loadError.value = false
  try { await userStore.fetchUserInfo() }
  catch { loadError.value = true }
  finally { loading.value = false }
}

function enterEdit() { resetProfileForm(); isEditing.value = true }
function cancelEdit() { isEditing.value = false }

async function handleSaveProfile() {
  if (!profileForm.realName || !profileForm.email || !profileForm.phone) { ElMessage.warning("请填写完整的个人信息"); return }
  if (!/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(profileForm.email)) { ElMessage.warning("请输入有效的邮箱地址"); return }
  if (!/^1\d{10}$/.test(profileForm.phone)) { ElMessage.warning("请输入有效的手机号码"); return }
  profileSubmitting.value = true
  try {
    await updateProfile({ realName: profileForm.realName, email: profileForm.email, phone: profileForm.phone })
    const info = userStore.userInfo
    if (info) { info.realName = profileForm.realName; info.email = profileForm.email; info.phone = profileForm.phone }
    ElMessage.success("个人信息更新成功"); isEditing.value = false
  } catch { ElMessage.error("更新失败") }
  finally { profileSubmitting.value = false }
}

async function handleAvatarChange(e: Event) {
  const file = (e.target as HTMLInputElement).files?.[0]
  if (!file) return
  avatarUploading.value = true
  try {
    const result = await uploadAvatar(file)
    const info = userStore.userInfo
    if (info) info.avatar = result.avatar
    ElMessage.success("头像更新成功")
  } catch { ElMessage.error("头像上传失败") }
  finally { avatarUploading.value = false; (e.target as HTMLInputElement).value = "" }
}

async function handleChangePassword() {
  if (!passwordForm.oldPassword || !passwordForm.newPassword || !passwordForm.confirmPassword) { ElMessage.warning("请填写完整的密码信息"); return }
  if (passwordForm.newPassword.length < 6) { ElMessage.warning("新密码长度至少6位"); return }
  if (passwordForm.newPassword !== passwordForm.confirmPassword) { ElMessage.warning("两次输入的密码不一致"); return }
  passwordSubmitting.value = true
  try {
    await changePassword({ oldPassword: passwordForm.oldPassword, newPassword: passwordForm.newPassword })
    ElMessage.success("密码修改成功")
    passwordForm.oldPassword = ""; passwordForm.newPassword = ""; passwordForm.confirmPassword = ""
  } catch (err: unknown) {
    const e = err as { response?: { data?: { code?: number } }; message?: string }
    ElMessage.error(e?.response?.data?.code === 2009 ? "原密码错误" : e?.message || "密码修改失败")
  } finally {
    passwordSubmitting.value = false
  }
}

onMounted(() => {
  window.addEventListener("online", handleOnline)
  window.addEventListener("offline", handleOffline)
  loadUserInfo()
})
onUnmounted(() => {
  window.removeEventListener("online", handleOnline)
  window.removeEventListener("offline", handleOffline)
})
</script>

<template>
  <div class="profile-page">
    <PageHeader title="个人中心" />
    <LoadingState v-if="loading" text="加载用户信息..." />
    <ErrorState v-else-if="loadError" message="加载用户信息失败" @retry="loadUserInfo" />
    <div v-else-if="userInfo" class="profile-grid">
      <div class="profile-card">
        <div class="profile-card__title">个人信息</div>
        <div class="profile-card__avatar">
          <div class="avatar-wrap">
            <el-avatar :size="80" :src="userInfo.avatar || undefined">{{ avatarChar(userInfo.realName) }}</el-avatar>
            <label class="avatar-overlay">
              <input type="file" accept="image/*" hidden :disabled="!isOnline" @change="handleAvatarChange" />
              <Camera :size="18" />
            </label>
          </div>
        </div>
        <template v-if="!isEditing">
          <div class="info-list">
            <div class="info-row"><span class="info-label">姓名</span><span class="info-value">{{ userInfo.realName }}</span></div>
            <div class="info-row"><span class="info-label">邮箱</span><span class="info-value">{{ userInfo.email }}</span></div>
            <div class="info-row"><span class="info-label">手机</span><span class="info-value">{{ userInfo.phone }}</span></div>
          </div>
          <BaseButton class="mt-sm" :disabled="!isOnline" @click="enterEdit">编辑信息</BaseButton>
        </template>
        <div v-else class="edit-form">
          <div class="field"><label class="field-label">姓名</label><BaseInput v-model="profileForm.realName" placeholder="输入姓名" /></div>
          <div class="field"><label class="field-label">邮箱</label><BaseInput v-model="profileForm.email" placeholder="输入邮箱" /></div>
          <div class="field"><label class="field-label">手机</label><BaseInput v-model="profileForm.phone" placeholder="输入手机号" /></div>
          <div class="edit-actions">
            <BaseButton @click="cancelEdit">取消</BaseButton>
            <BaseButton type="primary" :loading="profileSubmitting" @click="handleSaveProfile">保存</BaseButton>
          </div>
        </div>
      </div>
      <div class="profile-card">
        <div class="profile-card__title">修改密码</div>
        <div class="field"><label class="field-label">原密码</label><BaseInput v-model="passwordForm.oldPassword" type="password" :disabled="!isOnline" placeholder="输入原密码" /></div>
        <div class="field"><label class="field-label">新密码</label><BaseInput v-model="passwordForm.newPassword" type="password" show-password :disabled="!isOnline" placeholder="输入新密码（至少6位）" /></div>
        <div class="field"><label class="field-label">确认密码</label><BaseInput v-model="passwordForm.confirmPassword" type="password" :disabled="!isOnline" placeholder="再次输入新密码" /></div>
        <BaseButton type="primary" block :loading="passwordSubmitting" :disabled="!isOnline" @click="handleChangePassword">修改密码</BaseButton>
      </div>
    </div>
  </div>
</template>

<style scoped>
.profile-page { height: 100%; display: flex; flex-direction: column; }
.profile-grid { display: grid; grid-template-columns: 1fr 1fr; gap: var(--spacing-lg, 24px); }
.profile-card { background: var(--color-card, #fff); border-radius: var(--radius-md, 8px); padding: var(--spacing-lg, 24px); box-shadow: var(--shadow-sm, 0 1px 2px rgba(0,0,0,.05)); }
.profile-card__title { font-size: var(--font-size-base, 16px); font-weight: 600; color: var(--color-text-primary, #1e293b); margin-bottom: var(--spacing-lg, 24px); padding-bottom: var(--spacing-sm, 8px); border-bottom: 1px solid var(--color-border, #e2e8f0); }
.profile-card__avatar { display: flex; justify-content: center; margin-bottom: var(--spacing-lg, 24px); }
.avatar-wrap { position: relative; }
.avatar-overlay { position: absolute; inset: 0; border-radius: 50%; display: flex; align-items: center; justify-content: center; background: rgba(0,0,0,.5); color: #fff; cursor: pointer; opacity: 0; transition: opacity var(--transition-fast, 150ms ease); }
.avatar-overlay:hover { opacity: 1; }
.info-list { display: flex; flex-direction: column; gap: var(--spacing-md, 16px); }
.info-row { display: flex; justify-content: space-between; align-items: center; padding: var(--spacing-sm, 8px) 0; border-bottom: 1px solid var(--color-border, #e2e8f0); }
.info-label { font-size: var(--font-size-sm, 14px); color: var(--color-text-secondary, #64748b); }
.info-value { font-size: var(--font-size-sm, 14px); color: var(--color-text-primary, #1e293b); font-weight: 500; }
.mt-sm { margin-top: var(--spacing-md, 16px); }
.edit-form { display: flex; flex-direction: column; gap: var(--spacing-md, 16px); }
.field { display: flex; flex-direction: column; gap: var(--spacing-xs, 4px); }
.field-label { font-size: var(--font-size-sm, 14px); color: var(--color-text-secondary, #64748b); font-weight: 500; }
.edit-actions { display: flex; justify-content: flex-end; gap: var(--spacing-sm, 8px); margin-top: var(--spacing-sm, 8px); }
</style>
