import dayjs from 'dayjs'

/**
 * 格式化日期为 YYYY-MM-DD
 */
export const formatDate = (date: Date): string => {
  return dayjs(date).format('YYYY-MM-DD')
}

/**
 * 获取月份的开始和结束日期
 * @param year 年份
 * @param month 月份（1-12）
 * @returns 包含开始和结束日期的对象
 */
export const getMonthRange = (year: number, month: number) => {
  const start = dayjs().year(year).month(month - 1).date(1).format('YYYY-MM-DD')
  const end = dayjs().year(year).month(month).date(1).subtract(1, 'day').format('YYYY-MM-DD')
  return { start, end }
}

/**
 * 获取周的开始和结束日期
 * @param date 日期
 * @returns 包含开始和结束日期的对象
 */
export const getWeekRange = (date: Date | string) => {
  const d = dayjs(date)
  const start = d.startOf('week').format('YYYY-MM-DD')
  const end = d.endOf('week').format('YYYY-MM-DD')
  return { start, end }
}

/**
 * 判断是否是今天
 * @param date 日期字符串（YYYY-MM-DD）
 * @returns 是否是今天
 */
export const isToday = (date: string): boolean => {
  return date === dayjs().format('YYYY-MM-DD')
}

/**
 * 判断是否是周末
 * @param date 日期字符串（YYYY-MM-DD）
 * @returns 是否是周末
 */
export const isWeekend = (date: string): boolean => {
  const day = dayjs(date).day()
  return day === 0 || day === 6
}

/**
 * 获取月份的文本描述
 * @param year 年份
 * @param month 月份（1-12）
 * @returns 月份文本（如：2024年1月）
 */
export const getMonthText = (year: number, month: number): string => {
  return `${year}年${month}月`
}

/**
 * 获取当前日期
 * @returns 当前日期字符串（YYYY-MM-DD）
 */
export const getCurrentDate = (): string => {
  return dayjs().format('YYYY-MM-DD')
}

/**
 * 获取当前年月
 * @returns 包含当前年份和月份的对象
 */
export const getCurrentYearMonth = (): { year: number; month: number } => {
  const now = dayjs()
  return {
    year: now.year(),
    month: now.month() + 1 // dayjs的month()返回0-11，需要+1
  }
}

/**
 * 切换到上个月
 * @param year 当前年份
 * @param month 当前月份（1-12）
 * @returns 上个月的年份和月份
 */
export const getPrevMonth = (year: number, month: number): { year: number; month: number } => {
  const date = dayjs().year(year).month(month - 1).date(1).subtract(1, 'month')
  return {
    year: date.year(),
    month: date.month() + 1
  }
}

/**
 * 切换到下个月
 * @param year 当前年份
 * @param month 当前月份（1-12）
 * @returns 下个月的年份和月份
 */
export const getNextMonth = (year: number, month: number): { year: number; month: number } => {
  const date = dayjs().year(year).month(month - 1).date(1).add(1, 'month')
  return {
    year: date.year(),
    month: date.month() + 1
  }
}
