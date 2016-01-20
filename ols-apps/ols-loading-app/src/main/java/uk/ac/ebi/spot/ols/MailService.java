package uk.ac.ebi.spot.ols;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

/**
 * @author Simon Jupp
 * @date 19/01/2016
 * Samples, Phenotypes and Ontologies Team, EMBL-EBI
 */
@Service
public class MailService {

    @Autowired
    private JavaMailSender javaMailSender;

    // Reading these from application.properties
    @Value("${mail.from:}")
    private String from;

    public MailService() {

    }
    public void sendEmailNotification(String to, String[] cc, String subject, String message) {


        // Set up some of the values used in mail body

        // Format mail message
        SimpleMailMessage mailMessage = new SimpleMailMessage();
        mailMessage.setTo(to);
        if (cc != null) {
            mailMessage.setCc(cc);
        }
        mailMessage.setFrom(from);
        mailMessage.setSubject(subject);
        mailMessage.setText(message);
        javaMailSender.send(mailMessage);

    }


}