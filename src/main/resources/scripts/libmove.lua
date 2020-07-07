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

function move.units(units_x, units_y)
    checktype(units_x, "number", [[move.units: First argument should be a number, the number of horizontal units to
        move, e.g. move.units(-1, 2)]])
    checktype(units_y, "number", [[move.units: Second argument should be a number, the number of vertical units to
        move, e.g. move.units(-1, 2)]])

    local initial_x = core.getpos().x
    local initial_y = core.getpos().y

    local final_x = initial_x + units_x
    local final_y = initial_y + units_y

    local distance_left_x = math.abs(units_x)
    local distance_left_y = math.abs(units_y)
    while distance_left_x > 0.5 or distance_left_y > 0.5 do
        distance_left_x = math.abs(final_x - core.getpos().x)
        distance_left_y = math.abs(final_y - core.getpos().y)

        local thrust_x = sign(units_x) * math.min(1, distance_left_x)
        local thrust_y = sign(units_y) * math.min(1, distance_left_y)
        core.set_thrust(thrust_x, thrust_y)

        coroutine.yield()
    end

    core.set_thrust(0, 0)
end

return move
