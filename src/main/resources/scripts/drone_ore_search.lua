function on_scan_detected(coords)
    print('Found coordinates:', coords)
    scanner.off()
    core.setled(102, 255, 255)

    print('Moving to coordinates...')
    move.to(coords, 3)

    print('Mining ore...')
    mining_laser.mine_tile(coords)

    print('Done')
    core.setled(255, 0, 0)
end

print('Searching for ores...')
scanner.on()
move.units(-6, 0)
move.units(0, 6)
move.units(6, 0)
move.units(0, -6)
