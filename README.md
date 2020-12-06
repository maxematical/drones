# Drones Game

A small game/technical demo I was working on back in July. You are part of a space mining operation and are tasked with
programming and operating a fleet of automated drones to locate and mine out valuable minerals in the area.

<img alt="Screenshot 1" src="https://user-images.githubusercontent.com/40149823/101296047-e8a40080-37e6-11eb-900c-1ec47d832919.png" width="640" />

As I wrote this mainly as a challenge for myself, this is more of a technical demo rather than a fully fleshed out
games. The drones are equipped with several items like scanners and mining lasers and you can script them to perform
tasks using Lua. The game is written from scratch using Kotlin and OpenGL (lwjgl) and I implemented several other game
engine related features.

* Integration with Lua; easy scripting that supports both blocking and callback styles of code
* Custom (albeit simple) fast OpenGL 2D renderer
* Grid based gameplay where autonomous drones can mine out blocks of ore and deposit it into the mining base
* A variety of virtual components that can be used by the drones, such as mining lasers, tractor beams, and inter-drone
  communications
* Physics integration with dyn4j physics engine and semi-realistic propulsion model
* GPU bitmap font renderer that works for monospaced fonts
* Custom UI framework and layout engine that supports text, text areas, layout boxes, padding/margins, and
  absolute/relative positioning (!)

<img alt="Screenshot 2" src="https://user-images.githubusercontent.com/40149823/101296125-59e3b380-37e7-11eb-94d6-9ec32696c2eb.png" width="640" />

*Sample drone script*
```lua
local found_coords = nil

-- Callback function that will be called by Kotlin code
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

-- Each drone has an in-game output console that can be
-- used to debug Lua code
print('Searching for ore')

comms.broadcast('Hello all')
scanner.on()
```

## Using

There are no precompiled binaries for this game so you will have to download and compile it yourself. Clone the
repository and build with Gradle. The Lua scripts for drones are located under the path `src/main/resources/scripts`;
to change which scripts are used, go to `Main.kt` and look at the instantiations of `ScriptManager` objects.

Controls are WASD to move the camera, `+` and `-` to zoom in and out, and left click to select objects or drones. You
can use right click to manually order drones around, but this may not work well with some of the scripts. You can pause
the game by pressing space.

<img alt="Screenshot 3" src="https://user-images.githubusercontent.com/40149823/101296179-a5965d00-37e7-11eb-8432-610879ad9e37.png" width="640" />

## License

Copyright (c) Max Battle 2020, all rights reserved.
