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

import com.google.zxing.BinaryBitmap;
import com.google.zxing.ChecksumException;
import com.google.zxing.DecodeHintType;
import com.google.zxing.FormatException;
import com.google.zxing.NotFoundException;
import com.google.zxing.Reader;
import com.google.zxing.ReaderException;
import com.google.zxing.Result;
import com.google.zxing.ResultMetadataType;
import com.google.zxing.ResultPoint;
import com.google.zxing.common.BitArray;

import java.util.Arrays;
import java.util.EnumMap;
import java.util.Map;

/**
 * Encapsulates functionality and implementation that is common to all families
 * of one-dimensional barcodes.
 *
 * @author dswitkin@google.com (Daniel Switkin)
 * @author Sean Owen
 */
public abstract class OneDReader implements Reader {

  @Override
  public Result decode(BinaryBitmap image) throws NotFoundException, FormatException {
    return decode(image, null);
  }

  @Override
  public Result decode(BinaryBitmap image,
                       Map<DecodeHintType,?> hints) throws NotFoundException, FormatException {
    try {
      return doDecode(image, hints);
    } catch (NotFoundException nfe) {
      boolean tryHarder = hints != null && hints.containsKey(DecodeHintType.TRY_HARDER);
      if (tryHarder && image.isRotateSupported()) {
        BinaryBitmap rotatedImage = image.rotateCounterClockwise();
        Result result = doDecode(rotatedImage, hints);
        Map<ResultMetadataType,?> metadata = result.getResultMetadata();
        int orientation = 270;
        if (metadata != null && metadata.containsKey(ResultMetadataType.ORIENTATION)) {
          orientation = (orientation +
              (Integer) metadata.get(ResultMetadataType.ORIENTATION)) % 360;
        }
        result.putMetadata(ResultMetadataType.ORIENTATION, orientation);
        ResultPoint[] points = result.getResultPoints();
        if (points != null) {
          int height = rotatedImage.getHeight();
          for (int i = 0; i < points.length; i++) {
            points[i] = new ResultPoint(height - points[i].getY() - 1, points[i].getX());
          }
        }
        return result;
      } else {
        throw nfe;
      }
    }
  }

  @Override
  public void reset() {
  }

  /**
   * We're going to examine rows from the middle outward, searching alternately above and below the
   * middle, and farther out each time. rowStep is the number of rows between each successive
   * attempt above and below the middle. So we'd scan row middle, then middle - rowStep, then
   * middle + rowStep, then middle - (2 * rowStep), etc.
   * rowStep is bigger as the image is taller, but is always at least 1. We've somewhat arbitrarily
   * decided that moving up and down by about 1/16 of the image is pretty good; we try more of the
   * image if "trying harder".
   *
   * @param image The image to decode
   * @param hints Any hints that were requested
   * @return The contents of the decoded barcode
   * @throws NotFoundException Any spontaneous errors which occur
   */
  private Result doDecode(BinaryBitmap image,
                          Map<DecodeHintType,?> hints) throws NotFoundException {
    int width = image.getWidth();
    int height = image.getHeight();
    BitArray row = new BitArray(width);

    boolean tryHarder = hints != null && hints.containsKey(DecodeHintType.TRY_HARDER);
    int rowStep = Math.max(1, height >> (tryHarder ? 8 : 5));
    int maxLines;
    if (tryHarder) {
      maxLines = height;
    } else {
      maxLines = 15;
    }

    int middle = height / 2;
    for (int x = 0; x < maxLines; x++) {

      int rowStepsAboveOrBelow = (x + 1) / 2;
      boolean isAbove = (x & 0x01) == 0;
      int rowNumber = middle + rowStep * (isAbove ? rowStepsAboveOrBelow : -rowStepsAboveOrBelow);
      if (rowNumber < 0 || rowNumber >= height) {
        break;
      }

      try {
        row = image.getBlackRow(rowNumber, row);
      } catch (NotFoundException ignored) {
        continue;
      }

      for (int attempt = 0; attempt < 2; attempt++) {
        if (attempt == 1) {
          row.reverse();
          if (hints != null && hints.containsKey(DecodeHintType.NEED_RESULT_POINT_CALLBACK)) {
            Map<DecodeHintType,Object> newHints = new EnumMap<>(DecodeHintType.class);
            newHints.putAll(hints);
            newHints.remove(DecodeHintType.NEED_RESULT_POINT_CALLBACK);
            hints = newHints;
          }
        }
        try {
          Result result = decodeRow(rowNumber, row, hints);
          if (attempt == 1) {
            result.putMetadata(ResultMetadataType.ORIENTATION, 180);
            ResultPoint[] points = result.getResultPoints();
            if (points != null) {
              points[0] = new ResultPoint(width - points[0].getX() - 1, points[0].getY());
              points[1] = new ResultPoint(width - points[1].getX() - 1, points[1].getY());
            }
          }
          return result;
        } catch (ReaderException re) {
        }
      }
    }

    throw NotFoundException.getNotFoundInstance();
  }

  /**
   * Records the size of successive runs of white and black pixels in a row, starting at a given point.
   * The values are recorded in the given array, and the number of runs recorded is equal to the size
   * of the array. If the row starts on a white pixel at the given start point, then the first count
   * recorded is the run of white pixels starting from that point; likewise it is the count of a run
   * of black pixels if the row begin on a black pixels at that point.
   *
   * @param row row to count from
   * @param start offset into row to start at
   * @param counters array into which to record counts
   * @throws NotFoundException if counters cannot be filled entirely from row before running out
   *  of pixels
   */
  protected static void recordPattern(BitArray row,
                                      int start,
                                      int[] counters) throws NotFoundException {
    int numCounters = counters.length;
    Arrays.fill(counters, 0, numCounters, 0);
    int end = row.getSize();
    if (start >= end) {
      throw NotFoundException.getNotFoundInstance();
    }
    boolean isWhite = !row.get(start);
    int counterPosition = 0;
    int i = start;
    while (i < end) {
      if (row.get(i) != isWhite) {
        counters[counterPosition]++;
      } else {
        if (++counterPosition == numCounters) {
          break;
        } else {
          counters[counterPosition] = 1;
          isWhite = !isWhite;
        }
      }
      i++;
    }
    if (!(counterPosition == numCounters || (counterPosition == numCounters - 1 && i == end))) {
      throw NotFoundException.getNotFoundInstance();
    }
  }

  protected static void recordPatternInReverse(BitArray row, int start, int[] counters)
      throws NotFoundException {
    int numTransitionsLeft = counters.length;
    boolean last = row.get(start);
    while (start > 0 && numTransitionsLeft >= 0) {
      if (row.get(--start) != last) {
        numTransitionsLeft--;
        last = !last;
      }
    }
    if (numTransitionsLeft >= 0) {
      throw NotFoundException.getNotFoundInstance();
    }
    recordPattern(row, start + 1, counters);
  }

  /**
   * Determines how closely a set of observed counts of runs of black/white values matches a given
   * target pattern. This is reported as the ratio of the total variance from the expected pattern
   * proportions across all pattern elements, to the length of the pattern.
   *
   * @param counters observed counters
   * @param pattern expected pattern
   * @param maxIndividualVariance The most any counter can differ before we give up
   * @return ratio of total variance between counters and pattern compared to total pattern size
   */
  protected static float patternMatchVariance(int[] counters,
                                              int[] pattern,
                                              float maxIndividualVariance) {
    int numCounters = counters.length;
    int total = 0;
    int patternLength = 0;
    for (int i = 0; i < numCounters; i++) {
      total += counters[i];
      patternLength += pattern[i];
    }
    if (total < patternLength) {
      return Float.POSITIVE_INFINITY;
    }

    float unitBarWidth = (float) total / patternLength;
    maxIndividualVariance *= unitBarWidth;

    float totalVariance = 0.0f;
    for (int x = 0; x < numCounters; x++) {
      int counter = counters[x];
      float scaledPattern = pattern[x] * unitBarWidth;
      float variance = counter > scaledPattern ? counter - scaledPattern : scaledPattern - counter;
      if (variance > maxIndividualVariance) {
        return Float.POSITIVE_INFINITY;
      }
      totalVariance += variance;
    }
    return totalVariance / total;
  }

  /**
   * <p>Attempts to decode a one-dimensional barcode format given a single row of
   * an image.</p>
   *
   * @param rowNumber row number from top of the row
   * @param row the black/white pixel data of the row
   * @param hints decode hints
   * @return {@link Result} containing encoded string and start/end of barcode
   * @throws NotFoundException if no potential barcode is found
   * @throws ChecksumException if a potential barcode is found but does not pass its checksum
   * @throws FormatException if a potential barcode is found but format is invalid
   */
  public abstract Result decodeRow(int rowNumber, BitArray row, Map<DecodeHintType,?> hints)
      throws NotFoundException, ChecksumException, FormatException;

}
