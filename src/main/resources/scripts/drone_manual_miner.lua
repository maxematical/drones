function on_scan_detected(coords)
    print('Detected ore!')
    mining_laser.mine_tile(coords)
end

scanner.on()
