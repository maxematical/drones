function on_scan_detected(coords)
    core.setled(102, 255, 255)
    print('Found coordinates:', coords)
    scanner.off()
    print('Moving to coordinates...')
    move.to(coords)
    print('Done, maybe? going back to normal')

    core.setled(255, 0, 0)
end

print('Searching for ores...')
scanner.on()
move.units(0, 6)
move.units(-6, 0)
move.units(0, -6)
move.units(6, 0)

-- local coords
-- local sleep_length = 6.0
--
--sleep(sleep_length)
--coords = scanner.scan()
--if coords then goto mine end
--
--core.set_thrust(-1, 0)
--sleep(sleep_length)
--coords = scanner.scan()
--if coords then goto mine end
--
--core.set_thrust(0, -1)
--sleep(sleep_length)
--coords = scanner.scan()
--if coords then goto mine end
--
--core.set_thrust(1, 0)
--sleep(sleep_length)
--coords = scanner.scan()
--if coords then goto mine end
--
--print('Could not find any ores.')
--
--goto the_end
--
--::mine::
--print('Found coordinates:', coords)
--move.to(coords)
--print('Done')
--
--::the_end::
