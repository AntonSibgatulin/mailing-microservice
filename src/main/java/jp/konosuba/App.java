package jp.konosuba;

import jp.konosuba.config.Config;
import jp.konosuba.database.DatabaseService;
import jp.konosuba.demo.ConsumerDemo;
import jp.konosuba.main.MainTask;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.errors.WakeupException;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import redis.clients.jedis.Jedis;

import java.io.*;
import java.time.Duration;
import java.util.Arrays;
import java.util.Properties;

/**
 * Hello world!
 */
public class App {
    public static final Config config = new Config();
    private static final Logger log = LoggerFactory.getLogger(ConsumerDemo.class);

    public static void main(String[] args) {
        //load configure
        initConfig("configure/conf.json");


        //init all depended classes
        Jedis jedis = new Jedis();
        DatabaseService databaseService = new DatabaseService();


        MainTask mainTask = new MainTask(config
                , jedis
                , databaseService);
        mainTask.start();


        String bootstrapServers = config.getKafka_host() + ":" + config.getKafka_port();
        String groupId = config.getGroupId();
        String topic = config.getName_of_topic();


        Properties properties = new Properties();
        properties.setProperty(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        properties.setProperty(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        properties.setProperty(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        properties.setProperty(ConsumerConfig.GROUP_ID_CONFIG, groupId);
        properties.setProperty(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        properties.setProperty(ConsumerConfig.MAX_POLL_RECORDS_CONFIG,config.getMax_poll_records());
        properties.setProperty(ConsumerConfig.PARTITION_ASSIGNMENT_STRATEGY_CONFIG, "org.apache.kafka.clients.consumer.RoundRobinAssignor");
        //properties.setProperty(ConsumerConfig.MAX_PARTITION_FETCH_BYTES_CONFIG, "250");

        KafkaConsumer<String, String> consumer = new KafkaConsumer<>(properties);

        try {


            consumer.subscribe(Arrays.asList(topic));


            while (true) {
                ConsumerRecords<String, String> records =
                        consumer.poll(Duration.ofMillis(config.getDuration_of_poll()));

                for (ConsumerRecord<String, String> record : records) {
                    String message = record.value();
                    mainTask.getMainController().execute(message);
                    //log.info("Key: " + record.key() + ", Value: " + record.value());
                    //log.info("Partition: " + record.partition() + ", Offset:" + record.offset());
                }
            }

        } catch (WakeupException e) {
            log.info("Wake up exception!");
            // we ignore this as this is an expected exception when closing a consumer
        } catch (Exception e) {
            log.error("Unexpected exception", e);
        } finally {
            consumer.close(); // this will also commit the offsets if need be.
            log.info("The consumer is now gracefully closed.");
        }

    }

    public static void initConfig(String path) {
        JSONObject configJSON = readConfigureFile(path);
        config.setCountThreadInPoll(configJSON.getInt("countThreadInPoll"));
        config.setGroupId(configJSON.getString("groupId"));
        config.setName_of_topic(configJSON.getString("name_of_topic"));
        config.setAuth(configJSON.getString("mail.smtp.auth"));
        config.setMail_smtp_starttls_enable(configJSON.getString("mail.smtp.starttls.enable"));
        config.setMail_smtp_host(configJSON.getString("mail.smtp.host"));
        config.setMail_smtp_port(configJSON.getString("mail.smtp.port"));
        config.setEmail_username(configJSON.getString("email_username"));
        config.setEmail_password(configJSON.getString("email_password"));
        config.setKafka_host(configJSON.getString("kafka_host"));
        config.setKafka_port(configJSON.getString("kafka_port"));
        config.setMax_poll_records(configJSON.getString("max.poll.records"));
        config.setDuration_of_poll(configJSON.getLong("duration_of_poll"));

    }

    public static JSONObject readConfigureFile(String file) {
        try {
            BufferedReader bufferedReader = new BufferedReader(new FileReader(new File(file)));
            String all = "";
            String pie = null;
            while ((pie = bufferedReader.readLine()) != null) {
                all += pie;
            }
            return new JSONObject(all);
        } catch (FileNotFoundException e) {
            System.out.println("File not found " + file);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
}
