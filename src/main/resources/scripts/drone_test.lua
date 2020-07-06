print('hello')
local coords = scanner.scan_ores()
print('Ore Coordinates:', coords)

move.to(coords)
print('Moved to ore')

move.to(4, -4)
