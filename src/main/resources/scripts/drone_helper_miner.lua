function on_signal(message, contents)
    print('Got a message!!!!', message, contents)
    if message == "ore" then
        print('Moving to coordinates', contents)
        move.near(contents)
        local oree = scanner.scan()
        if oree ~= nil then
            mining_laser.mine_tile(oree)
            move.return_to_base()
            inventory.wait_until_empty()
        end
    end
end

comms.listen()
