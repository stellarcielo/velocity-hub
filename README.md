The velocity-hub plugin adds the /hub command to the velocity network for easy return if a hub server or similar is present.Simply add it to the velocity server and the /hub command will run on all connected servers!  
  
The game version is for all versions supported by Velocity 3.4.0-SNAPSHOT as it is added to the proxy server only. The game version must be specified and is specified, but see the [PaperMC website](https://docs.papermc.io/velocity) for details.
# Installation
1. Download the latest release of the plugin
2. Put velocity-hub-x.x-SNAPSHOT.jar in /plugins directory of velocity

This completes the process!
# Config reference
- hubServerName  
Specify the destination server. This is the server name listed in servers in velocity.toml. (Usually, the hubServerName is the same as the server name listed in the try=[] brackets.)
- transferMessage  
Sets the message to be sent to the player who used the command during the transfer.
- alreadyConnectedMessage  
Sets the message if the player using the command is on the server designated as the forwarding destination.
- serverNotAvailableMessage  
Sets the message when the specified server is unavailable.

# Issue and question
Problems and questions feature request [here](https://github.com/stellarcielo/velocity-hub/issues)
