package com.extractor.xml;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Slf4j
@Component
public class ImageDownloader {

    private static final String DOWNLOAD_FOLDER = "/home/intv0016/Desktop/slike/test";

    public static void downloadImage(String imageUrl) {
        downloadImage(imageUrl, DOWNLOAD_FOLDER);
    }

    public static void downloadImage(String imageUrl, String path) {
        try {
            URL url = new URL(imageUrl);
            String fileName = Paths.get(url.getPath()).getFileName().toString();

            Path savePath = Paths.get(path, fileName);

            if (Files.exists(savePath)) {
                log.info("Image already exists: " + savePath);
                return;
            }

            try (InputStream in = url.openStream()) {
                Files.copy(in, savePath);
                log.info("Image downloaded successfully to: " + savePath);
            } catch (IOException e) {
                log.error("Error downloading image: " + e.getMessage());
            }
        } catch (IOException e) {
            log.error("Error downloading image: " + e.getMessage());
        }
    }
}
