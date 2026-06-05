# 🤖 AI Document Intelligence Engine

> **RAG-Powered Full-Stack AI Document Analysis System**  
> Transform PDF documents into intelligent insights with cutting-edge LLM technology

![Java](https://img.shields.io/badge/Java-17-orange?style=flat-square&logo=openjdk)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.2.5-green?style=flat-square&logo=spring-boot)
![Next.js](https://img.shields.io/badge/Next.js-14.2.5-black?style=flat-square&logo=next.js)
![TypeScript](https://img.shields.io/badge/TypeScript-5.0-blue?style=flat-square&logo=typescript)
![Tailwind CSS](https://img.shields.io/badge/Tailwind-3.4.4-38B2AC?style=flat-square&logo=tailwind-css)
![MySQL](https://img.shields.io/badge/MySQL-8.0-blue?style=flat-square&logo=mysql)
![License](https://img.shields.io/badge/License-MIT-green?style=flat-square)

---

## 🌟 Features

### 🧠 Intelligent Document Processing
- **Long-Context LLM Integration**: Seamlessly handles documents up to 60,000 characters using advanced LLM APIs
- **Smart Text Chunking**: Implements sliding window algorithm with configurable overlap for optimal context preservation
- **Asynchronous Processing**: Non-blocking AI analysis with real-time status tracking via polling mechanism

### 🎨 Modern Full-Stack Architecture
- **Backend**: Robust Spring Boot REST API with JPA persistence and async task execution
- **Frontend**: Next.js 14 App Router with glassmorphism UI design and responsive layout
- **Real-time Updates**: Polling-based status synchronization between frontend and backend

### 🔧 Developer Experience
- **Type-Safe**: Full TypeScript implementation across frontend
- **Database-First**: Automatic schema evolution with Hibernate DDL
- **Cross-Origin Support**: CORS-enabled for seamless frontend-backend communication

---

## 🛠 Tech Stack

### Backend
| Technology | Purpose |
|------------|---------|
| ![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.2.5-green) | REST API framework |
| ![MySQL](https://img.shields.io/badge/MySQL-8.0-blue) | Relational database |
| ![HikariCP](https://img.shields.io/badge/HikariCP-Connection%20Pool-orange) | High-performance JDBC connection pool |
| ![Hutool](https://img.shields.io/badge/Hutool-5.8.25-yellow) | HTTP client & utilities |
| ![Apache PDFBox](https://img.shields.io/badge/PDFBox-3.0.7-red) | PDF text extraction |
| ![Lombok](https://img.shields.io/badge/Lombok-1.18.30-red) | Code generation |

### Frontend
| Technology | Purpose |
|------------|---------|
| ![Next.js](https://img.shields.io/badge/Next.js-14.2.5-black) | React framework with App Router |
| ![TypeScript](https://img.shields.io/badge/TypeScript-5.0-blue) | Type-safe JavaScript |
| ![Tailwind CSS](https://img.shields.io/badge/Tailwind-3.4.4-38B2AC) | Utility-first CSS framework |
| ![Lucide React](https://img.shields.io/badge/Lucide-0.400.0-purple) | Icon library |

### AI Integration
- **DeepSeek API**: Advanced LLM for intelligent document summarization
- **OpenAI-Compatible**: Standardized API format for easy integration

---

## 🚀 Quick Start

### Prerequisites
- Java 17+
- Node.js 18+
- MySQL 8.0+
- DeepSeek API Key

### 1. Clone the Repository
```bash
git clone https://github.com/jeretgit/AIDocSystem.git
cd AIDocSystem
```

### 2. Backend Setup

#### Configure Database
Edit `src/main/resources/application.yml`:
```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/ai_doc_db?useUnicode=true&characterEncoding=utf-8&useSSL=false&serverTimezone=Asia/Shanghai
    username: your_mysql_username
    password: your_mysql_password
```

#### Configure AI API
```yaml
ai:
  api-key: "your_deepseek_api_key"
  api-url: "https://api.deepseek.com/chat/completions"
```

#### Run Backend
```bash
mvn spring-boot:run
```
Backend will start on `http://localhost:8080`

### 3. Frontend Setup

```bash
cd frontend
npm install
npm run dev
```
Frontend will start on `http://localhost:3000`

### 4. Upload Your First Document
1. Open `http://localhost:3000` in your browser
2. Drag and drop a PDF file or click to select
3. Click "开始 AI 解析" (Start AI Analysis)
4. Wait for the AI to process and generate the summary

---

## 📊 Architecture & Data Flow

```
┌─────────────┐
│   Frontend  │
│  (Next.js)  │
└──────┬──────┘
       │ 1. Upload PDF (POST /api/documents/upload)
       ↓
┌─────────────────────────────────────────────────────────┐
│                    Backend (Spring Boot)                 │
├─────────────────────────────────────────────────────────┤
│  ┌─────────────────┐    ┌──────────────────────────┐  │
│  │ DocumentStorage │    │   DocumentService        │  │
│  │     Service     │───▶│   (@Async)               │  │
│  └─────────────────┘    │  ├─ PDF Text Extraction  │  │
│                         │  ├─ Sliding Window       │  │
│                         │  │    Chunking (1000/100)│  │
│                         │  ├─ LLM API Call         │  │
│                         │  │    (DeepSeek)          │  │
│                         │  └─ Database Update      │  │
│                         └──────────────────────────┘  │
│                              ↓                         │
│  ┌─────────────────────────────────────────────────┐  │
│  │              MySQL Database                      │  │
│  │  ├─ document_info table                         │  │
│  │  │  ├─ processStatus (0/1/2/3)                  │  │
│  │  │  └─ globalSummary (TEXT)                    │  │
│  │  └─ Auto-generated schema                       │  │
│  └─────────────────────────────────────────────────┘  │
└─────────────────────────────────────────────────────────┘
       │ 2. Poll Status (GET /api/documents/{id})
       │    Every 2 seconds
       ↓
┌─────────────┐
│   Frontend  │
│  Display    │
│  Summary    │
└─────────────┘
```

### Processing Pipeline

1. **PDF Upload** 📤
   - Frontend sends PDF via FormData to `/api/documents/upload`
   - Backend saves file to local storage
   - Returns `documentId` immediately

2. **Async Processing** ⚡
   - `@Async` method triggers background processing
   - PDFBox extracts full text from PDF
   - Sliding window algorithm chunks text (1000 chars, 100 overlap)
   - Full text (up to 60k chars) sent to DeepSeek API
   - AI generates intelligent summary
   - Database updated with `processStatus=2` and `globalSummary`

3. **Status Polling** 🔄
   - Frontend polls `/api/documents/{id}` every 2 seconds
   - Checks `processStatus`:
     - `0` or `1`: Still processing → Keep loading
     - `2`: Success → Display summary
     - `3`: Failed → Show error

4. **Result Display** 🎉
   - Glassmorphism card renders AI-generated summary
   - Supports multi-line text formatting
   - Modern, responsive UI

---

## 📝 API Endpoints

### Upload Document
```http
POST /api/documents/upload
Content-Type: multipart/form-data

Body: file (PDF)
Response: { "data": { "documentId": 123 } }
```

### Get Document Status
```http
GET /api/documents/{id}

Response: { 
  "data": {
    "id": 123,
    "fileName": "document.pdf",
    "processStatus": 2,
    "globalSummary": "AI-generated summary..."
  }
}
```

### Process Status Codes
- `0`: Pending
- `1`: Parsed (text extracted)
- `2`: Completed (AI summary generated)
- `3`: Failed

---

## 🎯 Key Implementation Details

### Sliding Window Chunking Algorithm
```java
private List<String> chunkText(String text, int chunkSize, int overlap) {
    List<String> chunks = new ArrayList<>();
    int step = chunkSize - overlap;
    int position = 0;
    
    while (position < text.length()) {
        int end = Math.min(position + chunkSize, text.length());
        String chunk = text.substring(position, end);
        chunks.add(chunk);
        position += step;
    }
    
    return chunks;
}
```

### Async Polling Mechanism
- Frontend uses `setInterval` for status checks
- 2-second polling interval
- 5-minute timeout protection
- Automatic cleanup on success/failure

### Database Schema
```sql
CREATE TABLE document_info (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    file_name VARCHAR(255) NOT NULL,
    file_size BIGINT,
    storage_path VARCHAR(512),
    process_status INT,
    global_summary TEXT,
    create_time TIMESTAMP,
    update_time TIMESTAMP
);
```

---

## 🤝 Contributing

Contributions are welcome! Please feel free to submit a Pull Request.

## 📄 License

This project is licensed under the MIT License.

## 🙏 Acknowledgments

- [DeepSeek](https://www.deepseek.com/) for providing the LLM API
- [Spring Boot](https://spring.io/projects/spring-boot) for the robust backend framework
- [Next.js](https://nextjs.org/) for the excellent React framework
- [Tailwind CSS](https://tailwindcss.com/) for the utility-first CSS framework

---

**Built with ❤️ by [Jeret](https://github.com/jeretgit)**
