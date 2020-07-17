function scanner.scan(range)
    scanner.push_scan(range)
    coroutine.yield()
    return scanner.pop_scan()
end

function scanner.scan_ores()
    return scanner.scan(1000)
end
