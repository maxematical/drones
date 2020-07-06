print("Helloworldddd")

local x, y = core.getpos()
print("Drone X,y: ", x, y)

core.set_thrust(0, 1)

resume_co = coroutine.wrap(function()
        while true do
            print('update')
            coroutine.yield()
        end
    end)
resume_co()
