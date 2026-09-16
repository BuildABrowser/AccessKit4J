package net.buildabrowser.ak4j;

import java.io.File;
import java.io.IOException;
import java.lang.foreign.Linker;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;

import net.buildabrowser.ak4j.imp.AK4JHandleImp;
import net.buildabrowser.ak4j.imp.AKNodeCallsImp;
import net.buildabrowser.ak4j.imp.AKPropertyCallsImp;
import net.buildabrowser.ak4j.imp.UnixAKAdapter;

public final class AK4J {
  
  private static final int MAJOR_VERSION = 0;
  private static final int MINOR_VERSION = 1;
  private static final int PATCH_VERSION = 0;

  public static int majorVersion() {
    return MAJOR_VERSION;
  }

  public static int minorVersion() {
    return MINOR_VERSION;
  }

  public static int patchVersion() {
    return PATCH_VERSION;
  }

  public static String versionString() {
    return majorVersion() + "." + minorVersion() + "." + patchVersion();
  }

  public static AK4JHandle init(
    AKCallbacks callbacks
  ) throws IOException {
    File copiedFile = File.createTempFile("libaccesskit", ".so");
    Files.copy(
      AK4J.class.getClassLoader().getResourceAsStream("natives/libaccesskit_x86_64.so"),
      copiedFile.toPath(),
      StandardCopyOption.REPLACE_EXISTING);
    System.load(copiedFile.getAbsolutePath());

    Linker linker = Linker.nativeLinker();
    AKAdapter adapter = new UnixAKAdapter(linker, callbacks);
    AKNodeCalls nodeCalls = new AKNodeCallsImp(linker);
    AKPropertyCalls propertyCalls = new AKPropertyCallsImp(linker);
    AK4JHandle handle = new AK4JHandleImp(
      linker, adapter, nodeCalls, propertyCalls);
    adapter.start(handle);
    return handle;
  }

}
