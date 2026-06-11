'use client';

import { useState, useRef, useEffect } from 'react';
import { Upload, FileText, Loader2, Sparkles, AlertCircle } from 'lucide-react';

export default function Home() {
  const [file, setFile] = useState<File | null>(null);
  const [loading, setLoading] = useState(false);
  const [summary, setSummary] = useState<string>('');
  const [isDragging, setIsDragging] = useState(false);
  const [documentId, setDocumentId] = useState<number | null>(null);
  const [error, setError] = useState<string>('');
  const fileInputRef = useRef<HTMLInputElement>(null);

  const handleDragOver = (e: React.DragEvent) => {
    e.preventDefault();
    setIsDragging(true);
  };

  const handleDragLeave = (e: React.DragEvent) => {
    e.preventDefault();
    setIsDragging(false);
  };

  const handleDrop = (e: React.DragEvent) => {
    e.preventDefault();
    setIsDragging(false);
    
    const droppedFile = e.dataTransfer.files[0];
    if (droppedFile && droppedFile.type === 'application/pdf') {
      setFile(droppedFile);
    } else {
      alert('请上传 PDF 文件');
    }
  };

  const handleFileSelect = (e: React.ChangeEvent<HTMLInputElement>) => {
    const selectedFile = e.target.files?.[0];
    if (selectedFile) {
      setFile(selectedFile);
    }
  };

  const handleUpload = async () => {
    if (!file) return;

    setLoading(true);
    setSummary('');
    setError('');
    setDocumentId(null);

    const formData = new FormData();
    formData.append('file', file);

    try {
      const response = await fetch('/api/documents/upload', {
        method: 'POST',
        body: formData,
      });

      if (response.ok) {
        const data = await response.json();
        const docId = data.data.documentId;
        setDocumentId(docId);

        // Start polling
        const pollInterval = setInterval(async () => {
          try {
            const statusResponse = await fetch(`/api/documents/${docId}`);
            if (statusResponse.ok) {
              const statusData = await statusResponse.json();
              const document = statusData.data;
              
              if (document.processStatus === 2) {
                // Success
                clearInterval(pollInterval);
                setSummary(document.globalSummary || 'AI 总结生成完成');
                setLoading(false);
              } else if (document.processStatus === 3) {
                // Failed
                clearInterval(pollInterval);
                setError(document.globalSummary || '文档解析失败');
                setLoading(false);
              }
              // processStatus 0 or 1: continue polling
            }
          } catch (pollError) {
            console.error('轮询错误:', pollError);
            clearInterval(pollInterval);
            setError('查询状态失败');
            setLoading(false);
          }
        }, 2000);

        // Set timeout to stop polling after 5 minutes
        setTimeout(() => {
          clearInterval(pollInterval);
          if (loading) {
            setError('处理超时，请重试');
            setLoading(false);
          }
        }, 300000);
      } else {
        const errorData = await response.json();
        const errorMessage = errorData.message || '上传失败，请重试';
        setError(errorMessage);
        setLoading(false);
      }
    } catch (error) {
      console.error('上传错误:', error);
      alert('上传失败，请检查网络连接');
      setLoading(false);
    }
  };

  return (
    <main className="min-h-screen bg-gradient-to-br from-gray-900 via-slate-800 to-gray-900 p-8">
      <div className="max-w-4xl mx-auto">
        {/* 标题 */}
        <div className="text-center mb-12">
          <h1 className="text-5xl font-bold text-white mb-4 flex items-center justify-center gap-3">
            <Sparkles className="w-10 h-10 text-blue-400" />
            AI 智能文档引擎
          </h1>
          <p className="text-gray-400 text-lg">上传 PDF 文档，AI 自动为您生成智能摘要</p>
        </div>

        {/* 上传区域 */}
        <div
          className={`border-2 border-dashed rounded-2xl p-12 text-center transition-all duration-300 cursor-pointer ${
            isDragging
              ? 'border-blue-500 bg-blue-500/10'
              : 'border-gray-600 bg-gray-800/50 hover:border-gray-500 hover:bg-gray-800/70'
          }`}
          onDragOver={handleDragOver}
          onDragLeave={handleDragLeave}
          onDrop={handleDrop}
          onClick={() => fileInputRef.current?.click()}
        >
          <input
            ref={fileInputRef}
            type="file"
            accept=".pdf"
            onChange={handleFileSelect}
            className="hidden"
          />
          
          {file ? (
            <div className="space-y-4">
              <FileText className="w-16 h-16 text-blue-400 mx-auto" />
              <p className="text-white text-xl font-medium">{file.name}</p>
              <p className="text-gray-400 text-sm">点击更换文件</p>
            </div>
          ) : (
            <div className="space-y-4">
              <Upload className="w-16 h-16 text-gray-500 mx-auto" />
              <p className="text-gray-400 text-xl">拖拽 PDF 文件到此处</p>
              <p className="text-gray-500 text-sm">或点击选择文件</p>
            </div>
          )}
        </div>

        {/* 按钮 */}
        {file && (
          <div className="mt-8 text-center">
            <button
              onClick={handleUpload}
              disabled={loading}
              className="px-8 py-4 bg-blue-600 hover:bg-blue-700 disabled:bg-blue-800 text-white font-semibold rounded-xl transition-all duration-300 flex items-center gap-3 mx-auto disabled:cursor-not-allowed shadow-lg hover:shadow-blue-500/25"
            >
              {loading ? (
                <>
                  <Loader2 className="w-5 h-5 animate-spin" />
                  AI 正在深度思考中...
                </>
              ) : (
                <>
                  <Sparkles className="w-5 h-5" />
                  开始 AI 解析
                </>
              )}
            </button>
          </div>
        )}

        {/* 错误提示 */}
        {error && (
          <div className="mt-8">
            <div className="bg-red-500/10 border border-red-500/30 rounded-xl p-6 flex items-start gap-4">
              <AlertCircle className="w-6 h-6 text-red-400 flex-shrink-0 mt-0.5" />
              <div>
                <h3 className="text-red-400 font-semibold mb-1">处理失败</h3>
                <p className="text-red-300">{error}</p>
              </div>
            </div>
          </div>
        )}

        {/* AI 总结卡片 */}
        {summary && (
          <div className="mt-12">
            <div className="bg-white/10 backdrop-blur-lg border border-white/20 rounded-2xl p-8 shadow-2xl">
              <div className="flex items-center gap-3 mb-6">
                <Sparkles className="w-6 h-6 text-blue-400" />
                <h2 className="text-2xl font-bold text-white">AI 智能摘要</h2>
              </div>
              <div className="text-gray-200 leading-relaxed whitespace-pre-wrap">
                {summary}
              </div>
            </div>
          </div>
        )}
      </div>
    </main>
  );
}
