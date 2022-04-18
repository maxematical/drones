# Drones Game

This is a small game that I created in July 2020. You are part of an asteroid mining company and are tasked with mining
out nearby minerals as fast as possible using your fleet of mining drones. You can program and automate the drones using
the Lua scripting language.

<img alt="Screenshot" src="https://user-images.githubusercontent.com/40149823/101296125-59e3b380-37e7-11eb-94d6-9ec32696c2eb.png" width="640" />

This project still remains mostly a "technical demonstration" and is very far from being a fully playable game. The graphics
are very simple and there isn't really any goal/objective yet. However, it
has the basics down and you can write your own lua scripts then launch the game and watch as the drones complete the tasks
you programmed them to do.

The drones are equipped with several items like scanners and mining lasers and you can script them to perform
tasks using Lua. The game is written from scratch using the Kotlin programming language, and implements:

* Lua scripting; the game exposes an API for each drone and you can write your own logic for what they should do
* A custom 2D renderer using the lwjgl (OpenGL) library
* Grid-based gameplay where autonomous drones can mine out blocks of ore and deposit it into the mining base
* A variety of virtual components that can be used by the drones, such as mining lasers, tractor beams, and inter-drone
  communications
* Physics integration with dyn4j physics engine
* Custom bitmap font renderer 
* Custom UI framework and layout engine that supports text, text areas, layout boxes, padding and margins, and relative/absolute
  positioned elements

<img alt="Screenshot" src="https://user-images.githubusercontent.com/40149823/101296179-a5965d00-37e7-11eb-8432-610879ad9e37.png" width="640" />

*Sample drone script*
```lua
-- Can store persistent state for each drone in the script
local found_coords = nil

-- Each drone has an in-game output console that can be
-- used to debug Lua code
print('Searching for ore')

-- Callback function that will be called from Kotlin
-- when the drone's scanner has detected nearby ore deposits
function on_scan_detected(coords, has_detected_before)
    -- A variety of APIs for drones such as inventory,
    -- mining_laser, scanner, etc.
    if inventory.is_empty() then
        found_coords = coords
        print('Found coordinates!')
    end

    -- Drones can communicate with each other by
    -- broadcasting messages
    if not has_detected_before then
        comms.broadcast('ore', coords)
    end
end

comms.broadcast('Hello all')
scanner.on()
```

## Compiling / contributing

To run the game, clone the repository and build with Gradle. The Lua scripts for drones are located under the path `src/main/resources/scripts`;
to change which scripts are used, go to `Main.kt` and look at the instantiations of `ScriptManager` objects.

Controls are WASD to move the camera, `+` and `-` to zoom in and out, and left click to select objects or drones. You
can use right click to manually order drones around, but this may not work well with some of the scripts. You can pause
the game by pressing space.

## License

Copyright (c) Max Battle 2020, all rights reserved.
