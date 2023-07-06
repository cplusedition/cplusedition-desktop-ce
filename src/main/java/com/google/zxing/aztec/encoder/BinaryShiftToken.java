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

package com.google.zxing.aztec.encoder;

import com.google.zxing.common.BitArray;

final class BinaryShiftToken extends Token {

  private final short binaryShiftStart;
  private final short binaryShiftByteCount;

  BinaryShiftToken(Token previous,
                   int binaryShiftStart,
                   int binaryShiftByteCount) {
    super(previous);
    this.binaryShiftStart = (short) binaryShiftStart;
    this.binaryShiftByteCount = (short) binaryShiftByteCount;
  }

  @Override
  public void appendTo(BitArray bitArray, byte[] text) {
    for (int i = 0; i < binaryShiftByteCount; i++) {
      if (i == 0 || (i == 31 && binaryShiftByteCount <= 62)) {
        bitArray.appendBits(31, 5);
        if (binaryShiftByteCount > 62) {
          bitArray.appendBits(binaryShiftByteCount - 31, 16);
        } else if (i == 0) {
          bitArray.appendBits(Math.min(binaryShiftByteCount, 31), 5);
        } else {
          bitArray.appendBits(binaryShiftByteCount - 31, 5);
        }
      }
      bitArray.appendBits(text[binaryShiftStart + i], 8);
    }
  }

  @Override
  public String toString() {
    return "<" + binaryShiftStart + "::" + (binaryShiftStart + binaryShiftByteCount - 1) + '>';
  }

}
