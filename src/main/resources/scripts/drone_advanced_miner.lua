local found_coords = nil
function on_scan_detected(coords, has_detected_before)
    if inventory.is_empty() then
        found_coords = coords
    end
    if not has_detected_before then
        print('Found coordinates!')
        comms.broadcast('ore', coords)
    end
end

print('Searching for ore')
comms.broadcast('Hello all')
scanner.on()

function patrol()
    move.units(-6, 0)
    move.units(0, 6)
    move.units(6, 0)
    move.units(0, -6)
end

function should_mine()
    return found_coords ~= nil
end

function mine()
    print('mining it')
    move.near(found_coords)
    --mine here
    mining_laser.mine_tile(found_coords)

    --return to base
    print('returning to base')
    move.return_to_base()

    --deposit stuff
    inventory.wait_until_empty()

    --go back to coordinates, maybe find some more
    print('going back')
    move.near(found_coords)
    print('ready')
    scanner.on()
    found_coords = nil
end

print('STARTING TO PATROL...')
core.do_until(patrol, should_mine)
print('FINISHED PATROL')

if should_mine() then
    mine()
else
    patrol()
end
