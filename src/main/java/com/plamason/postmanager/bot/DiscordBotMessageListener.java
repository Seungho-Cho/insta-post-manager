package com.plamason.postmanager.bot;

import com.plamason.postmanager.dto.PostRequest;
import com.plamason.postmanager.service.ImageHostingService;
import com.plamason.postmanager.service.PostManageService;
import lombok.AllArgsConstructor;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.components.buttons.Button;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@AllArgsConstructor
public class DiscordBotMessageListener extends ListenerAdapter {

    private final String channelId;
    private final String postManagerUrl;
    private final PostManageService postManageService;
    private final ImageHostingService imageHostingService;

    private static final String UI_URL = "/ui/posts/";

    @Override
    public void onMessageReceived(MessageReceivedEvent event) {
        if(!event.getChannel().getId().equals(channelId)) return;
        if(event.getAuthor().isBot()) return;

        List<Message.Attachment> attachments = event.getMessage().getAttachments();
        List<String> imageUrls = attachments.stream()
                .filter(Message.Attachment::isImage)
                .map(Message.Attachment::getUrl)
                .toList();

        if(imageUrls.isEmpty()) return;
        if(postManageService == null) throw new RuntimeException("postManageService is null");

        String author = Optional.ofNullable(event.getMessage().getMember())
                .map(Member::getNickname)
                .orElse(event.getAuthor().getName());

        List<String> uploadedUrls = new ArrayList<>();
        imageUrls.forEach(imageUrl ->
            uploadedUrls.add(imageHostingService.uploadImage(imageUrl, UUID.randomUUID().toString()))
        );

        Long postId = postManageService.createPost(PostRequest.builder()
                .imageUrls(uploadedUrls)
                .author(author)
                .build()
        ).getId();

        event.getMessage().reply("이미지 확인완료! 아래 버튼을 눌러 포스팅을 계속 진행해 주세요.")
                .setActionRow(Button.link(postManagerUrl + UI_URL + postId,"📤 포스팅 하러 가기"))
                .queue();
    }
}
