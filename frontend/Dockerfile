# 1. 基础环境：使用轻量级 Node.js 18
FROM node:18-alpine

WORKDIR /app

# 2. 先复制依赖清单并安装（利用 Docker 缓存加速机制）
COPY package*.json ./
RUN npm install --registry=https://registry.npmmirror.com

# 3. 复制前端所有源码并打包生产环境
COPY . .
RUN npm run build

# 4. 声明前端暴露 3000 端口
EXPOSE 3000

# 5. 启动前端服务
CMD ["npm", "start"]