function inventory.is_full()
    return inventory.current_volume() >= inventory.capacity()
end

function inventory.is_empty()
    return inventory.current_volume() == 0
end

function inventory.wait_until_empty()
    core.wait_until(function() return inventory.is_empty() end)
end
