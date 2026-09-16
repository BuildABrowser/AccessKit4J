package net.buildabrowser.ak4j.imp;

import java.lang.foreign.Arena;
import java.lang.foreign.FunctionDescriptor;
import java.lang.foreign.Linker;
import java.lang.foreign.MemorySegment;
import java.lang.foreign.SymbolLookup;
import java.lang.foreign.ValueLayout;
import java.lang.invoke.MethodHandle;
import java.nio.charset.StandardCharsets;

import net.buildabrowser.ak4j.AKAction;
import net.buildabrowser.ak4j.AKNodeCalls;
import net.buildabrowser.ak4j.AKRole;
import net.buildabrowser.ak4j.AKTextSelection;
import net.buildabrowser.ak4j.AKTextSelection.AKTextPosition;

public class AKNodeCallsImp implements AKNodeCalls {

  private final Linker linker;

  private final MethodHandle createNodeHandle;
  private final MethodHandle pushChildHandle;
  private final MethodHandle setValueHandle;
  private final MethodHandle setHTMLTagHandle;
  private final MethodHandle addActionHandle;
  private final MethodHandle setCharacterLengths;
  private final MethodHandle setTextSelection;

  public AKNodeCallsImp(
    Linker linker
  ) {
    this.linker = linker;
    this.createNodeHandle = getCreateNodeMethodHandle();
    this.pushChildHandle = getPushChildMethodHandle();
    this.setValueHandle = getSetValueMethodHandle();
    this.setHTMLTagHandle = getSetHTMLTagMethodHandle();
    this.addActionHandle = getAddActionMethodHandle();
    this.setCharacterLengths = getSetCharacterLengthsHandle();
    this.setTextSelection = getSetTextSelectionHandle();
  }

  @Override
  public MemorySegment create(AKRole role, Arena scope) {
    return CommonUtil.rethrow(() ->
      (MemorySegment) createNodeHandle.invokeExact(role.ordinal()))
      .reinterpret(scope, _1 -> {});
    // TODO: Does the node need manually freed?
  }

  @Override
  public void pushChild(MemorySegment parent, long childId) {
    CommonUtil.rethrowV(() -> {
      pushChildHandle.invokeExact(parent, childId);});
  }

  @Override
  public void setValue(MemorySegment node, String value, Arena scope) {
    MemorySegment valuePtr = scope.allocateFrom(value); // TODO: Handle null character
    CommonUtil.rethrowV(() -> {
      setValueHandle.invokeExact(
        node, 
        valuePtr,
        value.getBytes(StandardCharsets.UTF_8).length);});
  }

  @Override
  public void setHTMLTag(MemorySegment node, String value, Arena scope) {
    MemorySegment valuePtr = scope.allocateFrom(value); // TODO: Handle null character
    CommonUtil.rethrowV(() -> {
      setHTMLTagHandle.invokeExact(
        node, 
        valuePtr,
        value.getBytes(StandardCharsets.UTF_8).length);});
  }

  @Override
  public void setBounds(MemorySegment node, float x, float y, float w, float h) {
    // TODO
  }

  @Override
  public void addAction(MemorySegment node, AKAction action) {
    CommonUtil.rethrowV(() -> {
      addActionHandle.invokeExact(node, action.ordinal());});
  }

  @Override
  public void setCharacterLengths(MemorySegment node, byte[] lengths, int size, Arena scope) {
    MemorySegment lengthsPtr = Arena.ofConfined().allocateFrom(ValueLayout.JAVA_BYTE, lengths);
    CommonUtil.rethrowV(() -> {
      setCharacterLengths.invokeExact(
        node, 
        size,
        lengthsPtr);});
  }

  @Override
  public void setTextSelection(MemorySegment node, AKTextSelection textSelection, Arena scope) {
    MemorySegment textSelectionPtr = scope.allocate(AKValueLayouts.TEXT_SELECTION_LAYOUT);
    populateTextSelection(textSelectionPtr, textSelection);
    CommonUtil.rethrowV(() -> {
      setTextSelection.invokeExact(node, textSelectionPtr);});
  }

  private MethodHandle getCreateNodeMethodHandle() {
    SymbolLookup symbolLookup = SymbolLookup.loaderLookup();
    MemorySegment methodAddr = symbolLookup.findOrThrow("accesskit_node_new");
    FunctionDescriptor methodDesc = FunctionDescriptor.of(
      ValueLayout.ADDRESS, // Return Value
      ValueLayout.JAVA_INT // role
    );

    return linker.downcallHandle(methodAddr, methodDesc);
  }

  private MethodHandle getPushChildMethodHandle() {
    SymbolLookup symbolLookup = SymbolLookup.loaderLookup();
    MemorySegment methodAddr = symbolLookup.findOrThrow("accesskit_node_push_child");
    FunctionDescriptor methodDesc = FunctionDescriptor.ofVoid(
      ValueLayout.ADDRESS,  // item
      ValueLayout.JAVA_LONG // node_id
    );

    return linker.downcallHandle(methodAddr, methodDesc);
  }

  private MethodHandle getSetValueMethodHandle() {
    SymbolLookup symbolLookup = SymbolLookup.loaderLookup();
    MemorySegment methodAddr = symbolLookup.findOrThrow("accesskit_node_set_value_with_length");
    FunctionDescriptor methodDesc = FunctionDescriptor.ofVoid(
      ValueLayout.ADDRESS, // node
      ValueLayout.ADDRESS, // value
      ValueLayout.JAVA_INT // length
    );

    return linker.downcallHandle(methodAddr, methodDesc);
  }


  private MethodHandle getSetHTMLTagMethodHandle() {
    SymbolLookup symbolLookup = SymbolLookup.loaderLookup();
    MemorySegment methodAddr = symbolLookup.findOrThrow("accesskit_node_set_html_tag_with_length");
    FunctionDescriptor methodDesc = FunctionDescriptor.ofVoid(
      ValueLayout.ADDRESS, // node
      ValueLayout.ADDRESS, // value
      ValueLayout.JAVA_INT // length
    );

    return linker.downcallHandle(methodAddr, methodDesc);
  }

  private MethodHandle getAddActionMethodHandle() {
    SymbolLookup symbolLookup = SymbolLookup.loaderLookup();
    MemorySegment methodAddr = symbolLookup.findOrThrow("accesskit_node_add_action");
    FunctionDescriptor methodDesc = FunctionDescriptor.ofVoid(
      ValueLayout.ADDRESS, // node
      ValueLayout.JAVA_INT // action
    );

    return linker.downcallHandle(methodAddr, methodDesc);
  }

  private MethodHandle getSetCharacterLengthsHandle() {
    SymbolLookup symbolLookup = SymbolLookup.loaderLookup();
    MemorySegment methodAddr = symbolLookup.findOrThrow("accesskit_node_set_character_lengths");
    FunctionDescriptor methodDesc = FunctionDescriptor.ofVoid(
      ValueLayout.ADDRESS,  // node
      ValueLayout.JAVA_INT, // length
      ValueLayout.ADDRESS   // values
    );

    return linker.downcallHandle(methodAddr, methodDesc);
  }

  private MethodHandle getSetTextSelectionHandle() {
    SymbolLookup symbolLookup = SymbolLookup.loaderLookup();
    MemorySegment methodAddr = symbolLookup.findOrThrow("accesskit_node_set_text_selection");
    FunctionDescriptor methodDesc = FunctionDescriptor.ofVoid(
      ValueLayout.ADDRESS, // node
      AKValueLayouts.TEXT_SELECTION_LAYOUT // value
    );

    return linker.downcallHandle(methodAddr, methodDesc);
  }

  private MemorySegment populateTextSelection(MemorySegment textSelectionPtr, AKTextSelection textSelection) {
    MemorySegment anchorPtr = textSelectionPtr.asSlice(
      AKValueLayouts.TEXT_POSITION_ANCHOR, 
      AKValueLayouts.TEXT_POSITION_LAYOUT.byteSize());
    MemorySegment focusPtr = textSelectionPtr.asSlice(
      AKValueLayouts.TEXT_POSITION_FOCUS, 
      AKValueLayouts.TEXT_POSITION_LAYOUT.byteSize());

    populateTextPosition(anchorPtr, textSelection.anchor());
    populateTextPosition(focusPtr, textSelection.focus());

    return textSelectionPtr;
  }

  private void populateTextPosition(MemorySegment targetPtr, AKTextPosition textPosition) {
    AKValueLayouts.TEXT_POSITION_NODE.set(targetPtr, 0L, textPosition.nodeId());
    AKValueLayouts.TEXT_POSITION_CHARACTER_INDEX.set(targetPtr, 0L, textPosition.characterIndex());
  }
  
}
