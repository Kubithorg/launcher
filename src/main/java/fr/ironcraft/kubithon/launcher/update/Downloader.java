package fr.ironcraft.kubithon.launcher.update;

import fr.ironcraft.kubithon.launcher.Launcher;
import fr.ironcraft.kubithon.launcher.LauncherPanel;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;
import org.apache.commons.io.IOUtils;
import org.json.JSONArray;
import org.json.JSONObject;

public class Downloader
{
    public static final String INDEX_REMOTE = "https://litarvan.github.io/Kubithon";
    public static final String RESOURCES_REMOTE = "https://resources.download.minecraft.net/";
    public static final String LIBRARIES_REMOTE = "https://libraries.minecraft.net/";
    public static final String KUBITHON_REMOTE = "http://i.kubithon.org/";

    public static final String KUBITHON_INDEX = INDEX_REMOTE + "/Kubithon.json";
    public static final String GAME_INDEX = INDEX_REMOTE + "/1.12.json";
    public static final String MODS_INDEX = KUBITHON_REMOTE + "md5sum";

    private LauncherPanel panel;
    private List<DownloadableFile> toDownload;

    // TODO: RULES, CLASSIFIERS

    public Downloader(LauncherPanel panel)
    {
        this.panel = panel;
        this.toDownload = new ArrayList<DownloadableFile>();
    }

    public void addAssets() throws IOException
    {
        JSONObject gameIndex = json(download(GAME_INDEX, new File(Launcher.KUBITHON_DIR, "versions/Kubithon/1.12.2.json")));
        JSONObject assets = json(download(gameIndex.getJSONObject("assetIndex").getString("url"), new File(Launcher.KUBITHON_DIR, "assets/indexes/1.12.json")));

        for (Entry<String, Object> entry : assets.getJSONObject("objects").toMap().entrySet())
        {
            HashMap<String, Object> asset = (HashMap<String, Object>) entry.getValue();
            String hash = (String) asset.get("hash");
            String prefix = hash.substring(0, 2);
            String path = prefix + "/" + hash;
            String url = RESOURCES_REMOTE + path;

            this.toDownload.add(new DownloadableFile(new URL(url), new File(Launcher.KUBITHON_DIR, "assets/" + path), hash));
        }
    }

    public void addLibs() throws IOException
    {
        JSONObject kubithonIndex = json(download(KUBITHON_INDEX, new File(Launcher.KUBITHON_DIR, "versions/Kubithon/Kubithon.json")));
        addLibs(kubithonIndex);

        JSONObject gameIndex = json(download(GAME_INDEX, new File(Launcher.KUBITHON_DIR, "versions/Kubithon/1.12.2.json")));
        addLibs(gameIndex);
    }

    protected void addLibs(JSONObject index) throws IOException
    {
        JSONArray libraries = index.getJSONArray("libraries");

        for (int i = 0; i < libraries.length(); i++)
        {
            JSONObject library = libraries.getJSONObject(i);

            String name = library.getString("name");

            String path = makePath(name);

            String url = "http://google.fr/";
            String hash = null;

            if (library.has("downloads"))
            {
                JSONObject downloads = library.getJSONObject("downloads");

                if (downloads.has("artifact"))
                {
                    JSONObject artifact = downloads.getJSONObject("artifact");

                    url = artifact.getString("url");
                    hash = artifact.getString("sha1");
                }
            }
            else
            {
                url = (library.has("url") ? library.getString("url") : LIBRARIES_REMOTE) + path;
            }

            toDownload.add(new DownloadableFile(new URL(url), new File(Launcher.KUBITHON_DIR, "libraries/" + path), hash));
        }
    }

    public void addMods() throws IOException
    {
        String index = IOUtils.toString(new URL(MODS_INDEX).openStream(), Charset.defaultCharset());
        String[] mods = index.split("\n");

        for (String mod : mods)
        {
            String[] split = mod.split("  ");
            toDownload.add(new DownloadableFile(new URL(KUBITHON_REMOTE + split[1]), new File(Launcher.KUBITHON_DIR, "mods/" + split[1]), split[0]));
        }
    }

    public void addMainJar() throws IOException
    {
        JSONObject gameIndex = json(download(GAME_INDEX, new File(Launcher.KUBITHON_DIR, "versions/Kubithon/1.12.2.json")));
        JSONObject client = gameIndex.getJSONObject("downloads").getJSONObject("client");

        toDownload.add(new DownloadableFile(new URL(client.getString("url")), new File(Launcher.KUBITHON_DIR, "versions/1.12.2/1.12.2.jar"), client.getString("sha1")));
    }

    protected String makePath(String library)
    {
        String[] split = library.split(":");
        String name = "";

        for (int i = 0; i < split.length; i++)
        {
            String current = split[i];

            if (i < split.length - 2)
            {
                current = current.replace('.', '/');
            }

            name += current + "/";
        }

        name += split[split.length - 2] + "-" + split[split.length - 1] + ".jar";

        return name;
    }

    protected File download(String url, File file) throws IOException
    {
        file.getParentFile().mkdirs();

        InputStream in = new URL(url).openStream();
        OutputStream out = new FileOutputStream(file);

        IOUtils.copy(in, out);

        return file;
    }

    protected JSONObject json(File file) throws IOException
    {
        String content = IOUtils.toString(new FileInputStream(file), Charset.defaultCharset());
        return new JSONObject(content);
    }

    public List<DownloadableFile> getToDownload()
    {
        return toDownload;
    }
}
