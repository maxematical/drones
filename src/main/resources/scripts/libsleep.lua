function sleep(seconds)
    checktype(seconds, "number",
        "First argument should be a number, the amount of seconds to sleep. E.g. sleep(2.5)")

    -- Wait until the current time since sleep was called (core.gettime() - start_time) is at least the number of
    -- seconds we want to wait
    local start_time = core.gettime()
    while (core.gettime() - start_time) < seconds do
        coroutine.yield()
    end
end
