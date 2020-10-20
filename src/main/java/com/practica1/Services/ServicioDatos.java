package com.practica1.Services;

import com.amazonaws.AmazonServiceException;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicSessionCredentials;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.GetObjectRequest;
import com.amazonaws.services.s3.model.S3Object;
import com.amazonaws.util.StringUtils;
import com.google.gson.Gson;
import com.practica1.Models.Conciertos;
import org.apache.tomcat.util.json.JSONParser;
import org.apache.tomcat.util.json.ParseException;
import org.json.JSONException;
import org.json.JSONObject;
import software.amazon.awssdk.services.sqs.SqsClient;
import software.amazon.awssdk.services.sqs.model.*;

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

    @Value("${jsa.s3.region}")
    private String region;

    private String keyConciertos="conciertos.json";
    int contador=0;
    S3Object fullObject = null;
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
        AmazonS3 s3 = AmazonS3ClientBuilder.standard()
                .withRegion(Regions.fromName(region))
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
    public String getJSON() throws IOException, ParseException, JSONException {
        System.out.format("Getting file from S3 bucket bucketp1ta...\n");
        AmazonS3 s3 = AmazonS3ClientBuilder.standard()
                .withRegion(Regions.fromName(region))
                .build();
        try {
            System.out.println("Downloading an object");
            fullObject = s3.getObject(new GetObjectRequest("bucketp1ta", keyConciertos));
        } catch (AmazonServiceException e) {
            System.err.println(e.getErrorMessage());
            System.exit(1);
        }
        System.out.println("Archivo recuperado!");
        String parsedJSON = getAsString(fullObject.getObjectContent());
        Gson g = new Gson();
        String informacionConciertos="";
        Conciertos conciertos = g.fromJson(parsedJSON, Conciertos.class);
        for (int i=0; i<conciertos.getLength();i++){
            informacionConciertos=informacionConciertos+"\n"+conciertos.getNombreConcierto(i)+"-"+conciertos.getTicketsConcierto(i);
        }

        return informacionConciertos;
    }
    public void updateJSON(String concierto,int entradas) throws IOException {
        System.out.format("Getting file from S3 bucket bucketp1ta...\n");
        AmazonS3 s3 = AmazonS3ClientBuilder.standard()
                .withRegion(Regions.fromName(region))
                .build();
        try {
            System.out.println("Downloading an object");
            fullObject = s3.getObject(new GetObjectRequest("bucketp1ta", keyConciertos));
        } catch (AmazonServiceException e) {
            System.err.println(e.getErrorMessage());
            System.exit(1);
        }
        System.out.println("Archivo recuperado!");
        String parsedJSON = getAsString(fullObject.getObjectContent());
        Gson g = new Gson();
        Conciertos conciertos = g.fromJson(parsedJSON, Conciertos.class);
        for (int i=0; i<conciertos.getLength();i++){
            if(conciertos.getNombreConcierto(i).equals(concierto)){
                int entradasPrevias = conciertos.getTicketsConcierto(i);
                int nuevasEntradas= entradasPrevias-entradas;
                conciertos.setTicketsConcierto(i,nuevasEntradas);
            }
        }
        String JSON = g.toJson(conciertos);
        System.out.println("Escribiendo json en archivo");
        System.out.println(JSON);
        FileWriter file = new FileWriter("conciertos.json");
        file.write(JSON);
        file.close();
        File f = new File("conciertos.json");
        try {
            System.out.println("Subiendo json actualizado");
            s3.putObject("bucketp1ta", "conciertos.json", f);
        } catch (AmazonServiceException e) {
            System.err.println(e.getErrorMessage());
            System.exit(1);
        }
        System.out.println("Finalizado!");
    }
    public static void sendMessage(SqsClient sqsClient, String queueName, String message) {

        try {
            CreateQueueRequest request = CreateQueueRequest.builder()
                    .queueName(queueName)
                    .build();
            CreateQueueResponse createResult = sqsClient.createQueue(request);

            GetQueueUrlRequest getQueueRequest = GetQueueUrlRequest.builder()
                    .queueName(queueName)
                    .build();

            String queueUrl = sqsClient.getQueueUrl(getQueueRequest).queueUrl();

            SendMessageRequest sendMsgRequest = SendMessageRequest.builder()
                    .queueUrl(queueUrl)
                    .messageBody(message)
                    .delaySeconds(0)
                    .build();
            sqsClient.sendMessage(sendMsgRequest);

        } catch (QueueNameExistsException e) {
            throw e;
        }
    }
    private static String getAsString(InputStream is) throws IOException {
        if (is == null)
            return "";
        StringBuilder sb = new StringBuilder();
        try {
            BufferedReader reader = new BufferedReader(
                    new InputStreamReader(is, StringUtils.UTF8));
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line);
            }
        } finally {
            is.close();
        }
        return sb.toString();
    }
}
