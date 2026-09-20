package net.buildabrowser.ak4j.imp;

import java.io.IOException;
import java.lang.foreign.Arena;
import java.lang.foreign.FunctionDescriptor;
import java.lang.foreign.Linker;
import java.lang.foreign.MemorySegment;
import java.lang.foreign.SymbolLookup;
import java.lang.foreign.ValueLayout;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

import net.buildabrowser.ak4j.AK4JHandle;
import net.buildabrowser.ak4j.AKAction;
import net.buildabrowser.ak4j.AKActionRequest;
import net.buildabrowser.ak4j.AKAdapter;
import net.buildabrowser.ak4j.AKCallbacks;

public class UnixAKAdapter implements AKAdapter {

  private final Linker linker;
  private final Arena arena;
  private final Consumer<AK4JHandle> startFunc;

  // TODO: Reinterpret return values to use the arena
  
  private final MethodHandle freeHandle;
  private final MethodHandle debugHandle;
  private final MethodHandle updateHandle;
  private final MethodHandle setFocusHandle;

  private MemorySegment adapterPointer;
  
  public UnixAKAdapter(
    Linker linker,
    AKCallbacks callbacks
  ) {
    try {
      this.linker = linker;
      // Unfortunately ofShared does not seem to shutdown correctly (throws an exception)
      this.arena = Arena.ofAuto();
      this.startFunc = ak4j -> CommonUtil.rethrowV(
        () -> start(callbacks, ak4j));

      this.freeHandle = getFreeMethodHandle();
      this.updateHandle = getUpdateMethodHandle();
      this.debugHandle = getDebugMethodHandle();
      this.setFocusHandle = getSetFocusMethodHandle();
    } catch (RuntimeException e) {
      throw e;
    } catch (Throwable e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  public void update(Supplier<MemorySegment> updateFunc) {
    // TODO: Maybe take the update func in the constructor, re-use it
    MemorySegment updateFuncPtr = CommonUtil.rethrow(() ->
      updateFunctionToFunctionPointer(_ -> updateFunc.get()));
    // For some reason, the JVM requires the brackets for invokeExact to work correctly
    CommonUtil.rethrowV(() -> {updateHandle.invokeExact(
      adapterPointer, // adapter
      updateFuncPtr, // update_factory
      MemorySegment.NULL // update_factory_userdata
    );});
  }

  @Override
  public void setFocus(boolean focused) {
    CommonUtil.rethrowV(() -> {
      setFocusHandle.invokeExact(adapterPointer, focused);});
  }

  @Override
  public String debug() {
    MemorySegment resultPtr = CommonUtil.rethrow(
      () -> (MemorySegment) debugHandle.invokeExact(adapterPointer));
    return resultPtr.reinterpret(Long.MAX_VALUE).getString(0);
  }

  @Override
  public void start(AK4JHandle ak4jHandle) {
    startFunc.accept(ak4jHandle);
  }

  private void start(
    AKCallbacks callbacks,
    AK4JHandle ak4jHandle
  ) throws Throwable {
    MemorySegment activationHandlerPtr = updateFunctionToFunctionPointer(
      _ -> callbacks.onActivation(ak4jHandle));
    MemorySegment actionHandlerPtr = actionRequestConsumerToFunctionPointer(
      (actionRequest, _) -> { callbacks.onAction(
        ak4jHandle,
        mapActionRequest((MemorySegment) actionRequest));});
    MemorySegment deactivationHandlerPtr = userDataConsumerToFunctionPointer(
      _ -> callbacks.onDeactivation(ak4jHandle));

    MethodHandle newMethodHandle = getNewMethodHandle();
    this.adapterPointer = (MemorySegment) newMethodHandle.invokeExact(
      activationHandlerPtr, // activation_handler
      MemorySegment.NULL, // activation_handler_userdata
      actionHandlerPtr, // action_handler
      MemorySegment.NULL, // action_handler_userdata
      deactivationHandlerPtr, // deactivation_handler
      MemorySegment.NULL  // deactivation_handler_userdata
    );
  }

  private MethodHandle getNewMethodHandle() {
    SymbolLookup symbolLookup = SymbolLookup.loaderLookup();
    MemorySegment newMethodAddr = symbolLookup.findOrThrow("accesskit_unix_adapter_new");
    FunctionDescriptor newMethodDesc = FunctionDescriptor.of(
      ValueLayout.ADDRESS, // RTN
      ValueLayout.ADDRESS, // activation_handler
      ValueLayout.ADDRESS, // activation_handler_userdata
      ValueLayout.ADDRESS, // action_handler
      ValueLayout.ADDRESS, // action_handler_userdata
      ValueLayout.ADDRESS, // deactivation_handler
      ValueLayout.ADDRESS  // deactivation_handler_userdata
    );

    return linker.downcallHandle(newMethodAddr, newMethodDesc);
  }

  private MethodHandle getFreeMethodHandle() {
    SymbolLookup symbolLookup = SymbolLookup.loaderLookup();
    MemorySegment freeMethodAddr = symbolLookup.findOrThrow("accesskit_unix_adapter_free");
    FunctionDescriptor freeMethodDesc = FunctionDescriptor.ofVoid(
      ValueLayout.ADDRESS // adapter
    );

    return linker.downcallHandle(freeMethodAddr, freeMethodDesc);
  }

  private MethodHandle getUpdateMethodHandle() {
    SymbolLookup symbolLookup = SymbolLookup.loaderLookup();
    MemorySegment setFocusMethodAddr = symbolLookup.findOrThrow(
      "accesskit_unix_adapter_update_if_active");
    FunctionDescriptor setFocusMethodDesc = FunctionDescriptor.ofVoid(
      ValueLayout.ADDRESS, // adapter
      ValueLayout.ADDRESS, // update_factory
      ValueLayout.ADDRESS  // update_factory_userdata
    );

    return linker.downcallHandle(setFocusMethodAddr, setFocusMethodDesc);
  }

  private MethodHandle getSetFocusMethodHandle() {
    SymbolLookup symbolLookup = SymbolLookup.loaderLookup();
    MemorySegment setFocusMethodAddr = symbolLookup.findOrThrow(
      "accesskit_unix_adapter_update_window_focus_state");
    FunctionDescriptor setFocusMethodDesc = FunctionDescriptor.ofVoid(
      ValueLayout.ADDRESS, // adapter
      ValueLayout.JAVA_BOOLEAN // is_focused
    );

    return linker.downcallHandle(setFocusMethodAddr, setFocusMethodDesc);
  }

  private MethodHandle getDebugMethodHandle() {
    SymbolLookup symbolLookup = SymbolLookup.loaderLookup();
    MemorySegment debugMethodAddr = symbolLookup.findOrThrow("accesskit_unix_adapter_debug");
    FunctionDescriptor debugMethodDesc = FunctionDescriptor.of(
      ValueLayout.ADDRESS, // Return Value
      ValueLayout.ADDRESS  // adapter
    );

    return linker.downcallHandle(debugMethodAddr, debugMethodDesc);
  }

  // TODO: Look up method handles in advance?
  private MemorySegment updateFunctionToFunctionPointer(
    Function<MemorySegment, MemorySegment> updateFunction
  ) throws NoSuchMethodException, IllegalAccessException {
    MethodType methodType = MethodType.methodType(Object.class, Object.class);
    MethodHandle methodHandle = MethodHandles.lookup()
      .findVirtual(Function.class, "apply", methodType)
      .bindTo(updateFunction)
      .asType(MethodType.methodType(MemorySegment.class, MemorySegment.class));

    FunctionDescriptor updateSupplierDesc = FunctionDescriptor.of(
      ValueLayout.ADDRESS, // Return Value
      ValueLayout.ADDRESS  // Userdata
    ); 
    return linker.upcallStub(methodHandle, updateSupplierDesc, arena);
  }

  private MemorySegment userDataConsumerToFunctionPointer(
    Consumer<Object> consumer
  ) throws NoSuchMethodException, IllegalAccessException {
    MethodType methodType = MethodType.methodType(void.class, Object.class);
    MethodHandle methodHandle = MethodHandles.lookup()
      .findVirtual(Consumer.class, "accept", methodType)
      .bindTo(consumer)
      .asType(MethodType.methodType(void.class, MemorySegment.class));
    
    FunctionDescriptor descriptor = FunctionDescriptor.ofVoid(ValueLayout.ADDRESS);
    return linker.upcallStub(methodHandle, descriptor, arena);
  }

  private MemorySegment actionRequestConsumerToFunctionPointer(
    BiConsumer<Object, Object> consumer
  ) throws NoSuchMethodException, IllegalAccessException {
    MethodType methodType = MethodType.methodType(void.class, Object.class, Object.class);
    MethodHandle methodHandle = MethodHandles.lookup()
      .findVirtual(BiConsumer.class, "accept", methodType)
      .bindTo(consumer)
      .asType(MethodType.methodType(void.class, MemorySegment.class, MemorySegment.class));
    
    FunctionDescriptor descriptor = FunctionDescriptor.ofVoid(ValueLayout.ADDRESS, ValueLayout.ADDRESS);
    return linker.upcallStub(methodHandle, descriptor, arena);
  }

  private AKActionRequest mapActionRequest(MemorySegment actionRequest) {
    actionRequest = actionRequest.reinterpret(AKValueLayouts.ACTION_REQUEST_LAYOUT.byteSize());
    byte actionByte = (byte) AKValueLayouts.ACTION_REQUEST_ACTION.get(actionRequest, 0L);
    int actionOrdinal = Byte.toUnsignedInt(actionByte);
    AKAction action = AKAction.fromInt(actionOrdinal);
    long nodeId = (long) AKValueLayouts.ACTION_REQUEST_TARGET_NODE.get(actionRequest, 0L);
    MemorySegment dataPtr = actionRequest.asSlice(
      AKValueLayouts.ACTION_REQUEST_DATA, AKValueLayouts.OPT_ACTION_DATA_LAYOUT);
    MemorySegment valuePtr = dataPtr.asSlice(
      AKValueLayouts.OPT_ACTION_DATA_VALUE, AKValueLayouts.ACTION_DATA_LAYOUT);
    MemorySegment unionPtr = valuePtr.asSlice(
      AKValueLayouts.ACTION_DATA_UNION, AKValueLayouts.ACTION_DATA_UNION_LAYOUT);

    return switch (action) {
      case SET_TEXT_SELECTION -> {
        MemorySegment textSelectionPtr = unionPtr.asSlice(
          AKValueLayouts.ACTION_DATA_TEXT_SELECTION,
          AKValueLayouts.TEXT_SELECTION_LAYOUT);
        yield new AKActionRequest(action, nodeId,
          AKValueUtil.decodeTextSelection(textSelectionPtr));
      }
      default -> new AKActionRequest(action, nodeId, null);
    };
  }

  @Override
  public void close() throws IOException {
    try {
      freeHandle.invokeExact(adapterPointer);
      // Using auto arena, don't need to close
    } catch (IOException | RuntimeException e) {
      throw e;
    } catch (Throwable e) {
      throw new RuntimeException(e);
    }
  }

}
