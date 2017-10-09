package fr.ironcraft.kubithon.launcher;

import fr.ironcraft.kubithon.launcher.update.DownloadableFile;
import fr.theshark34.openlauncherlib.minecraft.util.GameDirGenerator;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import javax.swing.JOptionPane;
import org.apache.commons.io.FileUtils;
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
        panel.setStatus("Listage des fichiers", LauncherPanel.BLUE);
        panel.getProgressBar().setVisible(true);

        fr.ironcraft.kubithon.launcher.update.Downloader downloader = new fr.ironcraft.kubithon.launcher.update.Downloader(panel);

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

        /*String total = "";

        for (DownloadableFile file : downloader.getToDownload())
        {
            total += file.getUrl();
            total += repeat(146 - file.getUrl().toString().length());
            total += file.getFile().getAbsolutePath().replace("/home/litarvan/", "");
            total += repeat(128 - file.getFile().getAbsolutePath().replace("/home/litarvan/", "").length());
            total += file.getSha1();
            total += repeat(42 - (file.getSha1() == null ? 4 : file.getSha1().length()));
            total += file.getSize();
            total += "\n";
        }

        total += "\n";

        for (DownloadableFile file : downloader.getNatives())
        {
            total += file.getUrl();
            total += repeat(146 - file.getUrl().toString().length());
            total += file.getFile().getAbsolutePath().replace("/home/litarvan/", "");
            total += repeat(128 - file.getFile().getAbsolutePath().replace("/home/litarvan/", "").length());
            total += file.getSha1();
            total += repeat(42 - (file.getSha1() == null ? 4 : file.getSha1().length()));
            total += file.getSize();
            total += "\n";
        }

        try
        {
            FileUtils.write(new File("files.txt"), total, Charset.defaultCharset());
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }*/

        panel.setStatus("Téléchargement", LauncherPanel.BLUE);

        final ThreadPoolExecutor pool = (ThreadPoolExecutor) Executors.newFixedThreadPool(15);

        panel.getProgressBar().setMaximum(downloader.getToDownload().size());

        List<DownloadableFile> toDownload = downloader.getToDownload();
        for (final DownloadableFile file : toDownload)
        {
            pool.submit(new Runnable()
            {
                @Override
                public void run()
                {
                    try
                    {
                        file.query();
                        panel.getProgressBar().setValue(panel.getProgressBar().getValue() + 1);
                    }
                    catch (IOException e)
                    {
                        e.printStackTrace();
                    }
                }
            });
        }

        pool.shutdown();

        try
        {
            pool.awaitTermination(1000L, TimeUnit.DAYS);
        }
        catch (InterruptedException ignored)
        {
        }

        System.out.println("--> Downloaded " + downloader.getToDownload().size() + " files");
        panel.setStatus("Lancement...", LauncherPanel.BLUE);
    }

    private String repeat(int amount)
    {
        String a = "";
        for (int i = 0; i < amount; i++)
        {
            a += " ";
        }

        return a;
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
        panel.setStatus("Erreur", LauncherPanel.RED);
        e.printStackTrace();

        JOptionPane.showMessageDialog(panel, "Erreur : " + error + "\nVérifiez que vous êtes bien connecté à Internet et qu'il vous reste de l'espace disque !\n(Détail : " + e + ")", "Erreur", JOptionPane.ERROR_MESSAGE);
    }
}
