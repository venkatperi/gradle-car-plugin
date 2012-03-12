package com.github.venkatperi.gradle.api.java.archives.internal

/*
 * Copyright 2012 the original author or authors.
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
 *
 * @author Venkat Peri 
 */
class QuotedString {

    private static final String DOUBLE_QUOTE = '"'
    private static final String EMPTY = ''
    private static final String OPEN_SQ = '['
    private static final String CLOSE_SQ = ']'
    private static final String COLON = ':'
    private static final String COMMA = ','

    static def String quoteIt(String s) {
        return DOUBLE_QUOTE + s + DOUBLE_QUOTE
    }

    static def String quoteIt(Object s) {
        if (s == null)
            return EMPTY
        return s.toString()
    }

    static def String quoteIt(List<?> l) {
        int i = 0;
        return OPEN_SQ + l.sum { (i++ == 0 ? EMPTY : COMMA) + toString(it) } + CLOSE_SQ
    }

    static def String quoteIt(Map<?, ?> l) {
        int i = 0;
        return OPEN_SQ + l.collect {k, v -> quoteIt(k) + COLON + quoteIt(v) }.sum {(i++ == 0 ? EMPTY : COMMA) + it} + CLOSE_SQ
    }

    static def String toString(Object o) {
        if (o == null)
            return EMPTY
        return quoteIt(o)
    }
}
