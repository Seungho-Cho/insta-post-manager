package com.plamason.postmanager.enums;

public enum AppSettingName {
    POST_MANAGER_URL("post.manager.url"),
    POST_MANAGER_SCHEDULE_HOUR("post.manager.schedule.hour"),
    DISCORD_BOT_TOKEN("discord.bot.token"),
    DISCORD_BOT_CHANNEL_ID("discord.bot.channel.id"),
    INSTAGRAM_API_USER_ID("instagram.api.user.id"),
    INSTAGRAM_API_ACCESS_TOKEN("instagram.api.access.token"),
    GROQ_API_KEY("groq.api.key"),
    GROQ_LLM_MODEL("groq.llm.model"),
    GROQ_BASE_PROMPT("groq.base.prompt"),
    GROQ_TRANSLATE_PROMPT("groq.translate.prompt"),
    APP_BASE_TAG("app.base.tag"),
    R2_API_KEY("Cloudflare.R2.api.key"),
    R2_ACCESS_TOKEN("Cloudflare.R2.access.token"),
    R2_ENDPOINT_URL("Cloudflare.R2.endpoint.url"),
    R2_BUCKET_NAME("Cloudflare.R2.bucket.name"),
    R2_OPEN_URL("Cloudflare.R2.open.url");

    private final String key;

    AppSettingName(String key) {
        this.key = key;
    }

    @Override
    public String toString() {
        return key;
    }
}
