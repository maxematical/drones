local move = {}

local function sign(x)
    if (x == 0) then return 0 end
    if (x > 0) then return 1
    else return -1 end
end

function move.to(target)
    local dist
    repeat
        local pos = core.getpos()
        dist = vector.length(pos - target)

        local thrustx = sign(target.x - pos.x) * math.min(1, dist)
        local thrusty = sign(target.y - pos.y) * math.min(1, dist)
        core.set_thrust(thrustx, thrusty)

        coroutine.yield()
    until (dist < 1)

    core.set_thrust(0, 0)
end

return move
