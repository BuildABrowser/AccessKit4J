package net.buildabrowser.ak4j;

import java.lang.foreign.Arena;
import java.lang.foreign.MemorySegment;

// Nodes are highly common, so referring to them via their MemorySegment
// instead of a Java wrapper reduces their wrapper tax
public interface AKNodeCalls {

  MemorySegment create(AKRole role, Arena scope);
  
  void pushChild(MemorySegment parent, long childId);

  void setValue(MemorySegment node, String value, Arena scope);

  void setHTMLTag(MemorySegment node, String value, Arena scope);

  void setBounds(MemorySegment node, float x, float y, float w, float h);

  void addAction(MemorySegment node, AKAction action);

  void setCharacterLengths(MemorySegment node, byte[] lengths, int size, Arena scope);

  void setTextSelection(MemorySegment node, AKTextSelection textSelection, Arena scope);

}
