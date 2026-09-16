package net.buildabrowser.ak4j.sample;

import java.lang.foreign.Arena;
import java.lang.foreign.MemorySegment;

import net.buildabrowser.ak4j.AK4J;
import net.buildabrowser.ak4j.AK4JHandle;
import net.buildabrowser.ak4j.AKAction;
import net.buildabrowser.ak4j.AKActionRequest;
import net.buildabrowser.ak4j.AKCallbacks;
import net.buildabrowser.ak4j.AKRole;
import net.buildabrowser.ak4j.imp.CommonUtil;
import net.buildabrowser.ak4j.util.TextRunUtil;

public class MainTest implements AKCallbacks {

  private static final int WINDOW_ID = 0;
  private static final int DOCUMENT_ID = 1;
  private static final int BUTTON_1_ID = 2;
  private static final int BUTTON_2_ID = 3;
  private static final int HEADING_ID = 4;
  private static final int LABEL_1_ID = 5;
  private static final int LABEL_2_ID = 6;

  public static void main(String[] args) throws Throwable {
    MainTest mainTest = new MainTest();
    try (AK4JHandle ak4jHandle = AK4J.init(mainTest)) {
      System.out.println("Running sample program for AK4J v" + AK4J.versionString() + "!");
      System.out.println("Adapter: " + ak4jHandle.adapter());
      ak4jHandle.adapter().setFocus(true);
      Thread.sleep(30000);
    }
  }

  private MemorySegment updateTree(
    AK4JHandle ak4jHandle
  ) throws InterruptedException {
    try (Arena scope = Arena.ofConfined()) {
      MemorySegment tree = ak4jHandle.createTree(WINDOW_ID, scope);
      MemorySegment update = ak4jHandle.createTreeUpdate(tree, 7, DOCUMENT_ID, scope);
      
      MemorySegment rootNode = ak4jHandle.nodes().create(AKRole.WINDOW, scope);
      ak4jHandle.nodes().addAction(rootNode, AKAction.FOCUS);
      ak4jHandle.nodes().pushChild(rootNode, DOCUMENT_ID);
      ak4jHandle.pushTreeUpdateNode(update, WINDOW_ID, rootNode);

      MemorySegment documentNode = ak4jHandle.nodes().create(AKRole.DOCUMENT, scope);
      ak4jHandle.nodes().addAction(documentNode, AKAction.FOCUS);
      ak4jHandle.nodes().pushChild(documentNode, HEADING_ID);
      ak4jHandle.nodes().pushChild(documentNode, BUTTON_1_ID);
      ak4jHandle.nodes().pushChild(documentNode, BUTTON_2_ID);
      ak4jHandle.pushTreeUpdateNode(update, DOCUMENT_ID, documentNode);

      MemorySegment headingNode = ak4jHandle.nodes().create(AKRole.HEADING, scope);
      ak4jHandle.properties().setLabel(headingNode, "Heading 1", scope);
      ak4jHandle.properties().setLevel(headingNode, 1);
      ak4jHandle.nodes().pushChild(headingNode, LABEL_1_ID);
      ak4jHandle.nodes().addAction(headingNode, AKAction.FOCUS);
      ak4jHandle.pushTreeUpdateNode(update, HEADING_ID, headingNode);

      MemorySegment buttonNode = ak4jHandle.nodes().create(AKRole.BUTTON, scope);
      ak4jHandle.nodes().pushChild(buttonNode, LABEL_2_ID);
      ak4jHandle.pushTreeUpdateNode(update, BUTTON_1_ID, buttonNode);

      MemorySegment buttonNode2 = ak4jHandle.nodes().create(AKRole.BUTTON, scope);
      ak4jHandle.pushTreeUpdateNode(update, BUTTON_2_ID, buttonNode2);
      
      MemorySegment textNode1 = ak4jHandle.nodes().create(AKRole.TEXT_RUN, scope);
      // TODO: Does accesskit copy the string? If not, it will be free'd too early
      String text1 = "Hello, World!";
      byte[] text1Lengths = new byte[text1.length()];
      int text1LengthsSize = TextRunUtil.getTextLengths(text1, text1Lengths);
      ak4jHandle.nodes().setValue(textNode1, "Hello, World!", scope);
      ak4jHandle.nodes().setCharacterLengths(textNode1, text1Lengths, text1LengthsSize, scope);
      ak4jHandle.pushTreeUpdateNode(update, LABEL_1_ID, textNode1);

      String text2 = "Goodbye, Cruel World!";
      byte[] text2Lengths = new byte[text2.length()];
      int text2LengthsSize = TextRunUtil.getTextLengths(text2, text2Lengths);
      MemorySegment textNode2 = ak4jHandle.nodes().create(AKRole.TEXT_RUN, scope);
      ak4jHandle.nodes().setValue(textNode2, text2, scope);
      ak4jHandle.nodes().setCharacterLengths(textNode1, text2Lengths, text2LengthsSize, scope);
      ak4jHandle.pushTreeUpdateNode(update, LABEL_2_ID, textNode2);

      return update;
    }
  }

  @Override
  public MemorySegment onActivation(AK4JHandle ak4jHandle) {
    System.out.println("Activated!");
    return CommonUtil.rethrow(() -> updateTree(ak4jHandle));
  }

  @Override
  public void onAction(AK4JHandle ak4jHandle, AKActionRequest actionRequest) {
    System.out.println("Action! " + actionRequest);
  }

  @Override
  public void onDeactivation(AK4JHandle ak4jHandle) {
    System.out.println("Deactivated!");
  }

}
