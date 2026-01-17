<template>
  <div class="project-gantt">
    <!-- 工具栏 -->
    <div class="gantt-toolbar">
      <div class="toolbar-left">
        <el-radio-group v-model="viewMode" @change="handleViewModeChange">
          <el-radio-button value="day">日视图</el-radio-button>
          <el-radio-button value="week">周视图</el-radio-button>
          <el-radio-button value="month">月视图</el-radio-button>
        </el-radio-group>
      </div>
      <div class="toolbar-right">
        <el-button @click="handleRefresh" :icon="Refresh">刷新</el-button>
        <el-button @click="handleExport" :icon="Download">导出</el-button>
      </div>
    </div>

    <!-- 甘特图容器 -->
    <div ref="ganttContainer" class="gantt-container"></div>

    <!-- 加载状态 -->
    <div v-if="loading" class="gantt-loading">
      <el-icon class="is-loading"><Loading /></el-icon>
      <span>加载中...</span>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, onBeforeUnmount, computed } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Refresh, Download, Loading } from '@element-plus/icons-vue'
import { gantt } from 'dhtmlx-gantt'
import 'dhtmlx-gantt/codebase/dhtmlxgantt.css'
import { getProjectGanttData, updateTaskDates, updateTaskParent, type GanttTask } from '@/api/gantt'

interface Props {
  projectId: number
}

const props = defineProps<Props>()
const router = useRouter()

// 视图模式
const viewMode = ref('day')
const loading = ref(false)
const ganttContainer = ref<HTMLElement>()

// 获取类型图标
const getTypeIcon = (type: string): string => {
  const icons: Record<string, string> = {
    'project': '📊',
    'iteration': '🔄',
    'task': '📋'
  }
  return icons[type] || '📋'
}

// 获取类型颜色
const getTypeColor = (task: any): string => {
  if (task.type === 'project') return '#3498db' // 蓝色
  if (task.type === 'iteration') return '#9b59b6' // 紫色
  if (task.type === 'task') {
    // 任务根据优先级显示颜色
    const priorityColors: Record<string, string> = {
      'HIGH': '#e74c3c',   // 红色
      'MEDIUM': '#f39c12', // 橙色
      'LOW': '#2ecc71'     // 绿色
    }
    return priorityColors[task.priority] || '#2ecc71'
  }
  return '#2ecc71'
}

// 初始化甘特图
const initGantt = () => {
  // 设置中文语言包
  gantt.i18n.setLocale('cn')

  // 配置日期格式
  gantt.config.date_format = '%Y-%m-%d'
  gantt.config.xml_date = '%Y-%m-%d'

  // 启用树形结构，默认展开
  gantt.config.open_tree_initially = true
  gantt.config.order_branch = true
  gantt.config.order_branch_free = true
  gantt.config.show_grid = true
  gantt.config.show_links = false

  // 配置左侧列（类似 MS Project）
  gantt.config.columns = [
    {
      name: 'text',
      label: '任务名称',
      tree: true,
      width: 280,
      resize: true,
      template: (obj: any) => {
        // 所有类型都不显示图标，只显示文本
        return obj.text || ''
      }
    },
    { name: 'start_date', label: '开始日期', align: 'center', width: 90 },
    { name: 'duration', label: '工期(天)', align: 'center', width: 80 },
    { name: 'owner', label: '负责人', align: 'center', width: 90, template: (obj: any) => obj.owner || '-' },
    { name: 'status', label: '状态', align: 'center', width: 70, template: (obj: any) => obj.status || '-' }
  ]

  // 启用拖拽编辑
  gantt.config.drag_links = false // 暂不启用依赖关系连线
  gantt.config.drag_progress = false
  gantt.config.drag_resize = true
  gantt.config.drag_move = true

  // 配置时间刻度（中文格式）
  gantt.config.scale_unit = 'day'
  gantt.config.date_scale = '%Y年%m月%d日'
  gantt.config.subscales = [
    { unit: 'day', step: 1, date: '%M%d日 周%D' }
  ]
  gantt.config.scale_height = 50

  // 配置任务条模板（右侧时间轴只显示进度条，不显示文字）
  gantt.templates.task_class = (start: Date, end: Date, task: any) => {
    const type = task.type || 'task'
    return `gantt_task_${type}`
  }

  // 右侧时间轴任务条不显示文字（已在左侧显示）
  gantt.templates.task_text = (start: Date, end: Date, task: any) => {
    return ''
  }

  gantt.templates.grid_row_class = (start: Date, end: Date, task: any) => {
    const type = task.type || 'task'
    return `gantt_row_${type}`
  }

  gantt.templates.task_row_class = (start: Date, end: Date, task: any) => {
    const type = task.type || 'task'
    return `gantt_row_${type}`
  }

  // 监听拖拽事件 - 更新任务时间
  gantt.attachEvent('onAfterTaskDrag', async (id: string, mode: string, e: Event) => {
    const task = gantt.getTask(id)
    const startDate = formatDate(task.start_date)
    const endDate = formatDate(task.end_date)
    const duration = task.duration

    // 计算结束日期（dhtmlx-gantt的end_date是任务最后一天的下一天）
    const actualEndDate = new Date(task.end_date)
    actualEndDate.setDate(actualEndDate.getDate() - 1)
    const formattedEndDate = formatDate(actualEndDate)

    try {
      await updateTaskDates(Number(id), {
        planStartDate: startDate,
        planEndDate: formattedEndDate
      })
      ElMessage.success('任务时间已更新')
    } catch (error: any) {
      ElMessage.error(error.message || '更新失败')
      // 恢复原位置
      loadGanttData()
    }
  })

  // 监听任务拖拽到新父节点
  gantt.attachEvent('onAfterTaskMove', async (id: string, parent: string, oldParent: string) => {
    if (parent === oldParent) return

    try {
      await updateTaskParent(Number(id), {
        parentId: parent === '0' || parent === null ? null : Number(parent)
      })
      ElMessage.success('任务层级已更新')
      loadGanttData()
    } catch (error: any) {
      ElMessage.error(error.message || '更新失败')
      loadGanttData()
    }
  })

  // 监听任务双击事件 - 使用 businessId 跳转（双击避免与拖拽冲突）
  gantt.attachEvent('onTaskDblClick', (id: string) => {
    const task = gantt.getTask(id)
    const businessId = task.businessId

    if (task.type === 'task') {
      router.push(`/tasks/${businessId}`)
    } else if (task.type === 'iteration') {
      router.push(`/iterations/${businessId}`)
    } else if (task.type === 'project') {
      router.push(`/projects/${businessId}`)
    }
    return true
  })

  // 初始化
  if (ganttContainer.value) {
    gantt.init(ganttContainer.value)
  }
}

// 加载甘特图数据
const loadGanttData = async () => {
  loading.value = true
  try {
    const data = await getProjectGanttData(props.projectId)

    // 转换任务数据格式 - 扁平化嵌套结构
    const ganttTasks: any[] = []
    const flattenTasks = (tasks: GanttTask[], parentId: number | null = null) => {
      tasks.forEach((task: GanttTask) => {
        const ganttTask = {
          id: task.ganttId,  // 使用 ganttId 给 dhtmlx-gantt 渲染
          businessId: task.id,  // 保留原始 id 用于显示和跳转
          text: task.text,
          type: task.type || 'task',
          start_date: task.startDate ? new Date(task.startDate) : null,
          duration: task.duration || 1,
          progress: task.progress || 0,
          parent: parentId || 0, // 使用父节点的 ganttId
          owner: task.owner,
          ownerId: task.ownerId,
          status: task.status,
          priority: task.priority,
          color: getTypeColor(task),
          $virtual: false
        }
        ganttTasks.push(ganttTask)

        // 递归处理子任务，传递 ganttId 作为父 ID
        if (task.subtasks && task.subtasks.length > 0) {
          flattenTasks(task.subtasks, task.ganttId)
        }
      })
    }

    flattenTasks(data.data || [])

    const ganttLinks = (data.links || []).map((link: any) => ({
      id: link.id,
      source: link.source,
      target: link.target,
      type: link.type || '0'
    }))

    // 解析数据
    gantt.clearAll()
    gantt.parse({ data: ganttTasks, links: ganttLinks })

    // 自动调整时间范围
    adjustTimeScale()
  } catch (error: any) {
    ElMessage.error(error.message || '加载甘特图数据失败')
  } finally {
    loading.value = false
  }
}

// 格式化日期
const formatDate = (date: Date | string): string => {
  const d = typeof date === 'string' ? new Date(date) : date
  const year = d.getFullYear()
  const month = String(d.getMonth() + 1).padStart(2, '0')
  const day = String(d.getDate()).padStart(2, '0')
  return `${year}-${month}-${day}`
}

// 调整时间刻度
const adjustTimeScale = () => {
  const task = gantt.getTaskByTime()
  if (!task || task.length === 0) return

  // 根据任务时间范围自动调整刻度
  const dates = gantt.getSubtaskDates()
  const minDate = new Date(dates.start_date)
  const maxDate = new Date(dates.end_date)

  // 添加前后缓冲
  minDate.setDate(minDate.getDate() - 7)
  maxDate.setDate(maxDate.getDate() + 7)

  gantt.config.start_date = minDate
  gantt.config.end_date = maxDate
  gantt.render()
}

// 视图模式切换
const handleViewModeChange = () => {
  switch (viewMode.value) {
    case 'day':
      gantt.config.scale_unit = 'day'
      gantt.config.date_scale = '%Y年%m月%d日'
      gantt.config.subscales = [
        { unit: 'day', step: 1, date: '%M%d日 周%D' }
      ]
      gantt.config.scale_height = 50
      break
    case 'week':
      gantt.config.scale_unit = 'week'
      gantt.config.date_scale = '%Y年 第%W周'
      gantt.config.subscales = [
        { unit: 'day', step: 1, date: '%m/%d 周%D' }
      ]
      gantt.config.scale_height = 50
      break
    case 'month':
      gantt.config.scale_unit = 'month'
      gantt.config.date_scale = '%Y年 %m月'
      gantt.config.subscales = [
        { unit: 'week', step: 1, date: '第%W周' },
        { unit: 'day', step: 1, date: '%d日' }
      ]
      gantt.config.scale_height = 50
      break
  }
  gantt.render()
}

// 刷新
const handleRefresh = () => {
  loadGanttData()
}

// 导出（导出为图片）
const handleExport = () => {
  // 生成 PNG 图片
  gantt.exportToPNG({
    name: `gantt-${props.projectId}-${Date.now()}.png`,
    header: `<style>
      .gantt_task_line { border: 1px solid #000; }
    </style>`
  })
}

// 生命周期
onMounted(() => {
  initGantt()
  loadGanttData()
})

onBeforeUnmount(() => {
  // 清理甘特图实例
  if (ganttContainer.value) {
    gantt.clearAll()
  }
})
</script>

<style scoped>
.project-gantt {
  width: 100%;
  height: 100%;
  display: flex;
  flex-direction: column;
}

.gantt-toolbar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12px 16px;
  background-color: var(--el-bg-color);
  border-bottom: 1px solid var(--el-border-color);
}

.toolbar-left,
.toolbar-right {
  display: flex;
  gap: 12px;
}

.gantt-container {
  flex: 1;
  width: 100%;
  min-height: 400px;
  position: relative;
}

.gantt-loading {
  position: absolute;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 8px;
  color: var(--el-text-color-secondary);
}

/* dhtmlx-gantt 自定义样式 */
:deep(.gantt_task_line) {
  border-radius: 4px;
}

/* 确保折叠/展开按钮可见 */
:deep(.gantt_tree_icon) {
  width: 20px;
  height: 20px;
  display: inline-block;
  vertical-align: middle;
  cursor: pointer;
  margin-right: 4px;
}

:deep(.gantt_tree_icon.gantt_open) {
  background-position: 0 0;
}

:deep(.gantt_tree_icon.gantt_closed) {
  background-position: 0 -20px;
}

/* 确保任务名称文本垂直居中对齐 */
:deep(.gantt_tree_content) {
  display: flex;
  align-items: center;
  line-height: 1;
}

:deep(.gantt_cell) {
  display: flex;
  align-items: center;
}

:deep(.gantt_task_project) {
  background-color: #3498db;
  border-color: #2980b9;
}

:deep(.gantt_task_iteration) {
  background-color: #9b59b6;
  border-color: #8e44ad;
}

:deep(.gantt_task_task) {
  background-color: #2ecc71;
  border-color: #27ae60;
}

:deep(.gantt_row_project) {
  background-color: #f8f9fa;
  font-weight: bold;
}

:deep(.gantt_row_iteration) {
  background-color: #f0f0f0;
}

:deep(.gantt_row_task) {
  background-color: #ffffff;
}

/* 高优先级任务 */
:deep(.gantt_task_priority_HIGH) {
  background-color: #e74c3c;
  border-color: #c0392b;
}
</style>
