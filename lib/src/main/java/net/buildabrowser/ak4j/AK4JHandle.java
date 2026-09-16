package net.buildabrowser.ak4j;

import java.lang.foreign.Arena;
import java.lang.foreign.MemorySegment;

public interface AK4JHandle extends AutoCloseable {

  AKAdapter adapter();

  AKNodeCalls nodes();

  AKPropertyCalls properties();

  MemorySegment createTree(long nodeId, Arena scope);

  void setTreeToolkitName(
    MemorySegment tree,
    String name,
    Arena scope
  );

  MemorySegment createTreeUpdate(
    MemorySegment tree,
    long capacity,
    long focusNodeId,
    Arena scope
  );

  void pushTreeUpdateNode(
    MemorySegment treeUpdate,
    long nodeId,
    MemorySegment node
  );

}
