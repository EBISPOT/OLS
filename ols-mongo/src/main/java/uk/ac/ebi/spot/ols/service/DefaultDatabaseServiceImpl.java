package uk.ac.ebi.spot.ols.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import uk.ac.ebi.spot.ols.xrefs.Database;
import uk.ac.ebi.spot.ols.xrefs.DatabaseService;
import uk.ac.ebi.spot.ols.xrefs.OboXrefLoader;

import java.util.Collection;
import java.util.Optional;

/**
 * @author Simon Jupp
 * @date 13/06/2016
 * Samples, Phenotypes and Ontologies Team, EMBL-EBI
 */
@Service
public class DefaultDatabaseServiceImpl implements DatabaseService {

    @Autowired
    OboXrefLoader loader;

    @Override
    public Collection<Database> find() {
        return loader.getDatabases();
    }

    @Override
    public Optional<Database> findByName(String name) {
        return loader.findByName(name);
    }

}
