package fr.ironcraft.kubithon.launcher.update;

import fr.ironcraft.kubithon.launcher.Launcher;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import org.apache.commons.io.FileUtils;

public class NativesManager
{
    private List<DownloadableFile> natives;

    public NativesManager(List<DownloadableFile> natives)
    {
        this.natives = natives;
    }

    public void extract() throws IOException
    {
        File folder = new File(Launcher.KUBITHON_DIR, "natives");

        if (folder.exists())
        {
            try
            {
                FileUtils.deleteDirectory(folder);
            }
            catch (IOException ignored)
            {
            }
        }

        for (DownloadableFile nat : natives)
        {
            System.out.println("Extracting " + nat.getFile().getAbsolutePath() + " to " + folder.getAbsolutePath());
            unzip(nat.getFile(), folder);
        }
    }

    public void unzip(File zip, File destDir) throws IOException
    {
        if (!destDir.exists())
        {
            destDir.mkdirs();
        }

        ZipInputStream zipIn = new ZipInputStream(new FileInputStream(zip));
        ZipEntry entry = zipIn.getNextEntry();

        while (entry != null)
        {
            File file = new File(destDir, entry.getName());

            if (!entry.getName().endsWith("META-INF"))
            {
                if (!entry.isDirectory())
                {
                    extractFile(zipIn, file);
                }
                else
                {
                    file.mkdirs();
                }
            }

            zipIn.closeEntry();
            entry = zipIn.getNextEntry();
        }

        zipIn.close();
    }

    private void extractFile(ZipInputStream zipIn, File file) throws IOException {
        BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(file));

        byte[] bytesIn = new byte[4096];
        int read;

        while ((read = zipIn.read(bytesIn)) != -1)
        {
            bos.write(bytesIn, 0, read);
        }

        bos.close();
    }
}
