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

import edu.umn.biomedicus.acronyms.Acronym;
import edu.umn.biomedicus.common.dictionary.StringsBag;
import edu.umn.biomedicus.common.types.syntax.PartOfSpeech;
import edu.umn.biomedicus.common.types.syntax.PartsOfSpeech;
import edu.umn.biomedicus.normalization.NormForm;
import edu.umn.biomedicus.sentences.Sentence;
import edu.umn.biomedicus.tagging.PosTag;
import edu.umn.biomedicus.tokenization.TermToken;
import edu.umn.biomedicus.tokenization.Token;
import edu.umn.nlpengine.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import javax.inject.Inject;
import java.util.*;

import static edu.umn.biomedicus.common.types.syntax.PartOfSpeech.*;

/**
 * Uses a {@link ConceptDictionary} to recognize concepts in text. First, it will try to find direct
 * matches against all in-order sublists of tokens in a sentence. Then it will perform syntactic
 * permutations on any prepositional phrases in those sublists.
 *
 * @author Ben Knoll
 * @author Serguei Pakhomov
 * @since 1.0.0
 */
class DetectDictionaryConcepts implements DocumentTask {

  private static final Logger LOGGER = LoggerFactory.getLogger(DetectDictionaryConcepts.class);

  private static final Set<PartOfSpeech> TRIVIAL_POS = buildTrivialPos();

  private static final int SPAN_SIZE = 5;

  private final ConceptDictionary conceptDictionary;

  private Labeler<DictionaryTerm> termLabeler;

  private LabelIndex<PosTag> posTags;

  private LabelIndex<NormForm> normIndexes;

  private Labeler<UmlsConcept> conceptLabeler;

  /**
   * Creates a dictionary concept recognizer from a concept dictionary and a document.
   *
   * @param conceptDictionary the dictionary to get concepts from.
   */
  @Inject
  DetectDictionaryConcepts(ConceptDictionary conceptDictionary) {
    this.conceptDictionary = conceptDictionary;
  }

  private static Set<PartOfSpeech> buildTrivialPos() {
    Set<PartOfSpeech> builder = new HashSet<>();
    Collections.addAll(builder,
        DT,
        CD,
        WDT,
        TO,
        CC,
        PRP,
        PRP$,
        MD,
        EX,
        IN,
        XX);

    Set<PartOfSpeech> punctuationClass = PartsOfSpeech.getPunctuationClass();
    builder.addAll(punctuationClass);
    return Collections.unmodifiableSet(builder);
  }

  private boolean checkPhrase(Span span, String phrase, boolean oneToken, double confMod) {
    List<ConceptRow> phraseSUI = conceptDictionary.forPhrase(phrase);

    if (phraseSUI != null) {
      makeTerm(span, phraseSUI, 1 - confMod);
      return true;
    }

    if (oneToken) {
      return false;
    }

    phraseSUI = conceptDictionary.forLowercasePhrase(phrase.toLowerCase(Locale.ENGLISH));

    if (phraseSUI != null) {
      makeTerm(span, phraseSUI, 0.6 - confMod);
      return true;
    }

    return false;
  }

  private void checkTokenSet(List<TermToken> tokenSet) {
    if (tokenSet.size() <= 1) {
      return;
    }

    Span phraseAsSpan = new Span(tokenSet.get(0).getStartIndex(),
        tokenSet.get(tokenSet.size() - 1).getEndIndex());
    StringsBag.Builder builder = StringsBag.builder();
    for (NormForm normForm : normIndexes.inside(phraseAsSpan)) {

      PosTag posTag = posTags.firstAtLocation(normForm);

      if (posTag != null && TRIVIAL_POS.contains(posTag.getPartOfSpeech())) {
        continue;
      }

      builder.addTerm(normForm.normIdentifier());
    }
    StringsBag normBag = builder.build();

    List<ConceptRow> normsCUI = conceptDictionary.forNorms(normBag);
    if (normsCUI != null) {
      makeTerm(phraseAsSpan, normsCUI, .3);
    }
  }

  private void makeTerm(TextRange label, List<ConceptRow> cuis, double confidence) {
    for (ConceptRow row : cuis) {
      String source = conceptDictionary.source(row.getSource());
      if (source == null) {
        source = "unknown";
        LOGGER.warn("Unknown source");
      }
      conceptLabeler.add(
          new UmlsConcept(
              label,
              row.getSui().toString(),
              row.getCui().toString(),
              row.getTui().toString(),
              source,
              confidence
          )
      );
    }
    termLabeler.add(new DictionaryTerm(label));
  }

  @Override
  public void run(@Nonnull Document document) {
    LOGGER.debug("Finding concepts in document.");

    LabelIndex<Sentence> sentences = document.labelIndex(Sentence.class);
    normIndexes = document.labelIndex(NormForm.class);
    termLabeler = document.labeler(DictionaryTerm.class);
    conceptLabeler = document.labeler(UmlsConcept.class);
    posTags = document.labelIndex(PosTag.class);
    LabelIndex<TermToken> termTokenLabelIndex = document.labelIndex(TermToken.class);
    LabelIndex<Acronym> acronymLabelIndex = document.labelIndex(Acronym.class);

    String documentText = document.getText();
    for (Sentence sentence : sentences) {
      LOGGER.trace("Identifying concepts in a sentence");

      StringBuilder editedString = new StringBuilder();
      List<Span> editedStringSpans = new ArrayList<>();
      List<TermToken> sentenceTermTokens = termTokenLabelIndex.inside(sentence).asList();

      for (TermToken sentenceTermToken : sentenceTermTokens) {
        Acronym acronymForToken = acronymLabelIndex.firstAtLocation(sentenceTermToken);

        Token token;
        if (acronymForToken != null) {
          token = acronymForToken;
        } else {
          token = sentenceTermToken;
        }

        String tokenText = token.getText();
        Span span = new Span(editedString.length(), editedString.length() + tokenText.length());
        editedString.append(tokenText);
        if (token.getHasSpaceAfter()) {
          editedString.append(' ');
        }
        editedStringSpans.add(span);
      }

      for (int from = 0; from < sentenceTermTokens.size(); from++) {
        int to = Math.min(from + SPAN_SIZE, sentenceTermTokens.size());
        List<TermToken> window = sentenceTermTokens.subList(from, to);

        TermToken first = window.get(0);

        for (int subsetSize = 1; subsetSize <= window.size(); subsetSize++) {
          List<TermToken> windowSubset = window.subList(0, subsetSize);
          TermToken last = windowSubset.get(subsetSize - 1);
          Span entire = new Span(first.getStartIndex(), last.getEndIndex());

          if (posTags.inside(entire).stream()
              .map(PosTag::getPartOfSpeech).allMatch(TRIVIAL_POS::contains)) {
            continue;
          }

          if (checkPhrase(entire, entire.coveredString(documentText), subsetSize == 1, 0)) {
            continue;
          }

          int editedBegin = editedStringSpans.get(from).getStartIndex();
          int editedEnd = editedStringSpans.get(from + subsetSize - 1).getEndIndex();
          String editedSubstring = editedString.substring(editedBegin, editedEnd);
          if (checkPhrase(entire, editedSubstring, subsetSize == 1, .1)) {
            continue;
          }

          checkTokenSet(windowSubset);
        }
      }
    }
  }
}
