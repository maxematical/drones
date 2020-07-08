function mining_laser.mine_tile(target)
    checktype(target, "table",
            [[mining_laser.mine_tile: First argument should be a table, the vector of where to mine. E.g.
            "mining_laser.mine_tile(vector.create(2, 2))]])

    local delta = (target + vector.create(0.5, 0.5)) - core.getpos()
    local angle = math.atan2(delta.y, delta.x) * 180 / math.pi

    mining_laser.laser_on(angle, vector.length(delta))

    local starttime = core.gettime()
    while core.gettime() - starttime < 2.5 do
        delta = (target + vector.create(0.5, 0.5)) - core.getpos()
        angle = math.atan2(delta.y, delta.x) * 180 / math.pi
        mining_laser.laser_target(angle, vector.length(delta))
        coroutine.yield()
    end

    mining_laser.laser_off()
    sleep(0.5)
end

print('mineee')
