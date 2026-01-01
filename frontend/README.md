# GSMS 前端应用

基于 Vue 3 + TypeScript + Element Plus 的工时管理系统前端应用。

## 技术栈

- **框架**: Vue 3 (Composition API)
- **语言**: TypeScript
- **构建工具**: Vite
- **UI 组件库**: Element Plus
- **状态管理**: Pinia
- **路由**: Vue Router 4
- **HTTP 客户端**: Axios

## 快速开始

### 安装依赖

```bash
npm install
```

### 启动开发服务器

```bash
npm run dev
```

访问 http://localhost:3000

### 构建生产版本

```bash
npm run build
```

### 预览生产构建

```bash
npm run preview
```

## 项目结构

```
frontend/
├── src/
│   ├── api/              # API 调用封装
│   ├── assets/           # 静态资源
│   ├── components/       # 通用组件
│   │   ├── common/       # 通用组件
│   │   ├── layout/       # 布局组件
│   │   └── business/     # 业务组件
│   ├── router/           # 路由配置
│   ├── stores/           # 状态管理
│   ├── types/            # TypeScript 类型
│   ├── utils/            # 工具函数
│   ├── views/            # 页面视图
│   ├── App.vue
│   └── main.ts
├── public/               # 公共资源
├── index.html
├── vite.config.ts
├── tsconfig.json
└── package.json
```

## 开发规范

### 组件命名

- **通用组件**: PascalCase (如 `UserList.vue`)
- **业务组件**: PascalCase (如 `ProjectCard.vue`)
- **视图组件**: PascalCase + View 后缀 (如 `LoginView.vue`)

### API 调用

所有 API 调用统一放在 `src/api/` 目录下，按业务模块分类。

### 状态管理

使用 Pinia 进行状态管理，store 放在 `src/stores/` 目录。

### 类型定义

TypeScript 类型定义放在 `src/types/` 目录。

## 环境变量

创建 `.env.local` 文件：

```env
VITE_APP_TITLE=GSMS 工时管理系统
VITE_APP_API_BASE_URL=/api
VITE_APP_API_TIMEOUT=10000
```

## 联调后端

开发环境下，前端通过 Vite 代理访问后端 API（见 `vite.config.ts`）。

生产环境下需要配置 Nginx 反向代理。
