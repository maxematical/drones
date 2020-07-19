package drones.scripting

import drones.game.*
import drones.scripting.ModuleCore.Fchecktype
import drones.scripting.ModuleCore.Fclamparg
import org.dyn4j.collision.Filter
import org.dyn4j.dynamics.RaycastResult
import org.dyn4j.geometry.Vector2
import org.joml.Vector2f
import org.joml.Vector2fc
import org.joml.Vector2i
import org.luaj.vm2.*
import org.luaj.vm2.compiler.LuaC
import org.luaj.vm2.lib.*
import org.luaj.vm2.lib.jse.JseBaseLib
import org.luaj.vm2.lib.jse.JseIoLib
import org.luaj.vm2.lib.jse.JseMathLib

class ScriptManager(filename: String, instructionLimit: Int = 20,
                    addLibs: ScriptManager.(Globals) -> Unit) {
    val globals: Globals
    var thread: LuaThread
    var onComplete: (() -> Unit)? = null

    val debug: LuaValue

    var activeScanning: Boolean = false

    var nextCallback: LuaValue? = null

    var isRunningCallback = false
        private set
    private var unsetRunningCallback = object : ZeroArgFunction() {
        override fun call(): LuaValue {
            isRunningCallback = false
            return LuaValue.NIL
        }
    }

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

        globals.set("_unsetcb", unsetRunningCallback)

        val oldPrint = globals.get("print")
        globals.set("print", object : VarArgFunction() {
            override fun invoke(args: Varargs): Varargs {
                return oldPrint.invoke(LuaValue.varargsOf(LuaValue.valueOf("LUA:"), args))
            }
        })

        thread = createCoroutine(globals.get("loadfile").call(filename))

        // Limit the instruction count per script execution
        // We have to do this using the debug lib, but we don't want scripts accessing it, so we'll remove the debug
        // table directly afterwards
        globals.load(object : DebugLib() {
            override fun onInstruction(pc: Int, v: Varargs?, top: Int) {
                super.onInstruction(pc, v, top)

                if (nextCallback != null && !isRunningCallback) {
                    prepareCallbackFunction().call()
                }
            }
        })
        debug = globals.get("debug")
        val sethook = globals.get("debug").get("sethook")
        val getinfo = globals.get("debug").get("getinfo") as VarArgFunction
        val info = globals.load("debug.getinfo(move.to)").call()
//        println("Current line: " + getinfo.call(LuaValue.valueOf(1), LuaValue.valueOf("l")).get("currentline"))
        //globals.set("debug", LuaValue.NIL)

        val onInstructionLimit = object : ZeroArgFunction() {
            override fun call(): LuaValue {
                throw RuntimeException("Instruction limit exceeded")
            }
        }

        // TODO: For some reason, the runtime exception doesn't get logged (though it does shut down that thread)
        //sethook.invoke(arrayOf<LuaValue>(thread, onInstructionLimit,
        //    LuaValue.EMPTYSTRING, LuaValue.valueOf(instructionLimit)))

        // Note: To get the "lua stack trace":
        // 1) cast thread.callstack to DebugLib.Callstack
        // 2) call currentline() on each CallFrame
        // Might require DebugLib to be installed
    }

    fun update(runCallback: LuaValue?) {
        // Update Lua script
        if (!isLuaFinished()) {
            val result: Varargs = thread.resume(LuaValue.varargsOf(emptyArray()))
            if (!result.checkboolean(1)) {
                throw RuntimeException("Error: lua thread terminated with error. ${result.checkjstring(2)}")
            }
        }
        if (isLuaFinished() && nextCallback != null) {
            thread = createCoroutine(prepareCallbackFunction())
        }

        if (!isRunningCallback && runCallback != null) {
            nextCallback = runCallback
        }

        if (isLuaFinished() && onComplete != null) {
            onComplete?.invoke()
            onComplete = null
        }
    }

    private fun isLuaFinished(): Boolean =
        thread.status == "dead"

    private fun createCoroutine(func: LuaValue): LuaThread =
        globals.get("coroutine").get("create").call(func) as LuaThread

    private fun prepareCallbackFunction(): LuaValue {
        isRunningCallback = true
        globals.set("_cb", nextCallback)
        nextCallback = null
        return globals.load("_cb(); _cb = nil; _unsetcb()")
    }
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
        metatable.set("__eq", Feq)
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

        operator fun invoke(vec: Vector2fc): LuaValue =
            invoke(vec.x(), vec.y())
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

    object Feq : TwoArgFunction() {
        override fun call(arg1: LuaValue, arg2: LuaValue): LuaValue {
            val vector1 = arg1.checktable()
            val vector2 = arg2.checktable()

            val v1x = vector1.get("x").checkdouble()
            val v1y = vector1.get("y").checkdouble()
            val v2x = vector2.get("x").checkdouble()
            val v2y = vector2.get("y").checkdouble()

            return LuaValue.valueOf(v1x == v2x && v1y == v2y)
        }
    }
}

class ModuleCore(drone: Drone) : DroneModule {
    private val getPosition = Fgetpos(drone)
    private val setDesiredVelocity = Fset_thrust(drone)
    private val getTime = Fgettime(drone)
    private val setLed = Fsetled(drone)
    private val getDestination = Fget_destination(drone)
    private val setDestination = Fset_destination(drone)

    override fun buildModule(): LuaValue {
        val module = LuaValue.tableOf()
        module.set("getpos", getPosition)
        module.set("set_thrust", setDesiredVelocity)
        module.set("gettime", getTime)
        module.set("setled", setLed)
        module.set("get_destination", getDestination)
        module.set("set_destination", setDestination)
        return module
    }

    override fun install(globals: Globals) {
        globals.set("core", buildModule())
        globals.set("checktype", Fchecktype)
        globals.set("clamparg", Fclamparg)
        globals.loadfile("libsleep.lua").call()
    }

    object Fchecktype : VarArgFunction() {
        override fun invoke(varargs: Varargs): Varargs {
            val arg1 = varargs.arg1()
            val expectedType = varargs.checkjstring(2)
            val errorMessage: String
            val optional: Boolean

            if (varargs.narg() == 3) {
                errorMessage = varargs.checkjstring(3)
                optional = false
            } else {
                errorMessage = varargs.checkjstring(4)
                optional = varargs.checkboolean(3)
            }

            if (arg1.typename() != expectedType && (!optional || arg1 != LuaValue.NIL)) {
                throw RuntimeException("$errorMessage. Got: ${arg1.typename()}")
            }
            return LuaValue.NIL
        }

        operator fun invoke(obj: LuaValue, expectedType: String, errorMessage: String) {
            invoke(LuaValue.varargsOf(arrayOf(obj, LuaValue.valueOf(expectedType), LuaValue.valueOf(errorMessage))))
        }

        operator fun invoke(obj: LuaValue, expectedType: String, optional: Boolean, errorMessage: String) {
            invoke(LuaValue.varargsOf(arrayOf(obj, LuaValue.valueOf(expectedType), LuaValue.valueOf(optional),
                LuaValue.valueOf(errorMessage))))
        }
    }

    object Fclamparg : VarArgFunction() {
        override fun invoke(args: Varargs): Varargs {
            val value = args.checkdouble(1)
            val min = args.checkdouble(2)
            val max = args.checkdouble(3)
            val message = args.checkjstring(4)

            var clamped = value
            if (value < min) clamped = min
            if (value > max) clamped = max

            if (clamped != value) {
                println(message.replace("%a", min.toString())
                    .replace("%b", max.toString())
                    .replace("%x", value.toString()))
            }
            return LuaValue.valueOf(clamped)
        }

        operator fun invoke(value: Int, min: Number, max: Number, message: String): Int =
            invoke(arrayOf<LuaValue>(LuaValue.valueOf(value.toDouble()),
                LuaValue.valueOf(min.toDouble()),
                LuaValue.valueOf(max.toDouble()),
                LuaValue.valueOf(message))).arg1().toint()

        operator fun invoke(value: Double, min: Number, max: Number, message: String): Double =
            invoke(arrayOf<LuaValue>(LuaValue.valueOf(value.toDouble()),
                LuaValue.valueOf(min.toDouble()),
                LuaValue.valueOf(max.toDouble()),
                LuaValue.valueOf(message))).arg1().todouble()
    }

    class Fgettime(val drone: Drone) : ZeroArgFunction() {
        override fun call(): LuaValue {
            return LuaValue.valueOf(drone.localTime.toDouble())
        }
    }

    class Fgetpos(val drone: Drone) : ZeroArgFunction() {
        override fun call(): LuaValue {
            val pos = ModuleVector.Fcreate(drone.position)
            ModuleVector.Fsub.call(pos, ModuleVector.Fcreate(drone.scriptOrigin))
            return pos
        }
    }

    class Fset_thrust(val drone: Drone): TwoArgFunction() {
        override fun call(velX: LuaValue, velY: LuaValue): LuaValue {
            drone.desiredVelocity.x = velX.tofloat()
            drone.desiredVelocity.y = velY.tofloat()
            return LuaValue.NIL
        }
    }

    class Fsetled(val drone: Drone) : ThreeArgFunction() {
        override fun call(arg1: LuaValue, arg2: LuaValue, arg3: LuaValue): LuaValue {
            Fchecktype(arg1, "number",
                "core.setled: First argument should be a number, the red value of the LED color, e.g. 255")
            Fchecktype(arg2, "number",
                "core.setled: Second argument should be a number, the green value of the LED color, e.g. 0")
            Fchecktype(arg3, "number",
                "core.setled: Third argument should be a number, the blue value of the LED color, e.g. 0")

            val r = Fclamparg(arg1.checkdouble(), 0, 255,
                "core.setled: First argument should be within %a to %b, not %x").toInt()
            val g = Fclamparg(arg2.checkdouble(), 0, 255,
                "core.setled: Second argument should be within %a to %b, not %x").toInt()
            val b = Fclamparg(arg3.checkdouble(), 0, 255,
                "core.setled: Third argument should be within %a to %b, not %x").toInt()

            drone.ledColor = ((r and 255) shl 16) or
                    ((g and 255) shl 8) or
                    (b and 255)

            return LuaValue.NIL
        }
    }

    class Fget_destination(val drone: Drone) : VarArgFunction() {
        override fun invoke(varargs: Varargs): Varargs {
            if (drone.hasDestination) {
                val destinationVector = ModuleVector.Fcreate(drone.destination.x, drone.destination.y)
                ModuleVector.Fsub.call(destinationVector, ModuleVector.Fcreate(drone.scriptOrigin))
                val targetDistance = LuaValue.valueOf(drone.destinationTargetDistance.toDouble())
                return LuaValue.varargsOf(destinationVector, targetDistance)
            } else {
                return LuaValue.varargsOf(LuaValue.NIL, LuaValue.NIL)
            }
        }
    }

    class Fset_destination(val drone: Drone) : TwoArgFunction() {
        override fun call(arg1: LuaValue, arg2: LuaValue): LuaValue {
            Fchecktype(arg1, "table",
                "core.set_destination: Expected first argument to be a table, the vector destination to use.")
            Fchecktype(arg2, "number", true,
                "core.set_destination: Expected second argument to be a number, the optional distance at which to " +
                "stop.")

            val vector = arg1.checktable()
            val distance = Fclamparg(arg2.optdouble(0.4), 0.2, 5, "core.set_destination: Expected second argument to " +
                    "be within the range %a to %b, got %x.")

            drone.hasDestination = true
            drone.destination.set(vector.get("x").checkdouble(), vector.get("y").checkdouble()).add(drone.scriptOrigin)
            drone.destinationTargetDistance = distance.toFloat()

            return LuaValue.NIL
        }
    }
}

class ModuleScanner(private val drone: Drone,
                    private val scriptMgr: ScriptManager, private val globals: Globals) : DroneModule {
    // TODO Refactor messy control flow between scriptmgr and modulescanner

    private val pushScan = Fpush_scan(drone)
    private val popScan = Fpop_scan(drone)

    override fun buildModule(): LuaValue {
        val module = LuaTable()
        module.set("push_scan", pushScan)
        module.set("pop_scan", popScan)
        module.set("on", Fon(drone, scriptMgr, this))
        module.set("off", Foff(scriptMgr))
        return module
    }

    override fun install(globals: Globals) {
        globals.set("scanner", buildModule()) // TODO load libscanner.lua
    }

    class Fpush_scan(private val drone: Drone) : OneArgFunction() {
        override fun call(arg: LuaValue): LuaValue {
            Fchecktype(arg, "number", true,
                "scanner.push_scan: First argument should be a number, e.g. scanner.push_scan(2)")
            val scanRadius = Fclamparg(arg.optint(3), 0, 3,
                "scanner.scan: First argument should be in the range %a to %b, got %x")

            drone.scanQueue.add(ScanRequest(Vector2f(drone.position), scanRadius))
            return LuaValue.TRUE
        }
    }

    class Fpop_scan(private val drone: Drone) : ZeroArgFunction() {
        override fun call(): LuaValue {
            val result = drone.scanResultQueue.poll()
            if (result == null) {
                // TODO Log warning
                return LuaValue.NIL
            }
            return ModuleVector.Fcreate(result.x(), result.y())
        }
    }

    class Fon(val drone: Drone, val scriptMgr: ScriptManager, val scannerModule: ModuleScanner) : OneArgFunction() {
        override fun call(arg: LuaValue): LuaValue {
            scriptMgr.activeScanning = true
            return LuaValue.NIL
        }
    }

    class Foff(val scriptMgr: ScriptManager) : ZeroArgFunction() { // TODO ; find a better name for this
        override fun call(): LuaValue {
            scriptMgr.activeScanning = false
            return LuaValue.NIL
        }
    }

    companion object {
        private const val TILE_DETECTED_CALLBACK = "on_scan_detected"
        private const val OBJECT_DETECTED_CALLBACK = "on_object_detected"

        /**
         * Processes one request from the scan queue.
         */
        fun processScanQueue(state: GameState, drone: Drone) {
            val request: ScanRequest? = drone.scanQueue.poll()
            if (request != null) {
                val result: Vector2fc? = doScan(state, request.position, request.radius, drone.scriptOrigin)
                result?.let(drone.scanResultQueue::add)
            }
        }

        fun updateActiveScanning(state: GameState, drone: Drone, globals: Globals): LuaValue? {
            // Scan for tiles
            if (globals.get(TILE_DETECTED_CALLBACK) != LuaValue.NIL) {
                val scan: Vector2fc? = doScan(state, drone.position, 3, drone.scriptOrigin)
                if (scan != null) {
                    println("Kotlin Scanner: Found stuff, sending to lua")
                    return globals.load("$TILE_DETECTED_CALLBACK(vector.create(${scan.x()}, ${scan.y()}))")
                }
            }

            // Scan for objects
            if (globals.get(OBJECT_DETECTED_CALLBACK) != LuaValue.NIL) {
                for (obj in state.objects) {
                    if (obj != drone && obj != drone.carryingObject?.carrying &&
                        drone.position.distance(obj.position) <= 3f) {
                        val objX = obj.position.x() - drone.scriptOrigin.x()
                        val objY = obj.position.y() - drone.scriptOrigin.y()
                        val isCarryable = obj is OreChunk
                        return globals.load("$OBJECT_DETECTED_CALLBACK(vector.create($objX, $objY), $isCarryable)")
                    }
                }
            }

            return null
        }

        private fun doScan(gameState: GameState, scanPos: Vector2fc, radius: Int, scriptOrigin: Vector2fc): Vector2fc? =
            genericScan(gameState, scanPos, radius) { tile, gridX, gridY ->
                if (tile == TileOre) {
                    val x = gameState.grid.gridToWorldX(gridX) - scriptOrigin.x()
                    val y = gameState.grid.gridToWorldY(gridY) - scriptOrigin.y()
                    Vector2f(x, y)
                } else null
            }

        private fun <T> genericScan(gameState: GameState, scanPos: Vector2fc, scanRadius: Int,
                                    processTile: (tile: Tile, gridX: Int, gridY: Int) -> T?): T? {
            val grid = gameState.grid

            val tilePosition = grid.worldToGrid(scanPos)
            val tileMin = Vector2i(tilePosition).sub(scanRadius, scanRadius)
            val tileMax = Vector2i(tilePosition).add(scanRadius, scanRadius)

            val found = mutableListOf<T>()

            for (gridY in Math.max(0, tileMin.y)..Math.min(grid.height - 1, tileMax.y)) {
                for (gridX in Math.max(0, tileMin.x)..Math.min(grid.width - 1, tileMax.x)) {
                    val tile = grid.getTile(gridX, gridY)
                    val result = processTile(tile, gridX, gridY)

                    if (result != null)
                        found.add(result)
                }
            }

            return if (found.isNotEmpty()) found.random() else null
        }
    }
}

class ModuleMiningLaser(private val drone: Drone) : DroneModule {
    override fun buildModule(): LuaValue {
        val table = LuaValue.tableOf()
        table.set("laser_on", Flaser_on(drone))
        table.set("laser_target", Flaser_target(drone))
        table.set("laser_off", Flaser_off(drone))
        return table
    }

    override fun install(globals: Globals) {
        globals.set("mining_laser", buildModule())
        globals.loadfile("libmininglaser.lua").call()
    }

    private class Flaser_on(private val drone: Drone) : OneArgFunction() {
        override fun call(arg: LuaValue): LuaValue {
            Fchecktype(arg, "number",
                "mining_laser.laser_on: First argument should be a number, the angle in degrees at which to point " +
                        "the laser")

            val angle = arg.checkdouble()

            if (drone.miningBeam != null) {
                throw LuaError("mining_laser.laser_on: Can't turn the laser beam on, it was already on")
            }
            val laser = LaserBeam(drone.position, angle.toFloat(), 0.4f, 5f)
            laser.createBehavior = CreateMiningLaserBehavior(laser, drone.inventory)
            laser.colorR = 0.85f
            laser.colorG = 0.85f
            laser.colorB = 1.80f
            drone.miningBeam = laser
            return LuaValue.NIL
        }
    }

    private class Flaser_target(private val drone: Drone) : OneArgFunction() {
        override fun call(arg: LuaValue): LuaValue {
            Fchecktype(arg, "number",
                "mining_laser.laser_target: First argument should be a number, the angle in degrees at which to point " +
                        "the laser")

            val angle = arg.checkdouble()

            if (drone.miningBeam == null) {
                throw LuaError("mining_laser.laser_target: Laser beam must be on before targeting")
            }
            drone.miningBeam?.rotation = angle.toFloat()
            return LuaValue.NIL
        }
    }

    private class Flaser_off(private val drone: Drone) : ZeroArgFunction() {
        override fun call(): LuaValue {
            if (drone.miningBeam == null) {
                throw LuaError("mining_laser.laser_off: Can't turn the laser beam off, it was already off")
            }
            drone.miningBeam?.requestDespawn = true
            drone.miningBeam = null
            return LuaValue.NIL
        }
    }
}

class ModuleTractorBeam(private val drone: Drone, private val gameState: GameState) : DroneModule {
    override fun buildModule(): LuaValue {
        val table = LuaValue.tableOf()
        table.set("fire_at", Ffire_at(drone, gameState))
        return table
    }

    override fun install(globals: Globals) {
        globals.set("tractor_beam", buildModule())
    }

    class Ffire_at(private val drone: Drone, private val gameState: GameState) : OneArgFunction() {
        override fun call(arg: LuaValue): LuaValue {
            Fchecktype(arg, "table", "tractor_beam.fire_at: Expected first argument to be a vector, the location to " +
                    "fire towards.")

            val fireTowardsVec = arg.checktable()
            val fireTowardsX = fireTowardsVec.get("x").checkdouble() + drone.scriptOrigin.x()
            val fireTowardsY = fireTowardsVec.get("y").checkdouble() + drone.scriptOrigin.y()

            val rot = Math.atan2(fireTowardsY - drone.position.y, fireTowardsX - drone.position.x)
            val rayLength = 3.0
            val rayStart = Vector2(drone.position.x.toDouble(), drone.position.y.toDouble())
            val rayEnd = Vector2(Math.cos(rot), Math.sin(rot)).multiply(rayLength).add(rayStart)
            val results = mutableListOf<RaycastResult>()
            if (gameState.world.raycast(rayStart, rayEnd, Filter { true }, true, false, true, results)) {
                for (result in results) {
                    val userData = result.body.userData
                    if (userData is OreChunk) {
                        drone.carryingObject = CarryingObject(userData, userData.physics.physicsBody)
                        return LuaValue.TRUE
                    }
                }
            }

            return LuaValue.FALSE
        }
    }
}
