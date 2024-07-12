/*
 *
 *  *
 *  *  *
 *  *  *  *  DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *  *  *  *
 *  *  *  *  Copyright (C) 2023 Dileksoft LLC  - All Rights Reserved.
 *  *  *  *  Unauthorized copying of this file, via any medium is strictly prohibited
 *  *  *  *  Proprietary and confidential.
 *  *  *  *
 *  *  *  *  Written by Yusuf E. Karanfil <yekaranfil@dileksoft.com>, May 2024
 *  *  *
 *  *
 *
 */

package automation.sdk.config;

import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendDocument;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.InputFile;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.io.File;

@Component
public class TelegramBot extends TelegramLongPollingBot {

    private final String BOT_USERNAME="botname";

    private final String BOT_TOKEN = "your-token";

    private final String GROUP_ID ="-your-chat-id";


    @Override
    public String getBotUsername() {
        return BOT_USERNAME;
    }

    @Override
    public String getBotToken() {
        return BOT_TOKEN;
    }

    public void sendMessage(String message) {
        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(GROUP_ID);
        sendMessage.setText(message);
        sendMessage.setParseMode("MarkdownV2"); // Biçimlendirme modunu ekliyoruz
        try {
            execute(sendMessage);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    public void sendDocument(String chatId, String filePath, String message) {
        System.out.println(BOT_TOKEN +"+" + BOT_USERNAME +chatId + " + " + filePath + " + " + message);
        SendDocument sendDocument = new SendDocument();
        sendDocument.setChatId(chatId);
        sendDocument.setCaption(message);
        sendDocument.setDocument(new InputFile(new File(filePath)));

        try {
            execute(sendDocument);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onUpdateReceived(Update update) {
        // Gelen mesajı al
        String message = update.getMessage().getText();
        // Gelen mesajın sahibinin chat id'sini al
        String chatId = update.getMessage().getChatId().toString();

        // Gelen mesajı ekrana yazdır
        System.out.println("Gelen mesaj: " + message);
        // Gelen mesajın sahibinin chat id'sini ekrana yazdır
        System.out.println("Chat id: " + chatId);

        // Gelen mesajı sahibine geri gönder
        sendMessage(message);
    }

    // Diğer metotlar
}
