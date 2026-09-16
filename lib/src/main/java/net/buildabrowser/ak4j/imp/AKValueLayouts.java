package net.buildabrowser.ak4j.imp;

import java.lang.foreign.MemoryLayout;
import java.lang.foreign.StructLayout;
import java.lang.foreign.UnionLayout;
import java.lang.foreign.ValueLayout;
import java.lang.foreign.MemoryLayout.PathElement;
import java.lang.invoke.VarHandle;

public final class AKValueLayouts {
  
  public static final ValueLayout NODE_ID_LAYOUT = ValueLayout.JAVA_LONG.withName("nodeId");

  public static final StructLayout TEXT_POSITION_LAYOUT = FFMUtil.alignedStruct( // accesskit_text_position
    NODE_ID_LAYOUT.withName("node"),
    ValueLayout.JAVA_LONG.withName("character_index")
  );

  public static final VarHandle TEXT_POSITION_NODE = TEXT_POSITION_LAYOUT
    .varHandle(PathElement.groupElement("node"));
  public static final VarHandle TEXT_POSITION_CHARACTER_INDEX = TEXT_POSITION_LAYOUT
    .varHandle(PathElement.groupElement("character_index"));

  public static final StructLayout TEXT_SELECTION_LAYOUT = FFMUtil.alignedStruct( // accesskit_text_selection
    TEXT_POSITION_LAYOUT.withName("anchor"),
    TEXT_POSITION_LAYOUT.withName("focus")
  );

  public static final long TEXT_POSITION_ANCHOR = TEXT_SELECTION_LAYOUT
    .byteOffset(PathElement.groupElement("anchor"));
  public static final long TEXT_POSITION_FOCUS = TEXT_SELECTION_LAYOUT
    .byteOffset(PathElement.groupElement("focus"));

  public static final StructLayout POINT_LAYOUT = FFMUtil.alignedStruct( // accesskit_point
    ValueLayout.JAVA_DOUBLE.withName("x"),
    ValueLayout.JAVA_DOUBLE.withName("y")
  );

  public static final UnionLayout ACTION_DATA_UNION_LAYOUT = MemoryLayout.unionLayout( // accesskit_action_data
    ValueLayout.JAVA_INT.withName("custom_action"),
    ValueLayout.ADDRESS.withName("value"),
    ValueLayout.JAVA_DOUBLE.withName("numeric_value"),
    ValueLayout.JAVA_BYTE.withName("scroll_unit"),
    ValueLayout.JAVA_BYTE.withName("scroll_hint"),
    POINT_LAYOUT.withName("scroll_to_point"),
    POINT_LAYOUT.withName("set_scroll_offset"),
    TEXT_SELECTION_LAYOUT.withName("set_text_selection")
  );

  public static final long ACTION_DATA_TEXT_SELECTION = ACTION_DATA_UNION_LAYOUT
    .byteOffset(PathElement.groupElement("set_text_selection"));

  public static final StructLayout ACTION_DATA_LAYOUT = FFMUtil.alignedStruct(
    ValueLayout.JAVA_BYTE.withName("tag"),
    ACTION_DATA_UNION_LAYOUT.withName("union"));

  public static final long ACTION_DATA_UNION = ACTION_DATA_LAYOUT
    .byteOffset(PathElement.groupElement("union"));

  public static final StructLayout OPT_ACTION_DATA_LAYOUT = FFMUtil.alignedStruct( // accesskit_opt_action_data
    ValueLayout.JAVA_BOOLEAN.withName("has_value"),
    ACTION_DATA_LAYOUT.withName("value")
  );

  public static final long OPT_ACTION_DATA_VALUE = OPT_ACTION_DATA_LAYOUT
    .byteOffset(PathElement.groupElement("value"));

  public static final StructLayout ACTION_REQUEST_LAYOUT = FFMUtil.alignedStruct( // accesskit_action_request
    ValueLayout.JAVA_BYTE.withName("action"),
    MemoryLayout.sequenceLayout(16, ValueLayout.JAVA_BYTE).withName("target_tree"),
    NODE_ID_LAYOUT.withName("target_node"),
    OPT_ACTION_DATA_LAYOUT.withName("data")
  );

  public static final VarHandle ACTION_REQUEST_ACTION = ACTION_REQUEST_LAYOUT
    .varHandle(PathElement.groupElement("action"));
  public static final VarHandle ACTION_REQUEST_TARGET_NODE = ACTION_REQUEST_LAYOUT
    .varHandle(PathElement.groupElement("target_node"));
  public static final long ACTION_REQUEST_DATA = ACTION_REQUEST_LAYOUT
    .byteOffset(PathElement.groupElement("data"));

}
