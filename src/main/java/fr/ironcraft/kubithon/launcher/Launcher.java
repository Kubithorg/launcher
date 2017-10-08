package fr.ironcraft.kubithon.launcher;

import fr.theshark34.openlauncherlib.minecraft.util.GameDirGenerator;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URL;
import java.nio.charset.Charset;
import javax.swing.JOptionPane;
import org.apache.commons.io.IOUtils;
import org.json.JSONObject;

public class Launcher
{
    public static final File KUBITHON_DIR = GameDirGenerator.createGameDir("kubithon");

    private LauncherPanel panel;

    public Launcher(LauncherPanel panel)
    {
        this.panel = panel;
    }

    public void nonPremium()
    {
        panel.setStatus("Mise à jour", LauncherPanel.BLUE);
        panel.getProgressBar().setVisible(true);
        panel.getProgressBar().setMaximum(100);
        panel.getProgressBar().setValue(47);

        try
        {
            Thread.sleep(250000L);
        }
        catch (InterruptedException e)
        {
            e.printStackTrace();
        }
    }

    public void premium()
    {
        panel.setStatus("Lecture des infos de version", LauncherPanel.BLUE);
        panel.getProgressBar().setVisible(true);

        File version = new File(Downloader.MINECRAFT_FOLDER, "versions/Kubithon/Kubithon.json");
        version.getParentFile().mkdirs();

        try
        {
            IOUtils.copy(new URL(Downloader.VERSION_FILE).openStream(), new FileOutputStream(version));
        }
        catch (IOException e)
        {
            error("Impossible de télécharger le fichier de version", e);
            return;
        }

        Downloader downloader = new Downloader(panel);
        try
        {
            downloader.start();
        }
        catch (IOException e)
        {
            error("Impossible de télécharger les mods", e);
            return;
        }

        panel.setStatus("Installation du profil", LauncherPanel.BLUE);

        File launcherProfiles = new File(Downloader.MINECRAFT_FOLDER, "launcher_profiles.json");

        try
        {
            JSONObject object = new JSONObject(IOUtils.toString(new FileInputStream(launcherProfiles), Charset.defaultCharset()));
            JSONObject profiles = object.getJSONObject("profiles");

            if (!profiles.has("Kubithon"))
            {
                JSONObject kubithon = new JSONObject("{ \"name\": \"Kubithon\", \"gameDir\": \"" + KUBITHON_DIR.getAbsolutePath().replace("\\", "\\\\") + "\", \"lastVersionId\": \"Kubithon\", \"useHopperCrashService\": false, \"type\": \"custom\" }");
                profiles.put("Kubithon", kubithon);

                object.put("selectedProfile", "Kubithon");

                BufferedWriter writer = new BufferedWriter(new FileWriter(launcherProfiles));
                object.write(writer, 2, 0);

                IOUtils.closeQuietly(writer);
            }

            panel.setStatus("Terminé", "#22ee22");
        }
        catch (IOException e)
        {
            error("Impossible de modifier les profils du launcher !\nVous devez avoir installé Minecraft 1.10.2 avant de lancer l'installeur !", e);
        }
    }

    private void error(String error, Exception e)
    {
        JOptionPane.showMessageDialog(panel, "Erreur : " + error, "Erreur", JOptionPane.ERROR_MESSAGE);
        panel.setStatus("Erreur", LauncherPanel.RED);

        e.printStackTrace();
    }
}
