package uk.ac.ebi.spot.ols.ui.controller;

import org.apache.tomcat.util.http.fileupload.IOUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

import javax.servlet.http.HttpServletResponse;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

/**
 * @author Julie McMurry adapted from Emma Hastings
 *         Controller used to handle download of files
 * @date 9 Apr 2015
 */
@Controller
public class FileController {

    // These parameters are read from application.properties file
    @Value("${download.full}")
    private Resource fullFileDownload;

    @Value("${download.alternative}")
    private Resource alternativeFileDownload;

    @RequestMapping(value = "api/search/downloads/full",
                    method = RequestMethod.GET)
    public void getFullDownload(HttpServletResponse response) throws IOException {
       if (fullFileDownload.exists()) {

            DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
            Date date = new Date();
            String now = dateFormat.format(date);

            String fileName = "gwas_catalog_v1.0-downloaded_".concat(now).concat(".tsv");
            response.setContentType("text/tsv");
            response.setHeader("Content-Disposition", "attachement; filename=" + fileName);

            InputStream inputStream = null;
            inputStream = fullFileDownload.getInputStream();

            OutputStream outputStream;
            outputStream = response.getOutputStream();

            IOUtils.copy(inputStream, outputStream);
            inputStream.close();
            outputStream.close();

        }
        else {
            throw new FileNotFoundException();
        }
    }

    @RequestMapping(value = "api/search/downloads/alternative",
            method = RequestMethod.GET,
            produces = MediaType.TEXT_PLAIN_VALUE)
    public void getAlternativeDownload(HttpServletResponse response) throws IOException {
        if (alternativeFileDownload.exists()) {

            DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
            Date date = new Date();
            String now = dateFormat.format(date);

            String fileName = "OLS-downloaded_".concat(now).concat(".tsv");
            response.setContentType("text/tsv");
            response.setHeader("Content-Disposition", "attachment; filename=" + fileName);

            InputStream inputStream = null;
            inputStream = alternativeFileDownload.getInputStream();

            OutputStream outputStream;
            outputStream = response.getOutputStream();

            IOUtils.copy(inputStream, outputStream);
            inputStream.close();
            outputStream.close();

        }
        else {
            throw new FileNotFoundException();
        }
    }


    @ResponseStatus(value = HttpStatus.NOT_FOUND, reason = "File not found for download")
    @ExceptionHandler(FileNotFoundException.class)
    public void FileNotFoundException(FileNotFoundException fileNotFoundException) {
    }
}
