package uk.ac.ebi.spot.ols.neo4j.service;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.util.stream.Stream;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@Tag("integrationTest")
public class ClassJsTreeBuilderTest {
    private static final Logger logger = LoggerFactory.getLogger(ClassJsTreeBuilderTest.class);

    ClassJsTreeBuilderHelper classJsTreeBuilderHelper;

    public ClassJsTreeBuilderTest() {
        Path path = FileSystems.getDefault().getPath("src/test/resources/integrationTests/preferredRoots/neo4j").toAbsolutePath();
        logger.debug("Neo4j path = " + path.toString() + "\n");
        classJsTreeBuilderHelper = new ClassJsTreeBuilderHelper(path.toString());
    }


    @ParameterizedTest
    @MethodSource("provideGetJsTreeForParentQueryData")
    public void testGetJsTreeForParentQuery(String ontologyName, String iri, boolean siblings, ViewMode viewMode) {
        Object object = classJsTreeBuilderHelper.getJsTree(ontologyName, iri, siblings, viewMode);

        logger.debug("Ontology: " + ontologyName);
        logger.debug("Iri: " + iri);
        logger.debug("Siblings: " + siblings);
        logger.debug("View mode: " + viewMode);
        logger.debug("Result: " + object);
    }


    private static Stream<Arguments> provideGetJsTreeForParentQueryData() {
        return Stream.of(
                Arguments.of("duo", "http://purl.obolibrary.org/obo/BFO_0000001", false, ViewMode.ALL),
                Arguments.of("duo", "http://purl.obolibrary.org/obo/BFO_0000001", true, ViewMode.ALL),
                Arguments.of("duo", "http://purl.obolibrary.org/obo/BFO_0000002", false, ViewMode.ALL),
                Arguments.of("duo", "http://purl.obolibrary.org/obo/BFO_0000002", true, ViewMode.ALL),

                Arguments.of("duo", "http://purl.obolibrary.org/obo/DUO_0000017", false, ViewMode.ALL),
                Arguments.of("duo", "http://purl.obolibrary.org/obo/DUO_0000017", true, ViewMode.ALL),
                Arguments.of("duo", "http://purl.obolibrary.org/obo/DUO_0000021", false, ViewMode.ALL),
                Arguments.of("duo", "http://purl.obolibrary.org/obo/DUO_0000021", true, ViewMode.ALL),

                Arguments.of("duo", "http://purl.obolibrary.org/obo/DUO_0000017", false, ViewMode.PREFERRED_ROOTS),
                Arguments.of("duo", "http://purl.obolibrary.org/obo/DUO_0000017", true, ViewMode.PREFERRED_ROOTS),
                Arguments.of("duo", "http://purl.obolibrary.org/obo/DUO_0000021", false, ViewMode.PREFERRED_ROOTS),
                Arguments.of("duo", "http://purl.obolibrary.org/obo/DUO_0000021", true, ViewMode.PREFERRED_ROOTS)
        );
    }
}
