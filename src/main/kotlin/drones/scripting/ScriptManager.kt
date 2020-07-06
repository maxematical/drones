package drones.scripting

import drones.Drone
import drones.TileStone
import org.joml.Vector2i
import org.luaj.vm2.*
import org.luaj.vm2.compiler.LuaC
import org.luaj.vm2.lib.*
import org.luaj.vm2.lib.jse.JseBaseLib
import org.luaj.vm2.lib.jse.JseIoLib
import org.luaj.vm2.lib.jse.JseMathLib
import java.lang.RuntimeException

class ScriptManager(filename: String, instructionLimit: Int = 20, addLibs: (Globals) -> Unit) {
    val globals: Globals
    val thread: LuaThread

    init {
        globals = Globals()

        val baseLib = JseBaseLib()
        globals.load(baseLib)
        globals.finder = ResourceFinder {
            var f: String = it
            if (!f.startsWith(('/'))) f = "/$f"
            baseLib.findResource("scripts/$f")
        }

        globals.load(PackageLib())
        globals.load(Bit32Lib())
        globals.load(TableLib())
        globals.load(StringLib())
        globals.load(JseMathLib())
        globals.load(CoroutineLib())
        globals.load(JseIoLib())
        LoadState.install(globals)
        LuaC.install(globals)

        addLibs(globals)

        globals.load("_thread = coroutine.create(loadfile('$filename'))").call()
        thread = globals.get("_thread") as LuaThread
        //globals.set("_thread", LuaValue.NIL)

        // Limit the instruction count per script execution
        // We have to do this using the debug lib, but we don't want scripts accessing it, so we'll remove the debug
        // table directly afterwards
        globals.load(DebugLib())
        val sethook = globals.get("debug").get("sethook")
        globals.set("debug", LuaValue.NIL)

        val onInstructionLimit = object : ZeroArgFunction() {
            override fun call(): LuaValue {
                throw RuntimeException("Instruction limit exceeded")
            }
        }

        // TODO: For some reason, the runtime exception doesn't get logged (though it does shut down that thread)
        //sethook.invoke(arrayOf<LuaValue>(thread, onInstructionLimit,
        //    LuaValue.EMPTYSTRING, LuaValue.valueOf(instructionLimit)))
    }

    fun resume() {
        val result: Varargs = thread.resume(LuaValue.varargsOf(emptyArray()))
        if (!result.checkboolean(1)) {
            throw RuntimeException("Error: lua thread terminated with error. ${result.checkjstring(2)}")
        }
    }

    fun isFinished(): Boolean =
        thread.status == "dead"
}

interface DroneModule {
    fun buildModule(): LuaValue
    fun install(globals: Globals)
}

object ModuleVector : DroneModule {
    val metatable: LuaValue = LuaValue.tableOf()

    init {
        metatable.set("__tostring", Ftostring)
    }

    override fun buildModule(): LuaValue {
        val module = LuaValue.tableOf()
        module.set("create", Fcreate)
        module.set("tostring", Ftostring)
        return module
    }

    override fun install(globals: Globals) {
        globals.set("vector", buildModule())
    }

    object Fcreate : TwoArgFunction() {
        override fun call(x: LuaValue, y: LuaValue): LuaValue {
            val vector = LuaValue.tableOf(2, 2)
            vector.set("x", x.checknumber())
            vector.set("y", y.checknumber())
            vector.setmetatable(metatable)
            return vector
        }

        operator fun invoke(x: Float, y: Float): LuaValue =
            call(LuaValue.valueOf(x.toDouble()), LuaValue.valueOf(y.toDouble()))
    }

    object Ftostring : OneArgFunction() {
        override fun call(arg: LuaValue): LuaValue {
            val vector = arg.checktable()
            val x = vector.get("x")
            val y = vector.get("y")
            return LuaValue.valueOf("($x, $y)")
        }
    }
}

class ModuleMovement(drone: Drone) : DroneModule {
    private val getPosition = GetPosition(drone)
    private val setDesiredVelocity = SetDesiredVelocity(drone)

    override fun buildModule(): LuaValue {
        val module = LuaValue.tableOf()
        module.set("getpos", getPosition)
        module.set("set_thrust", setDesiredVelocity)
        return module
    }

    override fun install(globals: Globals) {
        globals.set("core", buildModule())
    }

    class GetPosition(val drone: Drone) : VarArgFunction() {
        override fun invoke(varargs: Varargs): Varargs {
            return LuaValue.varargsOf(LuaValue.valueOf(drone.position.x.toDouble()),
                LuaValue.valueOf(drone.position.y.toDouble()))
        }
    }

    class SetDesiredVelocity(val drone: Drone): TwoArgFunction() {
        override fun call(velX: LuaValue, velY: LuaValue): LuaValue {
            drone.desiredVelocity.x = velX.tofloat()
            drone.desiredVelocity.y = velY.tofloat()
            return LuaValue.NIL
        }
    }
}

class ModuleScanner(drone: Drone) : DroneModule {
    private val scan = Scan(drone)

    override fun buildModule(): LuaValue {
        val module = LuaTable()
        module.set("scan", scan)
        return module
    }

    override fun install(globals: Globals) {
        globals.set("scanner", buildModule())
    }

    class Scan(val drone: Drone) : OneArgFunction() {
        override fun call(arg: LuaValue): LuaValue {
            val scanRadius = Math.min(3, arg.optint(3))

            val tileMin = Vector2i(drone.tilePosition).sub(scanRadius, scanRadius)
            val tileMax = Vector2i(drone.tilePosition).add(scanRadius, scanRadius)

            val grid = drone.grid
            for (gridY in Math.max(0, tileMin.y)..Math.min(grid.height - 1, tileMax.y)) {
                for (gridX in Math.max(0, tileMin.x)..Math.min(grid.width - 1, tileMax.x)) {
                    val tile = grid.tiles[gridY][gridX]
                    if (tile == TileStone) {
                        val x = grid.gridToWorldX(gridX)
                        val y = grid.gridToWorldY(gridY)
                        return ModuleVector.Fcreate(x, y)
                    }
                }
            }
            return LuaValue.NIL
        }
    }
}
