# AccessKit4J

This repository provides FFM-based bindings to AccessKit for Java.

It aims to provide a more modern approach to creating accessible UI frameworks in Java than
Java's own built-in options at time of writing.

AccessKit4J uses a fork of AccessKit, not vanilla AccessKit. The fork contains a number of code
changes needed to use Orca's Browsing Mode within AccessKit.

AccessKit4J's build script also override's accesskit-c's Cargo.toml file to point it towards the fork of AccessKit.

Currently, AccessKit4J only binds to the unix adapter.

To build:
```bash
./scripts/build-natives-linux.sh
./gradlew build
```