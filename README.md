MinecraftLTI
=========

An LTI tool for integrating Minecraft with LMS applications. See the [Demo video](http://www.youtube.com/watch?v=cTZgrmnaMko&list=UUSbm2g19jXCOfIe8OusD17w).

Demo server
-----------

Add the following LTI tool to your LMS:

* Configuration XML: http://minecraft.inseng.net:8133/config.xml
* Consumer key/secret: copy from http://minecraft.inseng.net:8133/consumer

Installation
------------
* MinecraftLTI requires a Minecraft API plugin for full functionality.
* For Bukkit servers, see the [BukkitLTI](https://github.com/instructure/BukkitLTI) plugin.

Development
------------
- mvn install
- java -jar target/minecraftlti-[version].jar
- open http://localhost:8133/config.xml
- see [BukkitLTI](https://github.com/instructure/BukkitLTI) for an example adapter
