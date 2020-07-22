local move = {}

local function sign(x)
    if (x == 0) then return 0 end
    if (x > 0) then return 1
    else return -1 end
end

function move.wait_until_arrived()
    local initial_destination = core.get_destination()

    while core.get_destination() == initial_destination do coroutine.yield() end
end

function move.to(target, target_distance)
    checktype(target, "table", "move.to: First argument should be a Vector, e.g. move.to(core.getpos())")
    checktype(target_distance, "number", true,
        [[move.to: Second argument should be a number or nil, the optional distance to stop from the target,
        e.g. move.to(target_vec, 2)]])

    core.set_destination(target, target_distance)
    move.wait_until_arrived()
end

function move.near(target)
    move.to(target, 2.5)
end

function move.units(units_x, units_y)
    checktype(units_x, "number", [[move.units: First argument should be a number, the number of horizontal units to
        move, e.g. move.units(-1, 2)]])
    checktype(units_y, "number", [[move.units: Second argument should be a number, the number of vertical units to
        move, e.g. move.units(-1, 2)]])

    local target = vector.create(units_x, units_y)
    vector.add(target, core.getpos())

    move.to(target)
end

function move.return_to_base()
    move.to(vector.create(0, 0), 3)
end

return move
