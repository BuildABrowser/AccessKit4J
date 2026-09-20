# AccessKit4J

This repository provides FFM-based bindings to AccessKit for Java.

It aims to provide a more modern approach to creating accessible UI frameworks in Java than
Java's own built-in options at time of writing.

## Native Libraries

AccessKit4J utilizes AccessKit as a native library!

Natives (built from BAB's fork) are located in `lib/src/main/resources/natives`. They come pre-bundled so that JitPack can build the library properly.

If you do not want to use the bundled natives, you will need to delete them and replace them with your preferred builds.

### For normal applications

You can download a zip file containing natives at https://github.com/AccessKit/accesskit-c/releases/tag/0.22.3.

Unzip the file, and ensure the contained native names match those used by AccessKit4J (e.g. `libaccesskit_x86_64.so` for Linux).

Copy them to `lib/src/main/resources/natives`.

### For BuildABrowser Browser

Accessibility on BuildABrowser Browser will not work as intended with a raw AccessKit build.

BuildABrowser Browser uses a fork of AccessKit located at https://github.com/BuildABrowser/accesskit-bab-fork.

To build it:
```bash
git clone https://github.com/BuildABrowser/accesskit-bab-fork
mv accesskit-bab-fork accesskit
git clone https://github.com/AccessKit/accesskit-c
cd accesskit-c
```

Add this to the bottom of `accesskit-c`'s `Cargo.toml`
```toml
[patch.crates-io]
accesskit = { path = "../accesskit/accesskit" }
accesskit_windows = { path = "../accesskit/adapters/windows" }
accesskit_macos = { path = "../accesskit/adapters/macos" }
accesskit_unix = { path = "../accesskit/adapters/unix" }
accesskit_android = { path = "../accesskit/adapters/android" }
accesskit_ios = { path = "../accesskit/adapters/ios" }
```

If Rust gives warnings about patches not being used, change the versions in accesskit-c's Cargo.toml to the ones
shown in the log.

Finally, run
```bash
cargo build --release
```

Your natives will be in `accesskit-c`'s `target/release` (e.g. `target/release/libaccesskit.so` for Linux).

Copy this to the correct directory and rename it (as for normal applications).