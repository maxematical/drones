local move = {}

local function sign(x)
    if (x == 0) then return 0 end
    if (x > 0) then return 1
    else return -1 end
end

function move.to(target, target_distance)
    checktype(target, "table", "move.to: First argument should be a Vector, e.g. move.to(core.getpos())")
    checktype(target_distance, "number", true,
        [[move.to: Second argument should be a number or nil, the optional distance to stop from the target,
        e.g. move.to(target_vec, 2)]])

    target_distance = target_distance or 0.2

    local delta
    repeat
        local pos = core.getpos()
        delta = pos - target

        local thrustx = sign(target.x - pos.x) * math.min(1, math.abs(delta.x) / 3)
        local thrusty = sign(target.y - pos.y) * math.min(1, math.abs(delta.y) / 3)
        core.set_thrust(thrustx, thrusty)

        coroutine.yield()
    until (vector.length(delta) < target_distance)

    core.set_thrust(0, 0)
end

function move.units(units_x, units_y, debug)
    checktype(units_x, "number", [[move.units: First argument should be a number, the number of horizontal units to
        move, e.g. move.units(-1, 2)]])
    checktype(units_y, "number", [[move.units: Second argument should be a number, the number of vertical units to
        move, e.g. move.units(-1, 2)]])

    local t = 0
    local lasttime = core.gettime()

    local initial_x = core.getpos().x
    local initial_y = core.getpos().y

    local final_x = initial_x + units_x
    local final_y = initial_y + units_y

    local go_x = units_x
    local go_y = units_y
    while math.abs(go_x) > 0.5 or math.abs(go_y) > 0.5 do
        go_x = final_x - core.getpos().x
        go_y = final_y - core.getpos().y

        local thrust_x = sign(go_x) * math.min(1, math.abs(go_x))
        local thrust_y = sign(go_y) * math.min(1, math.abs(go_y))
        core.set_thrust(thrust_x, thrust_y)

        if debug ~= nil then
            local dt = core.gettime() - lasttime
            if (t <= 0) then print('move.units', units_x, units_y, debug); print('move.units', go_x, go_y); t = 1 end
            t = t - dt
            lasttime = core.gettime()
        end

        coroutine.yield()
    end

    core.set_thrust(0, 0)
end

return move
