package uk.ac.ebi.spot.ols;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;

import java.util.Properties;

/**
 * @author Simon Jupp
 * @date 19/01/2016
 * Samples, Phenotypes and Ontologies Team, EMBL-EBI
 */
@Configuration
public class MailConfig {

    @Value("${mail.protocol:smtp}")
    private String protocol;
    @Value("${mail.host:}")
    private String host;
    @Value("${mail.port:587}")
    private int port;


    // Code based on: http://stackoverflow.com/questions/22483407/send-emails-with-spring-by-using-java-annotations
    @Bean
    public JavaMailSender javaMailSender() {
        JavaMailSenderImpl mailSender = new JavaMailSenderImpl();
        mailSender.setJavaMailProperties(getMailProperties());
        if (StringUtils.isNoneEmpty(host)) {
            mailSender.setHost(host);
        }
        mailSender.setPort(port);
        mailSender.setProtocol(protocol);
        return mailSender;
    }

    private Properties getMailProperties() {
        Properties properties = new Properties();
        // Specifies the default message transport protocol
        properties.setProperty("mail.transport.protocol", "smtp");
        properties.setProperty("mail.smtp.auth", "false");
        properties.setProperty("mail.smtp.starttls.enable", "false");

        // Debug property
        properties.setProperty("mail.debug", "false");
        return properties;
    }
}