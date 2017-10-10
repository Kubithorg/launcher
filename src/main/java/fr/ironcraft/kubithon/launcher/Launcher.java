package fr.ironcraft.kubithon.launcher;

import fr.ironcraft.kubithon.launcher.update.Downloader;
import fr.ironcraft.kubithon.launcher.update.NativesManager;
import fr.theshark34.openlauncherlib.LaunchException;
import fr.theshark34.openlauncherlib.external.ExternalLauncher;
import fr.theshark34.openlauncherlib.internal.InternalLaunchProfile;
import fr.theshark34.openlauncherlib.internal.InternalLauncher;
import fr.theshark34.openlauncherlib.minecraft.AuthInfos;
import fr.theshark34.openlauncherlib.minecraft.GameFolder;
import fr.theshark34.openlauncherlib.minecraft.GameInfos;
import fr.theshark34.openlauncherlib.minecraft.GameTweak;
import fr.theshark34.openlauncherlib.minecraft.GameType;
import fr.theshark34.openlauncherlib.minecraft.GameVersion;
import fr.theshark34.openlauncherlib.minecraft.MinecraftLauncher;
import fr.theshark34.openlauncherlib.minecraft.util.GameDirGenerator;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.UUID;
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

    public void nonPremium(String username, LauncherFrame frame)
    {
        panel.setStatus("Listage des fichiers", LauncherPanel.BLUE);
        panel.getProgressBar().setVisible(true);

        Downloader downloader = new Downloader(panel);

        try
        {
            downloader.addAssets();
            downloader.addLibs(KUBITHON_DIR);
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

        panel.setStatus("Extraction des natives", LauncherPanel.BLUE);

        NativesManager natives = new NativesManager(downloader.getNatives());
        try
        {
            natives.extract();
        }
        catch (IOException e)
        {
            error("Impossible d'extraire les natives !\nVérifiez qu'il vous reste de l'espace disque, veuillez réessayer", e);
        }

        panel.setStatus("Lancement...", LauncherPanel.BLUE);

        GameInfos infos = new GameInfos("Kubithon", new GameVersion("1.12.2", GameType.V1_8_HIGHER), new GameTweak[]{GameTweak.FORGE});
        GameFolder folder = new GameFolder("assets", "libraries", "natives", "versions/1.12.2/1.12.2.jar");
        AuthInfos auth = new AuthInfos(username, "unauthorized", UUID.randomUUID().toString());

        try
        {
            InternalLaunchProfile profile = MinecraftLauncher.createInternalProfile(infos, folder, auth);
            System.out.println(Arrays.toString((String[]) profile.getParameters()[0]));
            InternalLauncher launcher = new InternalLauncher(profile);

            new Thread()
            {
                @Override
                public void run()
                {
                    try
                    {
                        Thread.sleep(5000L);
                    }
                    catch (InterruptedException ignored)
                    {
                    }

                    frame.setVisible(false);
                }
            }.start();

            launcher.launch();
            System.exit(0);
        }
        catch (LaunchException e)
        {
            error("Impossible de lancer le jeu !\nVeuillez réessayer", e);
        }
    }

    public void premium()
    {
        panel.setStatus("Lecture des infos de version", LauncherPanel.BLUE);
        panel.getProgressBar().setVisible(true);

        File version = new File(MINECRAFT_DIR, "versions/Kubithon/Kubithon.json");
        version.getParentFile().mkdirs();

        try
        {
            InputStream in = new URL(Downloader.KUBITHON_INDEX).openStream();
            OutputStream out = new FileOutputStream(version);

            IOUtils.copy(in, out);
            IOUtils.closeQuietly(in, out);
        }
        catch (IOException e)
        {
            error("Impossible de télécharger le fichier de version", e);
            return;
        }

        Downloader downloader = new Downloader(panel);
        try
        {
            downloader.addLibs(MINECRAFT_DIR);
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
            InputStream in = new FileInputStream(launcherProfiles);
            JSONObject object = new JSONObject(IOUtils.toString(in, Charset.defaultCharset()));
            IOUtils.closeQuietly(in);

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
