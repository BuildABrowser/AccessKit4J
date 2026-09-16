package net.buildabrowser.ak4j;

import java.lang.foreign.Arena;
import java.lang.foreign.MemorySegment;

public interface AKPropertyCalls {

  void setLabel(MemorySegment node, String value, Arena scope);
  
  void setLevel(MemorySegment node, long level);

}
