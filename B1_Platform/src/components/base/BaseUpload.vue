<script setup lang="ts">
import { Upload } from "lucide-vue-next"

interface UploadFile {
  name: string
  url?: string
  status?: string
  uid?: number
}

defineProps<{
  fileList?: UploadFile[]
  action?: string
  accept?: string
  limit?: number
  disabled?: boolean
  listType?: "text" | "picture" | "picture-card"
  autoUpload?: boolean
}>()

const emit = defineEmits<{
  "update:fileList": [files: UploadFile[]]
}>()
</script>

<template>
  <el-upload
    :file-list="fileList"
    :action="action ?? '#'"
    :accept="accept"
    :limit="limit"
    :disabled="disabled"
    :list-type="listType ?? 'text'"
    :auto-upload="autoUpload ?? false"
    v-bind="$attrs"
    @update:file-list="(files: UploadFile[]) => emit('update:fileList', files)"
  >
    <el-button type="primary" :disabled="disabled">
      <Upload :size="16" />
      <span style="margin-left: 4px">上传文件</span>
    </el-button>
  </el-upload>
</template>
