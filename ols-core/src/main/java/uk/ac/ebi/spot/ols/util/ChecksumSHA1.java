package uk.ac.ebi.spot.ols.util;

import uk.ac.ebi.spot.ols.exception.FileUpdateServiceException;

import java.io.*;
import java.security.MessageDigest;

/**
 * @author Simon Jupp
 * @date 16/02/2015
 * Samples, Phenotypes and Ontologies Team, EMBL-EBI
 */
public class ChecksumSHA1 {

    public static byte[] createChecksum(InputStream is) throws FileUpdateServiceException {

        try {

            byte[] buffer = new byte[1024];
            MessageDigest complete = MessageDigest.getInstance("SHA1");
            int numRead;
            do {
                numRead = is.read(buffer);
                if (numRead > 0) {
                    complete.update(buffer, 0, numRead);
                }
            } while (numRead != -1);
            is.close();
            return complete.digest();
        } catch (Exception e) {
            throw new FileUpdateServiceException("Failed to create a checksum", e);
        }
    }

    public static String getSHA1Checksum(File file) throws Exception {
        byte[] b = createChecksum(new FileInputStream(file));
        String result = "";
        for (int i=0; i < b.length; i++) {
            result +=
                    Integer.toString( ( b[i] & 0xff ) + 0x100, 16).substring( 1 );
        }
        return result;
    }
}