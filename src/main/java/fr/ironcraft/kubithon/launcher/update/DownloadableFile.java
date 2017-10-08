package fr.ironcraft.kubithon.launcher.update;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URL;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import org.apache.commons.codec.binary.Hex;

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
    private String sha1;

    public DownloadableFile(URL url, File file)
    {
        this.url = url;
        this.file = file;
    }

    public DownloadableFile(URL url, File file, String sha1)
    {
        this.url = url;
        this.file = file;
        this.sha1 = sha1;
    }

    public boolean isValid() throws IOException
    {
        if (this.sha1 == null || sha1Digest == null)
        {
            return true;
        }

        FileInputStream in = new FileInputStream(file);

        byte[] bytes = new byte[2048];
        int n;

        while ((n = in.read(bytes)) != -1) {
            sha1Digest.update(bytes, 0, n);
        }

        byte[] digest = sha1Digest.digest();

        return new String(Hex.encodeHex(digest)).equals(sha1);
    }

    public URL getUrl()
    {
        return url;
    }

    public File getFile()
    {
        return file;
    }

    public String getSha1()
    {
        return sha1;
    }
}
