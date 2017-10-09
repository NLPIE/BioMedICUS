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

package edu.umn.biomedicus.normalization;

import edu.umn.biomedicus.common.dictionary.StringIdentifier;
import java.io.Serializable;

/**
 * A payload tuple containing a term and a string;
 */
final class TermString implements Serializable {
  private final int term;
  private final String string;

  TermString(StringIdentifier term, String string) {
    this.term = term.value();
    this.string = string;
  }

  StringIdentifier getTerm() {
    return new StringIdentifier(term);
  }

  String getString() {
    return string;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }

    TermString that = (TermString) o;

    if (term != that.term) {
      return false;
    }
    return string.equals(that.string);
  }

  @Override
  public int hashCode() {
    int result = term;
    result = 31 * result + string.hashCode();
    return result;
  }
}
