/*!            
    C+edition for Desktop, Community Edition.
    Copyright (C) 2021 Cplusedition Limited.  All rights reserved.
    
    The author licenses this file to You under the Apache License, Version 2.0
    (the "License"); you may not use this file except in compliance with
    the License.  You may obtain a copy of the License at
    
        http://www.apache.org/licenses/LICENSE-2.0
    
    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.
*/
/*
 * Copyright 2011 ZXing authors
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

package com.google.zxing.oned;

import com.google.zxing.BarcodeFormat;

import java.util.Collection;
import java.util.Collections;

/**
 * This class renders CodaBar as {@code boolean[]}.
 *
 * @author dsbnatut@gmail.com (Kazuki Nishiura)
 */
public final class CodaBarWriter extends OneDimensionalCodeWriter {

  private static final char[] START_END_CHARS = {'A', 'B', 'C', 'D'};
  private static final char[] ALT_START_END_CHARS = {'T', 'N', '*', 'E'};
  private static final char[] CHARS_WHICH_ARE_TEN_LENGTH_EACH_AFTER_DECODED = {'/', ':', '+', '.'};
  private static final char DEFAULT_GUARD = START_END_CHARS[0];

  @Override
  protected Collection<BarcodeFormat> getSupportedWriteFormats() {
    return Collections.singleton(BarcodeFormat.CODABAR);
  }

  @Override
  public boolean[] encode(String contents) {

    if (contents.length() < 2) {
      contents = DEFAULT_GUARD + contents + DEFAULT_GUARD;
    } else {
      char firstChar = Character.toUpperCase(contents.charAt(0));
      char lastChar = Character.toUpperCase(contents.charAt(contents.length() - 1));
      boolean startsNormal = CodaBarReader.arrayContains(START_END_CHARS, firstChar);
      boolean endsNormal = CodaBarReader.arrayContains(START_END_CHARS, lastChar);
      boolean startsAlt = CodaBarReader.arrayContains(ALT_START_END_CHARS, firstChar);
      boolean endsAlt = CodaBarReader.arrayContains(ALT_START_END_CHARS, lastChar);
      if (startsNormal) {
        if (!endsNormal) {
          throw new IllegalArgumentException("Invalid start/end guards: " + contents);
        }
      } else if (startsAlt) {
        if (!endsAlt) {
          throw new IllegalArgumentException("Invalid start/end guards: " + contents);
        }
      } else {
        if (endsNormal || endsAlt) {
          throw new IllegalArgumentException("Invalid start/end guards: " + contents);
        }
        contents = DEFAULT_GUARD + contents + DEFAULT_GUARD;
      }
    }

    int resultLength = 20;
    for (int i = 1; i < contents.length() - 1; i++) {
      if (Character.isDigit(contents.charAt(i)) || contents.charAt(i) == '-' || contents.charAt(i) == '$') {
        resultLength += 9;
      } else if (CodaBarReader.arrayContains(CHARS_WHICH_ARE_TEN_LENGTH_EACH_AFTER_DECODED, contents.charAt(i))) {
        resultLength += 10;
      } else {
        throw new IllegalArgumentException("Cannot encode : '" + contents.charAt(i) + '\'');
      }
    }
    resultLength += contents.length() - 1;

    boolean[] result = new boolean[resultLength];
    int position = 0;
    for (int index = 0; index < contents.length(); index++) {
      char c = Character.toUpperCase(contents.charAt(index));
      if (index == 0 || index == contents.length() - 1) {
        switch (c) {
          case 'T':
            c = 'A';
            break;
          case 'N':
            c = 'B';
            break;
          case '*':
            c = 'C';
            break;
          case 'E':
            c = 'D';
            break;
        }
      }
      int code = 0;
      for (int i = 0; i < CodaBarReader.ALPHABET.length; i++) {
        if (c == CodaBarReader.ALPHABET[i]) {
          code = CodaBarReader.CHARACTER_ENCODINGS[i];
          break;
        }
      }
      boolean color = true;
      int counter = 0;
      int bit = 0;
      while (bit < 7) {
        result[position] = color;
        position++;
        if (((code >> (6 - bit)) & 1) == 0 || counter == 1) {
          color = !color;
          bit++;
          counter = 0;
        } else {
          counter++;
        }
      }
      if (index < contents.length() - 1) {
        result[position] = false;
        position++;
      }
    }
    return result;
  }
}
