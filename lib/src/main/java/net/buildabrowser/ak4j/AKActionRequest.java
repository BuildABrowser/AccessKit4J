package net.buildabrowser.ak4j;

public record AKActionRequest(
  AKAction action,
  long nodeId,
  Object data
) {
  
}
