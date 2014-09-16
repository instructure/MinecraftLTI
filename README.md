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
* MinecraftLTI requires a server running a Minecraft API mod.
* For Bukkit servers, see the [BukkitLTI](https://github.com/instructure/BukkitLTI) plugin.
* For Forge servers, see the [ForgeLTI](https://github.com/instructure/ForgeLTI) plugin.

Development
------------
- mvn install
- java -jar target/minecraftlti-[version].jar
- open http://localhost:8133/config.xml
- see [BukkitLTI](https://github.com/instructure/BukkitLTI) or [ForgeLTI](https://github.com/instructure/ForgeLTI) for an example adapter

Credits
-------
* [mikeprimm](https://github.com/mikeprimm), for his invaluable work on [dynmap](https://github.com/webbukkit/DynmapCore)
* [Dinnerbone](https://github.com/Dinnerbone), for Bukkit generally and the [Homebukkit](https://github.com/Bukkit/HomeBukkit) example in particular
* [Dr. Chuck](https://twitter.com/drchuck) and [pfgray](https://github.com/pfgray), for [basiclti-util](https://github.com/IMSGlobal/basiclti-util-java)
* [DJCordhose](https://github.com/DJCordhose), for [jmte](https://github.com/DJCordhose/jmte)
* [Gray Watson](https://stackoverflow.com/users/179850/gray), for [ORMLite](http://ormlite.com/)
* [Joel Mills](https://twitter.com/iLearningUK), for dedicated and enthusiastic beta testing
