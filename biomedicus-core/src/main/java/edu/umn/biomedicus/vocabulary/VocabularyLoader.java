package edu.umn.biomedicus.vocabulary;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import edu.umn.biomedicus.annotations.Setting;
import edu.umn.biomedicus.application.DataLoader;
import edu.umn.biomedicus.common.terms.TermIndex;
import edu.umn.biomedicus.exc.BiomedicusException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 *
 */
@Singleton
public class VocabularyLoader extends DataLoader<Vocabulary> {

    private static final Logger LOGGER = LogManager.getLogger();

    private final Path wordsPath;

    @Inject
    public VocabularyLoader(@Setting("vocabulary.wordIndex.path") Path wordsPath) {
        this.wordsPath = wordsPath;
    }

    @Override
    protected Vocabulary loadModel() throws BiomedicusException {
        try {
            LOGGER.info("Loading words into term index from path: {}", wordsPath);

            TermIndex wordIndex = new TermIndex();
            Files.lines(wordsPath).forEach(wordIndex::addTerm);

            return new Vocabulary(wordIndex);
        } catch (IOException e) {
            throw new BiomedicusException(e);
        }
    }
}