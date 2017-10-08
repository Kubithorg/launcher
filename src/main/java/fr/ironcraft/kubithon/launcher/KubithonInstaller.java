package fr.ironcraft.kubithon.launcher;

import fr.theshark34.swinger.Swinger;
import jiconfont.icons.FontAwesome;
import jiconfont.swing.IconFontSwing;

public class KubithonInstaller
{
    public static final String VERSION = "1.0.1";

    public static void main(String[] args)
    {
        Swinger.setSystemLookNFeel();
        IconFontSwing.register(FontAwesome.getIconFont());
        Swinger.setResourcePath("/res");

        Launcher.KUBITHON_DIR.mkdirs();

        LauncherFrame frame = new LauncherFrame();
        frame.setVisible(true);
    }
}
