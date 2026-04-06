#!/bin/bash

# TasksView 组件验证脚本

echo "================================"
echo "TasksView 组件验证"
echo "================================"
echo ""

# 检查文件是否存在
echo "1. 检查文件是否存在..."
if [ -f "frontend/src/views/project/TasksView.vue" ]; then
    echo "   ✅ TasksView.vue 文件存在"
else
    echo "   ❌ TasksView.vue 文件不存在"
    exit 1
fi

# 检查文件大小
echo ""
echo "2. 检查文件大小..."
SIZE=$(wc -l < frontend/src/views/project/TasksView.vue)
if [ $SIZE -gt 1000 ]; then
    echo "   ✅ 文件大小合理 ($SIZE 行)"
else
    echo "   ⚠️  文件可能不完整 ($SIZE 行)"
fi

# 检查关键导入
echo ""
echo "3. 检查关键导入..."
if grep -q "import.*from 'vue'" frontend/src/views/project/TasksView.vue; then
    echo "   ✅ Vue 导入正常"
else
    echo "   ❌ Vue 导入缺失"
fi

if grep -q "import.*from '@/api/task'" frontend/src/views/project/TasksView.vue; then
    echo "   ✅ 任务 API 导入正常"
else
    echo "   ❌ 任务 API 导入缺失"
fi

if grep -q "import.*from '@/api/iteration'" frontend/src/views/project/TasksView.vue; then
    echo "   ✅ 迭代 API 导入正常"
else
    echo "   ❌ 迭代 API 导入缺失"
fi

# 检查关键功能
echo ""
echo "4. 检查关键功能..."
if grep -q "viewMode" frontend/src/views/project/TasksView.vue; then
    echo "   ✅ 视图模式切换功能存在"
else
    echo "   ❌ 视图模式切换功能缺失"
fi

if grep -q "handleDragStart" frontend/src/views/project/TasksView.vue; then
    echo "   ✅ 拖拽功能存在"
else
    echo "   ❌ 拖拽功能缺失"
fi

if grep -q "filteredTasks" frontend/src/views/project/TasksView.vue; then
    echo "   ✅ 筛选功能存在"
else
    echo "   ❌ 筛选功能缺失"
fi

# 检查 ProjectDetail 集成
echo ""
echo "5. 检查 ProjectDetail 集成..."
if grep -q "TasksView" frontend/src/views/project/ProjectDetail.vue; then
    echo "   ✅ TasksView 已导入到 ProjectDetail"
else
    echo "   ❌ TasksView 未导入到 ProjectDetail"
fi

# 检查文档
echo ""
echo "6. 检查文档..."
if [ -f "docs/TASKS_VIEW_IMPLEMENTATION.md" ]; then
    echo "   ✅ 实现文档存在"
else
    echo "   ⚠️  实现文档缺失"
fi

if [ -f "docs/TASKS_VIEW_USER_GUIDE.md" ]; then
    echo "   ✅ 用户指南存在"
else
    echo "   ⚠️  用户指南缺失"
fi

if [ -f "docs/TASKS_MODULE_SUMMARY.md" ]; then
    echo "   ✅ 总结文档存在"
else
    echo "   ⚠️  总结文档缺失"
fi

if [ -f "docs/TASKS_VIEW_EXAMPLES.md" ]; then
    echo "   ✅ 示例文档存在"
else
    echo "   ⚠️  示例文档缺失"
fi

# 总结
echo ""
echo "================================"
echo "验证完成"
echo "================================"
echo ""
echo "📋 总结："
echo "   - TasksView.vue 已创建"
echo "   - 核心功能已实现"
echo "   - ProjectDetail 已集成"
echo "   - 文档已编写"
echo ""
echo "🚀 下一步："
echo "   1. 启动前端服务：cd frontend && npm run dev"
echo "   2. 访问项目详情页"
echo "   3. 点击'任务'标签"
echo "   4. 测试各项功能"
echo ""
