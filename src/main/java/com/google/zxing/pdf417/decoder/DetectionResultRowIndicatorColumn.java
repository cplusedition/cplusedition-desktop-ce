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
 * Copyright 2013 ZXing authors
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

package com.google.zxing.pdf417.decoder;

import com.google.zxing.ResultPoint;
import com.google.zxing.pdf417.PDF417Common;

/**
 * @author Guenther Grau
 */
final class DetectionResultRowIndicatorColumn extends DetectionResultColumn {

  private final boolean isLeft;

  DetectionResultRowIndicatorColumn(BoundingBox boundingBox, boolean isLeft) {
    super(boundingBox);
    this.isLeft = isLeft;
  }

  private void setRowNumbers() {
    for (Codeword codeword : getCodewords()) {
      if (codeword != null) {
        codeword.setRowNumberAsRowIndicatorColumn();
      }
    }
  }

  void adjustCompleteIndicatorColumnRowNumbers(BarcodeMetadata barcodeMetadata) {
    Codeword[] codewords = getCodewords();
    setRowNumbers();
    removeIncorrectCodewords(codewords, barcodeMetadata);
    BoundingBox boundingBox = getBoundingBox();
    ResultPoint top = isLeft ? boundingBox.getTopLeft() : boundingBox.getTopRight();
    ResultPoint bottom = isLeft ? boundingBox.getBottomLeft() : boundingBox.getBottomRight();
    int firstRow = imageRowToCodewordIndex((int) top.getY());
    int lastRow = imageRowToCodewordIndex((int) bottom.getY());
    int barcodeRow = -1;
    int maxRowHeight = 1;
    int currentRowHeight = 0;
    for (int codewordsRow = firstRow; codewordsRow < lastRow; codewordsRow++) {
      if (codewords[codewordsRow] == null) {
        continue;
      }
      Codeword codeword = codewords[codewordsRow];

      int rowDifference = codeword.getRowNumber() - barcodeRow;

      if (rowDifference == 0) {
        currentRowHeight++;
      } else if (rowDifference == 1) {
        maxRowHeight = Math.max(maxRowHeight, currentRowHeight);
        currentRowHeight = 1;
        barcodeRow = codeword.getRowNumber();
      } else if (rowDifference < 0 ||
                 codeword.getRowNumber() >= barcodeMetadata.getRowCount() ||
                 rowDifference > codewordsRow) {
        codewords[codewordsRow] = null;
      } else {
        int checkedRows;
        if (maxRowHeight > 2) {
          checkedRows = (maxRowHeight - 2) * rowDifference;
        } else {
          checkedRows = rowDifference;
        }
        boolean closePreviousCodewordFound = checkedRows >= codewordsRow;
        for (int i = 1; i <= checkedRows && !closePreviousCodewordFound; i++) {
          closePreviousCodewordFound = codewords[codewordsRow - i] != null;
        }
        if (closePreviousCodewordFound) {
          codewords[codewordsRow] = null;
        } else {
          barcodeRow = codeword.getRowNumber();
          currentRowHeight = 1;
        }
      }
    }
  }

  int[] getRowHeights() {
    BarcodeMetadata barcodeMetadata = getBarcodeMetadata();
    if (barcodeMetadata == null) {
      return null;
    }
    adjustIncompleteIndicatorColumnRowNumbers(barcodeMetadata);
    int[] result = new int[barcodeMetadata.getRowCount()];
    for (Codeword codeword : getCodewords()) {
      if (codeword != null) {
        int rowNumber = codeword.getRowNumber();
        if (rowNumber >= result.length) {
          continue;
        }
        result[rowNumber]++;
      }
    }
    return result;
  }

  private void adjustIncompleteIndicatorColumnRowNumbers(BarcodeMetadata barcodeMetadata) {
    BoundingBox boundingBox = getBoundingBox();
    ResultPoint top = isLeft ? boundingBox.getTopLeft() : boundingBox.getTopRight();
    ResultPoint bottom = isLeft ? boundingBox.getBottomLeft() : boundingBox.getBottomRight();
    int firstRow = imageRowToCodewordIndex((int) top.getY());
    int lastRow = imageRowToCodewordIndex((int) bottom.getY());
    Codeword[] codewords = getCodewords();
    int barcodeRow = -1;
    int maxRowHeight = 1;
    int currentRowHeight = 0;
    for (int codewordsRow = firstRow; codewordsRow < lastRow; codewordsRow++) {
      if (codewords[codewordsRow] == null) {
        continue;
      }
      Codeword codeword = codewords[codewordsRow];

      codeword.setRowNumberAsRowIndicatorColumn();

      int rowDifference = codeword.getRowNumber() - barcodeRow;

      if (rowDifference == 0) {
        currentRowHeight++;
      } else if (rowDifference == 1) {
        maxRowHeight = Math.max(maxRowHeight, currentRowHeight);
        currentRowHeight = 1;
        barcodeRow = codeword.getRowNumber();
      } else if (codeword.getRowNumber() >= barcodeMetadata.getRowCount()) {
        codewords[codewordsRow] = null;
      } else {
        barcodeRow = codeword.getRowNumber();
        currentRowHeight = 1;
      }
    }
  }

  BarcodeMetadata getBarcodeMetadata() {
    Codeword[] codewords = getCodewords();
    BarcodeValue barcodeColumnCount = new BarcodeValue();
    BarcodeValue barcodeRowCountUpperPart = new BarcodeValue();
    BarcodeValue barcodeRowCountLowerPart = new BarcodeValue();
    BarcodeValue barcodeECLevel = new BarcodeValue();
    for (Codeword codeword : codewords) {
      if (codeword == null) {
        continue;
      }
      codeword.setRowNumberAsRowIndicatorColumn();
      int rowIndicatorValue = codeword.getValue() % 30;
      int codewordRowNumber = codeword.getRowNumber();
      if (!isLeft) {
        codewordRowNumber += 2;
      }
      switch (codewordRowNumber % 3) {
        case 0:
          barcodeRowCountUpperPart.setValue(rowIndicatorValue * 3 + 1);
          break;
        case 1:
          barcodeECLevel.setValue(rowIndicatorValue / 3);
          barcodeRowCountLowerPart.setValue(rowIndicatorValue % 3);
          break;
        case 2:
          barcodeColumnCount.setValue(rowIndicatorValue + 1);
          break;
      }
    }
    if ((barcodeColumnCount.getValue().length == 0) ||
        (barcodeRowCountUpperPart.getValue().length == 0) ||
        (barcodeRowCountLowerPart.getValue().length == 0) ||
        (barcodeECLevel.getValue().length == 0) ||
        barcodeColumnCount.getValue()[0] < 1 ||
        barcodeRowCountUpperPart.getValue()[0] + barcodeRowCountLowerPart.getValue()[0] < PDF417Common.MIN_ROWS_IN_BARCODE ||
        barcodeRowCountUpperPart.getValue()[0] + barcodeRowCountLowerPart.getValue()[0] > PDF417Common.MAX_ROWS_IN_BARCODE) {
      return null;
    }
    BarcodeMetadata barcodeMetadata = new BarcodeMetadata(barcodeColumnCount.getValue()[0],
        barcodeRowCountUpperPart.getValue()[0], barcodeRowCountLowerPart.getValue()[0], barcodeECLevel.getValue()[0]);
    removeIncorrectCodewords(codewords, barcodeMetadata);
    return barcodeMetadata;
  }

  private void removeIncorrectCodewords(Codeword[] codewords, BarcodeMetadata barcodeMetadata) {
    for (int codewordRow = 0; codewordRow < codewords.length; codewordRow++) {
      Codeword codeword = codewords[codewordRow];
      if (codewords[codewordRow] == null) {
        continue;
      }
      int rowIndicatorValue = codeword.getValue() % 30;
      int codewordRowNumber = codeword.getRowNumber();
      if (codewordRowNumber > barcodeMetadata.getRowCount()) {
        codewords[codewordRow] = null;
        continue;
      }
      if (!isLeft) {
        codewordRowNumber += 2;
      }
      switch (codewordRowNumber % 3) {
        case 0:
          if (rowIndicatorValue * 3 + 1 != barcodeMetadata.getRowCountUpperPart()) {
            codewords[codewordRow] = null;
          }
          break;
        case 1:
          if (rowIndicatorValue / 3 != barcodeMetadata.getErrorCorrectionLevel() ||
              rowIndicatorValue % 3 != barcodeMetadata.getRowCountLowerPart()) {
            codewords[codewordRow] = null;
          }
          break;
        case 2:
          if (rowIndicatorValue + 1 != barcodeMetadata.getColumnCount()) {
            codewords[codewordRow] = null;
          }
          break;
      }
    }
  }

  boolean isLeft() {
    return isLeft;
  }

  @Override
  public String toString() {
    return "IsLeft: " + isLeft + '\n' + super.toString();
  }

}
