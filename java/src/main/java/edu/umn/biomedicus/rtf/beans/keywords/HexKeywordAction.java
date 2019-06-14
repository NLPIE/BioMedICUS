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

import edu.umn.biomedicus.rtf.exc.RtfReaderException;
import edu.umn.biomedicus.rtf.reader.KeywordAction;
import edu.umn.biomedicus.rtf.reader.RtfSink;
import edu.umn.biomedicus.rtf.reader.RtfSource;
import edu.umn.biomedicus.rtf.reader.State;

import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;

/**
 *
 */
@XmlRootElement
@XmlType
public class HexKeywordAction extends AbstractKeywordAction {

  @Override
  public String executeAction(State state, RtfSource source, RtfSink sink) throws IOException {
    char[] chars = new char[2];
    try {
      for (int i = 0; i < 2; i++) {
        int code = source.read();
        chars[i] = (char) code;
      }
      byte code = (byte)(Integer.parseInt(new String(chars), 16) & 0xff);
      CharBuffer decode = state.getDecoder().decode(ByteBuffer.allocate(1).put(code));
      sink.writeCharacter(decode.get(0), getStartIndex(), source.getIndex());
    } catch (IOException e) {
      throw new RtfReaderException(e);
    }
  }

  @Override
  public KeywordAction copy() {
    return new HexKeywordAction();
  }
}
