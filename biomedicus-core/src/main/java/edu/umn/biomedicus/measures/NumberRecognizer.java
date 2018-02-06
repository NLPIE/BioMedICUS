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

package edu.umn.biomedicus.measures;

import edu.umn.biomedicus.annotations.ProcessorSetting;
import edu.umn.biomedicus.common.StandardViews;
import edu.umn.biomedicus.common.types.text.ParseToken;
import edu.umn.biomedicus.common.types.text.Sentence;
import edu.umn.biomedicus.exc.BiomedicusException;
import edu.umn.biomedicus.framework.DocumentProcessor;
import edu.umn.biomedicus.framework.store.Document;
import edu.umn.biomedicus.framework.store.Label;
import edu.umn.biomedicus.framework.store.LabelIndex;
import edu.umn.biomedicus.framework.store.Labeler;
import edu.umn.biomedicus.framework.store.TextView;
import edu.umn.biomedicus.numbers.CombinedNumberDetector;
import edu.umn.biomedicus.numbers.NumberModel;
import edu.umn.biomedicus.numbers.NumberResult;
import edu.umn.biomedicus.numbers.NumberType;
import edu.umn.biomedicus.numbers.Numbers;
import java.math.BigDecimal;
import java.math.BigInteger;
import javax.annotation.Nonnull;
import javax.inject.Inject;

/**
 * Detects and labels instances of numbers in text, either English numerals or in decimal numeral
 * system.
 *
 * @author Ben Knoll
 * @since 1.8.0
 */
public class NumberRecognizer implements DocumentProcessor {

  private final CombinedNumberDetector numberDetector;

  private Labeler<Number> labeler;

  @Inject
  NumberRecognizer(
      NumberModel numberModel,
      @ProcessorSetting("measures.numbers.includePercent") boolean includePercent,
      @ProcessorSetting("measures.numbers.includeFractions") boolean includeFractions
  ) {
    numberDetector = Numbers.createNumberDetector(numberModel);
  }

  void setLabeler(Labeler<Number> labeler) {
    this.labeler = labeler;
  }

  @Override
  public void process(@Nonnull Document document) throws BiomedicusException {
    TextView systemView = StandardViews.getSystemView(document);

    LabelIndex<Sentence> sentenceLabelIndex = systemView.getLabelIndex(Sentence.class);
    LabelIndex<ParseToken> parseTokenLabelIndex = systemView.getLabelIndex(ParseToken.class);
    labeler = systemView.getLabeler(Number.class);

    for (Label<Sentence> sentenceLabel : sentenceLabelIndex) {
      extract(parseTokenLabelIndex.insideSpan(sentenceLabel));
    }
  }

  /**
   * Gets any numbers from the labels of parse tokens and labels them as such.
   *
   * @param labels labels of parse tokens
   * @throws BiomedicusException if there is an error labeling the text
   */
  void extract(Iterable<Label<ParseToken>> labels) throws BiomedicusException {
    for (Label<ParseToken> tokenLabel : labels) {
      String text = tokenLabel.getValue().text();
      int begin = tokenLabel.getBegin();
      int end = tokenLabel.getEnd();
      for (NumberResult numberResult : numberDetector.tryToken(text, begin, end)) {
        labelSeq(numberResult);
      }
    }

    for (NumberResult numberResult : numberDetector.finish()) {
      labelSeq(numberResult);
    }

  }

  private void labelSeq(NumberResult numberResult) throws BiomedicusException {
    BigDecimal numerator = numberResult.getNumerator();
    NumberType numberType = numberResult.getNumberType();
    BigDecimal denominator = numberResult.getDenominator();
    if (denominator == null) {
      ImmutableNumber number = ImmutableNumber.builder()
          .numerator(numerator.toString())
          .numberType(numberType)
          .denominator(BigInteger.ONE.toString()).build();
      labeler.value(number).label(numberResult.getBegin(), numberResult.getEnd());
    } else {
      ImmutableNumber number = ImmutableNumber.builder()
          .numerator(numerator.toString())
          .denominator(denominator.toString())
          .numberType(numberType)
          .build();
      labeler.value(number).label(numberResult.getBegin(), numberResult.getEnd());
    }
  }
}
