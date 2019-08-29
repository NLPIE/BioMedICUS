/*
 * Copyright 2019 Regents of the University of Minnesota.
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

package edu.umn.biomedicus.concepts;

import edu.umn.biomedicus.common.dictionary.StringsBag;
import edu.umn.biomedicus.framework.LifecycleManaged;
import org.rocksdb.RocksDB;
import org.rocksdb.RocksDBException;

import javax.annotation.Nullable;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * An implementation of {@link ConceptDictionary} that uses RocksDB as a backend.
 *
 * @since 1.8.0
 */
class RocksDbConceptDictionary implements ConceptDictionary, LifecycleManaged {

  private final RocksDB phrases;

  private final RocksDB lowercase;

  private final RocksDB normsDB;

  private final Map<Integer, String> sources;

  RocksDbConceptDictionary(
      RocksDB phrases,
      RocksDB lowercase,
      RocksDB normsDB,
      Map<Integer, String> sources
  ) {
    this.phrases = phrases;
    this.lowercase = lowercase;
    this.normsDB = normsDB;
    this.sources = sources;
  }

  static List<ConceptRow> toList(byte[] bytes) {
    List<ConceptRow> list = new ArrayList<>();
    ByteBuffer buffer = ByteBuffer.wrap(bytes);
    while (buffer.hasRemaining()) {
      list.add(ConceptRow.next(buffer));
    }

    return list;
  }

  @Nullable
  @Override
  public List<ConceptRow> forPhrase(String phrase) {
    try {
      byte[] bytes = phrases.get(phrase.getBytes(StandardCharsets.UTF_8));
      return bytes == null ? null : toList(bytes);
    } catch (RocksDBException e) {
      throw new RuntimeException(e);
    }
  }

  @Nullable
  @Override
  public List<ConceptRow> forLowercasePhrase(String phrase) {
    try {
      byte[] bytes = lowercase.get(phrase.getBytes(StandardCharsets.UTF_8));
      return bytes == null ? null : toList(bytes);
    } catch (RocksDBException e) {
      throw new RuntimeException(e);
    }
  }

  @Nullable
  @Override
  public List<ConceptRow> forNorms(StringsBag norms) {
    if (norms.uniqueTerms() == 0) {
      return null;
    }
    try {
      byte[] bytes = normsDB.get(norms.getBytes());
      return bytes == null ? null : toList(bytes);
    } catch (RocksDBException e) {
      throw new RuntimeException(e);
    }
  }

  @Nullable
  @Override
  public String source(int identifier) {
    return sources.get(identifier);
  }

  @Override
  public void doShutdown() {

  }
}
