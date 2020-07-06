local move = {}

local function sign(x)
    if (x == 0) then return 0 end
    if (x > 0) then return 1
    else return -1 end
end

function move.to(targetX, targetY)
    print('move.to')
    local x, y
    repeat
        x, y = core.getpos()
        local distx = math.abs(x - targetX)
        local disty = math.abs(y - targetY)

        local thrustx = sign(targetX - x) * math.min(1, distx / 3)
        local thrusty = sign(targetX - x) * math.min(1, distx / 3)
        core.set_thrust(thrustx, thrusty)

        coroutine.yield()
    until (distx * distx + disty * disty < 1)

    core.set_thrust(0, 0)
end

return move
