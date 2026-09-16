package net.buildabrowser.ak4j;

// See licenses/accesskit.txt
// Order of actions must match that in AccessKit-c's header file
public enum AKAction {
  
  CLICK,
  FOCUS,
  BLUR,
  COLLAPSE,
  EXPAND,
  CUSTOM_ACTION,
  DECREMENT,
  INCREMENT,
  HIDE_TOOLTIP,
  SHOW_TOOLTIP,
  REPLACE_SELECTED_TEXT,
  SCROLL_DOWN,
  SCROLL_LEFT,
  SCROLL_RIGHT,
  SCROLL_UP,
  SCROLL_INTO_VIEW,
  SCROLL_TO_POINT,
  SET_SCROLL_OFFSET,
  SET_TEXT_SELECTION,
  SET_SEQUENTIAL_FOCUS_NAVIGATION_STARTING_POINT,
  SET_VALUE,
  SHOW_CONTEXT_MENU;

  public int toInt() {
    return ordinal();
  }

  public static AKAction fromInt(int ordinal) {
    return values()[ordinal];
  }

}
