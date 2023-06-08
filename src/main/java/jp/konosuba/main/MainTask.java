package jp.konosuba.main;

import com.sun.mail.smtp.SMTPTransport;
import jp.konosuba.config.Config;
import jp.konosuba.contact.Contacts;
import jp.konosuba.controller.MainController;
import jp.konosuba.database.DatabaseService;
import jp.konosuba.message.MessageAction;
import jp.konosuba.message.MessageObject;
import jp.konosuba.utils.StringUtils;
import redis.clients.jedis.Jedis;

import javax.mail.*;
import javax.mail.event.TransportEvent;
import javax.mail.event.TransportListener;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import javax.xml.crypto.Data;
import java.net.InetAddress;
import java.util.Properties;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingDeque;
import javax.mail.Transport;
public class MainTask extends Thread{


    private ExecutorService executorService;
    private Config config;
    private Jedis jedis;


    private LinkedBlockingDeque<MessageAction> messageActionsQueue =
            new LinkedBlockingDeque<>();


    private MainController mainController;
    private DatabaseService databaseService;

    public MainTask(Config config, Jedis jedis, DatabaseService databaseService){
        this.config = config;
        executorService = Executors.newFixedThreadPool(this.config.getCountThreadInPoll());
        this.jedis = jedis;

        mainController = new MainController(this);
        this.databaseService = databaseService;
    }


    @Override
    public void run(){
        while (true){

            if (messageActionsQueue.isEmpty()==false){

                MessageAction messageAction = messageActionsQueue.poll();
                if (messageAction==null){
                    continue;
                }

                Runnable task = null;
                if(messageAction.getMessageObject().getType().equals("email")){
                    task = sendMessageEmail(messageAction);
                }
                executorService.execute(task);



            }

        }
    }



    public Runnable sendMessageEmail(MessageAction messageAction){
        Runnable task = new Runnable() {
            @Override
            public void run() {
                Contacts contacts = messageAction.getContacts();

                Properties properties = new Properties();
                properties.put("mail.smtp.auth", config.getAuth());
                properties.put("mail.smtp.starttls.enable", config.getMail_smtp_starttls_enable());
                properties.put("mail.smtp.host", config.getMail_smtp_host());
                properties.put("mail.smtp.port", config.getMail_smtp_port());
                Session session = Session.getInstance(properties, new javax.mail.Authenticator() {
                    protected PasswordAuthentication getPasswordAuthentication() {
                        return new PasswordAuthentication(config.getEmail_username(), config.getEmail_password());

                    }
                });


                Message message = new MimeMessage(session);
                try {
                    message.setFrom(new InternetAddress(config.getEmail_username()));

                    message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(contacts.getEmail()));
                    message.setSubject(messageAction.getMessageObject().getTypeMessage()+"#"+(new Random().nextInt(1000000)));
                    message.setText(messageAction.getMessageObject().getMessage());

                    SMTPTransport transport = (SMTPTransport)session.getTransport("smtp");
                    transport.connect(config.getMail_smtp_host(), config.getEmail_username(), config.getEmail_password());

                    transport.sendMessage(message, message.getAllRecipients());

                    // Check the delivery status of the message
                    int responseCode = transport.getLastReturnCode();
                    if(responseCode == 250) {
                        System.out.println("The message was delivered successfully.");
                    } else {
                        System.out.println("The message could not be delivered. Response code: " + responseCode);
                    }

                    transport.close();
                } catch (MessagingException e) {
                    e.printStackTrace();
                }

            }
        };

        return task;
    }

    public void put(MessageAction messageAction){
        this.messageActionsQueue.offer(messageAction);
    }

    public MainController getMainController() {
        return mainController;
    }

    public Jedis getJedis(){
        return jedis;
    }
    public DatabaseService getDatabaseService(){
        return databaseService;
    }
}
