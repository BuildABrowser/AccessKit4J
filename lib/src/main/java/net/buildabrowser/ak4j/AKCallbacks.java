package net.buildabrowser.ak4j;

import java.lang.foreign.MemorySegment;

public interface AKCallbacks {
  
  MemorySegment onActivation(AK4JHandle ak4jHandle);

  default void onAction(AK4JHandle ak4jHandle, AKActionRequest actionRequest) {}

  default void onDeactivation(AK4JHandle ak4jHandle) {}

}
