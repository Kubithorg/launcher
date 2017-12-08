package fr.ironcraft.kubithon.launcher;

import fr.ironcraft.kubithon.launcher.update.DownloadableFile;
import fr.ironcraft.kubithon.launcher.update.Downloader;
import fr.ironcraft.kubithon.launcher.update.NativesManager;
import fr.litarvan.openauth.AuthPoints;
import fr.litarvan.openauth.AuthenticationException;
import fr.litarvan.openauth.Authenticator;
import fr.litarvan.openauth.model.AuthAgent;
import fr.litarvan.openauth.model.response.AuthResponse;
import fr.theshark34.openlauncherlib.LaunchException;
import fr.theshark34.openlauncherlib.external.ExternalLaunchProfile;
import fr.theshark34.openlauncherlib.external.ExternalLauncher;
import fr.theshark34.openlauncherlib.minecraft.AuthInfos;
import fr.theshark34.openlauncherlib.minecraft.GameFolder;
import fr.theshark34.openlauncherlib.minecraft.GameInfos;
import fr.theshark34.openlauncherlib.minecraft.GameTweak;
import fr.theshark34.openlauncherlib.minecraft.GameType;
import fr.theshark34.openlauncherlib.minecraft.GameVersion;
import fr.theshark34.openlauncherlib.minecraft.MinecraftLauncher;
import fr.theshark34.openlauncherlib.minecraft.util.GameDirGenerator;
import fr.theshark34.openlauncherlib.util.ProcessLogManager;

import java.io.*;
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

    public void nonPremium(String username, String password, final LauncherFrame frame)
    {
        panel.setStatus("Authentification...", LauncherPanel.BLUE);
        Authenticator authenticator = new Authenticator("https://kubithon.org/authserver/", AuthPoints.NORMAL_AUTH_POINTS);
        AuthInfos auth;

        try
        {
            AuthResponse response = authenticator.authenticate(AuthAgent.MINECRAFT, username, password, "kubithon");
            auth = new AuthInfos(response.getSelectedProfile().getName(), response.getAccessToken(), response.getSelectedProfile().getId());
        }
        catch (AuthenticationException e)
        {
            error("Impossible de se connecter : " + e.getErrorModel().getError() + ", " + e.getErrorModel().getErrorMessage(), false, e);
            return;
        }
        //auth = new AuthInfos("Litarvan", "azeiruanzer", "ajzoeirjazer");

        if (System.getProperty("java.home").contains("9"))
        {
            JOptionPane.showMessageDialog(frame, "Alerte : Vous utilisez actuellement Java '9', Forge n'est pas compatible avec cette version. Veuillez supprimer cette version, et en installer une moins récente si ce n'est pas déjà le cas.\nLe jeu ne se lancera très probablement pas.", "Erreur", JOptionPane.ERROR_MESSAGE);
        }

        panel.setStatus("Lecture de l'index des fichiers", LauncherPanel.BLUE);
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
            error("Impossible d'établir la liste des fichiers", true, e);
            return;
        }

        String authLib = "";

        for (int i = 0; i < downloader.getToDownload().size(); i++)
        {
            DownloadableFile file = downloader.getToDownload().get(i);
            String path = file.getFile().getAbsolutePath();

            if (path.endsWith("authlib-1.5.25.jar"))
            {
                downloader.getToDownload().remove(i);
            }

            if (path.endsWith("authlib-kubithon.jar"))
            {
                authLib = path;
            }
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
            error("Impossible d'extraire les natives !", true, e);
        }

        panel.setStatus("Lancement...", LauncherPanel.BLUE);

        GameInfos infos = new GameInfos("kubithon", new GameVersion("1.12.2", GameType.V1_8_HIGHER), new GameTweak[]{GameTweak.FORGE});
        GameFolder folder = new GameFolder("assets", "libraries", "natives", "versions/1.12.2/1.12.2.jar");

        try
        {
            ExternalLaunchProfile profile = MinecraftLauncher.createExternalProfile(infos, folder, auth);
            profile.setClassPath(profile.getClassPath() + File.pathSeparator + authLib);

            ExternalLauncher launcher = new ExternalLauncher(profile);

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

            Process p = launcher.launch();
            ProcessLogManager manager = new ProcessLogManager(p.getInputStream());
            manager.start();

            try
            {
                p.waitFor();
            }
            catch (InterruptedException ignored)
            {
            }

            System.exit(0);
        }
        catch (LaunchException e)
        {
            error("Impossible de lancer le jeu !\nVeuillez réessayer", true, e);
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
            error("Impossible de télécharger le fichier de version", true, e);
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
            error("Impossible d'établir la liste des fichiers", true, e);
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

                BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(launcherProfiles), Charset.forName("UTF-8")));
                object.write(writer, 2, 0);

                IOUtils.closeQuietly(writer);
            }

            panel.setStatus("Terminé", "#22ee22");
            JOptionPane.showMessageDialog(panel, "Terminé : Fermez l'installeur, vous pouvez maintenant lancer le launcher officiel et jouer au jeu via le profil 'Kubithon' !", "Terminé", JOptionPane.INFORMATION_MESSAGE);
        }
        catch (IOException e)
        {
            error("Impossible de modifier les profils du launcher !\nVous devez avoir installé Minecraft 1.10.2 avant de lancer l'installeur !", false, e);
        }
    }

    private void error(String error, boolean misc, Exception e)
    {
        panel.setStatus("Erreur", LauncherPanel.RED);
        e.printStackTrace();

        JOptionPane.showMessageDialog(panel, "Erreur : " + error + (misc ? "\nVérifiez que vous êtes bien connecté à Internet et qu'il vous reste de l'espace disque !\n(Détail : " + e + ")": ""), "Erreur", JOptionPane.ERROR_MESSAGE);
    }
}
