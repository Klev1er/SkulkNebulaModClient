package net.skulknebula.snebula.client.update;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.MinecraftClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

public class ModUpdater {
    private static final Logger LOGGER = LoggerFactory.getLogger("ModUpdater");
    private static final HttpClient CLIENT = HttpClient.newBuilder()
            .followRedirects(HttpClient.Redirect.NORMAL)
            .build();

    private static final String REPO_OWNER = "Klev1er";
    private static final String REPO_NAME = "SkulkNebulaModClient";
    private static final String API_URL =
            "https://api.github.com/repos/" + REPO_OWNER + "/" + REPO_NAME + "/releases/latest";

    public record UpdateCheckResult(
            boolean hasUpdate,
            String latestVersion,
            String downloadUrl,
            String releaseName,      // Название релиза
            String releaseBody,      // Описание (changelog)
            String publishedAt       // Дата публикации
    ) {}

    public record DownloadProgress(double percent, long bytesRead, long totalBytes) {}

    public static CompletableFuture<UpdateCheckResult> checkForUpdates() {
        return CompletableFuture.supplyAsync(() -> {
            try {
                HttpRequest request = HttpRequest.newBuilder()
                        .uri(URI.create(API_URL))
                        .header("Accept", "application/vnd.github.v3+json")
                        .timeout(java.time.Duration.ofSeconds(10))
                        .GET()
                        .build();

                HttpResponse<String> response = CLIENT.send(request,
                        HttpResponse.BodyHandlers.ofString());

                if (response.statusCode() != 200) {
                    LOGGER.warn("GitHub API returned {}", response.statusCode());
                    return new UpdateCheckResult(false, null, null, null, null, null);
                }

                JsonObject json = JsonParser.parseString(response.body()).getAsJsonObject();
                String latestVersion = json.get("tag_name").getAsString();

                // Дополнительная инфа о релизе
                String releaseName = json.has("name") && !json.get("name").isJsonNull()
                        ? json.get("name").getAsString()
                        : latestVersion;
                String releaseBody = json.has("body") && !json.get("body").isJsonNull()
                        ? json.get("body").getAsString()
                        : "";
                String publishedAt = json.has("published_at") && !json.get("published_at").isJsonNull()
                        ? json.get("published_at").getAsString()
                        : "";

                JsonArray assets = json.getAsJsonArray("assets");

                if (assets.isEmpty()) {
                    LOGGER.warn("No assets in release");
                    return new UpdateCheckResult(false, null, null, null, null, null);
                }

                String downloadUrl = assets.get(0).getAsJsonObject()
                        .get("browser_download_url").getAsString();

                // Получаем текущую версию
                String currentVersion = FabricLoader.getInstance()
                        .getModContainer("snebula")
                        .map(container -> container.getMetadata().getVersion().getFriendlyString())
                        .orElse("0.0.0");

                // Нормализуем версии
                String cleanLatest = latestVersion.startsWith("v") ? latestVersion.substring(1) : latestVersion;
                String cleanCurrent = currentVersion.startsWith("v") ? currentVersion.substring(1) : currentVersion;

                boolean hasUpdate = !cleanLatest.equals(cleanCurrent);

                LOGGER.info("Update check: latest={}, current={}, hasUpdate={}",
                        cleanLatest, cleanCurrent, hasUpdate);

                return new UpdateCheckResult(hasUpdate, latestVersion, downloadUrl,
                        releaseName, releaseBody, publishedAt);

            } catch (Exception e) {
                LOGGER.error("Failed to check for updates", e);
                return new UpdateCheckResult(false, null, null, null, null, null);
            }
        });
    }

    public static CompletableFuture<Path> downloadUpdate(String url,
                                                         Consumer<DownloadProgress> progressCallback) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                HttpRequest request = HttpRequest.newBuilder()
                        .uri(URI.create(url))
                        .timeout(java.time.Duration.ofMinutes(5))
                        .GET()
                        .build();

                HttpResponse<InputStream> response = CLIENT.send(request,
                        HttpResponse.BodyHandlers.ofInputStream());

                long contentLength = response.headers().firstValueAsLong("Content-Length")
                        .orElse(-1L);

                Path modsFolder = FabricLoader.getInstance().getGameDir().resolve("mods");
                Path tempFile = modsFolder.resolve("update_" + System.currentTimeMillis() + ".jar.tmp");

                long startTime = System.currentTimeMillis();
                long lastLogTime = startTime;

                try (InputStream in = response.body()) {
                    byte[] buffer = new byte[8192];
                    int bytesRead;
                    long totalRead = 0;

                    try (var out = Files.newOutputStream(tempFile)) {
                        while ((bytesRead = in.read(buffer)) != -1) {
                            out.write(buffer, 0, bytesRead);
                            totalRead += bytesRead;

                            if (contentLength > 0) {
                                double percent = (double) totalRead / contentLength;

                                // Отправляем прогресс в UI поток
                                long finalTotalRead = totalRead;
                                MinecraftClient.getInstance().execute(() -> {
                                    if (progressCallback != null) {
                                        progressCallback.accept(new DownloadProgress(percent, finalTotalRead, contentLength));
                                    }
                                });
                            }

                            // Логируем прогресс каждые 2 секунды
                            long now = System.currentTimeMillis();
                            if (now - lastLogTime > 2000) {
                                double percent = contentLength > 0 ? (totalRead * 100.0 / contentLength) : -1;
                                LOGGER.info("Download progress: {}% ({} / {} bytes)",
                                        String.format("%.1f", percent), totalRead, contentLength);
                                lastLogTime = now;
                            }
                        }
                    }
                }

                long elapsed = System.currentTimeMillis() - startTime;
                LOGGER.info("Download completed in {} ms", elapsed);

                // Переименовываем в финальный файл
                String fileName = url.substring(url.lastIndexOf('/') + 1);
                Path finalFile = modsFolder.resolve(fileName);
                Files.move(tempFile, finalFile, StandardCopyOption.REPLACE_EXISTING);

                LOGGER.info("Downloaded update to {}", finalFile);
                return finalFile;

            } catch (Exception e) {
                LOGGER.error("Failed to download update", e);
                return null;
            }
        });
    }
}