# AI 智能文档引擎 - 前端

Next.js + Tailwind CSS + Lucide React 构建的现代化 AI 文档分析前端。

## 功能特性

- 🎨 现代化极简 UI 设计
- 📤 支持 PDF 文件拖拽上传和点击选择
- 🤖 AI 智能摘要生成
- ⚡ 实时加载状态显示
- 🪟 玻璃态 (Glassmorphism) 结果展示卡片

## 快速开始

### 1. 安装依赖

```bash
cd frontend
npm install
```

### 2. 启动开发服务器

```bash
npm run dev
```

访问 http://localhost:3000

### 3. 构建生产版本

```bash
npm run build
npm start
```

## 技术栈

- **Next.js 14.2.5** (App Router)
- **React 18**
- **TypeScript**
- **Tailwind CSS 3.4.4**
- **Lucide React** (图标库)

## 项目结构

```
frontend/
├── app/
│   ├── globals.css      # 全局样式
│   ├── layout.tsx       # 根布局
│   └── page.tsx         # 主页面
├── package.json
├── tsconfig.json
├── tailwind.config.ts
├── postcss.config.js
└── next.config.js
```

## API 集成

前端通过 POST 请求调用后端 API：

- **上传接口**: `http://localhost:8080/api/documents/upload`
- **请求参数**: FormData (file: PDF文件)
- **响应字段**: `globalSummary` (AI 生成的摘要)
