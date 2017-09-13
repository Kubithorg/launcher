# Kubithon Installer [![Build Status](https://img.shields.io/travis/ironcraft/Installer.svg?style=flat-square)](https://travis-ci.org/ironcraft/Installer)

## Features

- Download files from a remote using MD5 checksums on a separated folder
- Clean outdated mods
- Add "Kubithon" profile into the Minecraft launcher

## Building

### Jar without dependencies

```bash
$ gradle build
```

Output is `build/libs/kubithon-installer-<version>.jar`

### Jar with dependencies

```bash
$ gradle fatjar
```

Output is `build/libs/kubithon-installer-<version>.jar`

## Dependencies

 * Apache Commons IO (commons-io:commons-io) 2.5
 * Apache Commons Codec (commons-codec:commons-codec) 1.10
 * OpenLauncherLib (fr.litarvan.openlauncherlib) 3.0.3-BETA
 * Swinger (fr.theshark34.swinger) 1.0.1-BETA
 * Json (org.json) 20160810
