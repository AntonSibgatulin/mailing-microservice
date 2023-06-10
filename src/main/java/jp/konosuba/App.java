package jp.konosuba;

import jp.konosuba.config.Config;

import jp.konosuba.main.task.MainTask;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.common.errors.WakeupException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import redis.clients.jedis.Jedis;

import java.io.*;
import java.time.Duration;

/**
 * Hello world!
 */
public class App {

    public static final Config config = new Config();
    private static final Logger log = LoggerFactory.getLogger(App.class);

    public static void main(String[] args) {
        //load configure
        initConfig("configure/conf.json");


        //init all depended classes
        Jedis jedis = new Jedis(config.getRedis_host(),Integer.valueOf(config.getRedis_port()));



        MainTask mainTask = new MainTask(config
                , jedis
                );
        mainTask.start();


        try {


            while (true) {
                ConsumerRecords<String, String> records =
                        mainTask.getConsumer().poll(Duration.ofMillis(config.getDuration_of_poll()));

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
            mainTask.getConsumer().close(); // this will also commit the offsets if need be.
            log.info("The consumer is now gracefully closed.");
        }

    }

    public static void initConfig(String path) {

        JSONObject configJSON = readConfigureFile(path);
        config.setCountThreadInPoll(configJSON.getInt("countThreadInPoll"));
        config.setGroupId(configJSON.getString("groupId"));
        config.setName_of_topic(configJSON.getString("name_topic_from_api_service"));
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
        config.setKafka_topic_mainController(configJSON.getString("name_topic_for_main_controller_service"));
        config.setRedis_host(configJSON.getString("redis_host"));
        config.setRedis_port(configJSON.getString("redis_port"));
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
