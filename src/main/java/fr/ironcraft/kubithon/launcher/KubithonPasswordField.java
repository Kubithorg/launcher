package fr.ironcraft.kubithon.launcher;

import fr.theshark34.swinger.Swinger;

import javax.swing.*;
import java.awt.*;

public class KubithonPasswordField extends JPasswordField
{
    private String placeholder;

    public KubithonPasswordField(String placeholder)
    {
        this("", placeholder);
    }

    public KubithonPasswordField(String text, String placeholder)
    {
        super(text);
        this.placeholder = placeholder;

        this.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, Color.WHITE));
        this.setHorizontalAlignment(SwingConstants.CENTER);
        this.setBackground(Swinger.TRANSPARENT);
        this.setOpaque(false);
        this.setForeground(Color.WHITE);
        this.setCaretColor(this.getForeground());
        this.setCaretPosition(this.getText().length());
        this.setFont(this.getFont().deriveFont(16f));
    }

    @Override
    protected void paintComponent(Graphics g)
    {
        super.paintComponent(g);

        if (placeholder.length() == 0 || getText().length() > 0)
        {
            return;
        }

        final Graphics2D g2 = (Graphics2D) g;

        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setColor(getDisabledTextColor());
        g2.drawString(placeholder, getInsets().left, g.getFontMetrics().getMaxAscent() + getInsets().top);
    }

    public void setPlaceholder(String placeholder)
    {
        this.placeholder = placeholder;
    }

    public String getPlaceholder()
    {
        return placeholder;
    }
}
