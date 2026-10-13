<template>
  <!-- 商品类型管理：标准 增删改查 页面（渲染在后台布局内容区） -->
  <div class="type-manage">
    <!-- 操作栏 -->
    <div class="toolbar eb-card">
      <span class="eb-page-title">商品类型管理</span>
      <div class="toolbar-right">
        <el-input
          v-model="query.keywords"
          placeholder="搜索类型名称"
          clearable
          style="width: 200px"
          @keyup.enter="handleSearch"
          @clear="handleSearch"
        >
          <template #prefix><el-icon><Search /></el-icon></template>
        </el-input>
        <el-button type="primary" color="#1f4e79" :icon="Search" @click="handleSearch">
          搜索
        </el-button>
        <el-button type="primary" color="#1f4e79" :icon="Plus" @click="openAdd">
          新增类型
        </el-button>
      </div>
    </div>

    <!--
      数据表格：统一使用通用组件 EbTable
      由组件内部承载「加载态 / 空状态 / 分页器」三件事，页面只声明列
    -->
    <div class="eb-card table-wrap">
      <EbTable
        v-model:current-page="query.currentPage"
        :data="tableData"
        :loading="loading"
        :total="total"
        empty-text="暂无商品类型数据"
        @page-change="loadData"
      >
        <!-- 序号列按当前页偏移，避免每页都从 1 开始 -->
        <el-table-column
          type="index"
          label="序号"
          width="70"
          align="center"
          :index="indexMethod"
        />
        <el-table-column prop="id" label="类型ID" width="100" align="center" />
        <el-table-column prop="name" label="类型名称" min-width="200" />
        <el-table-column label="商品数量" width="140" align="center">
          <template #default="{ row }">
            <el-tag type="info" size="small">{{ row.goodsCount || 0 }} 件</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="180" align="center" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link :icon="Edit" @click="openEdit(row)">
              修改
            </el-button>
            <el-button type="danger" link :icon="Delete" @click="handleDelete(row)">
              删除
            </el-button>
          </template>
        </el-table-column>
      </EbTable>
    </div>

    <!--
      新增 / 修改 弹窗：统一使用通用组件 EbDialogForm
      校验在组件内部完成，通过后才抛 submit 事件，页面无需再写 validate 回调
    -->
    <EbDialogForm
      v-model="dialogVisible"
      :title="isEdit ? '修改商品类型' : '新增商品类型'"
      :model="form"
      :rules="rules"
      :saving="saving"
      width="460px"
      @submit="handleSubmit"
      @closed="resetForm"
    >
      <el-form-item label="类型名称" prop="name">
        <el-input v-model="form.name" placeholder="请输入商品类型名称" clearable />
      </el-form-item>
    </EbDialogForm>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, Edit, Delete, Search } from '@element-plus/icons-vue'
import { adminApi } from '@/utils/api'
// 通用公共组件
import EbTable from '@/components/EbTable.vue'
import EbDialogForm from '@/components/EbDialogForm.vue'
import { required } from '@/utils/validators'

/** 每页条数（与后端 ADMIN_PAGE_SIZE 保持一致） */
const PAGE_SIZE = 10

const loading = ref(false)
const saving = ref(false)
const dialogVisible = ref(false)
const isEdit = ref(false)

const tableData = ref([])
const total = ref(0)

// 查询条件
const query = reactive({
  keywords: '',
  currentPage: 1
})

/** 序号按分页偏移，第 2 页从 11 开始，而不是每页都从 1 开始 */
const indexMethod = (index) => (query.currentPage - 1) * PAGE_SIZE + index + 1

const form = reactive({
  id: null,
  name: ''
})

const rules = {
  name: [required('请输入类型名称')]
}

/* ---- 加载列表（分页 + 关键词） ---- */
const loadData = async () => {
  loading.value = true
  try {
    const res = await adminApi.getTypePage({
      currentPage: query.currentPage,
      keywords: query.keywords || undefined
    })
    const data = res.data || {}
    tableData.value = data.typeList || []
    total.value = Number(data.total || 0)
    if (data.currentPage) query.currentPage = Number(data.currentPage)
  } catch (e) {
    // 拦截器已统一提示
    tableData.value = []
    total.value = 0
  } finally {
    loading.value = false
  }
}

const handleSearch = () => {
  query.currentPage = 1
  loadData()
}

/* ---- 打开新增弹窗 ---- */
const openAdd = () => {
  isEdit.value = false
  dialogVisible.value = true
}

/* ---- 打开修改弹窗 ---- */
const openEdit = (row) => {
  isEdit.value = true
  form.id = row.id
  form.name = row.name
  dialogVisible.value = true
}

/* ---- 提交（表单校验已由 EbDialogForm 内部完成） ---- */
const handleSubmit = async () => {
  saving.value = true
  try {
    if (isEdit.value) {
      await adminApi.updateType({ id: form.id, name: form.name })
      ElMessage.success('修改成功')
    } else {
      await adminApi.addType({ name: form.name })
      ElMessage.success('新增成功')
    }
    dialogVisible.value = false
    loadData()
  } catch (e) {
    // 拦截器已统一提示
  } finally {
    saving.value = false
  }
}

/* ---- 删除 ---- */
const handleDelete = (row) => {
  ElMessageBox.confirm(`确定删除商品类型「${row.name}」吗？`, '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  })
    .then(async () => {
      try {
        await adminApi.deleteType(row.id)
        ElMessage.success('删除成功')
        // 删掉的是本页最后一条时回退一页，避免停在空白页
        if (tableData.value.length === 1 && query.currentPage > 1) {
          query.currentPage -= 1
        }
        loadData()
      } catch (e) {
        // 拦截器已统一提示
      }
    })
    .catch(() => {})
}

/* ---- 重置表单（弹窗关闭时触发） ---- */
const resetForm = () => {
  form.id = null
  form.name = ''
}

onMounted(loadData)
</script>

<style scoped>
.toolbar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 16px 20px;
  margin-bottom: 16px;
}

.toolbar .eb-page-title {
  margin-bottom: 0;
}

.toolbar-right {
  display: flex;
  gap: 10px;
}

.table-wrap {
  padding: 16px;
}

/* ---------- 响应式适配（桌面分辨率）---------- */
@media (max-width: 1024px) {
  .toolbar {
    flex-direction: column;
    align-items: stretch;
    gap: 12px;
  }

  .toolbar-right {
    flex-wrap: wrap;
  }
}
</style>
