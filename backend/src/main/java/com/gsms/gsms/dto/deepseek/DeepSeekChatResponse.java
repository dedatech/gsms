package com.gsms.gsms.dto.deepseek;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

/**
 * DeepSeek API 聊天响应
 */
public class DeepSeekChatResponse {

    /**
     * 响应ID
     */
    @JsonProperty("id")
    private String id;

    /**
     * 对象类型
     */
    @JsonProperty("object")
    private String object;

    /**
     * 创建时间戳
     */
    @JsonProperty("created")
    private Long created;

    /**
     * 模型名称
     */
    @JsonProperty("model")
    private String model;

    /**
     * 选择列表
     */
    @JsonProperty("choices")
    private List<Choice> choices;

    /**
     * token 使用情况
     */
    @JsonProperty("usage")
    private Usage usage;

    /**
     * 选择项
     */
    public static class Choice {
        /**
         * 索引
         */
        @JsonProperty("index")
        private Integer index;

        /**
         * 消息
         */
        @JsonProperty("message")
        private DeepSeekMessage message;

        /**
         * 日志概率（可选）
         */
        @JsonProperty("logprobs")
        private Object logprobs;

        /**
         * 完成原因
         */
        @JsonProperty("finish_reason")
        private String finishReason;

        public Object getLogprobs() {
            return logprobs;
        }

        public void setLogprobs(Object logprobs) {
            this.logprobs = logprobs;
        }

        public Integer getIndex() {
            return index;
        }

        public void setIndex(Integer index) {
            this.index = index;
        }

        public DeepSeekMessage getMessage() {
            return message;
        }

        public void setMessage(DeepSeekMessage message) {
            this.message = message;
        }

        public String getFinishReason() {
            return finishReason;
        }

        public void setFinishReason(String finishReason) {
            this.finishReason = finishReason;
        }
    }

    /**
     * token 使用情况
     */
    public static class Usage {
        /**
         * 提示 token 数
         */
        @JsonProperty("prompt_tokens")
        private Integer promptTokens;

        /**
         * 完成 token 数
         */
        @JsonProperty("completion_tokens")
        private Integer completionTokens;

        /**
         * 总 token 数
         */
        @JsonProperty("total_tokens")
        private Integer totalTokens;

        /**
         * 提示 token 详情（新增字段）
         */
        @JsonProperty("prompt_tokens_details")
        private PromptTokensDetails promptTokensDetails;

        /**
         * 提示缓存命中 token 数
         */
        @JsonProperty("prompt_cache_hit_tokens")
        private Integer promptCacheHitTokens;

        /**
         * 提示缓存未命中 token 数
         */
        @JsonProperty("prompt_cache_miss_tokens")
        private Integer promptCacheMissTokens;

        /**
         * 提示 token 详情
         */
        public static class PromptTokensDetails {
            /**
             * 缓存的 token 数
             */
            @JsonProperty("cached_tokens")
            private Integer cachedTokens;

            public Integer getCachedTokens() {
                return cachedTokens;
            }

            public void setCachedTokens(Integer cachedTokens) {
                this.cachedTokens = cachedTokens;
            }
        }

        public Integer getPromptTokens() {
            return promptTokens;
        }

        public void setPromptTokens(Integer promptTokens) {
            this.promptTokens = promptTokens;
        }

        public Integer getCompletionTokens() {
            return completionTokens;
        }

        public void setCompletionTokens(Integer completionTokens) {
            this.completionTokens = completionTokens;
        }

        public Integer getTotalTokens() {
            return totalTokens;
        }

        public void setTotalTokens(Integer totalTokens) {
            this.totalTokens = totalTokens;
        }

        public PromptTokensDetails getPromptTokensDetails() {
            return promptTokensDetails;
        }

        public void setPromptTokensDetails(PromptTokensDetails promptTokensDetails) {
            this.promptTokensDetails = promptTokensDetails;
        }

        public Integer getPromptCacheHitTokens() {
            return promptCacheHitTokens;
        }

        public void setPromptCacheHitTokens(Integer promptCacheHitTokens) {
            this.promptCacheHitTokens = promptCacheHitTokens;
        }

        public Integer getPromptCacheMissTokens() {
            return promptCacheMissTokens;
        }

        public void setPromptCacheMissTokens(Integer promptCacheMissTokens) {
            this.promptCacheMissTokens = promptCacheMissTokens;
        }
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getObject() {
        return object;
    }

    public void setObject(String object) {
        this.object = object;
    }

    public Long getCreated() {
        return created;
    }

    public void setCreated(Long created) {
        this.created = created;
    }

    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public List<Choice> getChoices() {
        return choices;
    }

    public void setChoices(List<Choice> choices) {
        this.choices = choices;
    }

    public Usage getUsage() {
        return usage;
    }

    public void setUsage(Usage usage) {
        this.usage = usage;
    }

    /**
     * 获取第一条回复的内容
     */
    public String getFirstContent() {
        if (choices != null && !choices.isEmpty()) {
            DeepSeekMessage message = choices.get(0).getMessage();
            return message != null ? message.getContent() : null;
        }
        return null;
    }
}
