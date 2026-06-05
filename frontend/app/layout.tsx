import type { Metadata } from "next";
import "./globals.css";

export const metadata: Metadata = {
  title: "AI 智能文档引擎",
  description: "AI-powered document analysis system",
};

export default function RootLayout({
  children,
}: Readonly<{
  children: React.ReactNode;
}>) {
  return (
    <html lang="zh-CN">
      <body className="antialiased">
        {children}
      </body>
    </html>
  );
}
