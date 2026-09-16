package net.buildabrowser.ak4j.imp;

import java.lang.foreign.MemoryLayout;
import java.lang.foreign.StructLayout;
import java.util.ArrayList;
import java.util.List;

public final class FFMUtil {

  private FFMUtil() {}

  public static StructLayout alignedStruct(MemoryLayout... elements) {
    int alignment = 0;
    List<MemoryLayout> adjustedElements = new ArrayList<>();
    for (MemoryLayout element: elements) {
      int extraBytes = alignment % (int) element.byteAlignment();
      if (extraBytes != 0) {
        int newBytes = (int) element.byteAlignment() - extraBytes;
        adjustedElements.add(MemoryLayout.paddingLayout(newBytes));
        alignment += newBytes;
      }
      adjustedElements.add(element);
      alignment += element.byteSize();
    }

    return MemoryLayout.structLayout(
      adjustedElements.toArray(new MemoryLayout[0]));
  }

}
