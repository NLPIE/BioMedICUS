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

package edu.umn.biomedicus.concepts;

import com.google.inject.ProvidedBy;
import edu.umn.biomedicus.common.dictionary.StringsBag;
import java.util.List;
import javax.annotation.Nullable;

/**
 * An interface for the dictionary of concepts used by the DictionaryConceptRecognizer.
 */
@ProvidedBy(ConceptDictionaryLoader.class)
public interface ConceptDictionary {

  @Nullable
  List<SuiCuiTui> forPhrase(String phrase);

  @Nullable
  List<SuiCuiTui> forLowercasePhrase(String phrase);

  @Nullable
  List<SuiCuiTui> forNorms(StringsBag norms);
}
