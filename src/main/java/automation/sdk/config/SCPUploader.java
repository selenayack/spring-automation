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

import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;

@Service
public class SCPUploader {
    /**
     * Dosyayı belirtilen hedef sunucuya SCP ile yükler.
     * @param host Hedef sunucu adresi
     * @param port Hedef sunucu SSH portu
     * @param user SSH kullanıcı adı
     * @param password SSH kullanıcı parolası
     * @param sourceFilePath Yerel dosyanın yolu
     * @param destinationFilePath Hedef sunucuda dosyanın yolu
     * @throws JSchException JSch kütüphanesi hatası
     * @throws IOException Dosya okuma/yazma hatası
     */


    public String uploadFile(String host, int port, String user, String password, String sourceFilePath, String destinationFilePath) throws JSchException, IOException {
        // JSch örneği oluştur
        JSch jsch = new JSch();
        // SSH oturumu oluştur
        Session session = jsch.getSession(user, host, port);
        // Kullanıcı parolasını ayarla
        session.setPassword(password);
        // Host anahtarlarının kontrolünü devre dışı bırak
        session.setConfig("StrictHostKeyChecking", "no");

        // Sunucuya bağlanma işlemi başlasın
        System.out.println("Sunucuya bağlanılıyor...");
        session.connect();
        System.out.println("Sunucuya bağlanıldı.");

        // Dosya zaman damgası kullanılsın mı?
        boolean ptimestamp = true;
        // Kaynak dosya nesnesi oluştur
        File sourceFile = new File(sourceFilePath);
        // Dosya boyutunu al
        long filesize = sourceFile.length();
        // SCP yükleme komutu oluştur
        String command = "scp " + (ptimestamp ? "-p" : "") + " -t " + destinationFilePath;
        // Kanal oluştur (exec)
        ChannelExec channel = (ChannelExec) session.openChannel("exec");
        // Komutu ayarla
        channel.setCommand(command);

        // Kanala yazılacak çıktı akışı
        OutputStream out = channel.getOutputStream();
        // Dosya okuma akışı
        FileInputStream fis = null;

        try {
            // Kanala bağlan
            channel.connect();

            // Zaman damgası gönder
            if (ptimestamp) {
                command = "T" + (sourceFile.lastModified() / 1000) + " 0";
                command += (" " + (sourceFile.lastModified() / 1000) + " 0\n");
                out.write(command.getBytes());
                out.flush();
            }

            // Dosya bilgisi gönder
            command = "C0644 " + filesize + " ";
            if (sourceFilePath.lastIndexOf('/') > 0) {
                command += sourceFilePath.substring(sourceFilePath.lastIndexOf('/') + 1);
            } else {
                command += sourceFilePath;
            }
            command += "\n";
            out.write(command.getBytes());
            out.flush();

            // Dosyayı oku ve kanala yaz
            fis = new FileInputStream(sourceFile);
            byte[] buffer = new byte[1024];
            while (true) {
                int len = fis.read(buffer, 0, buffer.length);
                if (len <= 0) break;
                out.write(buffer, 0, len);
            }

            // Dosya yazma işlemi tamamlandı
            fis.close();
            fis = null;
            out.write(0);
            out.flush();
            out.close();


            try {
                Thread.sleep(10000); // 5 saniye bekleme süresi
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            // Çıkış durumunu al
            int exitStatus = channel.getExitStatus();
            // Kanalı kapat
            channel.disconnect();
            // Oturumu kapat
            session.disconnect();

            // Dosya yükleme başarılı mı?
            if (exitStatus == 0) {

                //YEDEK İŞLEMİ BAŞARILI İSE TELEGRAMA BİLGİ GÖNDER
                System.out.println("Dosya başarıyla yüklendi: " + sourceFilePath + " -> " + destinationFilePath);
                return "0";
            } else {
                System.err.println("Dosya yükleme hatası, çıkış durumu: " + exitStatus);
                return "1";
            }
        } catch (Exception e) {
            // Hata durumunda işlemleri temizle
            System.err.println(e);
            try {
                if (fis != null) fis.close();
            } catch (Exception ee) {
            }
            out.close();
            channel.disconnect();
            session.disconnect();
        }
        return "bağlantı başarılı";
    }
}