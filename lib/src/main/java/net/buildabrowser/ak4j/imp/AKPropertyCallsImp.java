package net.buildabrowser.ak4j.imp;

import java.lang.foreign.Arena;
import java.lang.foreign.FunctionDescriptor;
import java.lang.foreign.Linker;
import java.lang.foreign.MemorySegment;
import java.lang.foreign.SymbolLookup;
import java.lang.foreign.ValueLayout;
import java.lang.invoke.MethodHandle;
import java.nio.charset.StandardCharsets;

import net.buildabrowser.ak4j.AKPropertyCalls;

public class AKPropertyCallsImp implements AKPropertyCalls {
  
  private final Linker linker;

  private final MethodHandle setLabelHandle;
  private final MethodHandle setLevelHandle;

  public AKPropertyCallsImp(
    Linker linker
  ) {
    this.linker = linker;
    this.setLabelHandle = getSetLabelMethodHandle();
    this.setLevelHandle = getSetLevelMethodHandle();
  }

  @Override
  public void setLabel(MemorySegment node, String value, Arena scope) {
    MemorySegment valuePtr = scope.allocateFrom(value);
    CommonUtil.rethrowV(() -> {
      setLabelHandle.invokeExact(
        node, 
        valuePtr,
        value.getBytes(StandardCharsets.UTF_8).length);});
  }

  @Override
  public void setLevel(MemorySegment node, long level) {
    CommonUtil.rethrowV(() -> {
      setLevelHandle.invokeExact(node, level);});
  }

  private MethodHandle getSetLabelMethodHandle() {
    SymbolLookup symbolLookup = SymbolLookup.loaderLookup();
    MemorySegment methodAddr = symbolLookup.findOrThrow("accesskit_node_set_label_with_length");
    FunctionDescriptor methodDesc = FunctionDescriptor.ofVoid(
      ValueLayout.ADDRESS, // node
      ValueLayout.ADDRESS, // value
      ValueLayout.JAVA_INT // length
    );

    return linker.downcallHandle(methodAddr, methodDesc);
  }

  private MethodHandle getSetLevelMethodHandle() {
    SymbolLookup symbolLookup = SymbolLookup.loaderLookup();
    MemorySegment methodAddr = symbolLookup.findOrThrow("accesskit_node_set_level");
    FunctionDescriptor methodDesc = FunctionDescriptor.ofVoid(
      ValueLayout.ADDRESS,  // node
      ValueLayout.JAVA_LONG // value
    );

    return linker.downcallHandle(methodAddr, methodDesc);
  }

}
