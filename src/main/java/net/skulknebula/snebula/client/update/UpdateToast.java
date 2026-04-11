package net.skulknebula.snebula.client.update;

import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gl.RenderPipelines;
import net.minecraft.client.toast.Toast;
import net.minecraft.client.toast.ToastManager;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.ColorHelper;
import net.minecraft.util.math.MathHelper;

public class UpdateToast implements Toast {
    private static final Identifier MOD_ICON = Identifier.of("snebula", "textures/gui/skulk_icon.png");
    private static final Text TITLE = Text.literal("Доступно обновление!");
    private final Text versionText;
    private boolean justUpdated = true;
    private long startTime;

    // Цвета
    private static final int BG_COLOR = 0xD01A1A2E;
    private static final int BORDER_COLOR = 0xFF00D4FF;
    private static final int ACCENT_COLOR = 0xFF6B4CFF;

    public UpdateToast(String version) {
        this.versionText = Text.literal("SkulkNebula " + version);
    }

    @Override
    public Visibility getVisibility() {
        return this.startTime >= 5000L ? Visibility.HIDE : Visibility.SHOW;
    }

    @Override
    public void update(ToastManager manager, long time) {
        if (this.justUpdated) {
            this.startTime = time;
            this.justUpdated = false;
        }
    }

    @Override
    public void draw(DrawContext context, TextRenderer textRenderer, long currentTime) {
        // ===== ПЛАВНАЯ АНИМАЦИЯ =====
        float age = (currentTime - this.startTime) / 1000f;

        // Плавное появление (ease-out)
        float appearProgress;
        if (age < 0.4f) {
            float t = age / 0.4f;
            appearProgress = 1f - (1f - t) * (1f - t);
        } else {
            appearProgress = 1f;
        }

        // Плавное исчезновение (ease-in)
        float hideProgress;
        boolean isHiding = age > 4.0f;
        if (isHiding) {
            float t = (age - 4.0f) / 0.8f;
            if (t >= 1f) {
                hideProgress = 0f;
            } else {
                hideProgress = 1f - t * t;
            }
        } else {
            hideProgress = 1f;
        }

        float globalAlpha = appearProgress * hideProgress;

        // Эффект "распускания" справа-налево
        float scaleX;
        if (age < 0.5f) {
            float t = age / 0.5f;
            scaleX = 1f - (1f - t) * (1f - t) * (1f - t);
        } else if (isHiding) {
            float t = (age - 4.0f) / 0.8f;
            if (t >= 1f) {
                scaleX = 0f;
            } else {
                scaleX = 1f - t * t * t;
            }
        } else {
            scaleX = 1f;
        }

        if (globalAlpha <= 0.01f || scaleX <= 0.01f) return;

        // ===== РАЗМЕРЫ =====
        int boxWidth = 200;
        int boxHeight = 46;

        // Применяем scaleX (сжатие справа)
        int drawWidth = (int) (boxWidth * scaleX);
        int offsetX = boxWidth - drawWidth;

        // ===== ФОН =====
        int bgAlpha = (int) (ColorHelper.getAlphaFloat(BG_COLOR) * 255 * globalAlpha);
        int bgColor = ColorHelper.withAlpha(bgAlpha, BG_COLOR);
        context.fill(offsetX, 0, boxWidth, boxHeight, bgColor);

        // ===== ГРАДИЕНТНАЯ РАМКА =====
        int borderAlpha = (int) (255 * globalAlpha);

        // Верхняя рамка (растёт справа-налево)
        for (int i = 0; i < drawWidth; i++) {
            float t = (float) i / boxWidth;
            int color = ColorHelper.lerp(t, BORDER_COLOR, ACCENT_COLOR);
            color = ColorHelper.withAlpha(borderAlpha, color);
            context.fill(offsetX + i, 0, offsetX + i + 1, 1, color);
        }

        // Нижняя рамка (растёт справа-налево)
        for (int i = 0; i < drawWidth; i++) {
            float t = (float) i / boxWidth;
            int color = ColorHelper.lerp(t, ACCENT_COLOR, BORDER_COLOR);
            color = ColorHelper.withAlpha(borderAlpha, color);
            context.fill(offsetX + i, boxHeight - 1, offsetX + i + 1, boxHeight, color);
        }

        // Боковые рамки - рисуем только если они в зоне видимости
        // Левая рамка (всегда на месте, если offsetX == 0)
        if (offsetX == 0) {
            int leftBorderAlpha = (int) (borderAlpha * (isHiding ? hideProgress : 1f));
            int leftBorderColor = ColorHelper.withAlpha(leftBorderAlpha, BORDER_COLOR);
            context.fill(0, 0, 1, boxHeight, leftBorderColor);
        }

        // Правая рамка - двигается вместе с правым краем
        int rightBorderAlpha = (int) (borderAlpha * (isHiding ? hideProgress : 1f));
        int rightBorderColor = ColorHelper.withAlpha(rightBorderAlpha, BORDER_COLOR);
        context.fill(offsetX + drawWidth - 1, 0, offsetX + drawWidth, boxHeight, rightBorderColor);

        // ===== КОНТЕНТ =====
        float contentAlpha = MathHelper.clamp((age - 0.2f) / 0.3f, 0f, 1f) * hideProgress;

        if (contentAlpha > 0.01f && drawWidth > 50) {
            // Иконка
            try {
                int iconColor = ColorHelper.withAlpha((int)(255 * contentAlpha), 0xFFFFFF);
                context.drawTexture(
                        RenderPipelines.GUI_TEXTURED,
                        MOD_ICON,
                        offsetX + 10, 9,
                        0, 0,
                        16, 16,
                        16, 16,
                        iconColor
                );
            } catch (Exception e) {
                int frameColor = ColorHelper.withAlpha((int)(255 * contentAlpha), BORDER_COLOR);
                context.drawStrokedRectangle(offsetX + 10, 9, 16, 16, frameColor);
            }

            // Заголовок
            int titleColor = ColorHelper.withAlpha((int)(255 * contentAlpha), 0xFFFFFF);
            context.drawText(
                    textRenderer,
                    TITLE,
                    offsetX + 32, 7,
                    titleColor,
                    false
            );

            // Версия
            int versionColor = ColorHelper.withAlpha((int)(255 * contentAlpha), BORDER_COLOR);
            context.drawText(
                    textRenderer,
                    versionText,
                    offsetX + 32, 19,
                    versionColor,
                    false
            );

            // Подсказка
            int hintColor = ColorHelper.withAlpha((int)(180 * contentAlpha), 0xAAAAAA);
            String hintText = "Нажмите F1 для установки";
            context.drawText(
                    textRenderer,
                    Text.literal(hintText),
                    offsetX + 32, 31,
                    hintColor,
                    false
            );

            // Авторство
            Text authorText = Text.literal("by Klev1er");
            int authorWidth = textRenderer.getWidth(authorText);
            int textEndX = offsetX + 32 + textRenderer.getWidth(versionText);
            int authorStartX = boxWidth - authorWidth - 8;

            if (authorStartX > textEndX + 10) {
                context.drawText(
                        textRenderer,
                        authorText,
                        authorStartX,
                        19,
                        ColorHelper.withAlpha((int)(150 * contentAlpha), 0x858585),
                        false
                );
            } else if (drawWidth > boxWidth - 30) {
                context.drawText(
                        textRenderer,
                        authorText,
                        boxWidth - authorWidth - 8,
                        boxHeight + 6,
                        ColorHelper.withAlpha((int)(60 * contentAlpha), 0x858585),
                        false
                );
            }
        }

        // ===== ДЕКОРАТИВНЫЕ УГОЛКИ =====
        if (scaleX > 0.95f && contentAlpha > 0.7f && offsetX == 0) {
            int cornerAlpha = (int) (255 * contentAlpha * (scaleX - 0.95f) / 0.05f);
            int cornerColor = ColorHelper.withAlpha(cornerAlpha, ACCENT_COLOR);
            int cornerSize = 5;

            // Левый верхний
            context.fill(0, 0, cornerSize, 1, cornerColor);
            context.fill(0, 0, 1, cornerSize, cornerColor);
        }

        // Правый нижний уголок - только когда полностью раскрыт
        if (scaleX > 0.95f && contentAlpha > 0.7f && drawWidth == boxWidth) {
            int cornerAlpha = (int) (255 * contentAlpha);
            int cornerColor = ColorHelper.withAlpha(cornerAlpha, ACCENT_COLOR);
            int cornerSize = 5;

            context.fill(boxWidth - cornerSize, boxHeight - 1, boxWidth, boxHeight, cornerColor);
            context.fill(boxWidth - 1, boxHeight - cornerSize, boxWidth, boxHeight, cornerColor);
        }
    }

    @Override
    public int getWidth() {
        return 200;
    }

    @Override
    public int getHeight() {
        return 46;
    }
}