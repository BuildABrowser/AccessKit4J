package net.buildabrowser.ak4j.util;

public final class TextRunUtil {
  
  private TextRunUtil() {}

  public static int getTextLengths(String string, byte[] lengths) {
    int stringIndex = 0;
    int lengthIndex = 0;
    while (stringIndex < string.length()) {
      int ch = string.codePointAt(lengthIndex);
      lengths[lengthIndex++] = utf8Length(ch);
      stringIndex = Character.offsetByCodePoints(string, stringIndex, 1);
    }

    return lengthIndex;
  }

  private static byte utf8Length(int codePoint) {
    return (byte) (
      codePoint <= 0x7F ? 1 :
      codePoint <= 0x7FF ? 2 :
      codePoint <= 0xFFFF ? 3 :
      4);
  }

}
