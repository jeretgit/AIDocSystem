package com.aidoc.service;

import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class AiService {

    @Value("${ai.api-key}")
    private String apiKey;

    @Value("${ai.api-url}")
    private String apiUrl;

    public String summarizeChunk(String textChunk) {
        try {
            JSONObject requestBody = new JSONObject();
            requestBody.set("model", "deepseek-chat");
            
            JSONObject message = new JSONObject();
            message.set("role", "user");
            message.set("content", "请简要总结以下文本：\n" + textChunk);
            
            requestBody.set("messages", new Object[]{message});

            HttpResponse response = HttpRequest.post(apiUrl)
                    .header("Authorization", "Bearer " + apiKey)
                    .header("Content-Type", "application/json")
                    .body(requestBody.toString())
                    .execute();

            if (response.isOk()) {
                String responseBody = response.body();
                JSONObject responseJson = JSONUtil.parseObj(responseBody);
                String content = responseJson.getJSONArray("choices")
                        .getJSONObject(0)
                        .getJSONObject("message")
                        .getStr("content");
                return content;
            } else {
                log.error("AI API 调用失败，状态码: {}, 响应: {}", response.getStatus(), response.body());
                throw new RuntimeException("AI API 调用失败");
            }
        } catch (Exception ex) {
            log.error("AI 总结失败", ex);
            throw new RuntimeException("AI 总结失败: " + ex.getMessage());
        }
    }
}
