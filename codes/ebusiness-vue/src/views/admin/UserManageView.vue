<template>
  <!-- 用户管理：查看注册用户列表、删除用户 -->
  <div class="user-manage">
    <!-- 操作栏 -->
    <div class="toolbar eb-card">
      <span class="eb-page-title">用户管理</span>
      <div class="toolbar-right">
        <el-input
          v-model="query.bemail"
          placeholder="按邮箱搜索"
          clearable
          style="width: 200px"
          @keyup.enter="handleSearch"
        >
          <template #prefix><el-icon><Search /></el-icon></template>
        </el-input>
        <el-input
          v-model="query.bname"
          placeholder="按昵称搜索"
          clearable
          style="width: 160px"
          @keyup.enter="handleSearch"
        >
          <template #prefix><el-icon><Search /></el-icon></template>
        </el-input>
        <el-button type="primary" color="#1f4e79" :icon="Search" @click="handleSearch">
          搜索
        </el-button>
      </div>
    </div>

    <!-- 数据表格：统一使用通用组件 EbTable（内置加载态 / 空态 / 分页器） -->
    <div class="eb-card table-wrap">
      <EbTable
        v-model:current-page="query.currentPage"
        :data="tableData"
        :loading="loading"
        :total="total"
        empty-text="暂无注册用户"
        @page-change="loadData"
      >
        <el-table-column type="index" label="序号" width="70" align="center" />
        <el-table-column prop="bid" label="用户ID" width="90" align="center" />
        <el-table-column prop="bemail" label="用户邮箱" min-width="200" show-overflow-tooltip />
        <el-table-column prop="bname" label="昵称" min-width="140">
          <template #default="{ row }">{{ row.bname || '未设置' }}</template>
        </el-table-column>
        <el-table-column prop="bphone" label="联系电话" width="150" align="center">
          <template #default="{ row }">{{ row.bphone || '-' }}</template>
        </el-table-column>
        <el-table-column prop="baddr" label="收货地址" min-width="200" show-overflow-tooltip>
          <template #default="{ row }">{{ row.baddr || '-' }}</template>
        </el-table-column>
        <el-table-column label="操作" width="120" align="center" fixed="right">
          <template #default="{ row }">
            <el-button type="danger" link :icon="Delete" @click="handleDelete(row)">
              删除
            </el-button>
          </template>
        </el-table-column>
      </EbTable>
    </div>
  </div>
</template>

<script setup>
import { reactive, ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Search, Delete } from '@element-plus/icons-vue'
import { adminApi } from '@/utils/api'
// 通用公共组件
import EbTable from '@/components/EbTable.vue'

const loading = ref(false)
const tableData = ref([])
const total = ref(0)

const query = reactive({
  bemail: '',
  bname: '',
  currentPage: 1
})

/* ---- 加载列表 ---- */
const loadData = async () => {
  loading.value = true
  try {
    const res = await adminApi.getUserList({
      currentPage: query.currentPage,
      bemail: query.bemail || undefined,
      bname: query.bname || undefined
    })
    const data = res.data || {}
    tableData.value = data.users || []
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

/* ---- 删除用户 ---- */
const handleDelete = (row) => {
  ElMessageBox.confirm(
    `确定删除用户「${row.bemail}」吗？删除后其订单等数据将一并受影响。`,
    '警告',
    {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    }
  )
    .then(async () => {
      try {
        await adminApi.deleteUser(row.bid)
        ElMessage.success('删除成功')
        loadData()
      } catch (e) {
        // 拦截器已统一提示
      }
    })
    .catch(() => {})
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
</style>
