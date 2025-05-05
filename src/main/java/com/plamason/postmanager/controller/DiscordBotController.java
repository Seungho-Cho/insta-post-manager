package com.plamason.postmanager.controller;

import com.plamason.postmanager.bot.DiscordBotService;
import lombok.RequiredArgsConstructor;
import net.dv8tion.jda.api.JDA;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/admin/bot")
@RequiredArgsConstructor
public class DiscordBotController {

    private final DiscordBotService discordBotService;
    private JDA discordBot;

    @GetMapping(value = "/start")
    public ResponseEntity<Void> startBot() {
        if (discordBot != null) {
            discordBotService.stopBot(discordBot);
        }
        try{
            discordBot = discordBotService.startBotAndListenChannel();
        } catch (IllegalStateException e) {
            return ResponseEntity.badRequest().build();
        }
        return ResponseEntity.ok().build();
    }

    @GetMapping(value = "/stop")
    public ResponseEntity<Void> shutdownBot() {
        discordBotService.stopBot(discordBot);
        return ResponseEntity.ok().build();
    }
}
