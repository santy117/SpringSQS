package com.practica1.Controllers;

import com.itextpdf.text.DocumentException;
import com.practica1.Models.ValueAttr;
import com.practica1.Services.ServicioDatos;
import org.apache.tomcat.util.json.ParseException;
import org.json.JSONException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.aws.messaging.core.QueueMessagingTemplate;
import org.springframework.cloud.aws.messaging.listener.annotation.SqsListener;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.sqs.SqsClient;

import java.io.IOException;
import java.net.URISyntaxException;



@RestController
public class Practica1Controller {
    private Logger logger = LoggerFactory.getLogger(Practica1Controller.class);

    @Autowired
    ServicioDatos servicioDatos;
    @Autowired
    private QueueMessagingTemplate queueMessagingTemplate;

    @Value("${jsa.s3.bucket}")
    private String bucketName;


    @SqsListener("ColaTAInboxSantiago")
    public void getMessage(ValueAttr message) throws DocumentException, IOException, URISyntaxException {
        logger.info("Message from SQS Queue - "+message.toString());
        String idCompra= String.valueOf(message.getIdCompra());
        String tickets= message.getTickets();
        logger.info("El id de la compra es: "+idCompra);
        logger.info("Creando archivo pdf de la compra...");
        servicioDatos.createFile(tickets,idCompra);
        logger.info("Enviando archivo a s3...");
        servicioDatos.sendFile(idCompra);
        logger.info("Enviando url del archivo al outbox...");
        String url = "https://bucketp1ta.s3.amazonaws.com/"+message.getIdCompra()+".pdf";
        SqsClient sqsClient = SqsClient.builder()
                .region(Region.US_EAST_1)
                .build();
        servicioDatos.sendMessage(sqsClient,"ColaTAOutboxSantiago",url);
        logger.info("Finalizado, acualizando numero de entradas disponibles...");
        servicioDatos.updateJSON(message.getConcierto(),Integer.valueOf(message.getTickets()));
    }//getMessage


    @CrossOrigin(origins = "*")
    @GetMapping("/sendFile")
    public String sendFile(@RequestParam(value = "cadena", defaultValue = "default") String cad) throws IOException {
        servicioDatos.sendFile(cad);
        return String.format("File enviado con el nombre %s.pdf ", cad);
    }
    @CrossOrigin(origins = "*")
    @GetMapping("/createFile")
    public void createFile(@RequestParam(value = "informacion") String info, @RequestParam(value = "key") String key) throws IOException, DocumentException, URISyntaxException {

        servicioDatos.createFile(info,key);

    }
    @CrossOrigin(origins = "*")
    @GetMapping("/getURL")
    public String getURL(@RequestParam(value = "key") String key) throws IOException, DocumentException, URISyntaxException {
        String url = "https://bucketp1ta.s3.amazonaws.com/"+key+".pdf";
       return url;

    }
    @CrossOrigin(origins = "*")
    @GetMapping("/getConciertos")
    public String getConciertos() throws IOException, ParseException, JSONException {
        String informacion= servicioDatos.getJSON();
        return informacion;
    }
    @CrossOrigin(origins = "*")
    @GetMapping("/ip")
    public String ip() throws IOException {

        String ip= servicioDatos.getIp();
        return "{\"ip\":\""+ip+"\"}";
    }
}
