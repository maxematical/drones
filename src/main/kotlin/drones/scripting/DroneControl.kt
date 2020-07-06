package drones.scripting

import drones.Drone
import org.luaj.vm2.*
import org.luaj.vm2.compiler.LuaC
import org.luaj.vm2.lib.*
import org.luaj.vm2.lib.jse.JseBaseLib
import org.luaj.vm2.lib.jse.JseIoLib
import org.luaj.vm2.lib.jse.JseMathLib
import java.lang.RuntimeException

class DroneControl {

}

fun main() {
    runScript("/scripts/test.lua")
    //runScript("/scripts/test2.lua")
}

fun runScript(filename: String, addLibs: (Globals) -> Unit = {}): Globals {
    val globals = Globals()
    globals.load(JseBaseLib())
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

//    globals.loadfile(filename).call()
//    Thread.sleep(1000)
//    globals.load("coroutine.resume(co)").call()
    return globals
}

class ScriptManager(filename: String, instructionLimit: Int = 20, addLibs: (Globals) -> Unit) {
    val globals: Globals
    val thread: LuaThread

    init {
        globals = Globals()
        globals.load(JseBaseLib())
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
        globals.set("_thread", LuaValue.NIL)

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
        thread.resume(LuaValue.varargsOf(emptyArray()))
    }
}

object GetFive : OneArgFunction() {
    override fun call(arg: LuaValue?): LuaValue =
        LuaInteger.valueOf(5)
}

class ModuleMovement(drone: Drone) {
    private val getPosition = GetPosition(drone)
    private val setDesiredVelocity = SetDesiredVelocity(drone)

    val module: LuaValue

    init {
        module = LuaValue.tableOf()
        module.set("getpos", getPosition)
        module.set("set_thrust", setDesiredVelocity)
    }

    fun installLib(globals: Globals) {
        globals.set("core", module)
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
