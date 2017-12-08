package fr.ironcraft.kubithon.launcher.update;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URL;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Formatter;

import org.json.JSONObject;

public class DownloadableFile
{
    private static final MessageDigest sha1Digest;

    static
    {
        MessageDigest digest = null;

        try
        {
            digest = MessageDigest.getInstance("SHA-1");
        }
        catch (NoSuchAlgorithmException ignored)
        {
        }

        sha1Digest = digest;
    }

    private URL url;
    private File file;
    private int size;
    private String sha1;

    public DownloadableFile(URL url, File file)
    {
        this.url = url;
        this.file = file;
    }

    public DownloadableFile(URL url, File file, int size, String sha1)
    {
        this.url = url;
        this.file = file;
        this.size = size;
        this.sha1 = sha1;
    }

    public File query() throws IOException
    {
        if (file.exists() && isValid(false))
        {
            if (!file.getAbsolutePath().endsWith(".json"))
            {
                System.out.println("No need to download " + file.getAbsolutePath());
            }

            return file;
        }

        for (int i = 0; i < 5 && !isValid(true); i++)
        {
            if (file.exists())
            {
                file.delete();
            }

            System.out.println("Downloading " + file.getAbsolutePath() + " (try " + i + ")");
            Downloader.download(url.toString(), file);
        }

        System.out.println("Finished downloading " + file.getAbsolutePath());
        return file;
    }

    public boolean isValid(boolean afterUpdate) throws IOException
    {
        if (!file.exists())
        {
            return false;
        }

        if (sha1Digest == null)
        {
            return true;
        }

        if (size != 0)
        {
            boolean sizeChecked = file.length() == size;

            if (sizeChecked || !afterUpdate)
            {
                return sizeChecked;
            }
        }

        if (this.sha1 != null && this.sha1.length() > 37)
        {
            MessageDigest sha1digest;

            try
            {
                sha1digest = MessageDigest.getInstance("SHA-1");
            }
            catch (NoSuchAlgorithmException ignored)
            {
                return true;
            }

            FileInputStream in = new FileInputStream(file);

            byte[] buffer = new byte[8192];
            int len = in.read(buffer);

            while (len != -1) {
                sha1digest.update(buffer, 0, len);
                len = in.read(buffer);
            }

            Formatter formatter = new Formatter();

            try
            {
                for (final byte b : sha1digest.digest())
                {
                    formatter.format("%02x", b);
                }

                return formatter.toString().equalsIgnoreCase(sha1);
            }
            finally
            {
                formatter.close();
            }
        }

        return true;
    }

    public URL getUrl()
    {
        return url;
    }

    public File getFile()
    {
        return file;
    }

    public int getSize()
    {
        return size;
    }

    public String getSha1()
    {
        return sha1;
    }

    public static DownloadableFile fromJson(JSONObject obj, File file) throws IOException
    {
        return new DownloadableFile(new URL(obj.getString("url")), file, obj.getInt("size"), obj.has("sha1") ? obj.getString("sha1") : obj.getString("hash"));
    }
}
