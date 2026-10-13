<template>
  <!-- 商品管理：含搜索 / 类型筛选 / 分页 / 图片上传的完整 CRUD -->
  <div class="goods-manage">
    <!-- 操作栏 -->
    <div class="toolbar eb-card">
      <span class="eb-page-title">商品管理</span>
      <div class="toolbar-right">
        <el-input
          v-model="query.keywords"
          placeholder="搜索商品名称"
          clearable
          style="width: 200px"
          @keyup.enter="handleSearch"
        >
          <template #prefix><el-icon><Search /></el-icon></template>
        </el-input>
        <el-select
          v-model="query.goodstypeId"
          placeholder="全部分类"
          clearable
          style="width: 160px"
          @change="handleSearch"
        >
          <el-option
            v-for="t in typeList"
            :key="t.id"
            :label="t.name"
            :value="t.id"
          />
        </el-select>
        <el-button type="primary" color="#1f4e79" :icon="Search" @click="handleSearch">
          搜索
        </el-button>
        <el-button
          type="success"
          plain
          :icon="Top"
          :disabled="selectedIds.length === 0"
          @click="handleBatchStatus(0)"
        >
          批量上架
        </el-button>
        <el-button
          type="warning"
          plain
          :icon="Bottom"
          :disabled="selectedIds.length === 0"
          @click="handleBatchStatus(1)"
        >
          批量下架
        </el-button>
        <el-button type="success" :icon="Plus" @click="openAdd">新增商品</el-button>
      </div>
    </div>

    <!-- 已选提示条 -->
    <div v-if="selectedIds.length > 0" class="select-bar">
      <span>已选中 <b>{{ selectedIds.length }}</b> 件商品</span>
      <el-button link type="primary" @click="clearSelection">取消选择</el-button>
    </div>

    <!-- 数据表格：统一使用通用组件 EbTable（内置加载态 / 空态 / 分页器） -->
    <div class="eb-card table-wrap">
      <EbTable
        ref="tableRef"
        v-model:current-page="query.currentPage"
        :data="tableData"
        :loading="loading"
        :total="total"
        empty-text="暂无商品数据"
        @selection-change="handleSelectionChange"
        @page-change="loadData"
      >
        <el-table-column type="selection" width="50" align="center" />
        <el-table-column type="index" label="序号" width="60" align="center" :index="indexMethod" />
        <el-table-column label="图片" width="90" align="center">
          <template #default="{ row }">
            <img :src="goodsImg(row.picture)" :alt="row.name" class="cell-img" />
          </template>
        </el-table-column>
        <el-table-column prop="name" label="商品名称" min-width="180" show-overflow-tooltip />
        <el-table-column prop="typeName" label="所属类型" width="120" align="center">
          <template #default="{ row }">{{ row.typeName || '未分类' }}</template>
        </el-table-column>
        <el-table-column label="价格" width="110" align="center">
          <template #default="{ row }">
            <span class="eb-price">￥{{ money(row.price) }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="stock" label="库存" width="90" align="center" />
        <el-table-column prop="sales" label="销量" width="90" align="center" />
        <el-table-column label="状态" width="120" align="center">
          <template #default="{ row }">
            <el-switch
              v-model="row.isoff"
              :active-value="0"
              :inactive-value="1"
              inline-prompt
              active-text="上架"
              inactive-text="下架"
              @change="(val) => handleStatusChange(row, val)"
            />
          </template>
        </el-table-column>
        <el-table-column label="操作" width="160" align="center" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link :icon="Edit" @click="openEdit(row)">修改</el-button>
            <el-button type="danger" link :icon="Delete" @click="handleDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </EbTable>
    </div>

    <!-- 新增 / 修改 弹窗：通用组件 EbDialogForm 统一承载 表单 / 校验 / 底部按钮 -->
    <EbDialogForm
      v-model="dialogVisible"
      :title="isEdit ? '修改商品' : '新增商品'"
      :model="form"
      :rules="rules"
      :saving="saving"
      width="620px"
      @submit="handleSubmit"
      @closed="resetForm"
    >
        <el-form-item label="商品名称" prop="name">
          <el-input v-model="form.name" placeholder="请输入商品名称" clearable />
        </el-form-item>

        <el-row :gutter="12">
          <el-col :span="12">
            <el-form-item label="所属类型" prop="goodstypeId">
              <el-select v-model="form.goodstypeId" placeholder="请选择类型" style="width: 100%">
                <el-option
                  v-for="t in typeList"
                  :key="t.id"
                  :label="t.name"
                  :value="t.id"
                />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="库存" prop="stock">
              <el-input-number v-model="form.stock" :min="0" style="width: 100%" />
            </el-form-item>
          </el-col>
        </el-row>

        <el-row :gutter="12">
          <el-col :span="12">
            <el-form-item label="价格" prop="price">
              <el-input-number
                v-model="form.price"
                :min="0"
                :precision="2"
                style="width: 100%"
              />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="上架状态">
              <el-switch
                v-model="form.isoff"
                :active-value="0"
                :inactive-value="1"
                active-text="上架"
                inactive-text="下架"
              />
            </el-form-item>
          </el-col>
        </el-row>

        <el-form-item label="商品图片">
          <el-upload
            class="uploader"
            :auto-upload="false"
            :show-file-list="false"
            :on-change="handleFileChange"
            accept="image/*"
          >
            <img v-if="previewUrl" :src="previewUrl" class="preview-img" alt="预览" />
            <div v-else class="uploader-icon">
              <el-icon :size="24"><Plus /></el-icon>
            </div>
          </el-upload>
          <div class="upload-tip">支持 jpg / png，大小不超过 2MB，不选则保留原图</div>
        </el-form-item>

        <el-form-item label="商品规格">
          <el-input
            v-model="form.specs"
            placeholder="格式：颜色:幻夜黑|星河银;版本:12+256G|12+512G（留空表示无规格）"
          />
          <div class="upload-tip">
            多个规格维度用「;」分隔，同一维度内的选项用「|」分隔；填了规格的顾客下单前必须选择
          </div>
        </el-form-item>

        <el-form-item label="商品描述">
          <el-input
            v-model="form.introduce"
            type="textarea"
            :rows="3"
            placeholder="请输入商品描述"
          />
        </el-form-item>
    </EbDialogForm>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, Edit, Delete, Search, Top, Bottom } from '@element-plus/icons-vue'
import { adminApi } from '@/utils/api'
import { goodsImg, money } from '@/utils/format'
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
const typeList = ref([])
const total = ref(0)
const tableRef = ref(null)

/** 表格中勾选的行 */
const selectedIds = ref([])

/** 序号按分页偏移，第 2 页从 11 开始 */
const indexMethod = (index) => (query.currentPage - 1) * PAGE_SIZE + index + 1

// 查询条件
const query = reactive({
  keywords: '',
  goodstypeId: null,
  currentPage: 1
})

// 表单数据
const form = reactive({
  id: null,
  name: '',
  goodstypeId: null,
  price: 0,
  stock: 0,
  isoff: 0,
  introduce: '',
  specs: '',
  picture: '',
  file: null
})

const rules = {
  name: [required('请输入商品名称')],
  goodstypeId: [required('请选择所属类型', 'change')],
  price: [required('请输入价格')],
  stock: [required('请输入库存')]
}

// 图片预览：优先本地新选文件，其次已有图片
const previewUrl = computed(() => {
  if (form.file) return form.fileUrl
  return form.picture ? goodsImg(form.picture) : ''
})

/* ---- 加载商品列表 ---- */
const loadData = async () => {
  loading.value = true
  try {
    const res = await adminApi.getGoodsList({
      currentPage: query.currentPage,
      goodstypeId: query.goodstypeId || undefined,
      keywords: query.keywords || undefined
    })
    const data = res.data || {}
    tableData.value = data.goodsList || []
    total.value = Number(data.total || 0)
    if (data.currentPage) query.currentPage = Number(data.currentPage)
    // 数据换了，之前的勾选已失效
    selectedIds.value = []
  } catch (e) {
    // 拦截器已统一提示
    tableData.value = []
    total.value = 0
  } finally {
    loading.value = false
  }
}

/* ---- 加载类型下拉 ---- */
const loadTypeList = async () => {
  try {
    const res = await adminApi.getTypeList()
    typeList.value = res.data || []
  } catch (e) {
    // 拦截器已统一提示
    typeList.value = []
  }
}

const handleSearch = () => {
  query.currentPage = 1
  loadData()
}

/* ---- 打开新增 ---- */
const openAdd = () => {
  isEdit.value = false
  dialogVisible.value = true
}

/* ---- 打开修改（用列表行数据回显） ---- */
const openEdit = (row) => {
  isEdit.value = true
  Object.assign(form, {
    id: row.id,
    name: row.name,
    goodstypeId: row.goodstypeId,
    price: Number(row.price || 0),
    stock: Number(row.stock || 0),
    isoff: Number(row.isoff ?? 0),
    introduce: row.introduce || '',
    specs: row.specs || '',
    picture: row.picture || '',
    file: null,
    fileUrl: ''
  })
  dialogVisible.value = true
}

/* ---- 选择图片 ---- */
const handleFileChange = (uploadFile) => {
  const raw = uploadFile.raw
  if (!raw) return
  const isImage = raw.type.startsWith('image/')
  const isLt2M = raw.size / 1024 / 1024 < 2
  if (!isImage) {
    ElMessage.error('只能上传图片文件')
    return
  }
  if (!isLt2M) {
    ElMessage.error('图片大小不能超过 2MB')
    return
  }
  form.file = raw
  form.fileUrl = URL.createObjectURL(raw)
}

/* ---- 提交（FormData） ---- */
/* ---- 提交（表单校验已由 EbDialogForm 内部完成，校验通过才会触发本方法） ---- */
const handleSubmit = async () => {
  saving.value = true
  try {
    const fd = new FormData()
    fd.append('name', form.name)
    fd.append('price', form.price)
    fd.append('stock', form.stock)
    fd.append('goodstypeId', form.goodstypeId)
    fd.append('isoff', form.isoff)
    fd.append('introduce', form.introduce || '')
    fd.append('specs', form.specs || '')
    fd.append('act', isEdit.value ? 'update' : 'add')
    if (isEdit.value) fd.append('id', form.id)
    if (form.file) fd.append('file', form.file)

    if (isEdit.value) {
      await adminApi.updateGoods(fd)
      ElMessage.success('修改成功')
    } else {
      await adminApi.addGoods(fd)
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

/* ---- 上下架切换 ---- */
const handleStatusChange = async (row, val) => {
  try {
    await adminApi.changeGoodsStatus({ id: row.id, isoff: val })
    ElMessage.success(val === 0 ? '已上架' : '已下架')
  } catch (e) {
    // 失败时重新拉取，恢复服务器的真实状态
    loadData()
  }
}

/* ---- 表格勾选 ---- */
const handleSelectionChange = (rows) => {
  selectedIds.value = rows.map((r) => r.id)
}

const clearSelection = () => {
  tableRef.value && tableRef.value.clearSelection()
  selectedIds.value = []
}

/* ---- 批量上架 / 下架 ---- */
const handleBatchStatus = (isoff) => {
  const action = isoff === 0 ? '上架' : '下架'
  const count = selectedIds.value.length
  ElMessageBox.confirm(`确定要批量${action}选中的 ${count} 件商品吗？`, '批量操作', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  })
    .then(async () => {
      try {
        const res = await adminApi.batchChangeGoodsStatus({
          ids: selectedIds.value,
          isoff
        })
        // 后端会在"批量上架且库存为 0"时跳过部分商品，消息里带说明
        ElMessage.success(res.msg || `已批量${action} ${count} 件商品`)
        clearSelection()
        loadData()
      } catch (e) {
        // 拦截器已统一提示
      }
    })
    .catch(() => {})
}

/* ---- 删除 ---- */
const handleDelete = (row) => {
  ElMessageBox.confirm(`确定删除商品「${row.name}」吗？`, '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  })
    .then(async () => {
      try {
        await adminApi.deleteGoods(row.id)
        ElMessage.success('删除成功')
        loadData()
      } catch (e) {
        // 拦截器已统一提示
      }
    })
    .catch(() => {})
}

/* ---- 重置表单 ---- */
const resetForm = () => {
  Object.assign(form, {
    id: null,
    name: '',
    goodstypeId: null,
    price: 0,
    stock: 0,
    isoff: 0,
    introduce: '',
    specs: '',
    picture: '',
    file: null,
    fileUrl: ''
  })
}

onMounted(() => {
  loadData()
  loadTypeList()
})
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

/* 已选提示条 */
.select-bar {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 9px 16px;
  margin-bottom: 12px;
  font-size: var(--eb-font-sm);
  color: var(--eb-text-regular);
  background: var(--eb-admin-50);
  border: 1px solid var(--eb-admin-100);
  border-radius: var(--eb-radius-md);
}

.select-bar b {
  color: var(--eb-admin-600);
}

.cell-img {
  width: 50px;
  height: 50px;
  object-fit: cover;
  border-radius: var(--eb-radius-sm);
  background: var(--eb-bg-page);
}

/* ---------- 响应式适配（桌面分辨率）----------
 * 工具栏按钮较多，窄屏时允许换行，避免按钮被挤出可视区
 */
@media (max-width: 1280px) {
  .toolbar {
    flex-wrap: wrap;
    gap: 12px;
  }

  .toolbar-right {
    flex-wrap: wrap;
  }
}

@media (max-width: 1024px) {
  .toolbar {
    flex-direction: column;
    align-items: stretch;
  }
}

/* ---------- 图片上传 ---------- */
.uploader {
  display: flex;
}

.preview-img {
  width: 100px;
  height: 100px;
  object-fit: cover;
  border-radius: var(--eb-radius-md);
  border: 1px dashed var(--eb-border-color);
}

.uploader-icon {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 100px;
  height: 100px;
  font-size: 24px;
  color: var(--eb-text-secondary);
  border: 1px dashed var(--eb-border-color);
  border-radius: var(--eb-radius-md);
  cursor: pointer;
  transition: all var(--eb-duration-fast) var(--eb-ease);
}

.uploader-icon:hover {
  border-color: var(--eb-admin-500);
  color: var(--eb-admin-500);
}

.upload-tip {
  width: 100%;
  font-size: var(--eb-font-xs);
  color: var(--eb-text-secondary);
  margin-top: 6px;
}
</style>
