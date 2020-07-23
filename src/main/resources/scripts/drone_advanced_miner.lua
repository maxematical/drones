local found_coords = nil
function on_scan_detected(coords)
    found_coords = coords
    scanner.off()
    print('Found coordinates!')
end

print('Searching for ore')
scanner.on()

-- TOTALLY random coordinates that I put in, I have NO IDEA if theres gonna be ore here or not
::patrol::
local move_objectives = {}
move_objectives[1] = vector.create(-6, 0)
move_objectives[2] = vector.create(0, 6)
move_objectives[3] = vector.create(6, 0)
move_objectives[4] = vector.create(0, -6)
for i=1,4 do
    local move_to = move_objectives[i] + core.getpos()
    print('Patrol objective#', i)
    core.set_destination(move_to)

    print(move_to, core.getpos(), core.get_destination())

    while core.get_destination() == move_to do
        if found_coords then goto mineit end
        coroutine.yield()
    end

    print('new destination', core.get_destination())
end

coroutine.yield()
goto patrol

::mineit::
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
goto patrol
