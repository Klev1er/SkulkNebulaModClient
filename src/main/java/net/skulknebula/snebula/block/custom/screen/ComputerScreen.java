package net.skulknebula.snebula.block.custom.screen;

import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.Click;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.gl.RenderPipelines;
import net.minecraft.client.input.CharInput;
import net.minecraft.client.input.KeyInput;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.skulknebula.snebula.SkulkNebulaMod;
import net.skulknebula.snebula.block.custom.ServerBlock;
import net.skulknebula.snebula.block.custom.ServerBlockEntity;
import net.skulknebula.snebula.signal.DecryptionManager;
import net.skulknebula.snebula.signal.DecryptionTicker;
import net.skulknebula.snebula.signal.SignalData;
import org.lwjgl.glfw.GLFW;

import java.util.*;

public class ComputerScreen extends HandledScreen<ComputerScreenHandler> {
    private static final Identifier TEXTURE =
            Identifier.of(SkulkNebulaMod.MOD_ID, "textures/gui/container/computer_container.png");
    private static final Identifier SERVER_ONLINE_BG =
            Identifier.of(SkulkNebulaMod.MOD_ID, "textures/gui/server_gui_online.png");
    private static final Identifier SERVER_OFFLINE_BG =
            Identifier.of(SkulkNebulaMod.MOD_ID, "textures/gui/server_gui_offline.png");

    private static final int GUI_WIDTH = 312;
    private static final int GUI_HEIGHT = 256;

    // Цвета
    private static final int COLOR_BG_DARK = 0x80000000;
    private static final int COLOR_BG_MEDIUM = 0xCC000000;
    private static final int COLOR_BG_LIGHT = 0xFF222222;
    private static final int COLOR_BG_BLUE_DARK = 0xFF0A0A15;
    private static final int COLOR_BG_BLUE = 0xFF111122;

    private static final int COLOR_BORDER_RED = 0xFFFF0000;
    private static final int COLOR_BORDER_GREEN = 0xFF00AA00;
    private static final int COLOR_BORDER_BLUE = 0xFF3355AA;
    private static final int COLOR_BORDER_CYAN = 0xFF00AAFF;
    private static final int COLOR_BORDER_GRAY = 0xFF555555;

    private static final int COLOR_TEXT_GREEN = -16711936;
    private static final int COLOR_TEXT_GREEN_DARK = -16755456;
    private static final int COLOR_TEXT_RED = -65536;
    private static final int COLOR_TEXT_YELLOW = -22016;
    private static final int COLOR_TEXT_CYAN = -16725761;
    private static final int COLOR_TEXT_BLUE = -10912598;
    private static final int COLOR_TEXT_WHITE = -1;
    private static final int COLOR_TEXT_GRAY = -5592406;
    private static final int COLOR_TEXT_GRAY_DARK = -11184811;
    private static final int COLOR_TEXT_LIGHT_BLUE = -5592321;

    private static final int COLOR_SCROLL_BG = 0x80000000;
    private static final int COLOR_SCROLL_BAR = 0xFFAAAAAA;
    private static final int COLOR_SCROLL_BAR_GREEN = 0xFF00AA00;

    // Список серверов
    private static final int SERVER_LIST_X = 10;
    private static final int SERVER_LIST_Y = 10;
    private static final int SERVER_LIST_WIDTH = 96;
    private static final int SERVER_LIST_HEIGHT = 230;
    private static final int SERVER_ENTRY_HEIGHT = 32;

    // Консоль - справа (вкладка 1)
    private static final int CONSOLE_X = 115;
    private static final int CONSOLE_Y = 10;
    private static final int CONSOLE_WIDTH = 185;
    private static final int CONSOLE_HEIGHT = 150;
    private static final int INPUT_X = 115;
    private static final int INPUT_Y = 168;
    private static final int INPUT_WIDTH = 185;
    private static final int INPUT_HEIGHT = 14;

    // Вкладка расшифровки (вкладка 2)
    private static final int DECRYPT_MAIN_X = 115;
    private static final int DECRYPT_MAIN_Y = 10;
    private static final int DECRYPT_MAIN_WIDTH = 185;
    private static final int DECRYPT_MAIN_HEIGHT = 230;

    // Изображение
    private static final int IMAGE_X = 120;
    private static final int IMAGE_Y = 15;
    private static final int IMAGE_SIZE = 64;

    // Текст сигнала
    private static final int SIGNAL_TEXT_X = 120;
    private static final int SIGNAL_TEXT_Y = 85;
    private static final int SIGNAL_TEXT_WIDTH = 175;
    private static final int SIGNAL_TEXT_HEIGHT = 80;

    // История сигналов
    private static final int HISTORY_X = 120;
    private static final int HISTORY_Y = 170;
    private static final int HISTORY_WIDTH = 175;
    private static final int HISTORY_HEIGHT = 65;

    // Мини-консоль для команд
    private static final int MINI_CONSOLE_X = 120;
    private static final int MINI_CONSOLE_Y = 240;
    private static final int MINI_CONSOLE_WIDTH = 150;
    private static final int MINI_CONSOLE_HEIGHT = 12;

    private int serverScrollOffset = 0;
    private int consoleScrollOffset = 0;
    private int historyScrollOffset = 0;

    private List<ServerBlockEntity> servers = new ArrayList<>();
    private final ComputerScreenHandler screenHandler;

    private final List<ConsoleMessage> consoleMessages = new ArrayList<>();
    private String inputText = "";
    private boolean inputFocused = false;

    private long lastServerUpdate = 0;
    private static final long SERVER_UPDATE_INTERVAL = 2000;

    // Текущая вкладка
    private int currentTab = 0; // 0 = консоль, 1 = расшифровка

    public ComputerScreen(ComputerScreenHandler handler, PlayerInventory inventory, Text title) {
        super(handler, inventory, title);
        this.screenHandler = handler;
        this.backgroundWidth = GUI_WIDTH;
        this.backgroundHeight = GUI_HEIGHT;
        this.playerInventoryTitleY = -100;
        this.titleY = -100;

        addConsoleMessage("§aComputer started");
        addConsoleMessage("§7Type 'help' for commands");
    }

    @Override
    protected void init() {
        super.init();
        this.x = (this.width - this.backgroundWidth) / 2;
        this.y = (this.height - this.backgroundHeight) / 2;
    }

    @Override
    public boolean shouldPause() {
        return false;
    }

    @Override
    protected void drawBackground(DrawContext context, float delta, int mouseX, int mouseY) {
        context.drawTexture(RenderPipelines.GUI_TEXTURED, TEXTURE, x, y, 0, 0, backgroundWidth, backgroundHeight, backgroundWidth, backgroundHeight);

        long now = System.currentTimeMillis();
        if (now - lastServerUpdate > SERVER_UPDATE_INTERVAL) {
            List<ServerBlockEntity> allServers = ServerBlockEntity.getAllServers();
            Map<BlockPos, ServerBlockEntity> uniqueServers = new LinkedHashMap<>();
            for (ServerBlockEntity server : allServers) {
                if (server != null && !server.isRemoved()) {
                    uniqueServers.putIfAbsent(server.getPos(), server);
                }
            }
            servers = new ArrayList<>(uniqueServers.values());
            lastServerUpdate = now;
        }

        context.applyBlur();

        drawServerList(context);

        if (currentTab == 0) {
            drawConsole(context);
            drawInputField(context);
            drawDecryptionProgress(context);
        } else {
            drawDecryptionPage(context);
            drawMiniConsole(context);
        }
    }

    private void drawServerList(DrawContext context) {
        int listX = x + SERVER_LIST_X;
        int listY = y + SERVER_LIST_Y;

        context.fill(listX, listY, listX + SERVER_LIST_WIDTH, listY + SERVER_LIST_HEIGHT, COLOR_BG_DARK);
        context.drawStrokedRectangle(listX, listY, SERVER_LIST_WIDTH, SERVER_LIST_HEIGHT, COLOR_BORDER_RED);
        context.enableScissor(listX, listY, listX + SERVER_LIST_WIDTH, listY + SERVER_LIST_HEIGHT);

        int maxScroll = Math.max(0, servers.size() * SERVER_ENTRY_HEIGHT - SERVER_LIST_HEIGHT);
        serverScrollOffset = Math.min(serverScrollOffset, maxScroll);

        for (int i = 0; i < servers.size(); i++) {
            ServerBlockEntity server = servers.get(i);
            int entryY = listY + i * SERVER_ENTRY_HEIGHT - serverScrollOffset;

            if (entryY + SERVER_ENTRY_HEIGHT < listY || entryY > listY + SERVER_LIST_HEIGHT) continue;

            Identifier bgTexture = server.isBroken() ? SERVER_OFFLINE_BG : SERVER_ONLINE_BG;
            context.drawTexture(RenderPipelines.GUI_TEXTURED, bgTexture, listX, entryY, 0, 0, SERVER_LIST_WIDTH, SERVER_ENTRY_HEIGHT, SERVER_LIST_WIDTH, SERVER_ENTRY_HEIGHT);

            BlockPos pos = server.getPos();
            String serverName = String.format("SRV-%02d%02d", Math.abs(pos.getX() % 100), Math.abs(pos.getZ() % 100));

            int tier = server.getCachedState().contains(ServerBlock.TIER) ? server.getCachedState().get(ServerBlock.TIER) : 0;

            context.drawText(textRenderer, Text.literal(serverName), listX + 18, entryY + 6, COLOR_TEXT_GREEN, true);
            String status = server.isBroken() ? "OFF" : "ON";
            int statusColor = server.isBroken() ? COLOR_TEXT_RED : COLOR_TEXT_GREEN;
            context.drawText(textRenderer, Text.literal(status), listX + 18, entryY + 18, statusColor, true);
            context.drawText(textRenderer, Text.literal("T" + tier), listX + SERVER_LIST_WIDTH - 25, entryY + 6, COLOR_TEXT_GRAY, true);
        }

        context.disableScissor();

        if (maxScroll > 0) {
            int scrollBarHeight = Math.max(10, (int) ((float) SERVER_LIST_HEIGHT / (servers.size() * SERVER_ENTRY_HEIGHT) * SERVER_LIST_HEIGHT));
            int scrollBarY = listY + (int) ((float) serverScrollOffset / maxScroll * (SERVER_LIST_HEIGHT - scrollBarHeight));
            context.fill(listX + SERVER_LIST_WIDTH - 3, listY, listX + SERVER_LIST_WIDTH, listY + SERVER_LIST_HEIGHT, COLOR_SCROLL_BG);
            context.fill(listX + SERVER_LIST_WIDTH - 3, scrollBarY, listX + SERVER_LIST_WIDTH, scrollBarY + scrollBarHeight, COLOR_SCROLL_BAR);
        }
    }

    private void drawConsole(DrawContext context) {
        int consoleX = x + CONSOLE_X;
        int consoleY = y + CONSOLE_Y;

        context.fill(consoleX, consoleY, consoleX + CONSOLE_WIDTH, consoleY + CONSOLE_HEIGHT, COLOR_BG_MEDIUM);
        context.drawStrokedRectangle(consoleX, consoleY, CONSOLE_WIDTH, CONSOLE_HEIGHT, COLOR_BORDER_GREEN);

        long now = System.currentTimeMillis();
        consoleMessages.removeIf(msg -> now - msg.timestamp > 600000);

        int lineHeight = 11;
        int messageAreaHeight = CONSOLE_HEIGHT - 40;
        int maxScroll = Math.max(0, consoleMessages.size() * lineHeight - messageAreaHeight);
        consoleScrollOffset = Math.min(consoleScrollOffset, maxScroll);

        int visibleLines = messageAreaHeight / lineHeight;
        int startIndex = consoleScrollOffset / lineHeight;
        int endIndex = Math.min(consoleMessages.size(), startIndex + visibleLines + 1);

        context.enableScissor(consoleX + 2, consoleY + 2, consoleX + CONSOLE_WIDTH - 2, consoleY + messageAreaHeight);

        for (int i = startIndex; i < endIndex; i++) {
            ConsoleMessage msg = consoleMessages.get(i);
            int drawY = consoleY + 4 + (i - startIndex) * lineHeight;
            context.drawText(textRenderer, Text.literal(msg.text), consoleX + 4, drawY, COLOR_TEXT_GREEN, false);
        }

        context.disableScissor();

        if (maxScroll > 0) {
            int scrollBarHeight = Math.max(10, (int) ((float) messageAreaHeight / (consoleMessages.size() * lineHeight) * messageAreaHeight));
            int scrollBarY = consoleY + 2 + (int) ((float) consoleScrollOffset / maxScroll * (messageAreaHeight - scrollBarHeight));
            context.fill(consoleX + CONSOLE_WIDTH - 4, consoleY + 2, consoleX + CONSOLE_WIDTH - 2, consoleY + messageAreaHeight, 0x40000000);
            context.fill(consoleX + CONSOLE_WIDTH - 4, scrollBarY, consoleX + CONSOLE_WIDTH - 2, scrollBarY + scrollBarHeight, COLOR_SCROLL_BAR_GREEN);
        }
    }

    private void drawInputField(DrawContext context) {
        int inputX = x + INPUT_X;
        int inputY = y + INPUT_Y;

        context.fill(inputX, inputY, inputX + INPUT_WIDTH, inputY + INPUT_HEIGHT, COLOR_BG_MEDIUM);
        int borderColor = inputFocused ? COLOR_TEXT_GREEN : COLOR_BORDER_GRAY;
        context.drawStrokedRectangle(inputX, inputY, INPUT_WIDTH, INPUT_HEIGHT, borderColor);

        String displayText = inputText;
        if (inputFocused && (System.currentTimeMillis() / 500) % 2 == 0) {
            displayText = displayText + "_";
        }

        if (!displayText.isEmpty()) {
            context.drawText(textRenderer, Text.literal(displayText), inputX + 4, inputY + 3, COLOR_TEXT_GREEN, false);
        } else if (!inputFocused) {
            context.drawText(textRenderer, Text.literal("Type command..."), inputX + 4, inputY + 3, COLOR_TEXT_GRAY_DARK, false);
        }
    }

    private void drawDecryptionProgress(DrawContext context) {
        DecryptionManager dm = DecryptionTicker.DECRYPTION_MANAGER;

        if (!dm.hasActiveSignal()) {
            return;
        }

        int barX = x + CONSOLE_X + 5;
        int barY = y + CONSOLE_Y + CONSOLE_HEIGHT - 25;
        int barWidth = CONSOLE_WIDTH - 10;
        int barHeight = 10;

        String signalName = dm.getCurrentSignalName();
        context.drawText(textRenderer, Text.literal("§e▶ " + signalName), barX, barY - 24, COLOR_TEXT_YELLOW, false);

        if (dm.getQueueSize() > 0) {
            context.drawText(textRenderer, Text.literal("§7+" + dm.getQueueSize()), barX + barWidth - 20, barY - 24, COLOR_TEXT_GRAY, false);
        }

        context.fill(barX, barY, barX + barWidth, barY + barHeight, COLOR_BG_LIGHT);

        float progress = dm.getProgress();
        int filledWidth = (int) (barWidth * progress);

        int barColor = COLOR_TEXT_GREEN_DARK;
        if (progress < 0.3f) {
            barColor = 0xFFAA0000;
        } else if (progress < 0.7f) {
            barColor = COLOR_TEXT_YELLOW;
        }

        context.fill(barX, barY, barX + filledWidth, barY + barHeight, barColor);
        context.drawStrokedRectangle(barX, barY, barWidth, barHeight, COLOR_TEXT_GREEN);

        String progressText = String.format("%.1f%%", progress * 100);
        int textWidth = textRenderer.getWidth(progressText);
        context.drawText(textRenderer, Text.literal(progressText), barX + (barWidth - textWidth) / 2, barY + 1, COLOR_TEXT_WHITE, false);

        String maskedText = dm.getMaskedText();
        if (!maskedText.isEmpty()) {
            int textY = barY - 12;
            context.enableScissor(barX, textY - 5, barX + barWidth, textY + 15);
            renderMaskedText(context, maskedText, barX, textY, barWidth);
            context.disableScissor();
        }
    }

    private void renderMaskedText(DrawContext context, String text, int x, int y, int maxWidth) {
        int currentX = x;
        boolean obfuscated = false;
        StringBuilder currentSegment = new StringBuilder();
        int currentColor = COLOR_TEXT_GRAY;

        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);

            if (c == '§' && i + 1 < text.length()) {
                if (currentSegment.length() > 0) {
                    int color = obfuscated ? COLOR_TEXT_GRAY_DARK : currentColor;
                    context.drawText(textRenderer, Text.literal(currentSegment.toString()), currentX, y, color, false);
                    currentX += textRenderer.getWidth(currentSegment.toString());
                    currentSegment = new StringBuilder();
                }

                char formatCode = text.charAt(i + 1);
                if (formatCode == 'k') {
                    obfuscated = true;
                } else if (formatCode == 'r') {
                    obfuscated = false;
                    currentColor = COLOR_TEXT_GRAY;
                }
                i++;
            } else {
                currentSegment.append(c);
            }
        }

        if (currentSegment.length() > 0) {
            int color = obfuscated ? COLOR_TEXT_GRAY_DARK : currentColor;
            context.drawText(textRenderer, Text.literal(currentSegment.toString()), currentX, y, color, false);
        }
    }

    private void drawDecryptionPage(DrawContext context) {
        DecryptionManager dm = DecryptionTicker.DECRYPTION_MANAGER;
        int pageX = x + DECRYPT_MAIN_X;
        int pageY = y + DECRYPT_MAIN_Y;

        context.fill(pageX, pageY, pageX + DECRYPT_MAIN_WIDTH, pageY + DECRYPT_MAIN_HEIGHT, COLOR_BG_MEDIUM);
        context.drawStrokedRectangle(pageX, pageY, DECRYPT_MAIN_WIDTH, DECRYPT_MAIN_HEIGHT, COLOR_BORDER_CYAN);

        context.drawText(textRenderer, Text.literal("§b=== SIGNAL DECRYPTION ==="), pageX + 5, pageY + 3, COLOR_TEXT_CYAN, false);

        if (!dm.hasActiveSignal() && dm.getDecryptedHistory().isEmpty()) {
            String msg = "No signals detected";
            int textWidth = textRenderer.getWidth(msg);
            context.drawText(textRenderer, Text.literal(msg), pageX + (DECRYPT_MAIN_WIDTH - textWidth) / 2, pageY + DECRYPT_MAIN_HEIGHT / 2, COLOR_TEXT_GRAY, false);
            return;
        }

        SignalData signal = dm.getCurrentSignal();
        if (signal == null && !dm.getDecryptedHistory().isEmpty()) {
            signal = dm.getDecryptedHistory().get(dm.getDecryptedHistory().size() - 1).signal();
        }

        if (signal != null) {
            context.drawText(textRenderer, Text.literal("§eSignal: " + signal.id()), pageX + 5, pageY + 15, COLOR_TEXT_YELLOW, false);

            int imgX = x + IMAGE_X;
            int imgY = y + IMAGE_Y;
            context.fill(imgX, imgY, imgX + IMAGE_SIZE, imgY + IMAGE_SIZE, COLOR_BG_BLUE);
            context.drawStrokedRectangle(imgX, imgY, IMAGE_SIZE, IMAGE_SIZE, COLOR_BORDER_BLUE);

            if (signal.hasImage()) {
                drawPixelatedImage(context, signal.imagePath(), imgX, imgY, IMAGE_SIZE, dm.hasActiveSignal() ? dm.getImageQuality() : 100);
            } else {
                String noImgText = "IMAGE";
                String noImgText2 = "NOT FOUND";
                int textW1 = textRenderer.getWidth(noImgText);
                int textW2 = textRenderer.getWidth(noImgText2);
                context.drawText(textRenderer, Text.literal(noImgText), imgX + (IMAGE_SIZE - textW1) / 2, imgY + IMAGE_SIZE/2 - 10, COLOR_TEXT_BLUE, false);
                context.drawText(textRenderer, Text.literal(noImgText2), imgX + (IMAGE_SIZE - textW2) / 2, imgY + IMAGE_SIZE/2 + 2, COLOR_TEXT_BLUE, false);
            }

            int textX = x + SIGNAL_TEXT_X;
            int textY = y + SIGNAL_TEXT_Y;
            context.fill(textX, textY, textX + SIGNAL_TEXT_WIDTH, textY + SIGNAL_TEXT_HEIGHT, COLOR_BG_BLUE_DARK);
            context.drawStrokedRectangle(textX, textY, SIGNAL_TEXT_WIDTH, SIGNAL_TEXT_HEIGHT, COLOR_BORDER_BLUE);

            context.enableScissor(textX + 2, textY + 2, textX + SIGNAL_TEXT_WIDTH - 2, textY + SIGNAL_TEXT_HEIGHT - 2);

            String displayText;
            if (dm.hasActiveSignal()) {
                displayText = dm.getMaskedText();
            } else {
                displayText = signal.content();
            }

            List<String> lines = wrapText(displayText, SIGNAL_TEXT_WIDTH - 10);
            for (int i = 0; i < lines.size(); i++) {
                context.drawText(textRenderer, Text.literal(lines.get(i)), textX + 5, textY + 5 + i * 11, COLOR_TEXT_GRAY, false);
            }

            context.disableScissor();

            if (dm.hasActiveSignal()) {
                int barX = textX;
                int barY = textY + SIGNAL_TEXT_HEIGHT + 3;
                int barWidth = SIGNAL_TEXT_WIDTH;
                int barHeight = 6;

                context.fill(barX, barY, barX + barWidth, barY + barHeight, COLOR_BG_LIGHT);
                float progress = dm.getProgress();
                context.fill(barX, barY, barX + (int)(barWidth * progress), barY + barHeight, COLOR_TEXT_GREEN_DARK);
                context.drawStrokedRectangle(barX, barY, barWidth, barHeight, COLOR_TEXT_GREEN);
            }
        }

        int histX = x + HISTORY_X;
        int histY = y + HISTORY_Y;
        context.fill(histX, histY, histX + HISTORY_WIDTH, histY + HISTORY_HEIGHT, COLOR_BG_BLUE_DARK);
        context.drawStrokedRectangle(histX, histY, HISTORY_WIDTH, HISTORY_HEIGHT, COLOR_BORDER_BLUE);
        context.drawText(textRenderer, Text.literal("§7--- HISTORY ---"), histX + 5, histY - 10, COLOR_TEXT_GRAY, false);

        List<DecryptionManager.DecryptedSignal> history = dm.getDecryptedHistory();
        if (!history.isEmpty()) {
            context.enableScissor(histX + 2, histY + 2, histX + HISTORY_WIDTH - 2, histY + HISTORY_HEIGHT - 2);

            int maxScroll = Math.max(0, history.size() * 24 - HISTORY_HEIGHT + 5);
            historyScrollOffset = Math.min(historyScrollOffset, maxScroll);

            int startIdx = historyScrollOffset / 24;
            for (int i = startIdx; i < Math.min(history.size(), startIdx + 3); i++) {
                DecryptionManager.DecryptedSignal ds = history.get(history.size() - 1 - i);
                int y = histY + 5 + (i - startIdx) * 24;

                context.drawText(textRenderer, Text.literal("§a✓ " + ds.signal().id()), histX + 5, y, COLOR_TEXT_GREEN, false);

                String preview = ds.signal().content();
                if (preview.length() > 28) preview = preview.substring(0, 28) + "...";
                context.drawText(textRenderer, Text.literal("§7" + preview), histX + 5, y + 11, COLOR_TEXT_GRAY, false);
            }

            context.disableScissor();
        }
    }

    private void drawMiniConsole(DrawContext context) {
        int consoleX = x + MINI_CONSOLE_X;
        int consoleY = y + MINI_CONSOLE_Y;

        context.fill(consoleX, consoleY, consoleX + MINI_CONSOLE_WIDTH, consoleY + MINI_CONSOLE_HEIGHT, COLOR_BG_MEDIUM);
        int borderColor = inputFocused ? COLOR_BORDER_CYAN : COLOR_BORDER_BLUE;
        context.drawStrokedRectangle(consoleX, consoleY, MINI_CONSOLE_WIDTH, MINI_CONSOLE_HEIGHT, borderColor);

        String displayText = inputText;
        if (inputFocused && (System.currentTimeMillis() / 500) % 2 == 0) {
            displayText = displayText + "_";
        }

        if (!displayText.isEmpty()) {
            context.drawText(textRenderer, Text.literal(displayText), consoleX + 3, consoleY + 2, 0xFFAAAAFF, false);
        } else if (!inputFocused) {
            context.drawText(textRenderer, Text.literal(">"), consoleX + 3, consoleY + 2, COLOR_TEXT_BLUE, false);
        }
    }

    private void drawPixelatedImage(DrawContext context, String path, int x, int y, int size, int quality) {
        int pixels = Math.max(2, (int)(size * quality / 100f));
        int pixelSize = size / pixels;

        for (int px = 0; px < pixels; px++) {
            for (int py = 0; py < pixels; py++) {
                int gray = (px * 37 + py * 73) & 0xFF;
                int color = 0xFF000000 | (gray << 16) | (gray << 8) | gray;

                if (new Random(px * 1000L + py).nextFloat() > quality / 100f) {
                    color = 0xFF111133;
                }

                context.fill(x + px * pixelSize, y + py * pixelSize,
                        x + (px + 1) * pixelSize, y + (py + 1) * pixelSize, color);
            }
        }
    }

    private List<String> wrapText(String text, int maxWidth) {
        List<String> lines = new ArrayList<>();
        StringBuilder current = new StringBuilder();

        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);
            if (c == '§' && i + 1 < text.length()) {
                current.append(c).append(text.charAt(i + 1));
                i++;
                continue;
            }
            current.append(c);
            if (textRenderer.getWidth(current.toString()) > maxWidth) {
                String line = current.substring(0, current.length() - 1);
                lines.add(line);
                current = new StringBuilder().append(c);
            }
        }
        if (current.length() > 0) lines.add(current.toString());
        return lines;
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
        if (currentTab == 0) {
            if (isMouseOver(mouseX, mouseY, SERVER_LIST_X, SERVER_LIST_Y, SERVER_LIST_WIDTH, SERVER_LIST_HEIGHT)) {
                int maxScroll = Math.max(0, servers.size() * SERVER_ENTRY_HEIGHT - SERVER_LIST_HEIGHT);
                serverScrollOffset = (int) Math.max(0, Math.min(maxScroll, serverScrollOffset - verticalAmount * 15));
                return true;
            }
            if (isMouseOver(mouseX, mouseY, CONSOLE_X, CONSOLE_Y, CONSOLE_WIDTH, CONSOLE_HEIGHT)) {
                int maxScroll = Math.max(0, consoleMessages.size() * 11 - CONSOLE_HEIGHT + 44);
                consoleScrollOffset = (int) Math.max(0, Math.min(maxScroll, consoleScrollOffset - verticalAmount * 15));
                return true;
            }
        } else {
            if (isMouseOver(mouseX, mouseY, HISTORY_X, HISTORY_Y, HISTORY_WIDTH, HISTORY_HEIGHT)) {
                DecryptionManager dm = DecryptionTicker.DECRYPTION_MANAGER;
                int maxScroll = Math.max(0, dm.getDecryptedHistory().size() * 24 - HISTORY_HEIGHT + 5);
                historyScrollOffset = (int) Math.max(0, Math.min(maxScroll, historyScrollOffset - verticalAmount * 15));
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean mouseClicked(Click click, boolean doubled) {
        if (currentTab == 0) {
            inputFocused = isMouseOver(click.x(), click.y(), INPUT_X, INPUT_Y, INPUT_WIDTH, INPUT_HEIGHT);
        } else {
            inputFocused = isMouseOver(click.x(), click.y(), MINI_CONSOLE_X, MINI_CONSOLE_Y, MINI_CONSOLE_WIDTH, MINI_CONSOLE_HEIGHT);
        }
        return super.mouseClicked(click, doubled);
    }

    @Override
    public boolean keyPressed(KeyInput input) {
        if (inputFocused) {
            int key = input.key();

            if (key == GLFW.GLFW_KEY_ENTER || key == GLFW.GLFW_KEY_KP_ENTER) {
                if (!this.inputText.isEmpty()) {
                    addConsoleMessage("> " + this.inputText);
                    processCommand(this.inputText);
                    this.inputText = "";
                }
                return true;
            }

            if (key == GLFW.GLFW_KEY_BACKSPACE) {
                if (!this.inputText.isEmpty()) {
                    this.inputText = this.inputText.substring(0, this.inputText.length() - 1);
                }
                return true;
            }

            if (key == GLFW.GLFW_KEY_ESCAPE) {
                inputFocused = false;
                return true;
            }

            if (key == GLFW.GLFW_KEY_E || key == GLFW.GLFW_KEY_L || key == GLFW.GLFW_KEY_T ||
                    key == GLFW.GLFW_KEY_SLASH || key == GLFW.GLFW_KEY_TAB) {
                return true;
            }

            return true;
        }

        if (input.isEscape() || input.key() == GLFW.GLFW_KEY_E) {
            this.close();
            return true;
        }

        return super.keyPressed(input);
    }

    @Override
    public boolean charTyped(CharInput input) {
        if (!inputFocused) {
            return super.charTyped(input);
        }

        char c = (char) input.codepoint();

        if (input.isValidChar() && c != '`' && c != '§' && c >= 32) {
            this.inputText += c;
            return true;
        }

        return true;
    }

    private void processCommand(String command) {
        String[] parts = command.toLowerCase().trim().split("\\s+");
        if (parts.length == 0 || parts[0].isEmpty()) return;

        switch (parts[0]) {
            case "help":
                addConsoleMessage("§e=== Commands ===");
                addConsoleMessage("§ahelp §7- Show this help");
                addConsoleMessage("§alist §7- List all servers");
                addConsoleMessage("§aclear §7- Clear console");
                addConsoleMessage("§asignal §7- Show decryption status");
                addConsoleMessage("§adecrypt §7- Switch to decryption tab");
                addConsoleMessage("§aconsole §7- Switch to console tab");
                break;
            case "list":
                addConsoleMessage("§e=== Servers (" + servers.size() + ") ===");
                for (int i = 0; i < servers.size(); i++) {
                    ServerBlockEntity server = servers.get(i);
                    BlockPos pos = server.getPos();
                    String status = server.isBroken() ? "§cOFF" : "§aON";
                    addConsoleMessage(String.format("%d: %s [%s]", i + 1, pos.toShortString(), status));
                }
                break;
            case "clear":
                consoleMessages.clear();
                addConsoleMessage("§aConsole cleared");
                break;
            case "signal":
                DecryptionManager dm = DecryptionTicker.DECRYPTION_MANAGER;
                if (dm.hasActiveSignal()) {
                    addConsoleMessage("§eActive signal: " + dm.getCurrentSignalName());
                    addConsoleMessage("§7Progress: " + String.format("%.1f%%", dm.getProgress() * 100));
                    addConsoleMessage("§7Queue: " + dm.getQueueSize());
                } else {
                    addConsoleMessage("§7No active signals");
                }
                break;
            case "decrypt":
                currentTab = 1;
                addConsoleMessage("§aSwitched to decryption tab");
                break;
            case "console":
                currentTab = 0;
                addConsoleMessage("§aSwitched to console tab");
                break;
            default:
                addConsoleMessage("§cUnknown command: " + parts[0]);
                addConsoleMessage("§7Type 'help' for available commands");
        }
    }

    public void addConsoleMessage(String message) {
        consoleMessages.add(new ConsoleMessage(message, System.currentTimeMillis()));
        consoleScrollOffset = Integer.MAX_VALUE;
    }

    private boolean isMouseOver(double mouseX, double mouseY, int relX, int relY, int width, int height) {
        return mouseX >= x + relX && mouseX < x + relX + width
                && mouseY >= y + relY && mouseY < y + relY + height;
    }

    @Override
    protected void drawForeground(DrawContext context, int mouseX, int mouseY) {}

    @Override
    public void close() {
        if (client != null && client.player != null) {
            screenHandler.clearComputerUser(client.player);
        }
        super.close();
    }

    @Override
    public void removed() {
        if (client != null && client.player != null) {
            screenHandler.clearComputerUser(client.player);
        }
        super.removed();
    }

    private static class ConsoleMessage {
        final String text;
        final long timestamp;

        ConsoleMessage(String text, long timestamp) {
            this.text = text;
            this.timestamp = timestamp;
        }
    }
}