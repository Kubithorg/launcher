package fr.ironcraft.kubithon.launcher;

import fr.theshark34.swinger.Swinger;
import fr.theshark34.swinger.util.WindowMover;
import java.awt.Color;
import javax.swing.JFrame;

public class LauncherFrame extends JFrame
{
    public static final Color BACKGROUND = new Color(60, 105, 148);

    private WindowMover mover = new WindowMover(this);

    public LauncherFrame()
    {
        this.setTitle("Kubithon");
        this.setUndecorated(true);
        this.setSize(450, 150);
        this.setResizable(false);
        this.setLocationRelativeTo(null);
        this.setDefaultCloseOperation(EXIT_ON_CLOSE);

        boolean translucent = false;
        try
        {
            translucent = getGraphicsConfiguration().isTranslucencyCapable();
        }
        catch (Throwable e)
        {
        }

        if (translucent)
        {
            this.setBackground(Swinger.TRANSPARENT);
        }
        else
        {
            this.setBackground(Color.WHITE);
        }

        this.addMouseListener(mover);
        this.addMouseMotionListener(mover);

        this.setContentPane(new LauncherPanel(translucent));
    }
}
