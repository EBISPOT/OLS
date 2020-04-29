package uk.ac.ebi.spot.ols.util;

import org.semanticweb.elk.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.FileCopyUtils;
import org.springframework.util.FileSystemUtils;
import sun.net.www.protocol.ftp.FtpURLConnection;
import uk.ac.ebi.spot.ols.exception.FileUpdateServiceException;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.file.FileSystem;
import java.nio.file.Files;
import java.time.Duration;
import java.time.Instant;
import java.util.zip.GZIPInputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

/**
 * @author Simon Jupp
 * @date 11/02/2015
 * Samples, Phenotypes and Ontologies Team, EMBL-EBI
 */
@Component
public class FileUpdater {


    private Logger log = LoggerFactory.getLogger(getClass());

    public Logger getLog() {
        return log;
    }

    public void setPath(String path) {
        this.path = path;
    }

    @Value("${ols.loader.filedir:}")
    private String path;

    public FileUpdater() {
    }

    public File getDefaultDirectory() {

        String defaultPath = OLSEnv.getOLSHome() + File.separator + "downloads";
        File pathFile = new File(defaultPath);
        if (!Files.exists(pathFile.toPath())) {
            pathFile.mkdir();
        }

        return pathFile;
    }

    public FileStatus getFile(String name, URI file) throws FileUpdateServiceException {

        getLog().info("Downloading " + name + " from " + file.toString());
        File pathFile = new File(path);
        if (!path.equals("")) {
            if (!Files.exists(pathFile.toPath())) {
                try {
                    pathFile.mkdir();
                } catch (Exception e) {

                    getLog().info("Couldn't create directory to store ontologies in " + path + " so defaulting to default");
                    try {
                        pathFile = getDefaultDirectory();
                    } catch (Exception e1) {
                        throw new FileUpdateServiceException("Can't create path to file download:" + path);
                    }
                }
            }
        }
        else {
            try {
                pathFile = getDefaultDirectory();
            } catch (Exception e1) {
                throw new FileUpdateServiceException("Can't create path to file download:" + path);
            }
        }

        String checkName = name + ".chk";
        File latestFileChecksum  = new File(pathFile, checkName);

        String downloadFileName = name + ".download";

        File latestFile  = new File(pathFile, name);

        // get file as input stream (check if zipped)
        try {

            InputStream is = getFileInputStream(file.toURL());

            Instant start = Instant.now();
            File downloadFile = writeInputStreamToFile(new File(pathFile, downloadFileName), is);
            Instant end = Instant.now();
            getLog().debug(name + " downloaded in " + Duration.between(start, end));

            start = Instant.now();
            String downloadChecksum = ChecksumSHA1.getSHA1Checksum(downloadFile);
            end = Instant.now();
            getLog().debug(downloadChecksum+ " " + name + " created in " + Duration.between(start, end));

            // compare new file to latest
            // if previous checksum exists
            if (latestFileChecksum.exists()) {

                getLog().debug("Previous version exists " + name );
                // read it
                String latestChecksum = readChecksum(latestFileChecksum);
                getLog().debug("Reading previous checksum " + name + ":" + latestChecksum);

                // create one for the downloaded file

                // if they are different, the file has changed
                if (!latestChecksum.equals(downloadChecksum)) {
                    // if they are different, copy the downloaded file to the latest file
                    FileCopyUtils.copy(downloadFile, latestFile );
                    // update the latest file checksum
                    writeChecksum(latestFileChecksum, downloadChecksum);
                }
            }
            else {
                FileCopyUtils.copy(downloadFile, latestFile );
                writeChecksum(latestFileChecksum, downloadChecksum);
            }
            return new FileStatus(latestFile, downloadChecksum);

        } catch (Exception e) {
            throw new FileUpdateServiceException("Failed to download file: " + e.getMessage(), e);
        }
    }

    private File writeInputStreamToFile(File file, InputStream is) throws IOException {
        OutputStream os = new FileOutputStream(file);
        byte[] b = new byte[2048];
        int length;

        while ((length = is.read(b)) != -1) {
            os.write(b, 0, length);
        }

        is.close();
        os.close();
        return file;
    }

    private InputStream getFileInputStream(URL url) throws IOException {

        File pathFile = new File(url.getPath());

        if (Files.exists(pathFile.toPath())) {
            FileInputStream fis= new FileInputStream(pathFile);
            if (pathFile.getName().endsWith(".gz")) {
                return new GZIPInputStream(fis);
            }
            else if (pathFile.getName().endsWith(".zip")) {
                ZipInputStream zis = new ZipInputStream(fis);
                if ( (zis.getNextEntry()) != null) {
                    ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
                    IOUtils.copy(zis, outputStream);
                    return new ByteArrayInputStream(outputStream.toByteArray());
                }
            }
            else {
                return new FileInputStream(pathFile);
            }
        }

        // try a http connection

        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setConnectTimeout(15000);
        connection.setReadTimeout(60000);
        connection.setInstanceFollowRedirects(true);
        connection.setRequestProperty("User-Agent", "Mozilla/5.0...");

        boolean redirect = false;

        // normally, 3xx is redirect
        int status = connection.getResponseCode();
        if (status != HttpURLConnection.HTTP_OK) {
            if (status == HttpURLConnection.HTTP_MOVED_TEMP
                    || status == HttpURLConnection.HTTP_MOVED_PERM
                    || status == HttpURLConnection.HTTP_SEE_OTHER)
                redirect = true;
        }
        
        if (redirect) {
            // get redirect url from "location" header field
            String newUrl = connection.getHeaderField("Location");
            // open the new connnection again

            try {
                connection = (HttpURLConnection) new URL(newUrl).openConnection();
                connection.setInstanceFollowRedirects(true);
            } catch (Exception e) {
                FtpURLConnection ftpURLConnection = (FtpURLConnection) new URL(newUrl).openConnection();
                return ftpURLConnection.getInputStream();
            }
        }

        if (pathFile.getName().endsWith(".zip")) {
            ZipInputStream zis = new ZipInputStream(connection.getInputStream());
            if ( (zis.getNextEntry()) != null) {
                ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
                IOUtils.copy(zis, outputStream);
                return new ByteArrayInputStream(outputStream.toByteArray());
            }
        }
        else if (pathFile.getName().endsWith(".gz")) {
            return new GZIPInputStream(connection.getInputStream());
        }
        return connection.getInputStream();
    }

    private String readChecksum (File fileCheck) throws IOException {
        InputStream is = new FileInputStream(fileCheck);
        byte [] content = Files.readAllBytes(fileCheck.toPath());
        return new String(content, "UTF-8");
    }

    private void writeChecksum (File fileCheck, String checksum) throws IOException {
        FileOutputStream out = new FileOutputStream(fileCheck);
        out.write(checksum.getBytes());
        out.close();
    }

    public class FileStatus {
        private File file;
        private String latestHash;

        public FileStatus(File latestFile, String latestHash) {
            this.file = latestFile;
            this.latestHash = latestHash;
        }

        public File getFile() {
            return file;
        }

        public String getLatestHash() {
            return latestHash;
        }
    }


}
