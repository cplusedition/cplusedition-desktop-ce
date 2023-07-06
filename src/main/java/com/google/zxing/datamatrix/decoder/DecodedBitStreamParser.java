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

package com.google.zxing.datamatrix.decoder;

import com.google.zxing.FormatException;
import com.google.zxing.common.BitSource;
import com.google.zxing.common.DecoderResult;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * <p>Data Matrix Codes can encode text as bits in one of several modes, and can use multiple modes
 * in one Data Matrix Code. This class decodes the bits back into text.</p>
 *
 * <p>See ISO 16022:2006, 5.2.1 - 5.2.9.2</p>
 *
 * @author bbrown@google.com (Brian Brown)
 * @author Sean Owen
 */
final class DecodedBitStreamParser {

  private enum Mode {
    PAD_ENCODE,
    ASCII_ENCODE,
    C40_ENCODE,
    TEXT_ENCODE,
    ANSIX12_ENCODE,
    EDIFACT_ENCODE,
    BASE256_ENCODE
  }

  /**
   * See ISO 16022:2006, Annex C Table C.1
   * The C40 Basic Character Set (*'s used for placeholders for the shift values)
   */
  private static final char[] C40_BASIC_SET_CHARS = {
    '*', '*', '*', ' ', '0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
    'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M', 'N',
    'O', 'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z'
  };

  private static final char[] C40_SHIFT2_SET_CHARS = {
    '!', '"', '#', '$', '%', '&', '\'', '(', ')', '*',  '+', ',', '-', '.',
    '/', ':', ';', '<', '=', '>', '?',  '@', '[', '\\', ']', '^', '_'
  };

  /**
   * See ISO 16022:2006, Annex C Table C.2
   * The Text Basic Character Set (*'s used for placeholders for the shift values)
   */
  private static final char[] TEXT_BASIC_SET_CHARS = {
    '*', '*', '*', ' ', '0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
    'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l', 'm', 'n',
    'o', 'p', 'q', 'r', 's', 't', 'u', 'v', 'w', 'x', 'y', 'z'
  };

  private static final char[] TEXT_SHIFT2_SET_CHARS = C40_SHIFT2_SET_CHARS;

  private static final char[] TEXT_SHIFT3_SET_CHARS = {
    '`', 'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M', 'N',
    'O',  'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z', '{', '|', '}', '~', (char) 127
  };

  private DecodedBitStreamParser() {
  }

  static DecoderResult decode(byte[] bytes) throws FormatException {
    BitSource bits = new BitSource(bytes);
    StringBuilder result = new StringBuilder(100);
    StringBuilder resultTrailer = new StringBuilder(0);
    List<byte[]> byteSegments = new ArrayList<>(1);
    Mode mode = Mode.ASCII_ENCODE;
    do {
      if (mode == Mode.ASCII_ENCODE) {
        mode = decodeAsciiSegment(bits, result, resultTrailer);
      } else {
        switch (mode) {
          case C40_ENCODE:
            decodeC40Segment(bits, result);
            break;
          case TEXT_ENCODE:
            decodeTextSegment(bits, result);
            break;
          case ANSIX12_ENCODE:
            decodeAnsiX12Segment(bits, result);
            break;
          case EDIFACT_ENCODE:
            decodeEdifactSegment(bits, result);
            break;
          case BASE256_ENCODE:
            decodeBase256Segment(bits, result, byteSegments);
            break;
          default:
            throw FormatException.getFormatInstance();
        }
        mode = Mode.ASCII_ENCODE;
      }
    } while (mode != Mode.PAD_ENCODE && bits.available() > 0);
    if (resultTrailer.length() > 0) {
      result.append(resultTrailer);
    }
    return new DecoderResult(bytes, result.toString(), byteSegments.isEmpty() ? null : byteSegments, null);
  }

  /**
   * See ISO 16022:2006, 5.2.3 and Annex C, Table C.2
   */
  private static Mode decodeAsciiSegment(BitSource bits,
                                         StringBuilder result,
                                         StringBuilder resultTrailer) throws FormatException {
    boolean upperShift = false;
    do {
      int oneByte = bits.readBits(8);
      if (oneByte == 0) {
        throw FormatException.getFormatInstance();
      } else if (oneByte <= 128) {
        if (upperShift) {
          oneByte += 128;
        }
        result.append((char) (oneByte - 1));
        return Mode.ASCII_ENCODE;
      } else if (oneByte == 129) {
        return Mode.PAD_ENCODE;
      } else if (oneByte <= 229) {
        int value = oneByte - 130;
        if (value < 10) {
          result.append('0');
        }
        result.append(value);
      } else {
        switch (oneByte) {
          case 230:
            return Mode.C40_ENCODE;
          case 231:
            return Mode.BASE256_ENCODE;
          case 232:
            result.append((char) 29);
            break;
          case 233:
          case 234:
            break;
          case 235:
            upperShift = true;
            break;
          case 236:
            result.append("[)>\u001E05\u001D");
            resultTrailer.insert(0, "\u001E\u0004");
            break;
          case 237:
            result.append("[)>\u001E06\u001D");
            resultTrailer.insert(0, "\u001E\u0004");
            break;
          case 238:
            return Mode.ANSIX12_ENCODE;
          case 239:
            return Mode.TEXT_ENCODE;
          case 240:
            return Mode.EDIFACT_ENCODE;
          case 241:
            break;
          default:
            if (oneByte != 254 || bits.available() != 0) {
              throw FormatException.getFormatInstance();
            }
            break;
        }
      }
    } while (bits.available() > 0);
    return Mode.ASCII_ENCODE;
  }

  /**
   * See ISO 16022:2006, 5.2.5 and Annex C, Table C.1
   */
  private static void decodeC40Segment(BitSource bits, StringBuilder result) throws FormatException {
    boolean upperShift = false;

    int[] cValues = new int[3];
    int shift = 0;

    do {
      if (bits.available() == 8) {
        return;
      }
      int firstByte = bits.readBits(8);
      if (firstByte == 254) {
        return;
      }

      parseTwoBytes(firstByte, bits.readBits(8), cValues);

      for (int i = 0; i < 3; i++) {
        int cValue = cValues[i];
        switch (shift) {
          case 0:
            if (cValue < 3) {
              shift = cValue + 1;
            } else if (cValue < C40_BASIC_SET_CHARS.length) {
              char c40char = C40_BASIC_SET_CHARS[cValue];
              if (upperShift) {
                result.append((char) (c40char + 128));
                upperShift = false;
              } else {
                result.append(c40char);
              }
            } else {
              throw FormatException.getFormatInstance();
            }
            break;
          case 1:
            if (upperShift) {
              result.append((char) (cValue + 128));
              upperShift = false;
            } else {
              result.append((char) cValue);
            }
            shift = 0;
            break;
          case 2:
            if (cValue < C40_SHIFT2_SET_CHARS.length) {
              char c40char = C40_SHIFT2_SET_CHARS[cValue];
              if (upperShift) {
                result.append((char) (c40char + 128));
                upperShift = false;
              } else {
                result.append(c40char);
              }
            } else {
              switch (cValue) {
                case 27:
                  result.append((char) 29);
                  break;
                case 30:
                  upperShift = true;
                  break;
                default:
                  throw FormatException.getFormatInstance();
              }
            }
            shift = 0;
            break;
          case 3:
            if (upperShift) {
              result.append((char) (cValue + 224));
              upperShift = false;
            } else {
              result.append((char) (cValue + 96));
            }
            shift = 0;
            break;
          default:
            throw FormatException.getFormatInstance();
        }
      }
    } while (bits.available() > 0);
  }

  /**
   * See ISO 16022:2006, 5.2.6 and Annex C, Table C.2
   */
  private static void decodeTextSegment(BitSource bits, StringBuilder result) throws FormatException {
    boolean upperShift = false;

    int[] cValues = new int[3];
    int shift = 0;
    do {
      if (bits.available() == 8) {
        return;
      }
      int firstByte = bits.readBits(8);
      if (firstByte == 254) {
        return;
      }

      parseTwoBytes(firstByte, bits.readBits(8), cValues);

      for (int i = 0; i < 3; i++) {
        int cValue = cValues[i];
        switch (shift) {
          case 0:
            if (cValue < 3) {
              shift = cValue + 1;
            } else if (cValue < TEXT_BASIC_SET_CHARS.length) {
              char textChar = TEXT_BASIC_SET_CHARS[cValue];
              if (upperShift) {
                result.append((char) (textChar + 128));
                upperShift = false;
              } else {
                result.append(textChar);
              }
            } else {
              throw FormatException.getFormatInstance();
            }
            break;
          case 1:
            if (upperShift) {
              result.append((char) (cValue + 128));
              upperShift = false;
            } else {
              result.append((char) cValue);
            }
            shift = 0;
            break;
          case 2:
            if (cValue < TEXT_SHIFT2_SET_CHARS.length) {
              char textChar = TEXT_SHIFT2_SET_CHARS[cValue];
              if (upperShift) {
                result.append((char) (textChar + 128));
                upperShift = false;
              } else {
                result.append(textChar);
              }
            } else {
              switch (cValue) {
                case 27:
                  result.append((char) 29);
                  break;
                case 30:
                  upperShift = true;
                  break;
                default:
                  throw FormatException.getFormatInstance();
              }
            }
            shift = 0;
            break;
          case 3:
            if (cValue < TEXT_SHIFT3_SET_CHARS.length) {
              char textChar = TEXT_SHIFT3_SET_CHARS[cValue];
              if (upperShift) {
                result.append((char) (textChar + 128));
                upperShift = false;
              } else {
                result.append(textChar);
              }
              shift = 0;
            } else {
              throw FormatException.getFormatInstance();
            }
            break;
          default:
            throw FormatException.getFormatInstance();
        }
      }
    } while (bits.available() > 0);
  }

  /**
   * See ISO 16022:2006, 5.2.7
   */
  private static void decodeAnsiX12Segment(BitSource bits,
                                           StringBuilder result) throws FormatException {

    int[] cValues = new int[3];
    do {
      if (bits.available() == 8) {
        return;
      }
      int firstByte = bits.readBits(8);
      if (firstByte == 254) {
        return;
      }

      parseTwoBytes(firstByte, bits.readBits(8), cValues);

      for (int i = 0; i < 3; i++) {
        int cValue = cValues[i];
        switch (cValue) {
          case 0:
            result.append('\r');
            break;
          case 1:
            result.append('*');
            break;
          case 2:
            result.append('>');
            break;
          case 3:
            result.append(' ');
            break;
          default:
            if (cValue < 14) {
              result.append((char) (cValue + 44));
            } else if (cValue < 40) {
              result.append((char) (cValue + 51));
            } else {
              throw FormatException.getFormatInstance();
            }
            break;
        }
      }
    } while (bits.available() > 0);
  }

  private static void parseTwoBytes(int firstByte, int secondByte, int[] result) {
    int fullBitValue = (firstByte << 8) + secondByte - 1;
    int temp = fullBitValue / 1600;
    result[0] = temp;
    fullBitValue -= temp * 1600;
    temp = fullBitValue / 40;
    result[1] = temp;
    result[2] = fullBitValue - temp * 40;
  }

  /**
   * See ISO 16022:2006, 5.2.8 and Annex C Table C.3
   */
  private static void decodeEdifactSegment(BitSource bits, StringBuilder result) {
    do {
      if (bits.available() <= 16) {
        return;
      }

      for (int i = 0; i < 4; i++) {
        int edifactValue = bits.readBits(6);

        if (edifactValue == 0x1F) {
          int bitsLeft = 8 - bits.getBitOffset();
          if (bitsLeft != 8) {
            bits.readBits(bitsLeft);
          }
          return;
        }

        if ((edifactValue & 0x20) == 0) {
          edifactValue |= 0x40;
        }
        result.append((char) edifactValue);
      }
    } while (bits.available() > 0);
  }

  /**
   * See ISO 16022:2006, 5.2.9 and Annex B, B.2
   */
  private static void decodeBase256Segment(BitSource bits,
                                           StringBuilder result,
                                           Collection<byte[]> byteSegments)
      throws FormatException {
    int codewordPosition = 1 + bits.getByteOffset();
    int d1 = unrandomize255State(bits.readBits(8), codewordPosition++);
    int count;
    if (d1 == 0) {
      count = bits.available() / 8;
    } else if (d1 < 250) {
      count = d1;
    } else {
      count = 250 * (d1 - 249) + unrandomize255State(bits.readBits(8), codewordPosition++);
    }

    if (count < 0) {
      throw FormatException.getFormatInstance();
    }

    byte[] bytes = new byte[count];
    for (int i = 0; i < count; i++) {
      if (bits.available() < 8) {
        throw FormatException.getFormatInstance();
      }
      bytes[i] = (byte) unrandomize255State(bits.readBits(8), codewordPosition++);
    }
    byteSegments.add(bytes);
    try {
      result.append(new String(bytes, "ISO8859_1"));
    } catch (UnsupportedEncodingException uee) {
      throw new IllegalStateException("Platform does not support required encoding: " + uee);
    }
  }

  /**
   * See ISO 16022:2006, Annex B, B.2
   */
  private static int unrandomize255State(int randomizedBase256Codeword,
                                          int base256CodewordPosition) {
    int pseudoRandomNumber = ((149 * base256CodewordPosition) % 255) + 1;
    int tempVariable = randomizedBase256Codeword - pseudoRandomNumber;
    return tempVariable >= 0 ? tempVariable : tempVariable + 256;
  }

}
