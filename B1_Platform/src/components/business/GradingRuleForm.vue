<script setup lang="ts">
interface IGradingRuleModel {
  roleText: string
  skillText: string
  ruleText: string
}

const props = defineProps<{ modelValue: IGradingRuleModel }>()
const emit = defineEmits<{ "update:modelValue": [value: IGradingRuleModel] }>()

function update(key: keyof IGradingRuleModel, value: string) {
  emit("update:modelValue", { ...props.modelValue, [key]: value })
}
</script>

<template>
  <div class="grading-rule">
    <div class="grading-rule__hint">评分细则（用于 AI 评审，可留空使用系统默认四维度）</div>

    <div class="form-group">
      <label class="form-label">Role · 角色</label>
      <textarea
        :value="modelValue.roleText"
        class="form-textarea"
        rows="2"
        placeholder="如：资深阅卷教师身份"
        @input="update('roleText', ($event.target as HTMLTextAreaElement).value)"
      />
    </div>

    <div class="form-group">
      <label class="form-label">Skill · 能力要求</label>
      <textarea
        :value="modelValue.skillText"
        class="form-textarea"
        rows="2"
        placeholder="如：精准客观打分、分项统计、出具评语"
        @input="update('skillText', ($event.target as HTMLTextAreaElement).value)"
      />
    </div>

    <div class="form-group">
      <label class="form-label">Rule · 打分细则（重点）</label>
      <textarea
        :value="modelValue.ruleText"
        class="form-textarea"
        rows="4"
        placeholder="详细打分细则、扣分标准、分值权重"
        @input="update('ruleText', ($event.target as HTMLTextAreaElement).value)"
      />
    </div>

    <div class="grading-rule__readonly">
      Output Format · 输出格式：系统已固定为标准四维度 JSON，无需填写
    </div>
  </div>
</template>

<style scoped>
.grading-rule { border-top: 1px solid var(--color-border, #e2e8f0); padding-top: 16px; margin-top: 4px; }
.grading-rule__hint { font-size: 13px; font-weight: 600; color: var(--color-text-secondary, #64748b); margin-bottom: 12px; }
.grading-rule__readonly { font-size: 12px; color: var(--color-text-placeholder, #94a3b8); background: #f8fafc; border: 1px dashed var(--color-border, #e2e8f0); border-radius: var(--radius-sm, 4px); padding: 8px 12px; }
.form-group { margin-bottom: 16px; }
.form-label { display: block; font-size: 13px; font-weight: 500; color: var(--color-text-secondary, #64748b); margin-bottom: 5px; }
.form-textarea { width: 100%; padding: 8px 12px; font-size: 14px; border: 1px solid var(--color-border, #e2e8f0); border-radius: var(--radius-sm, 4px); background: var(--color-card, #fff); color: var(--color-text-primary, #1e293b); resize: vertical; outline: none; font-family: inherit; box-sizing: border-box; }
.form-textarea:focus { border-color: var(--color-primary, #3b82f6); }
</style>
