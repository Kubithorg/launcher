package fr.ironcraft.kubithon.launcher;

import fr.theshark34.swinger.Swinger;
import java.awt.Graphics;
import javax.swing.JLabel;

public class AntialiasingLabel extends JLabel
{
    @Override
    protected void paintComponent(Graphics g)
    {
        Swinger.activateAntialias(g);
        super.paintComponent(g);
    }
}
