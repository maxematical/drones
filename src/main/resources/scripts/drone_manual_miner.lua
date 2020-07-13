function on_scan_detected(coords)
    print('Detected ore!')
    mining_laser.mine_tile(coords)
end

print('Manual miner -- main script')
scanner.on()
move.return_to_base()
