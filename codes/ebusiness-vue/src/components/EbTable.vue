<template>
  <!--
    通用数据表格组件 EbTable
    ------------------------------------------------------------------
    封装目标：把后台列表页反复出现的「加载态 + 表格 + 空状态 + 分页器」
    这一整套组合收敛成一个组件，页面只需关心「有哪些列」。

    使用方式：
      <EbTable
        v-model:current-page="query.currentPage"
        :data="tableData"
        :loading="loading"
        :total="total"
        @page-change="loadData"
      >
        <el-table-column prop="id" label="ID" />
        <el-table-column prop="name" label="名称" />
      </EbTable>

    设计要点：
      1. 列定义仍由页面通过默认插槽传入 —— 保持 el-table 的原生写法，
         不额外发明一套 columns 配置，降低使用成本
      2. 分页用 v-model:current-page 双向绑定，页码变化同时抛出 page-change
         事件供页面触发重新请求
      3. 空状态统一走 el-empty，避免每个页面重复写一遍
      4. 未声明的属性（border / stripe / size 等）通过 $attrs 透传给 el-table
  -->
  <div class="eb-table">
    <el-table
      ref="tableRef"
      v-loading="loading"
      v-bind="$attrs"
      :data="data"
      border
      stripe
      style="width: 100%"
    >
      <!-- 列定义：由使用方传入 -->
      <slot />

      <!-- 空状态统一兜底 -->
      <template #empty>
        <el-empty :description="emptyText" :image-size="80" />
      </template>
    </el-table>

    <!-- 分页器：总数大于 0 时才显示，避免空表下方挂一条无意义的分页 -->
    <div v-if="showPagination && total > 0" class="eb-table__pagination">
      <el-pagination
        :current-page="currentPage"
        :page-size="pageSize"
        :total="total"
        :layout="paginationLayout"
        background
        @current-change="handlePageChange"
      />
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { ElTable, ElPagination, ElEmpty } from 'element-plus'

// 关闭自动继承，改由 <el-table> 显式接收 $attrs
defineOptions({ inheritAttrs: false })

const props = defineProps({
  /** 表格数据 */
  data: { type: Array, default: () => [] },
  /** 加载中（透传给 v-loading） */
  loading: { type: Boolean, default: false },
  /** 总条数 */
  total: { type: Number, default: 0 },
  /** 当前页码（配合 v-model:current-page 使用） */
  currentPage: { type: Number, default: 1 },
  /** 每页条数 */
  pageSize: { type: Number, default: 10 },
  /** 是否显示分页器 */
  showPagination: { type: Boolean, default: true },
  /** 空状态文案 */
  emptyText: { type: String, default: '暂无数据' },
  /** 分页器布局 */
  paginationLayout: { type: String, default: 'total, prev, pager, next' }
})

const emit = defineEmits(['update:currentPage', 'page-change'])

const handlePageChange = (page) => {
  emit('update:currentPage', page)
  emit('page-change', page)
}

/**
 * 对外暴露 el-table 的常用实例方法。
 * 这样页面写 <EbTable ref="tableRef"> 后仍可调用 tableRef.value.clearSelection()，
 * 不必为了拿到底层实例而放弃使用本组件。
 */
const tableRef = ref(null)

defineExpose({
  /** 清空所有选中行（多选表格用） */
  clearSelection: () => tableRef.value && tableRef.value.clearSelection(),
  /** 全选 / 取消全选 */
  toggleAllSelection: () => tableRef.value && tableRef.value.toggleAllSelection(),
  /** 切换某一行 / 多行的选中状态 */
  toggleRowSelection: (row, selected) =>
    tableRef.value && tableRef.value.toggleRowSelection(row, selected),
  /** 原始 el-table 组件实例，特殊场景可直接取用 */
  getTableRef: () => tableRef.value
})
</script>

<style scoped>
.eb-table__pagination {
  display: flex;
  justify-content: flex-end;
  margin-top: 16px;
}
</style>
