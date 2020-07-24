function on_signal(message, contents)
    print('Got a message!!!!', message, contents)
    if message == "ore" then
        print('Moving to coordinates', contents)
        move.near(contents)
        local oree = scanner.scan()
        if oree ~= nil then mining_laser.mine_tile(oree) end
    end
end

comms.listen()










--
--function on_scan_detected(coords)
--    --core.stop_waiting()
--    found_coords = coords
--end
--
--function patrol()
--    move.units(0, 0)
--    move.units(0, 0)
--    move.units(0, 0)
--    move.units(0, 0)
--end
--
--do_until(patrol, function() return found_coords ~= nil end)
--patrol()
