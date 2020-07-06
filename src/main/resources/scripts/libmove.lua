local move = {}

local function sign(x)
    if (x == 0) then return 0 end
    if (x > 0) then return 1
    else return -1 end
end

function move.to(target)
    checktype(target, "table", "move.to: First argument should be a Vector, e.g. move.to(core.getpos())")

    local delta
    repeat
        local pos = core.getpos()
        delta = pos - target

        local thrustx = sign(target.x - pos.x) * math.min(1, math.abs(delta.x) / 3)
        local thrusty = sign(target.y - pos.y) * math.min(1, math.abs(delta.y) / 3)
        core.set_thrust(thrustx, thrusty)

        coroutine.yield()
    until (vector.length(delta) < 0.2)

    core.set_thrust(0, 0)
end

return move
