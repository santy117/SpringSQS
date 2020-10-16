package com.practica1.Services;

import com.amazonaws.AmazonServiceException;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicSessionCredentials;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.itextpdf.text.*;
import com.itextpdf.text.pdf.PdfWriter;
import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.*;
import java.net.URISyntaxException;
import java.net.URL;


@Service
@Configurable
public class ServicioDatos {
    @Value("${aws_access_key_id}")
    private String awsId;

    @Value("${aws_secret_access_key}")
    private String awsKey;

    @Value("${aws_session_token}")
    private String awsSession;

    @Value("${jsa.s3.region}")
    private String region;

    int contador=0;

    public ServicioDatos() throws IOException {
    }

    public void incContador() {
        this.contador = this.contador+1;
    }

    public int getContador(){
        return contador;
    }

    public String getIp() throws IOException {
        URL whatismyip = new URL("http://checkip.amazonaws.com");
        BufferedReader in = new BufferedReader(new InputStreamReader(
                whatismyip.openStream()));

        String ip = in.readLine(); //you get the IP as a String
        return ip;
    }
    public void createFile(String informacion, String key) throws IOException, DocumentException, URISyntaxException {
        Document document = new Document();
        PdfWriter.getInstance(document, new FileOutputStream(key+".pdf"));

        document.open();
        Font font = FontFactory.getFont(FontFactory.COURIER, 16, BaseColor.BLACK);
        Chunk chunk = new Chunk(informacion+" clave: "+key, font);
        Image img = Image.getInstance("logo.png");
        img.scaleAbsolute(400, 400);
        document.add(img);
        document.add(chunk);
        document.close();

    }
    public void sendFile(String idCompra) throws IOException {
        System.out.format("Uploading to S3 bucket bucketp1ta...\n");
        BasicSessionCredentials awsCreds= new BasicSessionCredentials(awsId, awsKey, awsSession);
        AmazonS3 s3 = AmazonS3ClientBuilder.standard()
                .withRegion(Regions.fromName(region))
                .withCredentials(new AWSStaticCredentialsProvider(awsCreds))
                .build();
        File file = new File(idCompra+".pdf");
        try {
            s3.putObject("bucketp1ta", idCompra+".pdf", file);
        } catch (AmazonServiceException e) {
            System.err.println(e.getErrorMessage());
            System.exit(1);
        }
        System.out.println("Done!");
    }

}
