local coords
local sleep_length = 6.0

print('Searching for ores...')
core.set_thrust(0, 1)

sleep(sleep_length)
coords = scanner.scan()
if coords then goto mine end

core.set_thrust(-1, 0)
sleep(sleep_length)
coords = scanner.scan()
if coords then goto mine end

core.set_thrust(0, -1)
sleep(sleep_length)
coords = scanner.scan()
if coords then goto mine end

core.set_thrust(1, 0)
sleep(sleep_length)
coords = scanner.scan()
if coords then goto mine end

print('Could not find any ores.')

goto the_end

::mine::
print('Found coordinates:', coords)
move.to(coords)
print('Done')

::the_end::
