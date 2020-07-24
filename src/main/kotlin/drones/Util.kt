package drones

inline fun <reified T : Any> Any?.takeAs(): T? = if (this is T) this else null
