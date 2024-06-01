# forge-early-loading-screen-fabric
 Port of (Neo)Forge's earlydisplay to Fabric

The mod is separated into 4 parts: `core`, `neoforge`, `loader`, and `earlydisplay`.

### `core`
`core` contains all classes under `com.jab125.earlyloadingscreen` and is responsible for patching Fabric Loader to get verbose information about entrypoints, as well as handling the window handoff. It is also responsible for initializing the early display. 
Classes are patched using Byte Buddy Agent and some classes are forcibly loaded with the help of `net.minecraftforge:unsafe`.
### `neoforge`
`neoforge` contains all classes from NeoForge that are required to make the transition between the early display and Minecraft. All the classes here are identical to their NeoForge counterparts.
### `loader`
`loader` contains all the classes needed for `earlydisplay` to function, with a few changes:
- `FMLConfig` is replaced with a stub class, providing only the default values
- `ImmediateWindowHandler` has been hardcoded to only launch the `DisplayWindow`
- `ProgressMeter` has an extra field called `originalName`
### `earlydisplay`
`earlydisplay` is almost identical to FML's `earlydisplay` subproject, with the only changes being the replacement of some "NeoForge" strings with "Fabric", changes to how the `NeoForgeLoadingOverlay` is found, and compatibility with Drippy Loading Screen.

### Equivalent Version of NeoForge/FancyModLoader

| Mod   | NeoForge               | FancyModLoader         |
|-------|------------------------|------------------------|
| 0.1.0 | (Forge) 1.20.1-47.0.44 | (Forge) 1.20.1-47.0.44 |
| 0.2.0 | 20.4.27-beta           | 2.0.1                  |
| 0.3.0 | 20.4.27-beta           | 2.0.1                  |
| 0.3.1 | 20.4.27-beta           | 2.0.1                  |
| 0.4.0 | 20.4.48-beta           | 2.0.1                  |
| 0.5.0 | 20.4.48-beta           | 2.0.1                  |
| 0.6.0 | 20.6.99-beta           | 3.0.9                  |
| 0.6.1 | 20.6.99-beta           | 3.0.9                  |
## Licensing
The entire mod is licensed under LGPL-2.1-only

All untouched code  is completely Forge/NeoForge's.