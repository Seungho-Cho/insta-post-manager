package com.plamason.postmanager.bot;

import com.plamason.postmanager.enums.AppSettingName;
import com.plamason.postmanager.service.AppSettingService;
import com.plamason.postmanager.service.PostManageService;
import lombok.AllArgsConstructor;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.requests.GatewayIntent;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
@AllArgsConstructor
public class DiscordBotService {

    private final PostManageService postManageService;
    private final AppSettingService appSettingService;

    public JDA startBotAndListenChannel() throws IllegalStateException{
        Map<String, String> appSettings = appSettingService.getAllSettings();

        String token = appSettings.get(AppSettingName.DISCORD_BOT_TOKEN.toString());
        String channelId = appSettings.get(AppSettingName.DISCORD_BOT_CHANNEL_ID.toString());
        String postManagerUrl = appSettings.get(AppSettingName.POST_MANAGER_URL.toString());

        if (token == null || token.isBlank()) {
            throw new IllegalStateException("Discord bot token is not configured");
        }

        if (channelId == null || channelId.isBlank()) {
            throw new IllegalStateException("Discord channel ID is not configured");
        }

        return JDABuilder.createDefault(token)
                .addEventListeners(new DiscordBotMessageListener(channelId, postManagerUrl, postManageService))
                .enableIntents(
                        GatewayIntent.GUILD_MESSAGES,
                        GatewayIntent.MESSAGE_CONTENT
                )
                .build();
    }

    public void stopBot(JDA jda) {
        jda.shutdown();
    }
}
