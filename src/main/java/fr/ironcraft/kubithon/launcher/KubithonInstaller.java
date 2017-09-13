package fr.ironcraft.kubithon.launcher;

import fr.theshark34.swinger.Swinger;

public class KubithonInstaller
{
    public static final String VERSION = "1.0.0";

    public static void main(String[] args)
    {
        Swinger.setSystemLookNFeel();
        Swinger.setResourcePath("/res");

        Downloader.FOLDER.mkdirs();

        LauncherFrame frame = new LauncherFrame();
        frame.setVisible(true);
    }
}
