package net.skulknebula.snebula.client.update;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ConfirmScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.TitleScreen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.input.KeyInput;
import net.minecraft.client.gl.RenderPipelines;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.Util;
import net.minecraft.util.math.ColorHelper;
import net.minecraft.util.math.MathHelper;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.glfw.GLFW;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class ModUpdaterScreen extends Screen {
    // ==================== ТЕКСТУРЫ ====================
    private static final Identifier SKULK_ICON = Identifier.of("snebula", "textures/gui/skulk_icon.png");
    private static final Identifier PARTICLE_STAR = Identifier.of("snebula", "textures/gui/particle_star.png");
    private static final Identifier PARTICLE_GLOW = Identifier.of("snebula", "textures/gui/particle_glow.png");

    // ==================== ЦВЕТА ====================
    private static final int COLOR_DARK = 0xFF0A0E1A;
    private static final int COLOR_PRIMARY = 0xFF1A2B5E;
    private static final int COLOR_ACCENT = 0xFF00D4FF;
    private static final int COLOR_GLOW = 0xFF6B4CFF;
    private static final int COLOR_CYAN = 0xFF00F0FF;
    private static final int COLOR_SUCCESS = 0xFF00FF88;
    private static final int COLOR_ERROR = 0xFFFF5555;
    private static final int COLOR_BAR_BG = 0xFF1A1A2E;

    // ==================== ПОЛЯ ====================
    private final Screen parent;
    private State state = State.CHECKING;
    private float downloadProgress = 0f;
    private String statusMessage = "Подключение к источнику...";
    private String errorDetails = null;
    private ModUpdater.UpdateCheckResult updateInfo;
    private ButtonWidget installButton;

    private long downloadedBytes = 0;
    private long totalBytes = 0;
    private String changelog = "";

    // Текстурные частицы
    private final List<TextureParticle> particles = new ArrayList<>();
    private final Random random = new Random();
    private static final int MAX_PARTICLES = 15;

    // Анимации
    private long initTime;
    private float animProgress = 0f;
    private float pulsePhase = 0f;
    private boolean isResetting = false;
    private long resetStartTime = 0;

    // Кеш
    private int centerX;
    private int centerY;

    private enum State {
        CHECKING, DOWNLOADING, INSTALLING, COMPLETE, NO_UPDATE, ERROR
    }

    // ==================== ТЕКСТУРНАЯ ЧАСТИЦА ====================
    private static class TextureParticle {
        float x, y;
        float vx, vy;
        float size;
        float alpha;
        Identifier texture;
        int color;

        TextureParticle(int width, int height, Random rand) {
            reset(width, height, rand);
        }

        void reset(int width, int height, Random rand) {
            this.x = rand.nextInt(width);
            this.y = rand.nextInt(height);
            this.vx = (rand.nextFloat() - 0.5f) * 0.2f;
            this.vy = (rand.nextFloat() - 0.7f) * 0.15f;
            this.size = rand.nextFloat() * 12f + 6f;
            this.alpha = rand.nextFloat() * 0.5f + 0.3f;
            this.texture = rand.nextBoolean() ? PARTICLE_STAR : PARTICLE_GLOW;

            if (rand.nextBoolean()) {
                this.color = ColorHelper.getArgb(255, 0, 212, 255);
            } else {
                this.color = ColorHelper.getArgb(255, 107, 76, 255);
            }
        }

        void update(int width, int height) {
            x += vx;
            y += vy;
            vy -= 0.002f;

            if (y < -50) {
                y = height + 30;
                x = (float) (Math.random() * width);
                vy = -0.1f;
            }
            if (x < -30) x = width + 30;
            if (x > width + 30) x = -30;
        }
    }

    // ==================== КОНСТРУКТОР ====================
    public ModUpdaterScreen(@Nullable Screen parent) {
        super(Text.literal("§l🌌 SkulkNebula Installer 🌌"));
        this.parent = parent != null ? parent : new TitleScreen();
    }

    // ==================== ИНИЦИАЛИЗАЦИЯ ====================
    @Override
    protected void init() {
        this.initTime = Util.getMeasuringTimeMs();
        this.centerX = this.width / 2;
        this.centerY = this.height / 2;

        if (particles.isEmpty()) {
            for (int i = 0; i < MAX_PARTICLES; i++) {
                particles.add(new TextureParticle(this.width, this.height, random));
            }
        }

        createButtons();

        if (!isResetting) {
            startUpdateCheck();
        }
    }

    private void createButtons() {
        this.clearChildren();

        int buttonWidth = 100;
        int buttonHeight = 20;

        this.addDrawableChild(ButtonWidget.builder(
                Text.literal("📁 Файлы"),
                button -> openModsFolder()
        ).dimensions(this.width / 2 - 155, this.height - 55, buttonWidth, buttonHeight).build());

        this.addDrawableChild(ButtonWidget.builder(
                Text.literal("🔄 Сканировать"),
                button -> smoothReset()
        ).dimensions(this.width / 2 - 50, this.height - 55, buttonWidth, buttonHeight).build());

        this.addDrawableChild(ButtonWidget.builder(
                Text.literal("💜 Boosty"),
                button -> Util.getOperatingSystem().open("https://boosty.to/skulknebula")
        ).dimensions(this.width / 2 + 55, this.height - 55, buttonWidth, buttonHeight).build());

        this.addDrawableChild(ButtonWidget.builder(
                Text.literal("◀ Назад"),
                button -> this.close()
        ).dimensions(this.width / 2 - 50, this.height - 30, buttonWidth, buttonHeight).build());
    }

    private void smoothReset() {
        this.isResetting = true;
        this.resetStartTime = Util.getMeasuringTimeMs();
        this.state = State.CHECKING;
        this.statusMessage = "Сканирование сети...";
        this.downloadProgress = 0f;
    }

    private void openModsFolder() {
        File modsFolder = net.fabricmc.loader.api.FabricLoader.getInstance()
                .getGameDir().resolve("mods").toFile();
        Util.getOperatingSystem().open(modsFolder);
    }

    private void startUpdateCheck() {
        ModUpdater.checkForUpdates().thenAccept(result -> {
            this.updateInfo = result;

            if (result != null && result.hasUpdate()) {
                this.state = State.DOWNLOADING;
                this.statusMessage = "Загрузка " + result.latestVersion();
                this.changelog = result.releaseBody() != null && !result.releaseBody().isEmpty()
                        ? result.releaseBody()
                        : "• Нет описания обновления";

                ModUpdater.downloadUpdate(result.downloadUrl(), progress -> {
                    this.downloadProgress = (float) progress.percent();
                    this.downloadedBytes = progress.bytesRead();
                    this.totalBytes = progress.totalBytes();
                }).thenAccept(file -> {
                    if (file != null) {
                        this.state = State.COMPLETE;
                        this.statusMessage = "✓ Загрузка завершена";
                        addInstallButton();
                    } else {
                        this.state = State.ERROR;
                        this.statusMessage = "✗ Ошибка соединения";
                    }
                }).exceptionally(throwable -> {
                    this.state = State.ERROR;
                    this.statusMessage = "✗ " + throwable.getMessage();
                    return null;
                });
            } else {
                this.state = State.NO_UPDATE;
                this.statusMessage = "✓ Система актуальна";
                String currentVersion = net.fabricmc.loader.api.FabricLoader.getInstance()
                        .getModContainer("snebula")
                        .map(c -> c.getMetadata().getVersion().getFriendlyString())
                        .orElse("???");
                this.errorDetails = "Версия " + currentVersion;
            }
        }).exceptionally(throwable -> {
            this.state = State.ERROR;
            this.statusMessage = "✗ Нет подключения";
            return null;
        });
    }

    private void addInstallButton() {
        // Удаляем старую если есть
        if (installButton != null) {
            this.remove(installButton);
        }

        installButton = ButtonWidget.builder(
                Text.literal("⚡ УСТАНОВИТЬ"),
                button -> confirmInstall()
        ).dimensions(this.width / 2 - 75, this.height / 2 + 70, 150, 20).build();

        this.addDrawableChild(installButton);
    }
    private void confirmInstall() {
        this.client.setScreen(new ConfirmScreen(
                confirmed -> {
                    if (confirmed) this.client.scheduleStop();
                    else this.client.setScreen(this);
                },
                Text.literal("§l🌌 Перезапуск"),
                Text.literal("Мод обновится после перезапуска."),
                Text.literal("§a▶ ПЕРЕЗАПУСТИТЬ"),
                Text.literal("§8Отмена")
        ));
    }

    // ==================== ТИК ====================
    @Override
    public void tick() {
        long time = Util.getMeasuringTimeMs();

        if (isResetting) {
            float resetProgress = (time - resetStartTime) / 300f;
            if (resetProgress >= 1f) {
                isResetting = false;
                this.animProgress = 0f;
                this.initTime = time;
                startUpdateCheck();
                createButtons();
            } else {
                this.animProgress = 1f - resetProgress;
            }
        } else {
            this.animProgress = MathHelper.clamp((time - initTime) / 400f, 0f, 1f);
        }

        this.pulsePhase = (time % 2000) / 2000f * MathHelper.PI * 2;

        for (TextureParticle p : particles) {
            p.update(this.width, this.height);
        }
    }

    // ==================== РЕНДЕР ====================
    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        if (this.client.world == null) {
            this.renderPanoramaBackground(context, delta);
        }

        renderTextureParticles(context);
        renderVignetteOptimized(context);
        this.renderDarkening(context);
        renderDecorativeLines(context);
        renderHeader(context);

        if (this.animProgress > 0.15f) {
            renderMainContent(context);
        }

        renderStatusBar(context);
        super.render(context, mouseX, mouseY, delta);
    }

    private void renderTextureParticles(DrawContext context) {
        float globalAlpha = animProgress * 0.7f;
        if (globalAlpha <= 0.01f) return;

        for (TextureParticle p : particles) {
            int alpha = (int) (p.alpha * 255 * globalAlpha);
            if (alpha <= 0) continue;

            int color = ColorHelper.withAlpha(alpha, p.color);
            int size = (int) p.size;

            // ПРАВИЛЬНЫЙ ВЫЗОВ
            context.drawTexture(
                    RenderPipelines.GUI_TEXTURED,
                    p.texture,
                    (int) p.x, (int) p.y,
                    0, 0,
                    size, size,
                    size, size,
                    color
            );
        }
    }

    private void renderVignetteOptimized(DrawContext context) {
        float alpha = animProgress * 0.4f;
        if (alpha <= 0.01f) return;

        int step = 8;
        int maxDist = (int) Math.sqrt(centerX * centerX + centerY * centerY);

        for (int x = 0; x < this.width; x += step) {
            for (int y = 0; y < this.height; y += step) {
                int dx = x - centerX;
                int dy = y - centerY;
                float dist = (float) Math.sqrt(dx * dx + dy * dy) / maxDist;
                int vignetteAlpha = (int) (dist * 80 * alpha);

                if (vignetteAlpha > 0) {
                    int vignetteColor = ColorHelper.withAlpha(vignetteAlpha, COLOR_DARK);
                    context.fill(x, y, x + step, y + step, vignetteColor);
                }
            }
        }
    }

    private void renderDecorativeLines(DrawContext context) {
        float alpha = animProgress * 0.6f;
        if (alpha <= 0.01f) return;

        int lineColor = ColorHelper.withAlpha((int)(255 * alpha), COLOR_ACCENT);
        int sideColor = ColorHelper.withAlpha((int)(150 * alpha), COLOR_GLOW);
        int cornerColor = ColorHelper.withAlpha((int)(255 * alpha), COLOR_CYAN);
        int cornerSize = 20;

        context.fill(0, 0, this.width, 1, lineColor);
        context.fill(0, this.height - 1, this.width, this.height, lineColor);
        context.fill(0, 0, 1, this.height, sideColor);
        context.fill(this.width - 1, 0, this.width, this.height, sideColor);

        context.fill(0, 0, cornerSize, 2, cornerColor);
        context.fill(0, 0, 2, cornerSize, cornerColor);
        context.fill(this.width - cornerSize, 0, this.width, 2, cornerColor);
        context.fill(this.width - 2, 0, this.width, cornerSize, cornerColor);
        context.fill(0, this.height - 2, cornerSize, this.height, cornerColor);
        context.fill(0, this.height - cornerSize, 2, this.height, cornerColor);
        context.fill(this.width - cornerSize, this.height - 2, this.width, this.height, cornerColor);
        context.fill(this.width - 2, this.height - cornerSize, this.width, this.height, cornerColor);
    }

    private void renderHeader(DrawContext context) {
        int headerY = (int) (this.height * 0.12f);
        int titleColor = ColorHelper.withAlpha((int)(255 * animProgress), COLOR_CYAN);
        int subColor = ColorHelper.withAlpha((int)(180 * animProgress), COLOR_GLOW);

        // Основной заголовок (без эмодзи)
        context.drawCenteredTextWithShadow(
                this.textRenderer,
                Text.literal("SkulkNebula Installer"),
                this.width / 2,
                headerY,
                titleColor
        );

        // Подзаголовок с версией протокола
        context.drawCenteredTextWithShadow(
                this.textRenderer,
                Text.literal("// Nebula Protocol //"),
                this.width / 2,
                headerY + 15,
                subColor
        );
    }

    private void renderMainContent(DrawContext context) {
        float contentAlpha = MathHelper.clamp((animProgress - 0.15f) / 0.85f, 0f, 1f);

        // УЗКИЙ CHANGELOG СЛЕВА
        if (state == State.DOWNLOADING || state == State.COMPLETE) {
            renderChangelogNarrow(context, contentAlpha);
        }

        // Иконка и статус ПО ЦЕНТРУ (без смещения!)
        renderSkulkIcon(context, centerX, centerY - 20);

        int statusColor = getStatusColor();
        int finalStatusColor = ColorHelper.withAlpha((int)(255 * contentAlpha), statusColor);

        String displayStatus = statusMessage;
        if (state == State.CHECKING) {
            String[] spinner = {"|", "/", "—", "\\"};
            int spinIndex = (int) ((Util.getMeasuringTimeMs() / 150) % spinner.length);
            displayStatus = spinner[spinIndex] + " " + statusMessage;
        }

        context.drawCenteredTextWithShadow(
                this.textRenderer,
                Text.literal(displayStatus),
                centerX,
                centerY + 30,
                finalStatusColor
        );

        // Прогресс-бар
        if (state == State.DOWNLOADING) {
            renderProgressBar(context, centerX - 100, centerY + 50, 200, 12, contentAlpha);

            if (totalBytes > 0) {
                String sizeText = formatFileSize(downloadedBytes) + " / " + formatFileSize(totalBytes);
                context.drawCenteredTextWithShadow(
                        this.textRenderer,
                        Text.literal(sizeText),
                        centerX,
                        centerY + 67,
                        ColorHelper.withAlpha((int)(180 * contentAlpha), 0x969696)
                );
            }
        }

        // Версия
        if (errorDetails != null) {
            context.drawCenteredTextWithShadow(
                    this.textRenderer,
                    Text.literal(errorDetails),
                    centerX,
                    centerY + 60,
                    ColorHelper.withAlpha((int)(180 * contentAlpha), COLOR_GLOW)
            );
        }
    }

    private void renderChangelogNarrow(DrawContext context, float contentAlpha) {
        if (changelog == null || changelog.isEmpty()) return;

        int boxX = 15;
        int boxY = this.height / 2 - 50;
        int boxWidth = 160;  // УЖЕ!
        int boxHeight = 120; // Чуть ниже

        int bgAlpha = (int)(40 * contentAlpha);
        int borderAlpha = (int)(100 * contentAlpha);

        context.fill(boxX, boxY, boxX + boxWidth, boxY + boxHeight,
                ColorHelper.withAlpha(bgAlpha, COLOR_DARK));
        context.drawStrokedRectangle(boxX, boxY, boxWidth, boxHeight,
                ColorHelper.withAlpha(borderAlpha, COLOR_ACCENT));

        String headerText = "CHANGELOG";
        if (updateInfo != null) {
            headerText = updateInfo.latestVersion();
        }

        context.drawTextWithShadow(
                this.textRenderer,
                Text.literal(headerText),
                boxX + 5, boxY + 5,
                ColorHelper.withAlpha((int)(255 * contentAlpha), COLOR_CYAN)
        );

        context.fill(boxX + 3, boxY + 16, boxX + boxWidth - 3, boxY + 17,
                ColorHelper.withAlpha((int)(60 * contentAlpha), COLOR_ACCENT));

        String[] lines = changelog.split("\n");
        int maxLines = 6;
        int textAlpha = (int)(200 * contentAlpha);
        int textColor = ColorHelper.withAlpha(textAlpha, 0xAAAAAA);

        int lineY = boxY + 22;
        int linesDrawn = 0;

        for (String line : lines) {
            String trimmed = line.trim();
            if (trimmed.isEmpty()) continue;
            if (linesDrawn >= maxLines) break;

            // Обрезаем длинные строки
            if (this.textRenderer.getWidth(trimmed) > boxWidth - 10) {
                trimmed = this.textRenderer.trimToWidth(trimmed, boxWidth - 15) + "..";
            }

            context.drawTextWithShadow(
                    this.textRenderer,
                    Text.literal(trimmed),
                    boxX + 5, lineY,
                    textColor
            );
            lineY += 12;
            linesDrawn++;
        }

        // Дата
        if (updateInfo != null && updateInfo.publishedAt() != null && !updateInfo.publishedAt().isEmpty()) {
            String dateText = updateInfo.publishedAt().substring(0, 10);
            context.drawTextWithShadow(
                    this.textRenderer,
                    Text.literal(dateText),
                    boxX + boxWidth - this.textRenderer.getWidth(dateText) - 5,
                    boxY + boxHeight - 12,
                    ColorHelper.withAlpha((int)(120 * contentAlpha), 0x888888)
            );
        }
    }

    private void renderSkulkIcon(DrawContext context, int centerX, int centerY) {
        int iconSize = 64;
        int iconX = centerX - iconSize / 2;
        int iconY = centerY - iconSize / 2;

        float glowPulse = (MathHelper.sin(pulsePhase) + 1f) * 0.15f + 0.3f;
        int glowColor = ColorHelper.scaleAlpha(COLOR_ACCENT, glowPulse * animProgress);

        if (ColorHelper.getAlphaFloat(glowColor) > 0.01f) {
            context.fill(iconX - 8, iconY - 8, iconX + iconSize + 8, iconY + iconSize + 8, glowColor);
        }

        try {
            // ПРАВИЛЬНЫЙ ВЫЗОВ drawTexture с RenderPipeline
            context.drawTexture(
                    RenderPipelines.GUI_TEXTURED,
                    SKULK_ICON,
                    iconX, iconY,
                    0, 0,
                    iconSize, iconSize,
                    iconSize, iconSize
            );
        } catch (Exception e) {
            int iconBgColor = ColorHelper.withAlpha((int)(255 * animProgress), COLOR_PRIMARY);
            context.fill(iconX, iconY, iconX + iconSize, iconY + iconSize, iconBgColor);
            context.drawCenteredTextWithShadow(
                    this.textRenderer,
                    Text.literal("SN"),
                    iconX + iconSize / 2,
                    iconY + iconSize / 2 - this.textRenderer.fontHeight / 2,
                    COLOR_CYAN
            );
        }

        int borderColor = ColorHelper.withAlpha((int)(255 * animProgress), COLOR_CYAN);
        context.drawStrokedRectangle(iconX, iconY, iconSize, iconSize, borderColor);
    }

    private void renderProgressBar(DrawContext context, int x, int y, int width, int height, float alpha) {
        context.fill(x, y, x + width, y + height, COLOR_BAR_BG);

        int fillWidth = (int) (width * downloadProgress);
        if (fillWidth > 0) {
            int startColor = COLOR_PRIMARY;
            int endColor = COLOR_CYAN;

            int segments = 10;
            for (int i = 0; i < segments && i * width / segments < fillWidth; i++) {
                int segX = x + 1 + i * width / segments;
                int segWidth = Math.min(width / segments, fillWidth - i * width / segments);
                if (segWidth <= 0) break;

                float t = (float) (i * width / segments) / width;
                int segColor = ColorHelper.lerp(t, startColor, endColor);

                context.fill(segX, y + 1, segX + segWidth, y + height - 1, segColor);
            }
        }

        context.drawStrokedRectangle(x, y, width, height, COLOR_PRIMARY);

        String percentText = (int) (downloadProgress * 100) + "%";
        context.drawCenteredTextWithShadow(
                this.textRenderer,
                Text.literal(percentText),
                x + width / 2,
                y + height / 2 - this.textRenderer.fontHeight / 2,
                downloadProgress > 0.5f ? 0xFFFFFF : COLOR_CYAN
        );
    }

    private void renderStatusBar(DrawContext context) {
        int barY = this.height - 65;
        int barColor = ColorHelper.withAlpha((int)(50 * animProgress), COLOR_ACCENT);

        context.fill(0, barY, this.width, barY + 1, barColor);

        String leftText = "F1/ESC - Закрыть | Статус: " + state;
        String rightText = "SkulkNebula by Klev1er";

        context.drawTextWithShadow(this.textRenderer, Text.literal(leftText), 5, barY - 12, 0x888888);
        context.drawTextWithShadow(this.textRenderer, Text.literal(rightText),
                this.width - this.textRenderer.getWidth(rightText) - 5, barY - 12, 0x888888);
    }

    private int getStatusColor() {
        return switch (state) {
            case CHECKING -> COLOR_CYAN;
            case DOWNLOADING -> 0xFF00AAFF;
            case INSTALLING -> COLOR_GLOW;
            case COMPLETE, NO_UPDATE -> COLOR_SUCCESS;
            case ERROR -> COLOR_ERROR;
        };
    }

    private String formatFileSize(long bytes) {
        if (bytes < 1024) return bytes + " B";
        if (bytes < 1024 * 1024) return String.format("%.1f KB", bytes / 1024.0);
        return String.format("%.2f MB", bytes / (1024.0 * 1024.0));
    }

    // ==================== ОБРАБОТКА КЛАВИШ ====================
    @Override
    public boolean keyPressed(KeyInput keyInput) {
        if (keyInput.getKeycode() == GLFW.GLFW_KEY_F1 || keyInput.getKeycode() == GLFW.GLFW_KEY_ESCAPE) {
            if (this.state == State.DOWNLOADING || this.state == State.INSTALLING) {
                this.client.setScreen(new ConfirmScreen(
                        confirmed -> {
                            if (confirmed) this.close();
                            else this.client.setScreen(this);
                        },
                        Text.literal("§l⚠ ПРЕРВАТЬ?"),
                        Text.literal("Прогресс будет потерян."),
                        Text.literal("§cПРЕРВАТЬ"),
                        Text.literal("§aПРОДОЛЖИТЬ")
                ));
                return true;
            }
            this.close();
            return true;
        }
        return super.keyPressed(keyInput);
    }

    @Override
    public boolean shouldCloseOnEsc() {
        return this.state != State.DOWNLOADING && this.state != State.INSTALLING;
    }

    @Override
    public void close() {
        if (this.client != null) {
            this.client.setScreen(this.parent);
        }
    }

    @Override
    public boolean shouldPause() {
        return false;
    }
}