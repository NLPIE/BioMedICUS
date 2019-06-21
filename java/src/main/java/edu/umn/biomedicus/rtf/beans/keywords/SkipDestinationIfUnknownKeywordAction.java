/*
 * Copyright 2019 Regents of the University of Minnesota
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

package edu.umn.biomedicus.rtf.beans.keywords;

import edu.umn.biomedicus.rtf.reader.*;

import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;
import java.io.IOException;

/**
 * Keyword action that handles the "\*" command which skips destinations that the reader does not
 * understand.
 */
@XmlRootElement
@XmlType
public class SkipDestinationIfUnknownKeywordAction extends AbstractKeywordAction {

  @Override
  public void executeAction(RtfState state, RtfSource source, RtfSink sink) {
    if (state.isSkippingDestination()) {
      return;
    }
    state.setSkipDestinationIfUnknown(true);
  }

  @Override
  public KeywordAction copy() {
    return new SkipDestinationIfUnknownKeywordAction();
  }
}
