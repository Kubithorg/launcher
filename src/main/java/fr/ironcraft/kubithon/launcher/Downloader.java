package fr.ironcraft.kubithon.launcher;

import fr.theshark34.openlauncherlib.minecraft.util.GameDirGenerator;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map.Entry;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;

public class Downloader
{
    public static final String ENDPOINT = "http://i.kubithon.org/";
    public static final String FILES_URL = ENDPOINT + "md5sum";
    public static final File MINECRAFT_FOLDER = GameDirGenerator.createGameDir("minecraft");
    public static final String VERSION_FILE = "http://litarvan.github.io/Kubithon/Kubithon.json";

    private LauncherPanel panel;
    private HashMap<String, String> files = new HashMap<String, String>();

    public Downloader(LauncherPanel panel)
    {
        this.panel = panel;
    }

    public void start() throws IOException
    {
        File mods = new File(Launcher.KUBITHON_DIR, "mods");

        loadFiles();

        File[] files = mods.listFiles();

        if (files != null)
        {
            for (File file : files)
            {
                if (!this.files.containsKey("./mods/" + file.getName()))
                {
                    FileUtils.forceDelete(file);
                }
            }
        }

        check();
        download();
    }

    private void loadFiles() throws IOException
    {
        String content = IOUtils.toString(new URL(FILES_URL).openStream(), Charset.defaultCharset());
        String[] files = content.split("\n");

        for (String file : files)
        {
            String[] split = file.split("  ");
            this.files.put(split[1], split[0]);
        }
    }

    private void check() throws IOException
    {
        HashMap<String, String> newFiles = new HashMap<String, String>();

        for (Entry<String, String> entry : files.entrySet())
        {
            File file = new File(Launcher.KUBITHON_DIR, entry.getKey());

            if (!file.exists())
            {
                newFiles.put(entry.getKey(), entry.getValue());
                continue;
            }

            String md5 = DigestUtils.md5Hex(new FileInputStream(file));

            if (!md5.equals(entry.getValue()))
            {
                newFiles.put(entry.getKey(), entry.getValue());
            }
        }

        this.files = newFiles;
    }

    private void download() throws IOException
    {
        int val = 0;
        int max = files.size();

        panel.getProgressBar().setMaximum(max);

        for (Entry<String, String> entry : files.entrySet())
        {
            val++;

            panel.setStatus("Téléchargement</font><font> " + val + "/" + max, LauncherPanel.BLUE);

            File file = new File(Launcher.KUBITHON_DIR, entry.getKey());
            file.getParentFile().mkdirs();
            file.createNewFile();

            InputStream in = new URL(ENDPOINT + entry.getKey()).openStream();
            OutputStream out = new FileOutputStream(file);

            IOUtils.copy(in, out);

            panel.getProgressBar().setValue(val);
        }
    }
}
