<template>
  <!--
    通用弹窗表单组件 EbDialogForm
    ------------------------------------------------------------------
    封装目标：把后台「新增 / 修改」弹窗里反复出现的
    「el-dialog + el-form + 校验 + 底部按钮 + saving 状态」收敛成一个组件。

    使用方式：
      <EbDialogForm
        v-model="dialogVisible"
        :title="isEdit ? '修改商品类型' : '新增商品类型'"
        :model="form"
        :rules="rules"
        :saving="saving"
        @submit="handleSubmit"
        @closed="resetForm"
      >
        <el-form-item label="类型名称" prop="name">
          <el-input v-model="form.name" />
        </el-form-item>
      </EbDialogForm>

    设计要点：
      1. 表单校验收进组件内部：对外暴露 validate()，页面拿到 Promise 结果即可，
         不必再写 el-form 的 validate 回调嵌套
      2. 底部按钮统一：取消直接关闭；确定先校验、通过后再抛 submit 事件，
         保证「校验不过不发请求」这条规则在所有页面一致
      3. 关闭后抛 closed 事件，页面在此时机重置表单（与 el-dialog 的 closed 语义一致）
      4. 表单模型与校验规则由页面通过 props 传入，组件不持有业务字段
  -->
  <el-dialog
    :model-value="modelValue"
    :title="title"
    :width="width"
    :close-on-click-modal="false"
    @update:model-value="(val) => emit('update:modelValue', val)"
    @closed="handleClosed"
  >
    <el-form
      ref="formRef"
      :model="model"
      :rules="rules"
      :label-width="labelWidth"
    >
      <!-- 表单项：由使用方传入 -->
      <slot />
    </el-form>

    <template #footer>
      <el-button @click="handleCancel">{{ cancelText }}</el-button>
      <el-button type="primary" color="#1f4e79" :loading="saving" @click="handleConfirm">
        {{ confirmText }}
      </el-button>
    </template>
  </el-dialog>
</template>

<script setup>
import { ref } from 'vue'
import { ElDialog, ElForm, ElButton } from 'element-plus'

const props = defineProps({
  /** 显隐控制（v-model） */
  modelValue: { type: Boolean, default: false },
  /** 弹窗标题 */
  title: { type: String, default: '' },
  /** 弹窗宽度 */
  width: { type: String, default: '520px' },
  /** 表单数据对象 */
  model: { type: Object, default: () => ({}) },
  /** 校验规则 */
  rules: { type: Object, default: () => ({}) },
  /** 标签宽度 */
  labelWidth: { type: String, default: '90px' },
  /** 提交中（按钮 loading） */
  saving: { type: Boolean, default: false },
  /** 确定按钮文案 */
  confirmText: { type: String, default: '确定' },
  /** 取消按钮文案 */
  cancelText: { type: String, default: '取消' }
})

const emit = defineEmits(['update:modelValue', 'submit', 'closed'])

const formRef = ref(null)

/**
 * 触发内置表单校验
 * @returns {Promise<boolean>} 是否通过校验
 */
const validate = () =>
  new Promise((resolve) => {
    if (!formRef.value) {
      resolve(false)
      return
    }
    formRef.value.validate((valid) => resolve(!!valid))
  })

// 暴露给父组件，便于页面在特殊场景下自行调用
defineExpose({ validate })

/* 确定：先校验，通过后才抛 submit —— 保证「校验不过不发请求」 */
const handleConfirm = async () => {
  if (await validate()) {
    emit('submit')
  }
}

/* 取消：直接关闭 */
const handleCancel = () => {
  emit('update:modelValue', false)
}

/* 关闭后：清掉残留的校验红字，再把 closed 事件抛给页面去重置业务数据 */
const handleClosed = () => {
  if (formRef.value) {
    formRef.value.clearValidate()
  }
  emit('closed')
}
</script>
