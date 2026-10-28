The velocity-hub plugin adds the /hub command to the velocity network for easy return if a hub server or similar is present.Simply add it to the velocity server and the `/hub` and `/lobby` command will run on all connected servers!  

The `/hub`command is added across the entire Velocity network. There is no need to add the plugin to Paper servers or other server connected to Velocity.  
  
The game version is for all versions supported by Velocity 4.3.0 as it is added to the proxy server only. The game version must be specified and is specified, but see the [PaperMC website](https://docs.papermc.io/velocity/server-compatibility/) for details.
# Installation
1. Download the latest release of the plugin
2. Put velocity-hub-x.x-SNAPSHOT.jar in /plugins directory of velocity

This completes the process!  

**Starting with version 1.10-SNAPSHOT, Java 25 or later must be installed.**
## Download here↓
- [modrinth](https://modrinth.com/plugin/velocity-hub-command/versions)(Recommended)
- [Github](https://github.com/stellarcielo/velocity-hub/releases)
- [Hangar](https://hangar.papermc.io/stellarcielo/velocity-hub-command)
- [CurseForge](https://www.curseforge.com/minecraft/bukkit-plugins/velocity-hub-command)

The sites listed above are projects managed by the developer.
# Config reference
| Key | Default | Description |
| --- | --- | --- |
| `hubServerName` | `hub` | The destination server. **This is the server name listed in servers in velocity.toml.** (Usually the same as the server name listed in the `try=[]` brackets.) |
| `transferMessage` | `Sending you to the hub!` | Sent to the player who used the command during the transfer. |
| `alreadyConnectedMessage` | `You are already connected to the hub!` | Sent when the player is already on the destination server. |
| `serverNotAvailableMessage` | `The hub server is not available.` | Sent when the destination server is unavailable or the transfer failed. |
| `playerOnlyMessage` | `Only players can execute this command!` | Sent when the command is executed from the console. |

### Message custom
We support Japanese, Russian, and other languages. If there are any languages that we do not support, please let us know via GitHub issue!  
MiniMessage is supported in all message settings. If you write like `<red>hub`, it will be displayed in red. See [here](https://docs.papermc.io/misc/tools/minimessage-web-editor) for details.  
You can test the display at the following sites.  
[https://webui.advntr.dev/](https://webui.advntr.dev/)  
[https://minecraft.menu/minecraft-rgb-generator](https://minecraft.menu/minecraft-rgb-generator)  

If a setting is missing from config.json (for example after updating the plugin), it is added automatically with its default value.

# Issue and question
If the plugin does not work properly, check the server log for config errors. If the problem persists, try deleting config.json.  
Problems and questions feature request [here](https://github.com/stellarcielo/velocity-hub/issues).
