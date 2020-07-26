package org.luaj.vm2.lib

import org.luaj.vm2.LuaFunction
import org.luaj.vm2.LuaThread

object LuaDebugHelper {
    /**
     * Iterates through the lua call stack and finds information about what line number, lua script, and function are
     * currently executing. Then, stores all data stored into the given output object.
     */
    fun getCurrentLine(debugLib: DebugLib, thread: LuaThread, out: MutableCurrentLine) {
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

    /**
     * Pushes a call frame onto the Lua call stack. It is needed to do this manually when calling Lua functions in
     * Kotlin, because although the call stack is automatically updated when calling these functions through Lua code,
     * it is not automatically updated when calling these functions through Kotlin code.
     */
    fun pushcall(debugLib: DebugLib, function: LuaFunction) {
        debugLib.onCall(function)
    }

    /**
     * Pops a call frame from the Lua call stack.
     * @see pushcall
     */
    fun popcall(debugLib: DebugLib) {
        debugLib.onReturn()
    }

    interface CurrentLine {
        val valid: Boolean
        val sourceFile: String
        val lineNumber: Int
        val insideFunction: String?
    }

    class MutableCurrentLine : CurrentLine {
        override var valid: Boolean = false
        override var sourceFile: String = ""
        override var lineNumber: Int = -1
        override var insideFunction: String? = null
    }
}
