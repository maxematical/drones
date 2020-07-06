local move = {}

local function sign(x)
    if (x == 0) then return 0 end
    if (x > 0) then return 1
    else return -1 end
end

function move.to(target)
    local x, y
    repeat
        x, y = core.getpos()
        local distx = math.abs(x - target.x)
        local disty = math.abs(y - target.y)

        local thrustx = sign(target.x - x) * math.min(1, distx)
        local thrusty = sign(target.y - y) * math.min(1, disty)
        core.set_thrust(thrustx, thrusty)

        coroutine.yield()
    until (distx * distx + disty * disty < 1)

    core.set_thrust(0, 0)
end

return move
