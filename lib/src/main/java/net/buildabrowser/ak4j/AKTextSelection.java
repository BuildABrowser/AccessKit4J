package net.buildabrowser.ak4j;

public record AKTextSelection(
  AKTextPosition anchor,
  AKTextPosition focus
) {

  public static record AKTextPosition(
    long nodeId, long characterIndex
  ) {}
  
}
