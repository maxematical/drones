print('Hello')
local coords = scanner.scan_ores()
print('Ore Coordinates:', coords)

move.to(coords)
print('Moved to ore')

sleep(2.5)

print('Moving to final coordinates...')
move.to(vector.create(4, 0))
print('Done')
