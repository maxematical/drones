package org.luaj.vm2.lib

import org.luaj.vm2.LuaFunction
import org.luaj.vm2.LuaThread

object LuaDebugHelper {
    fun getCurrentLine(debugLib: DebugLib, thread: LuaThread, out: CurrentLine) {
        val callstack = debugLib.callstack(thread)

        // Go through the frames until we meet the end
        var i = 1
        while (true) {
            val frame: DebugLib.CallFrame = callstack.getCallFrame(i++) ?: break

            // Determine the function's source file
            val prototype = frame.f.checkclosure().p
            val source = prototype.source.tojstring().substring(1)

            // Skip if the function's source file starts with "lib"
            if (source.startsWith("lib"))
                continue

            // Determine the current line number
            val line = frame.currentline()

            // Give result and return
            out.valid = true
            out.sourceFile = source
            out.lineNumber = line
            out.insideFunction = callstack.getCallFrame(i)?.let(DebugLib::getfuncname)?.name
            return
        }

        out.valid = false
        return
    }

    fun pushcall(debugLib: DebugLib, function: LuaFunction) {
        debugLib.onCall(function)
    }

    fun popcall(debugLib: DebugLib) {
        debugLib.onReturn()
    }

    class CurrentLine {
        var valid: Boolean = false
        var sourceFile: String = ""
        var lineNumber: Int = -1
        var insideFunction: String? = null
    }
}
