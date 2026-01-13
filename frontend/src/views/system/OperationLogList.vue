<template>
  <div class="operation-log-list">
    <!-- 页面头部 -->
    <div class="page-header">
      <div class="header-left">
        <h2 class="page-title">操作日志</h2>
      </div>
      <div class="header-right">
        <el-button :icon="Refresh" @click="handleRefresh">刷新</el-button>
      </div>
    </div>

    <!-- 搜索筛选卡片 -->
    <el-card class="search-card">
      <el-form :inline="true" :model="searchForm">
        <el-form-item label="操作人">
          <el-input v-model="searchForm.username" placeholder="请输入操作人用户名" clearable style="width: 200px" />
        </el-form-item>
        <el-form-item label="操作模块">
          <el-select v-model="searchForm.module" clearable placeholder="请选择模块" style="width: 150px">
            <el-option label="用户管理" value="USER" />
            <el-option label="角色管理" value="ROLE" />
            <el-option label="权限管理" value="PERMISSION" />
            <el-option label="项目管理" value="PROJECT" />
            <el-option label="任务管理" value="TASK" />
            <el-option label="工时管理" value="WORK_HOUR" />
            <el-option label="部门管理" value="DEPARTMENT" />
            <el-option label="迭代管理" value="ITERATION" />
            <el-option label="系统管理" value="SYSTEM" />
          </el-select>
        </el-form-item>
        <el-form-item label="操作类型">
          <el-select v-model="searchForm.operationType" clearable placeholder="请选择类型" style="width: 150px">
            <el-option label="创建" value="CREATE" />
            <el-option label="更新" value="UPDATE" />
            <el-option label="删除" value="DELETE" />
            <el-option label="分配" value="ASSIGN" />
            <el-option label="移除" value="REMOVE" />
            <el-option label="登录" value="LOGIN" />
            <el-option label="登出" value="LOGOUT" />
            <el-option label="查询" value="QUERY" />
          </el-select>
        </el-form-item>
        <el-form-item label="操作状态">
          <el-select v-model="searchForm.status" clearable placeholder="请选择状态" style="width: 120px">
            <el-option label="成功" value="SUCCESS" />
            <el-option label="失败" value="FAILED" />
          </el-select>
        </el-form-item>
        <el-form-item label="时间范围">
          <el-date-picker
            v-model="dateRange"
            type="datetimerange"
            range-separator="至"
            start-placeholder="开始时间"
            end-placeholder="结束时间"
            format="YYYY-MM-DD HH:mm:ss"
            value-format="YYYY-MM-DD HH:mm:ss"
            style="width: 400px"
          />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">搜索</el-button>
          <el-button @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 操作日志表格卡片 -->
    <el-card class="table-card">
      <el-table :data="list" stripe v-loading="loading" border>
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="username" label="操作人" width="120" />
        <el-table-column prop="module" label="操作模块" width="120">
          <template #default="{ row }">
            <el-tag :type="getModuleTagType(row.module)" size="small">
              {{ getModuleText(row.module) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="operationType" label="操作类型" width="100">
          <template #default="{ row }">
            <el-tag :type="getOperationTypeTagType(row.operationType)" size="small">
              {{ getOperationTypeText(row.operationType) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="operationContent" label="操作内容" min-width="250" show-overflow-tooltip />
        <el-table-column prop="ipAddress" label="IP地址" width="140" />
        <el-table-column prop="status" label="状态" width="80">
          <template #default="{ row }">
            <el-tag :type="row.status === 'SUCCESS' ? 'success' : 'danger'" size="small">
              {{ row.status === 'SUCCESS' ? '成功' : '失败' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="operationTime" label="操作时间" width="180" />
        <el-table-column label="操作" width="100" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" size="small" @click="handleViewDetail(row)">
              详情
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页 -->
      <div class="pagination-container">
        <el-pagination
          v-model:current-page="searchForm.pageNum"
          v-model:page-size="searchForm.pageSize"
          :total="total"
          :page-sizes="[10, 20, 50, 100]"
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="fetchData"
          @current-change="fetchData"
        />
      </div>
    </el-card>

    <!-- 详情对话框 -->
    <el-dialog v-model="detailDialogVisible" title="操作日志详情" width="800px">
      <el-descriptions :column="1" border>
        <el-descriptions-item label="日志ID">{{ currentLog?.id }}</el-descriptions-item>
        <el-descriptions-item label="操作人">{{ currentLog?.username }}</el-descriptions-item>
        <el-descriptions-item label="操作模块">
          <el-tag :type="getModuleTagType(currentLog?.module)" size="small">
            {{ getModuleText(currentLog?.module) }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="操作类型">
          <el-tag :type="getOperationTypeTagType(currentLog?.operationType)" size="small">
            {{ getOperationTypeText(currentLog?.operationType) }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="业务类型" v-if="currentLog?.businessType">
          {{ currentLog.businessType }}
        </el-descriptions-item>
        <el-descriptions-item label="业务ID" v-if="currentLog?.businessId">
          {{ currentLog.businessId }}
        </el-descriptions-item>
        <el-descriptions-item label="操作内容">{{ currentLog?.operationContent }}</el-descriptions-item>
        <el-descriptions-item label="IP地址">{{ currentLog?.ipAddress }}</el-descriptions-item>
        <el-descriptions-item label="操作状态">
          <el-tag :type="currentLog?.status === 'SUCCESS' ? 'success' : 'danger'" size="small">
            {{ currentLog?.status === 'SUCCESS' ? '成功' : '失败' }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item v-if="currentLog?.errorMessage" label="错误信息">
          <span style="color: #f56c6c">{{ currentLog.errorMessage }}</span>
        </el-descriptions-item>
        <el-descriptions-item label="操作时间">{{ currentLog?.operationTime }}</el-descriptions-item>
      </el-descriptions>

      <!-- 数据变更对比 -->
      <div v-if="hasDataChanges" class="change-comparison-section">
        <el-divider content-position="left">
          <span style="font-weight: 500;">数据变更追踪</span>
        </el-divider>

        <!-- Git Diff 风格差异对比 -->
        <ChangeDiffViewer
          :old-value="currentLog?.oldValue"
          :new-value="currentLog?.newValue"
        />

        <el-divider content-position="left">
          <span style="font-weight: 500;">原始 JSON 数据</span>
        </el-divider>

        <el-collapse>
          <!-- 变更前数据 -->
          <el-collapse-item v-if="currentLog?.oldValue" :name="'old'">
            <template #title>
              <span style="display: flex; align-items: center;">
                <el-tag size="small" style="margin-right: 8px;" type="warning">变更前</el-tag>
                <span>old_value JSON</span>
              </span>
            </template>
            <pre class="json-viewer">{{ formatJson(currentLog.oldValue) }}</pre>
          </el-collapse-item>

          <!-- 变更后数据 -->
          <el-collapse-item v-if="currentLog?.newValue" :name="'new'">
            <template #title>
              <span style="display: flex; align-items: center;">
                <el-tag size="small" style="margin-right: 8px;" type="success">变更后</el-tag>
                <span>new_value JSON</span>
              </span>
            </template>
            <pre class="json-viewer">{{ formatJson(currentLog.newValue) }}</pre>
          </el-collapse-item>
        </el-collapse>
      </div>

      <!-- 提示信息 -->
      <el-alert
        v-if="!hasDataChanges && currentLog"
        title="此操作日志不包含数据变更追踪信息"
        type="info"
        :closable="false"
        style="margin-top: 20px;"
      />
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, computed } from 'vue'
import { ElMessage } from 'element-plus'
import { Refresh } from '@element-plus/icons-vue'
import { getOperationLogList, type OperationLogInfo, type OperationLogQuery } from '@/api/operationLog'
import ChangeDiffViewer from '@/components/ChangeDiffViewer.vue'

const list = ref<OperationLogInfo[]>([])
const total = ref(0)
const loading = ref(false)
const dateRange = ref<[string, string]>([])
const detailDialogVisible = ref(false)
const currentLog = ref<OperationLogInfo | null>(null)

// 计算是否有数据变更
const hasDataChanges = computed(() => {
  return currentLog.value && (currentLog.value.oldValue || currentLog.value.newValue)
})

// 搜索表单
const searchForm = reactive<OperationLogQuery>({
  username: '',
  module: undefined,
  operationType: undefined,
  status: undefined,
  startTime: '',
  endTime: '',
  pageNum: 1,
  pageSize: 20
})

// 生命周期
onMounted(() => {
  fetchData()
})

// 获取数据
const fetchData = async () => {
  loading.value = true
  try {
    // 处理日期范围
    if (dateRange.value && dateRange.value.length === 2) {
      searchForm.startTime = dateRange.value[0]
      searchForm.endTime = dateRange.value[1]
    } else {
      searchForm.startTime = ''
      searchForm.endTime = ''
    }

    const res = await getOperationLogList(searchForm)
    list.value = res.list || []
    total.value = res.total || 0
  } catch (error) {
    console.error('获取操作日志失败:', error)
    ElMessage.error('获取操作日志失败')
  } finally {
    loading.value = false
  }
}

// 搜索
const handleSearch = () => {
  searchForm.pageNum = 1
  fetchData()
}

// 重置
const handleReset = () => {
  searchForm.username = ''
  searchForm.module = undefined
  searchForm.operationType = undefined
  searchForm.status = undefined
  dateRange.value = []
  searchForm.pageNum = 1
  fetchData()
}

// 刷新
const handleRefresh = () => {
  fetchData()
  ElMessage.success('刷新成功')
}

// 查看详情
const handleViewDetail = (row: OperationLogInfo) => {
  currentLog.value = row
  detailDialogVisible.value = true
}

// 获取模块文本
const getModuleText = (module?: string) => {
  const map: Record<string, string> = {
    'USER': '用户管理',
    'ROLE': '角色管理',
    'PERMISSION': '权限管理',
    'PROJECT': '项目管理',
    'TASK': '任务管理',
    'WORK_HOUR': '工时管理',
    'DEPARTMENT': '部门管理',
    'ITERATION': '迭代管理',
    'SYSTEM': '系统管理'
  }
  return map[module || ''] || module || ''
}

// 获取模块标签类型
const getModuleTagType = (module?: string) => {
  const map: Record<string, any> = {
    'USER': 'primary',
    'ROLE': 'success',
    'PERMISSION': 'warning',
    'PROJECT': 'info',
    'TASK': 'info',
    'WORK_HOUR': 'info',
    'DEPARTMENT': 'info',
    'ITERATION': 'info',
    'SYSTEM': 'danger'
  }
  return map[module || ''] || ''
}

// 获取操作类型文本
const getOperationTypeText = (type?: string) => {
  const map: Record<string, string> = {
    'CREATE': '创建',
    'UPDATE': '更新',
    'DELETE': '删除',
    'ASSIGN': '分配',
    'REMOVE': '移除',
    'LOGIN': '登录',
    'LOGOUT': '登出',
    'QUERY': '查询'
  }
  return map[type || ''] || type || ''
}

// 获取操作类型标签类型
const getOperationTypeTagType = (type?: string) => {
  const map: Record<string, any> = {
    'CREATE': 'success',
    'UPDATE': 'primary',
    'DELETE': 'danger',
    'ASSIGN': 'warning',
    'REMOVE': 'warning',
    'LOGIN': 'info',
    'LOGOUT': 'info',
    'QUERY': 'info'
  }
  return map[type || ''] || ''
}

// 格式化 JSON
const formatJson = (jsonStr?: string) => {
  if (!jsonStr) return ''
  try {
    const obj = JSON.parse(jsonStr)
    return JSON.stringify(obj, null, 2)
  } catch (e) {
    console.error('JSON 解析失败:', e)
    return jsonStr
  }
}
</script>

<style scoped>
.operation-log-list {
  min-height: calc(100vh - 160px);
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24px;
  padding: 20px;
  background: #fff;
  border-radius: 4px;
  box-shadow: 0 1px 2px rgba(0, 0, 0, 0.03);
}

.header-left {
  display: flex;
  align-items: center;
}

.page-title {
  margin: 0;
  font-size: 20px;
  font-weight: 500;
  color: #333;
}

.header-right {
  display: flex;
  align-items: center;
}

.search-card {
  margin-bottom: 16px;
}

.table-card {
  margin-bottom: 16px;
}

.pagination-container {
  display: flex;
  justify-content: flex-end;
  padding: 20px;
}

/* 数据变更对比区域 */
.change-comparison-section {
  margin-top: 20px;
}

.json-viewer {
  background: #f5f5f5;
  border: 1px solid #e0e0e0;
  border-radius: 4px;
  padding: 16px;
  overflow-x: auto;
  font-size: 13px;
  line-height: 1.5;
  color: #333;
  max-height: 400px;
  overflow-y: auto;
}
</style>
