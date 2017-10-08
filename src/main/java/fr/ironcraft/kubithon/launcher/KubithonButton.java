package fr.ironcraft.kubithon.launcher;

import fr.theshark34.swinger.colored.SColoredButton;
import java.awt.Color;
import java.awt.Graphics;
import javax.swing.Icon;
import jiconfont.IconCode;
import jiconfont.swing.IconFontSwing;

public class KubithonButton extends SColoredButton
{
    private static final Color COLOR = new Color(45, 45, 45);
    private static final Color HOVER_COLOR = new Color(50, 50, 50);

    private Icon icon;

    public KubithonButton(IconCode icon)
    {
        this((String) null);
        this.icon = IconFontSwing.buildIcon(icon, 18, Color.WHITE);
    }

    public KubithonButton(String text)
    {
        super(COLOR, HOVER_COLOR);

        if (text != null)
        {
            this.setText(text);
        }

        this.setBorder(null);
        this.setTextColor(Color.WHITE);
        this.setFont(LauncherPanel.FONT);
    }

    @Override
    public void paintComponent(Graphics g)
    {
        super.paintComponent(g);

        if (icon != null)
        {
            icon.paintIcon(this, g, getWidth() / 2 - icon.getIconWidth() / 2, getHeight() / 2 - icon.getIconHeight() / 2);
        }
    }
}
