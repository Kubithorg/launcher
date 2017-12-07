package fr.ironcraft.kubithon.launcher;

import fr.ironcraft.kubithon.launcher.update.Downloader;
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
    public static final File MINECRAFT_DIR = GameDirGenerator.createGameDir("minecraft");
    public static final File KUBITHON_DIR = GameDirGenerator.createGameDir("kubithon");

    private LauncherPanel panel;

    public Launcher(LauncherPanel panel)
    {
        this.panel = panel;
    }

    public void nonPremium()
    {
        panel.setStatus("Authentification...", LauncherPanel.BLUE);
        // TODO

        panel.setStatus("Listage des fichiers", LauncherPanel.BLUE);
        panel.getProgressBar().setVisible(true);

        Downloader downloader = new Downloader(panel);

        try
        {
            downloader.addAssets();
            downloader.addLibs();
            downloader.addMods();
            downloader.addMainJar();
        }
        catch (IOException e)
        {
            error("Impossible d'établir la liste des fichiers", e);
            return;
        }

        panel.setStatus("Téléchargement", LauncherPanel.BLUE);
        downloader.download();

        panel.setStatus("Lancement...", LauncherPanel.BLUE);
    }

    public void premium()
    {
        panel.setStatus("Lecture des infos de version", LauncherPanel.BLUE);
        panel.getProgressBar().setVisible(true);

        File version = new File(MINECRAFT_DIR, "versions/Kubithon/Kubithon.json");
        version.getParentFile().mkdirs();

        try
        {
            IOUtils.copy(new URL(Downloader.KUBITHON_INDEX).openStream(), new FileOutputStream(version));
        }
        catch (IOException e)
        {
            error("Impossible de télécharger le fichier de version", e);
            return;
        }

        Downloader downloader = new Downloader(panel);
        try
        {
            downloader.addMods();
        }
        catch (IOException e)
        {
            error("Impossible d'établir la liste des fichiers", e);
            return;
        }

        panel.setStatus("Téléchargement", LauncherPanel.BLUE);
        downloader.download();

        panel.setStatus("Installation du profil", LauncherPanel.BLUE);

        File launcherProfiles = new File(MINECRAFT_DIR, "launcher_profiles.json");

        try
        {
            JSONObject object = new JSONObject(IOUtils.toString(new FileInputStream(launcherProfiles), Charset.defaultCharset()));
            JSONObject profiles = object.getJSONObject("profiles");

            if (!profiles.has("Kubithon"))
            {
                // TODO: Clean this
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
        panel.setStatus("Erreur", LauncherPanel.RED);
        e.printStackTrace();

        JOptionPane.showMessageDialog(panel, "Erreur : " + error + "\nVérifiez que vous êtes bien connecté à Internet et qu'il vous reste de l'espace disque !\n(Détail : " + e + ")", "Erreur", JOptionPane.ERROR_MESSAGE);
    }
}
