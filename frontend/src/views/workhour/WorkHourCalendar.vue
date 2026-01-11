<template>
  <div class="work-hour-calendar">
    <!-- 页面头部 -->
    <div class="page-header">
      <h2 class="page-title">工时日历</h2>
    </div>

    <!-- 统计卡片 -->
    <el-row :gutter="16" class="stats-row">
      <el-col :span="6">
        <el-card class="stats-card">
          <div class="stats-content">
            <div class="stats-icon today">
              <el-icon><Clock /></el-icon>
            </div>
            <div class="stats-info">
              <div class="stats-label">今日工时</div>
              <div class="stats-value">{{ statistics.todayHours || 0 }} 小时</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="stats-card">
          <div class="stats-content">
            <div class="stats-icon week">
              <el-icon><Calendar /></el-icon>
            </div>
            <div class="stats-info">
              <div class="stats-label">本周工时</div>
              <div class="stats-value">{{ statistics.weekHours || 0 }} 小时</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="stats-card">
          <div class="stats-content">
            <div class="stats-icon month">
              <el-icon><List /></el-icon>
            </div>
            <div class="stats-info">
              <div class="stats-label">本月工时</div>
              <div class="stats-value">{{ statistics.monthHours || 0 }} 小时</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="stats-card">
          <div class="stats-content">
            <div class="stats-icon total">
              <el-icon><Folder /></el-icon>
            </div>
            <div class="stats-info">
              <div class="stats-label">总计工时</div>
              <div class="stats-value">{{ statistics.totalHours || 0 }} 小时</div>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 工具栏 -->
    <div class="calendar-toolbar">
      <div class="toolbar-left">
        <el-button-group>
          <el-button :icon="ArrowLeft" @click="handlePrevMonth">上个月</el-button>
          <el-button @click="handleToday">今天</el-button>
          <el-button :icon="ArrowRight" @click="handleNextMonth">下个月</el-button>
        </el-button-group>
        <div class="current-month">{{ currentMonthText }}</div>
      </div>
      <div class="toolbar-right">
        <el-button type="primary" :icon="Plus" @click="handleCreate">登记工时</el-button>
      </div>
    </div>

    <!-- 日历网格 -->
    <div class="month-view">
      <div class="weekdays">
        <div v-for="day in weekdays" :key="day" class="weekday">{{ day }}</div>
      </div>
      <div class="calendar-grid">
        <div
          v-for="cell in calendarCells"
          :key="cell.date"
          class="calendar-cell"
          :class="{
            'today': cell.isToday,
            'weekend': cell.isWeekend,
            'other-month': cell.isOtherMonth,
            'has-data': cell.hasData
          }"
          @dblclick="handleCellDblClick(cell)"
        >
          <div class="cell-header">
            <span class="date-number">{{ cell.dayNumber }}</span>
            <span v-if="cell.totalHours > 0" class="total-hours">
              <el-icon><Clock /></el-icon>
              {{ cell.totalHours }}h
            </span>
          </div>
          <div class="cell-body">
            <div v-if="cell.workHours.length === 0" class="empty-state">
              <el-icon><Plus /></el-icon>
            </div>
            <div v-else class="work-hours-list">
              <div
                v-for="wh in cell.workHours"
                :key="wh.id"
                class="work-hour-item"
              >
                <div class="wh-hours">{{ wh.hours }}h {{ wh.taskName || '' }}</div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- 创建/编辑对话框 -->
    <el-dialog
      v-model="dialogVisible"
      :title="dialogTitle"
      width="600px"
      :close-on-click-modal="false"
    >
      <el-form ref="formRef" :model="formData" :rules="formRules" label-width="100px">
        <el-form-item label="工作日期" prop="workDate">
          <el-date-picker
            v-model="formData.workDate"
            type="date"
            placeholder="选择日期"
            style="width: 100%"
            value-format="YYYY-MM-DD"
          />
        </el-form-item>
        <el-form-item label="项目" prop="projectId">
          <el-select v-model="formData.projectId" placeholder="请选择项目" filterable style="width: 100%" @change="handleProjectChange">
            <el-option
              v-for="project in projectList"
              :key="project.id"
              :label="project.name"
              :value="project.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="任务">
          <el-select v-model="formData.taskId" placeholder="请选择任务（可选）" clearable filterable style="width: 100%">
            <el-option
              v-for="task in taskList"
              :key="task.id"
              :label="task.title"
              :value="task.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="工时数" prop="hours">
          <el-input-number v-model="formData.hours" :min="0.5" :max="24" :step="0.5" :precision="1" style="width: 100%" />
        </el-form-item>
        <el-form-item label="工作内容" prop="content">
          <el-input
            v-model="formData.content"
            type="textarea"
            :rows="4"
            placeholder="请详细描述工作内容"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmit" :loading="submitLoading">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, watch, computed } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, type FormInstance, type FormRules } from 'element-plus'
import { Clock, Calendar, List, Folder, ArrowLeft, ArrowRight, Plus } from '@element-plus/icons-vue'
import { getMonthRange, getCurrentYearMonth, getMonthText, formatDate, isToday, isWeekend } from '@/utils/dateUtils'
import { getWorkHourList, createWorkHour, updateWorkHour, getUserWorkHourStatistics, type WorkHourInfo } from '@/api/workhour'
import { getProjectList } from '@/api/project'
import { getTaskList } from '@/api/task'

const router = useRouter()

// 当前年月
const { year: initYear, month: initMonth } = getCurrentYearMonth()
const currentYear = ref(initYear)
const currentMonth = ref(initMonth)

// 当前月份文本
const currentMonthText = computed(() => getMonthText(currentYear.value, currentMonth.value))

// 星期标题
const weekdays = ['周一', '周二', '周三', '周四', '周五', '周六', '周日']

// 加载状态
const loading = ref(false)

// 日历单元格接口
interface CalendarCell {
  date: string
  dayNumber: number
  isToday: boolean
  isWeekend: boolean
  isOtherMonth: boolean
  totalHours: number
  hasData: boolean
  workHours: WorkHourInfo[]
}

// 日历单元格
const calendarCells = computed<CalendarCell[]>(() => {
  const cells: CalendarCell[] = []
  const year = currentYear.value
  const month = currentMonth.value

  // 当月第一天和最后一天
  const firstDay = new Date(year, month - 1, 1)
  const lastDay = new Date(year, month, 0)

  // 获取第一天是周几（0-6，0是周日）
  let dayOfWeek = firstDay.getDay()
  // 转换为周一为1，周日为7
  dayOfWeek = dayOfWeek === 0 ? 7 : dayOfWeek

  // 填充上月剩余日期
  const prevMonthLastDay = new Date(year, month - 1, 0)
  for (let i = dayOfWeek - 1; i > 0; i--) {
    const date = new Date(prevMonthLastDay)
    date.setDate(prevMonthLastDay.getDate() - i + 1)
    const dateStr = formatDate(date)
    cells.push({
      date: dateStr,
      dayNumber: date.getDate(),
      isToday: isToday(dateStr),
      isWeekend: isWeekend(dateStr),
      isOtherMonth: true,
      totalHours: 0,
      hasData: false,
      workHours: []
    })
  }

  // 当月日期
  for (let day = 1; day <= lastDay.getDate(); day++) {
    const date = new Date(year, month - 1, day)
    const dateStr = formatDate(date)
    const dayData = monthData.value.get(dateStr)
    cells.push({
      date: dateStr,
      dayNumber: day,
      isToday: isToday(dateStr),
      isWeekend: isWeekend(dateStr),
      isOtherMonth: false,
      totalHours: dayData?.totalHours || 0,
      hasData: !!dayData?.workHours?.length,
      workHours: dayData?.workHours || []
    })
  }

  // 填充下月开始日期（补齐到42格）
  const remaining = 42 - cells.length
  for (let i = 1; i <= remaining; i++) {
    const date = new Date(year, month, i)
    const dateStr = formatDate(date)
    cells.push({
      date: dateStr,
      dayNumber: i,
      isToday: isToday(dateStr),
      isWeekend: isWeekend(dateStr),
      isOtherMonth: true,
      totalHours: 0,
      hasData: false,
      workHours: []
    })
  }

  return cells
})

// 月度数据（按日期聚合的 Map）
interface DayWorkHours {
  date: string
  totalHours: number
  workHours: WorkHourInfo[]
}
const monthData = ref<Map<string, DayWorkHours>>(new Map())

// 统计数据
const statistics = reactive({
  todayHours: 0,
  weekHours: 0,
  monthHours: 0,
  totalHours: 0
})

// 项目列表
const projectList = ref<any[]>([])

// 任务列表
const taskList = ref<any[]>([])

// 对话框
const dialogVisible = ref(false)
const dialogTitle = ref('')
const submitLoading = ref(false)
const formRef = ref<FormInstance>()
const formData = reactive({
  id: undefined as number | undefined,
  workDate: '',
  projectId: undefined as number | undefined,
  taskId: undefined as number | undefined,
  hours: 8,
  content: ''
})

const formRules: FormRules = {
  workDate: [{ required: true, message: '请选择工作日期', trigger: 'change' }],
  projectId: [{ required: true, message: '请选择项目', trigger: 'change' }],
  hours: [{ required: true, message: '请输入工时数', trigger: 'blur' }],
  content: [{ required: true, message: '请输入工作内容', trigger: 'blur' }]
}

// 获取月度数据
const fetchMonthData = async () => {
  loading.value = true
  try {
    const { start, end } = getMonthRange(currentYear.value, currentMonth.value)
    const res = await getWorkHourList({
      startDate: start,
      endDate: end,
      pageNum: 1,
      pageSize: 1000
    })

    // 聚合数据按日期
    const map = new Map<string, DayWorkHours>()
    const workHours = res?.list || []

    workHours.forEach(wh => {
      if (!map.has(wh.workDate)) {
        map.set(wh.workDate, {
          date: wh.workDate,
          totalHours: 0,
          workHours: []
        })
      }
      const day = map.get(wh.workDate)!
      day.totalHours += wh.hours
      day.workHours.push(wh)
    })

    monthData.value = map

    // 更新统计数据
    updateStatistics(workHours)
  } catch (error) {
    console.error('获取工时数据失败:', error)
    ElMessage.error('获取工时数据失败')
  } finally {
    loading.value = false
  }
}

// 更新统计数据
const updateStatistics = async (allWorkHours: WorkHourInfo[]) => {
  try {
    const today = new Date()
    const todayStr = today.toISOString().split('T')[0]

    // 计算今日工时
    const todayData = allWorkHours.filter(wh => wh.workDate === todayStr)
    statistics.todayHours = todayData.reduce((sum, wh) => sum + wh.hours, 0)

    // 计算本周工时
    const weekStart = new Date(today)
    weekStart.setDate(today.getDate() - today.getDay() + 1) // 周一
    const weekStartStr = weekStart.toISOString().split('T')[0]
    const weekData = allWorkHours.filter(wh => wh.workDate >= weekStartStr && wh.workDate <= todayStr)
    statistics.weekHours = weekData.reduce((sum, wh) => sum + wh.hours, 0)

    // 计算本月工时
    const monthStart = new Date(today.getFullYear(), today.getMonth(), 1)
    const monthStartStr = monthStart.toISOString().split('T')[0]
    const monthData_filtered = allWorkHours.filter(wh => wh.workDate >= monthStartStr && wh.workDate <= todayStr)
    statistics.monthHours = monthData_filtered.reduce((sum, wh) => sum + wh.hours, 0)

    // 获取总工时
    const totalRes = await getUserWorkHourStatistics(1)
    statistics.totalHours = totalRes?.totalHours || 0
  } catch (error) {
    console.error('更新统计数据失败:', error)
  }
}

// 获取项目列表
const fetchProjects = async () => {
  try {
    const res = await getProjectList({
      pageNum: 1,
      pageSize: 1000
    })
    projectList.value = res?.list || []
  } catch (error) {
    console.error('获取项目列表失败:', error)
  }
}

// 获取任务列表
const fetchTasks = async () => {
  if (!formData.projectId) {
    taskList.value = []
    return
  }
  try {
    const res = await getTaskList({
      projectId: formData.projectId,
      pageNum: 1,
      pageSize: 1000
    })
    taskList.value = res?.list || []
  } catch (error) {
    console.error('获取任务列表失败:', error)
  }
}

// 项目变化时加载任务
const handleProjectChange = () => {
  formData.taskId = undefined
  fetchTasks()
}

// 回到今天
const handleToday = () => {
  const { year, month } = getCurrentYearMonth()
  currentYear.value = year
  currentMonth.value = month
}

// 上个月
const handlePrevMonth = () => {
  if (currentMonth.value === 1) {
    currentYear.value--
    currentMonth.value = 12
  } else {
    currentMonth.value--
  }
}

// 下个月
const handleNextMonth = () => {
  if (currentMonth.value === 12) {
    currentYear.value++
    currentMonth.value = 1
  } else {
    currentMonth.value++
  }
}

// 单元格双击
const handleCellDblClick = (cell: CalendarCell) => {
  if (cell.isOtherMonth) return

  if (cell.workHours.length === 0) {
    handleCreate(cell.date)
  } else if (cell.workHours.length === 1) {
    handleEdit(cell.workHours[0])
  } else {
    // TODO: 显示选择对话框
    handleEdit(cell.workHours[0])
  }
}

// 新增
const handleCreate = (date?: string) => {
  dialogTitle.value = '登记工时'
  formData.id = undefined
  formData.workDate = date || new Date().toISOString().split('T')[0]
  formData.projectId = undefined
  formData.taskId = undefined
  formData.hours = 8
  formData.content = ''
  taskList.value = []
  dialogVisible.value = true
}

// 编辑
const handleEdit = (workHour: WorkHourInfo) => {
  dialogTitle.value = '编辑工时'
  formData.id = workHour.id
  formData.workDate = workHour.workDate
  formData.projectId = workHour.projectId
  formData.taskId = workHour.taskId
  formData.hours = workHour.hours
  formData.content = workHour.content
  fetchTasks()
  dialogVisible.value = true
}

// 提交
const handleSubmit = async () => {
  if (!formRef.value) return

  await formRef.value.validate(async (valid) => {
    if (valid) {
      submitLoading.value = true
      try {
        if (formData.id) {
          await updateWorkHour({
            id: formData.id,
            projectId: formData.projectId!,
            taskId: formData.taskId,
            workDate: formData.workDate,
            hours: formData.hours,
            content: formData.content
          })
          ElMessage.success('更新成功')
        } else {
          await createWorkHour({
            projectId: formData.projectId!,
            taskId: formData.taskId,
            workDate: formData.workDate,
            hours: formData.hours,
            content: formData.content
          })
          ElMessage.success('创建成功')
        }
        dialogVisible.value = false
        fetchMonthData()
      } catch (error) {
        console.error('操作失败:', error)
      } finally {
        submitLoading.value = false
      }
    }
  })
}

// 监听年月变化
watch([currentYear, currentMonth], () => {
  fetchMonthData()
})

onMounted(() => {
  fetchProjects()
  fetchMonthData()
})
</script>

<style scoped>
.work-hour-calendar {
  padding: 24px;
}

.page-header {
  margin-bottom: 24px;
  padding: 20px;
  background: #fff;
  border-radius: 4px;
  box-shadow: 0 1px 2px rgba(0, 0, 0, 0.03);
}

.page-title {
  margin: 0;
  font-size: 20px;
  font-weight: 500;
  color: #333;
}

.stats-row {
  margin-bottom: 16px;
}

.stats-card {
  border-radius: 8px;
  overflow: hidden;
}

.stats-content {
  display: flex;
  align-items: center;
  gap: 16px;
}

.stats-icon {
  width: 60px;
  height: 60px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 28px;
}

.stats-icon.today {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: #fff;
}

.stats-icon.week {
  background: linear-gradient(135deg, #f093fb 0%, #f5576c 100%);
  color: #fff;
}

.stats-icon.month {
  background: linear-gradient(135deg, #4facfe 0%, #00f2fe 100%);
  color: #fff;
}

.stats-icon.total {
  background: linear-gradient(135deg, #43e97b 0%, #38f9d7 100%);
  color: #fff;
}

.stats-info {
  flex: 1;
}

.stats-label {
  font-size: 14px;
  color: #666;
  margin-bottom: 8px;
}

.stats-value {
  font-size: 24px;
  font-weight: bold;
  color: #333;
}

.calendar-toolbar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
  padding: 16px;
  background: #fff;
  border-radius: 4px;
  box-shadow: 0 1px 2px rgba(0, 0, 0, 0.03);
}

.toolbar-left {
  display: flex;
  align-items: center;
  gap: 16px;
}

.toolbar-right {
  display: flex;
  align-items: center;
  gap: 12px;
}

.current-month {
  font-size: 16px;
  font-weight: 500;
  color: #333;
  min-width: 100px;
}

.month-view {
  background: #fff;
  padding: 16px;
  border-radius: 4px;
  box-shadow: 0 1px 2px rgba(0, 0, 0, 0.03);
}

.weekdays {
  display: grid;
  grid-template-columns: repeat(7, 1fr);
  gap: 1px;
  margin-bottom: 8px;
}

.weekday {
  text-align: center;
  padding: 12px;
  font-size: 14px;
  font-weight: 500;
  color: #666;
  background: #f5f5f5;
  border-radius: 4px;
}

.calendar-grid {
  display: grid;
  grid-template-columns: repeat(7, 1fr);
  gap: 8px;
}

.calendar-cell {
  min-height: 120px;
  padding: 8px;
  background: #fff;
  border: 1px solid #e8e8e8;
  border-radius: 4px;
  cursor: pointer;
  transition: all 0.2s;
}

.calendar-cell:hover {
  box-shadow: 0 0 0 2px #1890ff inset;
}

.calendar-cell.today {
  background: #e6f7ff;
}

.calendar-cell.weekend {
  background: #fafafa;
}

.calendar-cell.other-month {
  opacity: 0.3;
}

.calendar-cell.has-data {
  background: #f6ffed;
}

.cell-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 8px;
}

.date-number {
  font-size: 16px;
  font-weight: 500;
  color: #333;
}

.total-hours {
  display: flex;
  align-items: center;
  gap: 4px;
  font-size: 12px;
  color: #1890ff;
  font-weight: 500;
}

.cell-body {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.empty-state {
  display: flex;
  align-items: center;
  justify-content: center;
  height: 60px;
  color: #d9d9d9;
  font-size: 24px;
}

.work-hours-list {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.work-hour-item {
  padding: 4px 6px;
  background: #f5f5f5;
  border-radius: 3px;
  font-size: 12px;
}

.wh-project {
  color: #333;
  font-weight: 500;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.wh-hours {
  color: #666;
  font-weight: 500;
}
</style>
