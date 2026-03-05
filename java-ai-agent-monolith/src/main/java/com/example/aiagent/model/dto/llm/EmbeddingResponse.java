package com.example.aiagent.model.dto.llm;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

/**
 * Bailian (百炼) embedding response
 */
public record EmbeddingResponse(
    String request_id,
    @JsonProperty("output")
    Output output,
    Usage usage
) {
    public record Output(
        List<EmbeddingItem> embeddings
    ) {}

    public record EmbeddingItem(
        @JsonProperty("embedding")
        List<Float> embedding,
        @JsonProperty("text_index")
        int textIndex
    ) {}

    public record Usage(
        @JsonProperty("total_tokens")
        int totalTokens,
        @JsonProperty("unit_price")
        String unitPrice,
        @JsonProperty("price_unit")
        String priceUnit,
        @JsonProperty("total_price")
        String totalPrice,
        @JsonProperty("currency")
        String currency
    ) {}
}
