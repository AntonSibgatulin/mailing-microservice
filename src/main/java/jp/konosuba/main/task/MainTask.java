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
import org.apache.kafka.clients.consumer.ConsumerRebalanceListener;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.json.JSONObject;
import redis.clients.jedis.Jedis;

import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingDeque;

public class MainTask extends Thread {

    private int i = 0;

    private ExecutorService executorService;
    private Config config;
    private Jedis jedis;

    public LinkedBlockingDeque<MessageAction> messageActionsQueue =
            new LinkedBlockingDeque<>();

    private MainController mainController;

    private KafkaProducer<String, String> producer;
    private KafkaConsumer<String, String> consumer;
    private Map<String, MessageObject> messages = new HashMap<>();

    private Session session = null;

    private SMTPTransport transport=null;

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
        //properties.setProperty(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        properties.setProperty(ConsumerConfig.MAX_POLL_RECORDS_CONFIG, config.getMax_poll_records());
        properties.setProperty(ConsumerConfig.PARTITION_ASSIGNMENT_STRATEGY_CONFIG, "org.apache.kafka.clients.consumer.RoundRobinAssignor");
        properties.setProperty(ConsumerConfig.MAX_PARTITION_FETCH_BYTES_CONFIG, "250");
        //properties.put("client.id", "consumer_2");
        consumer = new KafkaConsumer<>(properties);
        consumer.assign(Arrays.asList(new TopicPartition(topic,config.getPartition())));
        //consumer.subscribe(Arrays.asList(topic));
        /*consumer.subscribe(Arrays.asList(topic), new ConsumerRebalanceListener() {
            public void onPartitionsRevoked(Collection<TopicPartition> partitions) {
                // Вызывается при перераспределении партиций перед балансировкой
            }

            public void onPartitionsAssigned(Collection<TopicPartition> partitions) {
                // Вызывается после балансировки при переназначении партиций
            }
        });

         */

        Properties props = new Properties();
        props.put("bootstrap.servers", App.config.getKafka_host() + ":" + App.config.getKafka_port());
        /*props.put("acks", "all");
        props.put("retries", 0);
        props.put("batch.size", 16384);
        props.put("linger.ms", 1);
        props.put("buffer.memory", 33554432);

         */
        props.put("key.serializer", "org.apache.kafka.common.serialization.StringSerializer");
        props.put("value.serializer", "org.apache.kafka.common.serialization.StringSerializer");

        producer = new KafkaProducer<>(props);


        properties = new Properties();
        properties.put("mail.smtp.auth", config.getAuth());
        properties.put("mail.smtp.starttls.enable", config.getMail_smtp_starttls_enable());
        properties.put("mail.smtp.host", config.getMail_smtp_host());
        properties.put("mail.smtp.port", config.getMail_smtp_port());

        //properties.put("mail.smtps.ssl.checkserveridentity", true);
        //properties.put("mail.smtps.ssl.trust", "*");
        //properties.put("mail.smtp.ssl.enable", "true");

        session = Session.getInstance(properties, new javax.mail.Authenticator() {
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(config.getEmail_username(), config.getEmail_password());

            }
        });
        //ryfvobkpsriatafi
         try {
             transport = (SMTPTransport) session.getTransport("smtp");

             transport.connect(config.getMail_smtp_host(), config.getEmail_username(), config.getEmail_password());
        } catch (MessagingException e) {
            e.printStackTrace();
        }


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



                Message message = new MimeMessage(session);
                try {

                    message.setFrom(new InternetAddress(config.getEmail_username()));

                    message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(contacts.getEmail()));
                    message.setSubject(messageAction.getMessageObject().getTypeMessage() + "#" + (new Random().nextInt(1000000)));
                    message.setText(messageAction.getMessageObject().getMessage());

                    try {
                        Transport.send(message, message.getAllRecipients());
                    }catch (Exception e){
                        //e.printStackTrace();
                    }
                    i ++;
                    System.out.println(i);
                    // Check the delivery status of the message
                    JSONObject jsonObject = new JSONObject();
                    jsonObject.put("messageAction",new JSONObject(ClassUtils.toJSON(messageAction)));
                    //jsonObject.put("id",messageAction.getMessageObject().getHashId());

                   /* int responseCode = transport.getLastReturnCode();
                    if (responseCode == 250) {
                        //System.out.println("The message was delivered successfully.");
                        jsonObject.put("typeOperation", "email_ok");
                        sendMessageInKafka(jsonObject.toString());
                    } else {
                        jsonObject.put("typeOperation", "email_error");
                        sendMessageInKafka(jsonObject.toString());
                    }

                    */

                    //transport.close();
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
        producer.send(record);
    }

    public String getMessage(String hash) {
        return getJedis().get(hash);
    }
    public void removeMessageFromCache(String hash){
        this.messages.remove(hash);
        getJedis().del(hash);
    }
}
