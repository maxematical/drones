package org.luaj.vm2.lib

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
            out.sourceFile = source
            out.lineNumber = line
            out.insideFunction = callstack.getCallFrame(i)?.let(DebugLib::getfuncname)?.name
            return
        }

        out.sourceFile = null
        out.lineNumber = null
        out.insideFunction = null
        return
    }

    class CurrentLine {
        var sourceFile: String? = null
        var lineNumber: Int? = null
        var insideFunction: String? = null
    }
}
