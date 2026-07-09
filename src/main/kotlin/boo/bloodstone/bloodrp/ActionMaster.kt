package boo.bloodstone.bloodrp

object ActionMaster {
    private val actions: MutableMap<String, Action> = linkedMapOf()

    fun register(name: String, action: Action) {
        actions[name.lowercase()] = action
    }

    fun fromName(name: String): Action? {
        return actions[name.lowercase()]
    }
}
