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
    APP_BASE_TAG("app.base.tag");

    private final String key;

    AppSettingName(String key) {
        this.key = key;
    }

    @Override
    public String toString() {
        return key;
    }
}
