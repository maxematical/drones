function on_scan_detected(coords)
    print('Detected ore!')
    mining_laser.mine_tile(coords)
end

function on_object_detected(coords, is_carryable)
    if is_carryable then
        print('Detected carryable object!')
        tractor_beam.fire_at(coords)
        move.return_to_base()
    end
end

print('Manual miner -- main script')
scanner.on()
move.return_to_base()
