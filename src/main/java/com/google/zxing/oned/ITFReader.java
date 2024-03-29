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
 * Copyright 2008 ZXing authors
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
import com.google.zxing.DecodeHintType;
import com.google.zxing.FormatException;
import com.google.zxing.NotFoundException;
import com.google.zxing.Result;
import com.google.zxing.ResultPoint;
import com.google.zxing.common.BitArray;

import java.util.Map;

/**
 * <p>Implements decoding of the ITF format, or Interleaved Two of Five.</p>
 *
 * <p>This Reader will scan ITF barcodes of certain lengths only.
 * At the moment it reads length 6, 8, 10, 12, 14, 16, 18, 20, 24, and 44 as these have appeared "in the wild". Not all
 * lengths are scanned, especially shorter ones, to avoid false positives. This in turn is due to a lack of
 * required checksum function.</p>
 *
 * <p>The checksum is optional and is not applied by this Reader. The consumer of the decoded
 * value will have to apply a checksum if required.</p>
 *
 * <p><a href="http://en.wikipedia.org/wiki/Interleaved_2_of_5">http://en.wikipedia.org/wiki/Interleaved_2_of_5</a>
 * is a great reference for Interleaved 2 of 5 information.</p>
 *
 * @author kevin.osullivan@sita.aero, SITA Lab.
 */
public final class ITFReader extends OneDReader {

  private static final float MAX_AVG_VARIANCE = 0.38f;
  private static final float MAX_INDIVIDUAL_VARIANCE = 0.5f;

  private static final int W = 3;
  private static final int w = 2;
  private static final int N = 1;

  /** Valid ITF lengths. Anything longer than the largest value is also allowed. */
  private static final int[] DEFAULT_ALLOWED_LENGTHS = {6, 8, 10, 12, 14};

  private int narrowLineWidth = -1;

  /**
   * Start/end guard pattern.
   *
   * Note: The end pattern is reversed because the row is reversed before
   * searching for the END_PATTERN
   */
  private static final int[] START_PATTERN = {N, N, N, N};
  private static final int[][] END_PATTERN_REVERSED = {
      {N, N, w},
      {N, N, W}
  };

  /**
   * Patterns of Wide / Narrow lines to indicate each digit
   */
  private static final int[][] PATTERNS = {
      {N, N, w, w, N},
      {w, N, N, N, w},
      {N, w, N, N, w},
      {w, w, N, N, N},
      {N, N, w, N, w},
      {w, N, w, N, N},
      {N, w, w, N, N},
      {N, N, N, w, w},
      {w, N, N, w, N},
      {N, w, N, w, N},
      {N, N, W, W, N},
      {W, N, N, N, W},
      {N, W, N, N, W},
      {W, W, N, N, N},
      {N, N, W, N, W},
      {W, N, W, N, N},
      {N, W, W, N, N},
      {N, N, N, W, W},
      {W, N, N, W, N},
      {N, W, N, W, N}
  };

  @Override
  public Result decodeRow(int rowNumber, BitArray row, Map<DecodeHintType,?> hints)
      throws FormatException, NotFoundException {

    int[] startRange = decodeStart(row);
    int[] endRange = decodeEnd(row);

    StringBuilder result = new StringBuilder(20);
    decodeMiddle(row, startRange[1], endRange[0], result);
    String resultString = result.toString();

    int[] allowedLengths = null;
    if (hints != null) {
      allowedLengths = (int[]) hints.get(DecodeHintType.ALLOWED_LENGTHS);

    }
    if (allowedLengths == null) {
      allowedLengths = DEFAULT_ALLOWED_LENGTHS;
    }

    int length = resultString.length();
    boolean lengthOK = false;
    int maxAllowedLength = 0;
    for (int allowedLength : allowedLengths) {
      if (length == allowedLength) {
        lengthOK = true;
        break;
      }
      if (allowedLength > maxAllowedLength) {
        maxAllowedLength = allowedLength;
      }
    }
    if (!lengthOK && length > maxAllowedLength) {
      lengthOK = true;
    }
    if (!lengthOK) {
      throw FormatException.getFormatInstance();
    }

    return new Result(
        resultString,
        null,
        new ResultPoint[] {new ResultPoint(startRange[1], rowNumber),
                           new ResultPoint(endRange[0], rowNumber)},
        BarcodeFormat.ITF);
  }

  /**
   * @param row          row of black/white values to search
   * @param payloadStart offset of start pattern
   * @param resultString {@link StringBuilder} to append decoded chars to
   * @throws NotFoundException if decoding could not complete successfully
   */
  private static void decodeMiddle(BitArray row,
                                   int payloadStart,
                                   int payloadEnd,
                                   StringBuilder resultString) throws NotFoundException {

    int[] counterDigitPair = new int[10];
    int[] counterBlack = new int[5];
    int[] counterWhite = new int[5];

    while (payloadStart < payloadEnd) {

      recordPattern(row, payloadStart, counterDigitPair);
      for (int k = 0; k < 5; k++) {
        int twoK = 2 * k;
        counterBlack[k] = counterDigitPair[twoK];
        counterWhite[k] = counterDigitPair[twoK + 1];
      }

      int bestMatch = decodeDigit(counterBlack);
      resultString.append((char) ('0' + bestMatch));
      bestMatch = decodeDigit(counterWhite);
      resultString.append((char) ('0' + bestMatch));

      for (int counterDigit : counterDigitPair) {
        payloadStart += counterDigit;
      }
    }
  }

  /**
   * Identify where the start of the middle / payload section starts.
   *
   * @param row row of black/white values to search
   * @return Array, containing index of start of 'start block' and end of
   *         'start block'
   */
  private int[] decodeStart(BitArray row) throws NotFoundException {
    int endStart = skipWhiteSpace(row);
    int[] startPattern = findGuardPattern(row, endStart, START_PATTERN);

    this.narrowLineWidth = (startPattern[1] - startPattern[0]) / 4;

    validateQuietZone(row, startPattern[0]);

    return startPattern;
  }

  /**
   * The start & end patterns must be pre/post fixed by a quiet zone. This
   * zone must be at least 10 times the width of a narrow line.  Scan back until
   * we either get to the start of the barcode or match the necessary number of
   * quiet zone pixels.
   *
   * Note: Its assumed the row is reversed when using this method to find
   * quiet zone after the end pattern.
   *
   * ref: http://www.barcode-1.net/i25code.html
   *
   * @param row bit array representing the scanned barcode.
   * @param startPattern index into row of the start or end pattern.
   * @throws NotFoundException if the quiet zone cannot be found
   */
  private void validateQuietZone(BitArray row, int startPattern) throws NotFoundException {

    int quietCount = this.narrowLineWidth * 10;

    quietCount = Math.min(quietCount, startPattern);

    for (int i = startPattern - 1; quietCount > 0 && i >= 0; i--) {
      if (row.get(i)) {
        break;
      }
      quietCount--;
    }
    if (quietCount != 0) {
      throw NotFoundException.getNotFoundInstance();
    }
  }

  /**
   * Skip all whitespace until we get to the first black line.
   *
   * @param row row of black/white values to search
   * @return index of the first black line.
   * @throws NotFoundException Throws exception if no black lines are found in the row
   */
  private static int skipWhiteSpace(BitArray row) throws NotFoundException {
    int width = row.getSize();
    int endStart = row.getNextSet(0);
    if (endStart == width) {
      throw NotFoundException.getNotFoundInstance();
    }

    return endStart;
  }

  /**
   * Identify where the end of the middle / payload section ends.
   *
   * @param row row of black/white values to search
   * @return Array, containing index of start of 'end block' and end of 'end
   *         block'
   */
  private int[] decodeEnd(BitArray row) throws NotFoundException {

    row.reverse();
    try {
      int endStart = skipWhiteSpace(row);
      int[] endPattern;
      try {
        endPattern = findGuardPattern(row, endStart, END_PATTERN_REVERSED[0]);
      } catch (NotFoundException nfe) {
        endPattern = findGuardPattern(row, endStart, END_PATTERN_REVERSED[1]);
      }

      validateQuietZone(row, endPattern[0]);

      int temp = endPattern[0];
      endPattern[0] = row.getSize() - endPattern[1];
      endPattern[1] = row.getSize() - temp;

      return endPattern;
    } finally {
      row.reverse();
    }
  }

  /**
   * @param row       row of black/white values to search
   * @param rowOffset position to start search
   * @param pattern   pattern of counts of number of black and white pixels that are
   *                  being searched for as a pattern
   * @return start/end horizontal offset of guard pattern, as an array of two
   *         ints
   * @throws NotFoundException if pattern is not found
   */
  private static int[] findGuardPattern(BitArray row,
                                        int rowOffset,
                                        int[] pattern) throws NotFoundException {
    int patternLength = pattern.length;
    int[] counters = new int[patternLength];
    int width = row.getSize();
    boolean isWhite = false;

    int counterPosition = 0;
    int patternStart = rowOffset;
    for (int x = rowOffset; x < width; x++) {
      if (row.get(x) != isWhite) {
        counters[counterPosition]++;
      } else {
        if (counterPosition == patternLength - 1) {
          if (patternMatchVariance(counters, pattern, MAX_INDIVIDUAL_VARIANCE) < MAX_AVG_VARIANCE) {
            return new int[]{patternStart, x};
          }
          patternStart += counters[0] + counters[1];
          System.arraycopy(counters, 2, counters, 0, counterPosition - 1);
          counters[counterPosition - 1] = 0;
          counters[counterPosition] = 0;
          counterPosition--;
        } else {
          counterPosition++;
        }
        counters[counterPosition] = 1;
        isWhite = !isWhite;
      }
    }
    throw NotFoundException.getNotFoundInstance();
  }

  /**
   * Attempts to decode a sequence of ITF black/white lines into single
   * digit.
   *
   * @param counters the counts of runs of observed black/white/black/... values
   * @return The decoded digit
   * @throws NotFoundException if digit cannot be decoded
   */
  private static int decodeDigit(int[] counters) throws NotFoundException {
    float bestVariance = MAX_AVG_VARIANCE;
    int bestMatch = -1;
    int max = PATTERNS.length;
    for (int i = 0; i < max; i++) {
      int[] pattern = PATTERNS[i];
      float variance = patternMatchVariance(counters, pattern, MAX_INDIVIDUAL_VARIANCE);
      if (variance < bestVariance) {
        bestVariance = variance;
        bestMatch = i;
      } else if (variance == bestVariance) {
        bestMatch = -1;
      }
    }
    if (bestMatch >= 0) {
      return bestMatch % 10;
    } else {
      throw NotFoundException.getNotFoundInstance();
    }
  }

}
