package net.buildabrowser.ak4j.imp;

import java.lang.foreign.MemorySegment;

import net.buildabrowser.ak4j.AKTextSelection;
import net.buildabrowser.ak4j.AKTextSelection.AKTextPosition;

public final class AKValueUtil {
  
  private AKValueUtil() {}

  public static AKTextSelection decodeTextSelection(
    MemorySegment textSelectionPtr
  ) {
    MemorySegment anchorPtr = textSelectionPtr.asSlice(
      AKValueLayouts.TEXT_POSITION_ANCHOR, AKValueLayouts.TEXT_POSITION_LAYOUT);
    MemorySegment focusPtr = textSelectionPtr.asSlice(
      AKValueLayouts.TEXT_POSITION_FOCUS, AKValueLayouts.TEXT_POSITION_LAYOUT);
    
    return new AKTextSelection(
      decodeTextPosition(anchorPtr),
      decodeTextPosition(focusPtr));
  }

  private static AKTextPosition decodeTextPosition(MemorySegment textPositionPtr) {
    long nodeId = (long) AKValueLayouts.TEXT_POSITION_NODE.get(textPositionPtr, 0L);
    long characterIndex = (long) AKValueLayouts.TEXT_POSITION_CHARACTER_INDEX.get(textPositionPtr, 0L);
    return new AKTextPosition(nodeId, characterIndex);
  }

}
