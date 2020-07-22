function inventory.is_full()
    return inventory.current_volume() >= inventory.capacity()
end

function inventory.is_empty()
    return inventory.current_volume() == 0
end

function inventory.wait_until_empty()
    while not inventory.is_empty() do coroutine.yield() end
end
