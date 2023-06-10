package jp.konosuba.main.task;

import com.sun.mail.smtp.SMTPTransport;
import jp.konosuba.App;
import jp.konosuba.config.Config;
import jp.konosuba.data.contact.Contacts;
import jp.konosuba.data.message.MessageAction;
import jp.konosuba.data.message.MessageObject;

import jp.konosuba.main.controller.MainController;
import jp.konosuba.utils.ClassUtils;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.json.JSONObject;
import redis.clients.jedis.Jedis;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingDeque;

public class MainTask extends Thread {


    private ExecutorService executorService;
    private Config config;
    private Jedis jedis;

    private LinkedBlockingDeque<MessageAction> messageActionsQueue =
            new LinkedBlockingDeque<>();

    private MainController mainController;

    private KafkaProducer<String, String> producer;
    private KafkaConsumer<String, String> consumer;
    private Map<String, MessageObject> messages = new HashMap<>();


    public MainTask(Config config, Jedis jedis) {
        this.config = config;
        executorService = Executors.newFixedThreadPool(this.config.getCountThreadInPoll());
        this.jedis = jedis;

        mainController = new MainController(this);


        String bootstrapServers = config.getKafka_host() + ":" + config.getKafka_port();
        String groupId = config.getGroupId();
        String topic = config.getName_of_topic();


        Properties properties = new Properties();
        properties.setProperty(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        properties.setProperty(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        properties.setProperty(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        properties.setProperty(ConsumerConfig.GROUP_ID_CONFIG, groupId);
        properties.setProperty(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        properties.setProperty(ConsumerConfig.MAX_POLL_RECORDS_CONFIG, config.getMax_poll_records());
        properties.setProperty(ConsumerConfig.PARTITION_ASSIGNMENT_STRATEGY_CONFIG, "org.apache.kafka.clients.consumer.RoundRobinAssignor");
        //properties.setProperty(ConsumerConfig.MAX_PARTITION_FETCH_BYTES_CONFIG, "250");

        consumer = new KafkaConsumer<>(properties);
        getConsumer().subscribe(Arrays.asList(topic));

        Properties props = new Properties();
        props.put("bootstrap.servers", App.config.getKafka_host() + ":" + App.config.getKafka_port());
        props.put("acks", "all");
        props.put("retries", 0);
        props.put("batch.size", 16384);
        props.put("linger.ms", 1);
        props.put("buffer.memory", 33554432);
        props.put("key.serializer", "org.apache.kafka.common.serialization.StringSerializer");
        props.put("value.serializer", "org.apache.kafka.common.serialization.StringSerializer");

        producer = new KafkaProducer<>(props);


    }


    @Override
    public void run() {
        while (true) {

            if (messageActionsQueue.isEmpty() == false) {

                MessageAction messageAction = messageActionsQueue.poll();
                if (messageAction == null) {
                    continue;
                }

                Runnable task = null;
                if (messageAction.getMessageObject().getType().equals("email")) {
                    task = sendMessageEmail(messageAction);
                }
                executorService.execute(task);


            }

        }
    }


    public Runnable sendMessageEmail(MessageAction messageAction) {
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
                    message.setSubject(messageAction.getMessageObject().getTypeMessage() + "#" + (new Random().nextInt(1000000)));
                    message.setText(messageAction.getMessageObject().getMessage());

                    SMTPTransport transport = (SMTPTransport) session.getTransport("smtp");
                    transport.connect(config.getMail_smtp_host(), config.getEmail_username(), config.getEmail_password());

                    transport.sendMessage(message, message.getAllRecipients());

                    // Check the delivery status of the message
                    JSONObject jsonObject = new JSONObject(ClassUtils.toJSON(messageAction));
                    jsonObject.put("id",messageAction.getMessageObject().getHashId());
                    int responseCode = transport.getLastReturnCode();
                    if (responseCode == 250) {
                        //System.out.println("The message was delivered successfully.");
                        jsonObject.put("type", "ok");
                        sendMessageInKafka(jsonObject.toString());
                    } else {
                        jsonObject.put("type", "error");
                        sendMessageInKafka(jsonObject.toString());
                    }

                    transport.close();
                } catch (MessagingException e) {
                    e.printStackTrace();
                }

            }
        };

        return task;
    }

    public void put(MessageAction messageAction) {
        this.messageActionsQueue.offer(messageAction);
    }

    public void saveMessageObject(String id, MessageObject messageObject) {
        this.messages.put(id, messageObject);
    }

    public MessageObject getMessageObject(String id) {
        return this.messages.get(id);
    }

    public MainController getMainController() {
        return mainController;
    }

    public Jedis getJedis() {
        return jedis;
    }


    public KafkaProducer<String, String> getProducer() {
        return this.producer;
    }

    public KafkaConsumer<String, String> getConsumer() {
        return this.consumer;
    }

    public void sendMessageInKafka(String message) {
        ProducerRecord<String, String> record = new ProducerRecord<String, String>(App.config.getKafka_topic_mainController(), null, message);
        getProducer().send(record);
    }

    public String getMessage(String hash) {
        return getJedis().get(hash);
    }
    public void removeMessageFromCache(String hash){
        this.messages.remove(hash);
        getJedis().del(hash);
    }
}
