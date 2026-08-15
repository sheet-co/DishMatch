-- Schema for Spring AI's JdbcChatMemoryRepository, managed here via Flyway instead of
-- Spring AI's own auto-initializer (spring.ai.chat.memory.repository.jdbc.initialize-schema=never).
-- Column/index shape matches what Spring AI 2.0.0 creates out of the box.
DROP TABLE IF EXISTS spring_ai_chat_memory;

CREATE TABLE spring_ai_chat_memory
(
    conversation_id VARCHAR(36) NOT NULL,
    content         TEXT        NOT NULL,
    type            VARCHAR(10) NOT NULL,
    "timestamp"     TIMESTAMP   NOT NULL,
    sequence_id     BIGINT      NOT NULL,

    CONSTRAINT spring_ai_chat_memory_type_check
        CHECK (type IN ('USER', 'ASSISTANT', 'SYSTEM', 'TOOL'))
);

CREATE INDEX idx_chat_memory_conversation_sequence
    ON spring_ai_chat_memory (conversation_id, sequence_id);

CREATE INDEX idx_chat_memory_conversation_timestamp
    ON spring_ai_chat_memory (conversation_id, "timestamp");
