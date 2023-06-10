package jp.konosuba.config;

import lombok.Data;

@Data
public class Config {
    private Integer countThreadInPoll;
    private String groupId;
    private String name_of_topic;
    private String auth;
    private String mail_smtp_starttls_enable;
    private String mail_smtp_host;
    private String mail_smtp_port;
    private String email_username;
    private String email_password;
    private String kafka_host;
    private String kafka_port;
    private Long duration_of_poll;
    private String max_poll_records;
    private String kafka_topic_mainController;
    private String redis_host;
    private String redis_port;
}
