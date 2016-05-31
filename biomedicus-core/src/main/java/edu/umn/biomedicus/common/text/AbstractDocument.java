/*
 * Copyright (c) 2015 Regents of the University of Minnesota.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package edu.umn.biomedicus.common.text;

import edu.umn.biomedicus.exc.BiomedicusException;

public abstract class AbstractDocument implements Document {

    /**
     * Creates a new token and adds to index. The implementation should check to make sure that the span is not just
     * whitespace.
     *
     * @param spanLike to create a token from
     * @return the newly created token.
     */
    @Override
    public Token createToken(SpanLike spanLike) {
        return createToken(spanLike.getBegin(), spanLike.getEnd());
    }

    /**
     * Add a sentence occurring over the span to this document.
     *
     * @param spanLike a {@link SpanLike} indicating where the sentence occurs.
     */
    @Override
    public Sentence createSentence(SpanLike spanLike) {
        return createSentence(spanLike.getBegin(), spanLike.getEnd());
    }


    public boolean hasNewInformationAnnotation(SpanLike spanLike) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Document getSiblingDocument(String identifier) throws BiomedicusException {
        throw new UnsupportedOperationException();
    }
}
