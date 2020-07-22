function mining_laser.mine_tile(target)
    checktype(target, "table",
            [[mining_laser.mine_tile: First argument should be a table, the vector of where to mine. E.g.
            "mining_laser.mine_tile(vector.create(2, 2))]])

    local target_tile = target + vector.create(0.5, -0.5)

    local delta = target_tile - core.getpos()
    local angle = math.atan2(delta.y, delta.x) * 180 / math.pi

    mining_laser.laser_on(angle)

    local starttime = core.gettime()
    while core.gettime() - starttime < 2.0 do
        delta = target_tile - core.getpos()
        angle = math.atan2(delta.y, delta.x) * 180 / math.pi
        mining_laser.laser_target(angle)
        coroutine.yield()
    end

    mining_laser.laser_off()
    sleep(0.5)
end
