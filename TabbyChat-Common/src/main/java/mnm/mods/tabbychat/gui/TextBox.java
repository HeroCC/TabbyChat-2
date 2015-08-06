package mnm.mods.tabbychat.gui;

import java.awt.Dimension;
import java.util.List;

import mnm.mods.tabbychat.api.Channel;
import mnm.mods.tabbychat.api.TabbyAPI;
import mnm.mods.tabbychat.core.GuiChatTC;
import mnm.mods.util.Color;
import mnm.mods.util.gui.GuiText;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiTextField;

public class TextBox extends ChatGui {

    private FontRenderer fr = mc.fontRendererObj;
    // Dummy textField
    private GuiText textField = new GuiText();
    private int cursorCounter;

    public TextBox() {
        super();
        textField.getTextField().setMaxStringLength(300);
        textField.setFocused(true);
        textField.getTextField().setCanLoseFocus(false);
    }

    @Override
    public void onClosed() {
        this.textField.setValue("");
        super.onClosed();
    }

    @Override
    public void drawComponent(int mouseX, int mouseY) {
        Gui.drawRect(0, 0, getBounds().width, getBounds().height, getBackColor());
        drawText();
        drawCursor();
        drawBorders(0, 0, getBounds().width, getBounds().height);
        super.drawComponent(mouseX, mouseY);

    }

    private void drawCursor() {
        boolean cursorBlink = this.cursorCounter / 6 % 2 == 0;
        if (cursorBlink) {
            char marker;
            int xPos = 0;
            int yPos = 2;
            int counter = -1;
            List<String> list = getWrappedLines();
            GuiTextField textField = this.textField.getTextField();
            int size = textField.getSelectedText().length();
            if (textField.getSelectionEnd() < textField.getCursorPosition()) {
                size *= -1;
            }

            // Count up to the target position.
            countLoop: for (String text : list) {
                for (char c : text.concat(" ").toCharArray()) {
                    counter++;
                    if (counter >= textField.getCursorPosition() + size) {
                        break countLoop;
                    }
                    xPos += fr.getCharWidth(c);
                }
                xPos = 0;
                yPos += fr.FONT_HEIGHT + 2;
            }

            if (textField.getCursorPosition() + size < this.textField
                    .getValue().length()) {
                marker = '|';
            } else {
                marker = '_';
                xPos += 1;
            }
            fr.drawString(Character.toString(marker), xPos, yPos, 0xeeeeee);
        }
    }

    private void drawText() {
        // selection
        boolean started = false;
        boolean ended = false;
        GuiTextField textField = this.textField.getTextField();

        int yPos = 2;
        int pos = 0;
        for (String line : getWrappedLines()) {
            int xPos = 1;
            for (Character c : line.toCharArray()) {
                fr.drawString(c.toString(), xPos, yPos, getForeColor());
                int width = fr.getCharWidth(c);
                int cursorPos = textField.getCursorPosition();
                int selectDist = textField.getSelectedText().length();
                if (textField.getSelectionEnd() < textField.getCursorPosition()) {
                    selectDist *= -1;
                }
                if (textField.getSelectedText().length() > 0) {
                    if (!started && pos == Math.min(cursorPos, cursorPos + selectDist)) {
                        // Mark for highlighting
                        started = true;
                    }

                    if (started && !ended) {
                        Gui.drawRect(xPos, yPos - 1, xPos + width, yPos + fr.FONT_HEIGHT + 1,
                                getBackColor());
                    }

                    if (!ended && pos == Math.max(cursorPos, selectDist + cursorPos) - 1) {
                        // unmark for highlighting
                        ended = true;
                    }
                }
                xPos += width;
                pos++;
            }
            yPos += fr.FONT_HEIGHT + 2;
        }
        // write the num of sends
        Channel active = TabbyAPI.getAPI().getChat().getActiveChannel();
        String[] msg = GuiChatTC.processSends(textField.getText(), active.getPrefix(), active.isPrefixHidden());
        int size = msg != null ? msg.length : 0;
        if (size > 0) {
            int color = 0x666666;
            if (!textField.getText().endsWith(msg[size - 1])) {
                // WARNING! Message will get cut off!
                color = 0xff6666;
            }
            int sizeW = fr.getStringWidth(size + "");
            fr.drawString(size + "", getBounds().width - sizeW, 2, color);
        }
    }

    @Override
    public void updateComponent() {
        super.updateComponent();
        this.cursorCounter++;

        List<String> list = getWrappedLines();
        int newHeight = Math.max(1, list.size()) * (fr.FONT_HEIGHT + 2);
        // int newY = getBounds().y + getBounds().height - newHeight;
        this.setSize(getMinimumSize().width, newHeight);

        Color color = new Color(getParent().getBackColor());
        Color bkg = new Color(color.getRed(), color.getGreen(), color.getBlue(),
                color.getAlpha() / 4 * 3);
        this.setBackColor(bkg.getColor());
    }

    public List<String> getWrappedLines() {
        return fr.listFormattedStringToWidth(textField.getValue(), getBounds().width);
    }

    @Override
    public Dimension getMinimumSize() {
        return new Dimension(100, (fr.FONT_HEIGHT + 2) * getWrappedLines().size());
    }

    public GuiText getTextField() {
        return textField;
    }

    public void mouseClicked(int x, int y, int mouseButton) {
        if (mouseButton == 0) {
            int xPos = this.getActualPosition().x;
            int yPos = this.getActualPosition().y;
            int width = this.getBounds().width;
            int row = (y - yPos) / (fr.FONT_HEIGHT + 2);
            int col = x - xPos;

            List<String> lines = getWrappedLines();
            if (row < 0 || row >= lines.size() || col < 0 || col > width) {
                return;
            }
            int index = 0;
            for (int i = 0; i < row; i++) {
                index += lines.get(i).length();
            }
            index += fr.trimStringToWidth(lines.get(row), col).length();
            textField.getTextField().setCursorPosition(index + 1);
        }
    }

}
