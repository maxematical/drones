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
        metatable.set("__add", Fplus)
        metatable.set("__sub", Fminus)
        metatable.set("__mul", Ftimes)
        metatable.set("__tostring", Ftostring)
    }

    override fun buildModule(): LuaValue {
        val module = LuaValue.tableOf()
        module.set("create", Fcreate)
        module.set("add", Fadd)
        module.set("sub", Fsub)
        module.set("mul", Fmul)
        module.set("length", Flength)
        module.set("tostring", Ftostring)
        return module
    }

    override fun install(globals: Globals) {
        globals.set("vector", buildModule())
    }

    object Fcreate : TwoArgFunction() {
        override fun call(x: LuaValue, y: LuaValue): LuaValue {
            val vector = LuaValue.tableOf()
            vector.set("x", x.checknumber())
            vector.set("y", y.checknumber())
            vector.setmetatable(metatable)
            return vector
        }

        operator fun invoke(x: Double, y: Double): LuaValue =
            call(LuaValue.valueOf(x), LuaValue.valueOf(y))

        operator fun invoke(x: Float, y: Float): LuaValue =
            invoke(x.toDouble(), y.toDouble())
    }

    object Fadd : TwoArgFunction() {
        override fun call(arg1: LuaValue, arg2: LuaValue): LuaValue {
            val vector1 = arg1.checktable()
            val vector2 = arg2.checktable()
            vector1.set("x", vector1.get("x").todouble() + vector2.get("x").todouble())
            vector1.set("y", vector1.get("y").todouble() + vector2.get("y").todouble())
            return vector1
        }
    }

    object Fplus : TwoArgFunction() {
        override fun call(arg1: LuaValue, arg2: LuaValue): LuaValue {
            val vector1 = arg1.checktable()
            val vector2 = arg2.checktable()
            return Fcreate(vector1.get("x").todouble() + vector2.get("x").todouble(),
                vector1.get("y").todouble() + vector2.get("y").todouble())
        }
    }

    object Fsub : TwoArgFunction() {
        override fun call(arg1: LuaValue, arg2: LuaValue): LuaValue {
            val vector1 = arg1.checktable()
            val vector2 = arg2.checktable()
            vector1.set("x", vector1.get("x").todouble() - vector2.get("x").todouble())
            vector1.set("y", vector1.get("y").todouble() - vector2.get("y").todouble())
            return LuaValue.NIL
        }
    }

    object Fminus : TwoArgFunction() {
        override fun call(arg1: LuaValue, arg2: LuaValue): LuaValue {
            val vector1 = arg1.checktable()
            val vector2 = arg2.checktable()
            return Fcreate(vector1.get("x").todouble() - vector2.get("x").todouble(),
                vector1.get("y").todouble() - vector2.get("y").todouble())
        }
    }

    object Fmul : TwoArgFunction() {
        override fun call(arg1: LuaValue, arg2: LuaValue): LuaValue {
            val vector = arg1.checktable()
            val scalar = arg2.checkdouble()
            vector.set("x", vector.get("x").todouble() * scalar)
            vector.set("y", vector.get("y").todouble() * scalar)
            return vector
        }
    }

    object Ftimes : TwoArgFunction() {
        override fun call(arg1: LuaValue, arg2: LuaValue): LuaValue {
            val vector = arg1.checktable()
            val scalar = arg2.checkdouble()
            return Fcreate(vector.get("x").todouble() * scalar, vector.get("y").todouble() * scalar)
        }
    }

    object Flength : OneArgFunction() {
        override fun call(arg: LuaValue): LuaValue {
            val vector = arg.checktable()
            val x = vector.get("x").todouble()
            val y = vector.get("y").todouble()
            return LuaValue.valueOf(Math.sqrt(x * x + y * y))
        }
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
    private val getPosition = Fgetpos(drone)
    private val setDesiredVelocity = Fset_thrust(drone)

    override fun buildModule(): LuaValue {
        val module = LuaValue.tableOf()
        module.set("getpos", getPosition)
        module.set("set_thrust", setDesiredVelocity)
        return module
    }

    override fun install(globals: Globals) {
        globals.set("core", buildModule())
    }

    class Fgetpos(val drone: Drone) : VarArgFunction() {
        override fun invoke(varargs: Varargs): Varargs {
            return LuaValue.varargsOf(arrayOf(ModuleVector.Fcreate(drone.position.x, drone.position.y)))
        }
    }

    class Fset_thrust(val drone: Drone): TwoArgFunction() {
        override fun call(velX: LuaValue, velY: LuaValue): LuaValue {
            drone.desiredVelocity.x = velX.tofloat()
            drone.desiredVelocity.y = velY.tofloat()
            return LuaValue.NIL
        }
    }
}

class ModuleScanner(drone: Drone) : DroneModule {
    private val scan = Fscan(drone)

    override fun buildModule(): LuaValue {
        val module = LuaTable()
        module.set("scan", scan)
        return module
    }

    override fun install(globals: Globals) {
        globals.set("scanner", buildModule())
    }

    class Fscan(val drone: Drone) : OneArgFunction() {
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
