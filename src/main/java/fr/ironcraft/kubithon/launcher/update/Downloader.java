package fr.ironcraft.kubithon.launcher.update;

import fr.ironcraft.kubithon.launcher.Launcher;
import fr.ironcraft.kubithon.launcher.LauncherPanel;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
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
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
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

    public static final String MAVEN_REPOSITORY = "https://repo.maven.apache.org/maven2/";
    public static final String FORGE_REPOSITORY = "http://files.minecraftforge.net/maven/";
    public static final String SPONGE_REPOSITORY = "https://repo.spongepowered.org/maven/";

    private LauncherPanel panel;
    private List<DownloadableFile> toDownload;
    private List<DownloadableFile> natives;

    // TODO: RULES, CLASSIFIERS

    public Downloader(LauncherPanel panel)
    {
        this.panel = panel;
        this.toDownload = new ArrayList<DownloadableFile>();
        this.natives = new ArrayList<DownloadableFile>();
    }

    public void addAssets() throws IOException
    {
        System.out.print("Listing assets -> Getting indexes... ");

        JSONObject gameIndex = json(download(GAME_INDEX, new File(Launcher.KUBITHON_DIR, "versions/Kubithon/1.12.2.json")));
        DownloadableFile assetsIndex = DownloadableFile.fromJson(gameIndex.getJSONObject("assetIndex"), file("assets/indexes/1.12.json"));
        JSONObject assets = json(assetsIndex.query());

        System.out.print("Listing... ");

        for (Entry<String, Object> entry : assets.getJSONObject("objects").toMap().entrySet())
        {
            HashMap<String, Object> asset = (HashMap<String, Object>) entry.getValue();
            String hash = (String) asset.get("hash");
            String prefix = hash.substring(0, 2);
            String path = prefix + "/" + hash;
            String url = RESOURCES_REMOTE + path;

            this.toDownload.add(new DownloadableFile(new URL(url), file("assets/" + path), (Integer) asset.get("size"), hash));
        }

        System.out.println("OK");
    }

    public void addLibs() throws IOException
    {
        System.out.print("Listing assets -> Getting indexes... ");
        JSONObject kubithonIndex = json(download(KUBITHON_INDEX, file( "versions/Kubithon/Kubithon.json")));
        JSONObject gameIndex = json(download(GAME_INDEX, file("versions/Kubithon/1.12.2.json")));

        System.out.print("Listing Kubithon libraries... ");
        addLibs(kubithonIndex);

        System.out.print("Listing Minecraft libraries... ");
        addLibs(gameIndex);

        System.out.println("OK");
    }

    protected void addLibs(JSONObject index) throws IOException
    {
        JSONArray libraries = index.getJSONArray("libraries");
        String nativesKey = "natives-" + os();

        for (int i = 0; i < libraries.length(); i++)
        {
            JSONObject library = libraries.getJSONObject(i);

            String name = library.getString("name");

            String path = makePath(name);

            String url = null;
            String hash = null;
            int size = 0;

            if (!checkRules(library))
            {
                continue;
            }

            if (library.has("downloads"))
            {
                JSONObject downloads = library.getJSONObject("downloads");

                if (downloads.has("artifact"))
                {
                    JSONObject artifact = downloads.getJSONObject("artifact");

                    url = artifact.getString("url");
                    hash = artifact.getString("sha1");
                    size = artifact.getInt("size");
                }

                if (downloads.has("classifiers"))
                {
                    JSONObject classifiers = downloads.getJSONObject("classifiers");

                    if (classifiers.has(nativesKey))
                    {
                        natives.add(DownloadableFile.fromJson(classifiers.getJSONObject(nativesKey), file("libraries/" + path.substring(0, path.length() - 4) + "-" + nativesKey + ".jar")));
                    }
                }
            }
            else
            {
                url = (library.has("url") ? library.getString("url") : LIBRARIES_REMOTE) + path;
            }

            if (url != null)
            {
                if (hash == null)
                {
                    if (name.startsWith("net.minecraftforge:forge:"))
                    {
                        url = url.substring(0, url.length() - 4) + "-universal.jar";
                    }
                    else if (!name.startsWith("net.minecraftforge"))
                    {
                        url = url.replace(FORGE_REPOSITORY, SPONGE_REPOSITORY); // huhuhu
                    }

                    InputStream stream;

                    try
                    {
                        stream = new URL(url + ".sha1").openStream();
                    }
                    catch (FileNotFoundException e)
                    {
                        stream = new URL((url = url.replace(SPONGE_REPOSITORY, MAVEN_REPOSITORY)) + ".sha1").openStream();
                    }

                    hash = IOUtils.toString(stream, Charset.defaultCharset());
                }

                toDownload.add(new DownloadableFile(new URL(url), file("libraries/" + path), size, hash));
            }
        }
    }

    public void addMods() throws IOException
    {
        System.out.print("Listing mods... Getting index... ");

        String index = IOUtils.toString(new URL(MODS_INDEX).openStream(), Charset.defaultCharset());
        String[] mods = index.split("\n");

        System.out.print("Listing... ");

        for (String mod : mods)
        {
            String[] split = mod.split(" {2}");
            toDownload.add(new DownloadableFile(new URL(KUBITHON_REMOTE + split[1]), new File(Launcher.KUBITHON_DIR, "mods/" + split[1]), 0, split[0]));
        }

        System.out.println("OK");
    }

    public void addMainJar() throws IOException
    {
        System.out.print("Getting main jar... ");

        JSONObject gameIndex = json(download(GAME_INDEX, file("versions/1.12.2/1.12.2.json")));
        JSONObject client = gameIndex.getJSONObject("downloads").getJSONObject("client");

        toDownload.add(DownloadableFile.fromJson(client, file("versions/1.12.2/1.12.2.jar")));

        System.out.println("OK");
    }

    public void download()
    {
        final ThreadPoolExecutor pool = (ThreadPoolExecutor) Executors.newFixedThreadPool(15);

        panel.getProgressBar().setMaximum(toDownload.size());

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

        System.out.println("--> Downloaded " + toDownload.size() + " files");
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

    protected boolean checkRules(JSONObject library)
    {
        if (!library.has("rules"))
        {
            return true;
        }

        boolean allow = true;

        JSONArray rules = library.getJSONArray("rules");

        for (int i = 0; i < rules.length(); i++)
        {
            JSONObject rule = rules.getJSONObject(i);

            String action = rule.getString("action");

            if (!rule.has("os"))
            {
                allow = action.equals("allow");
                continue;
            }

            String os = rule.getJSONObject("os").getString("name");

            if (action.equals("allow"))
            {
                allow = os.equals(os());
            }
            else if (action.equals("disallow"))
            {
                allow = !os.equals(os());
            }
        }

        return allow;
    }

    protected String os()
    {
        String os = System.getProperty("os.name");

        if (os.contains("win"))
        {
            return "windows";
        }
        else if (os.contains("mac"))
        {
            return "osx";
        }
        else
        {
            return "linux";
        }
    }

    protected File file(String path)
    {
        return new File(Launcher.KUBITHON_DIR, path);
    }

    public static File download(String url, File file) throws IOException
    {
        file.getParentFile().mkdirs();

        InputStream in = new URL(url).openStream();
        OutputStream out = new FileOutputStream(file);

        IOUtils.copy(in, out);
        IOUtils.closeQuietly(in, out);

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

    public List<DownloadableFile> getNatives()
    {
        return natives;
    }
}
