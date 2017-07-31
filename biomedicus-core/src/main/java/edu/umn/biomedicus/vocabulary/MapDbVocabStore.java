/*
 * Copyright (c) 2017 Regents of the University of Minnesota.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package edu.umn.biomedicus.vocabulary;

import static com.google.common.base.Preconditions.checkState;

import com.google.inject.Inject;
import edu.umn.biomedicus.annotations.Setting;
import edu.umn.biomedicus.common.terms.TermIndex;
import java.io.IOException;
import java.nio.file.Path;
import javax.annotation.Nullable;
import org.mapdb.DB;
import org.mapdb.DBMaker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Vocabulary store implemented using MapDB term indexes.
 *
 * @author Ben Knoll
 * @since 1.6.0
 */
class MapDbVocabStore extends VocabularyStore {

  private static final Logger LOGGER = LoggerFactory.getLogger(MapDbVocabStore.class);

  private final Path dbPath;

  private final Boolean inMemory;

  @Nullable
  private DB db;

  @Nullable
  private TermIndex words;

  @Nullable
  private TermIndex terms;

  @Nullable
  private TermIndex norms;

  @Inject
  MapDbVocabStore(@Setting("vocabulary.db.path") Path dbPath,
      @Setting("vocabulary.inMemory") Boolean inMemory) {
    this.dbPath = dbPath;
    this.inMemory = inMemory;
  }

  @Override
  public void open() {
    String dbPathString = dbPath.toString();
    db = DBMaker.fileDB(dbPathString).readOnly().make();
    LOGGER.info("Loading vocabulary indices from database: {}", dbPathString);
    LOGGER.info("Vocabulary: loading words index");
    words = new MapDbTermIndex(db, "words").inMemory(inMemory);
    LOGGER.info("Vocabulary: loading terms index");
    terms = new MapDbTermIndex(db, "terms").inMemory(inMemory);
    LOGGER.info("Vocabulary: loading norms index");
    norms = new MapDbTermIndex(db, "norms").inMemory(inMemory);
  }

  @Override
  TermIndex getWords() {
    checkState(db != null, "Not open");
    checkState(words != null, "Not open yet");
    return words;
  }

  @Override
  TermIndex getTerms() {
    checkState(db != null, "Not open");
    checkState(terms != null, "Not open yet");
    return terms;
  }

  @Override
  TermIndex getNorms() {
    checkState(db != null, "Not open");
    checkState(norms != null, "Not open yet");
    return norms;
  }

  @Override
  public void close() throws IOException {
    if (db != null) {
      db.close();
    }
    db = null;
  }
}