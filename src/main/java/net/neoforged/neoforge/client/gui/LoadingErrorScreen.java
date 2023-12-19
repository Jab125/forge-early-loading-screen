/*
 * Copyright (c) Forge Development LLC and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.client.gui;

import com.google.common.base.Strings;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.ModContainer;
import net.fabricmc.loader.impl.FabricLoaderImpl;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.FatalErrorScreen;
import net.minecraft.client.gui.widget.AlwaysSelectedEntryListWidget;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.resource.language.I18n;
import net.minecraft.text.OrderedText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Util;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Supplier;

public class LoadingErrorScreen extends FatalErrorScreen {
    private static final Logger LOGGER = LogManager.getLogger();
    private final Path modsDir;
    private final Path logFile;
    private final Map<ModContainer, List<Throwable>> modLoadErrors;
    private final List<?> modLoadWarnings;
    private final Path dumpedLocation;
    private LoadingEntryList entryList;
    private Text errorHeader;
    private Text warningHeader;

    public LoadingErrorScreen(Map<ModContainer, List<Throwable>> loadingException, List<?> warnings, final File dumpedLocation) {
        super(Text.literal("Loading Error"), null);
        this.modLoadWarnings = warnings;
        this.modLoadErrors = loadingException == null ? Collections.emptyMap() : loadingException;
        this.modsDir = FabricLoaderImpl.INSTANCE.getModsDirectory().toPath();
        this.logFile = FabricLoader.getInstance().getGameDir().resolve(Paths.get("logs", "latest.log"));
        this.dumpedLocation = dumpedLocation != null ? dumpedLocation.toPath() : null;
    }

    @Override
    public void init() {
        super.init();
        this.clearChildren();

        this.errorHeader = Text.literal(Formatting.RED + I18n.translate("fml.loadingerrorscreen.errorheader", this.modLoadErrors.size()) + Formatting.RESET);
        this.warningHeader = Text.literal(Formatting.YELLOW + I18n.translate("fml.loadingerrorscreen.warningheader", this.modLoadErrors.size()) + Formatting.RESET);

        int yOffset = 46;
        this.addDrawableChild(new ButtonWidget(50, this.height - yOffset, this.width / 2 - 55, 20, Text.literal(I18n.translate("fml.button.open.mods.folder")), b -> Util.getOperatingSystem().open(modsDir.toFile()), Supplier::get){});
        this.addDrawableChild(new ButtonWidget(this.width / 2 + 5, this.height - yOffset, this.width / 2 - 55, 20, Text.literal(I18n.translate("fml.button.open.file", logFile.getFileName())), b -> Util.getOperatingSystem().open(logFile.toFile()), Supplier::get){});
        if (this.modLoadErrors.isEmpty()) {
            this.addDrawableChild(new ButtonWidget(this.width / 4, this.height - 24, this.width / 2, 20, Text.literal(I18n.translate("fml.button.continue.launch")), b -> {
                this.client.setScreen(null);
            }, Supplier::get){});
        } else {
            this.addDrawableChild(new ButtonWidget(this.width / 4, this.height - 24, this.width / 2, 20, Text.literal(I18n.translate("fml.button.open.file", dumpedLocation.getFileName())), b -> Util.getOperatingSystem().open(dumpedLocation.toFile()), Supplier::get){});
        }

        this.entryList = new LoadingEntryList(this, this.modLoadErrors, this.modLoadWarnings);
        this.addSelectableChild(this.entryList);
        this.setFocused(this.entryList);
    }

    @Override
    public void render(DrawContext guiGraphics, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(guiGraphics, mouseX, mouseY, partialTick);
        this.entryList.render(guiGraphics, mouseX, mouseY, partialTick);
        drawMultiLineCenteredString(guiGraphics, textRenderer, this.modLoadErrors.isEmpty() ? warningHeader : errorHeader, this.width / 2, 10);
        this.drawables.forEach(button -> button.render(guiGraphics, mouseX, mouseY, partialTick));
    }

    private void drawMultiLineCenteredString(DrawContext guiGraphics, TextRenderer fr, Text str, int x, int y) {
        for (OrderedText s : fr.wrapLines(str, this.width)) {
            guiGraphics.drawText(fr, s, (int)(x - fr.getWidth(s) / 2.0), y, 0xFFFFFF, false);
            y += fr.fontHeight;
        }
    }

    public static class LoadingEntryList extends AlwaysSelectedEntryListWidget<LoadingEntryList.LoadingMessageEntry> {
        LoadingEntryList(final LoadingErrorScreen parent, final Map<ModContainer, List<Throwable>> errors, final List<?> warnings) {
            super(Objects.requireNonNull(parent.client), parent.width, parent.height - 50, 35,
                    Math.max(
                            errors.entrySet().stream().mapToInt(error -> parent.textRenderer.wrapLines(Text.literal("%s (%s) has failed to load correctly\n\u00a77%s".formatted(error.getKey().getMetadata().getName(), error.getKey().getMetadata().getId(), error.getValue().get(0).getMessage() != null ? error.getValue().get(0).getMessage() : "")), parent.width - 20).size()).max().orElse(0),
                            0));//warnings.stream().mapToInt(warning -> parent.textRenderer.wrapLines(Text.literal(warning.formatToString() != null ? warning.formatToString() : ""), parent.width - 20).size()).max().orElse(0)) * parent.minecraft.font.lineHeight + 8);
            boolean both = !errors.isEmpty() && !warnings.isEmpty();
            if (both)
                addEntry(new LoadingMessageEntry(parent.errorHeader, true));
            errors.entrySet().forEach(e -> e.getValue().forEach(d -> addEntry(new LoadingMessageEntry(Text.literal("%s (%s) has failed to load correctly\n\u00a77%s".formatted(e.getKey().getMetadata().getName(), e.getKey().getMetadata().getId(), d.getMessage() != null ? d.getMessage() : ""))))));
            if (both) {
                int maxChars = (this.width - 10) / parent.client.textRenderer.getWidth("-");
                addEntry(new LoadingMessageEntry(Text.literal("\n" + Strings.repeat("-", maxChars) + "\n")));
                addEntry(new LoadingMessageEntry(parent.warningHeader, true));
            }
            warnings.forEach(w -> addEntry(new LoadingMessageEntry(Text.literal(w.toString()))));
        }

        @Override
        protected int getScrollbarPositionX() {
            return this.getRight() - 6;
        }

        @Override
        public int getRowWidth() {
            return this.width;
        }

        public class LoadingMessageEntry extends AlwaysSelectedEntryListWidget.Entry<LoadingMessageEntry> {
            private final Text message;
            private final boolean center;

            LoadingMessageEntry(final Text message) {
                this(message, false);
            }

            LoadingMessageEntry(final Text message, final boolean center) {
                this.message = Objects.requireNonNull(message);
                this.center = center;
            }

            @Override
            public Text getNarration() {
                return Text.translatable("narrator.select", message);
            }

            @Override
            public void render(DrawContext guiGraphics, int entryIdx, int top, int left, final int entryWidth, final int entryHeight, final int mouseX, final int mouseY, final boolean p_194999_5_, final float partialTick) {
                TextRenderer font = MinecraftClient.getInstance().textRenderer;
                final List<OrderedText> strings = font.wrapLines(message, LoadingEntryList.this.width - 20);
                int y = top + 2;
                for (OrderedText string : strings) {
                    if (center)
                        guiGraphics.drawText(font, string, (int) (left + (width) - font.getWidth(string) / 2F), y, 0xFFFFFF, false);
                    else
                        guiGraphics.drawText(font, string, left + 5, y, 0xFFFFFF, false);
                    y += font.fontHeight;
                }
            }
        }

    }
}
