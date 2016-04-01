package uk.ac.ebi.spot.ols.config;

import com.mongodb.MongoClientOptions;
import com.mongodb.MongoCredential;
import com.mongodb.ReadPreference;
import com.mongodb.ServerAddress;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.mongo.MongoProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.core.MongoClientFactoryBean;

import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Simon Jupp
 * @date 14/09/15
 * Samples, Phenotypes and Ontologies Team, EMBL-EBI
 */
@Configuration
public class MongoAppConfig {

    @Value("${ols.mongo.readpreference:}")
    String readPreference ="";

    @Value("${ols.mongo.seedlist:}")
    String seedList = "";

    @Autowired
    MongoProperties properties;

/*
* Factory bean that creates the com.mongodb.Mongo instance
* Checks supplied properties for a seed list and read preference
*
*/

//    @Bean
//    MongoClientOptions mongoOption( ) {
//
//    }

    @Bean
    MongoClientFactoryBean mongoFactory() throws UnknownHostException {

        MongoClientFactoryBean mongoClientFactoryBean = new MongoClientFactoryBean();

        if (properties.getAuthenticationDatabase() != null) {
            MongoCredential credential = MongoCredential.createCredential(properties.getUsername(), properties.getAuthenticationDatabase(), properties.getPassword());
            mongoClientFactoryBean.setCredentials(new MongoCredential[]{credential});
        }


        if (!("").equals(readPreference) && !("").equals(seedList)) {
            List<ServerAddress> seedListArray = new ArrayList<ServerAddress>();

            for (String seed : seedList.split(",")) {
                seedListArray.add(new ServerAddress(seed));
            }

            mongoClientFactoryBean.setReplicaSetSeeds(seedListArray.toArray(new ServerAddress[seedListArray.size()]));
            ReadPreference preference = ReadPreference.valueOf(readPreference);
            if (preference != null) {
                MongoClientOptions.Builder clientOptions = new MongoClientOptions.Builder();
                clientOptions.readPreference(preference);
                mongoClientFactoryBean.setMongoClientOptions(clientOptions.build());
            }
        }
        else {
            mongoClientFactoryBean.setHost(properties.getHost());
            if (properties.getPort() != null) {
                mongoClientFactoryBean.setPort(properties.getPort());
            }
        }
        return mongoClientFactoryBean;

    }
}
