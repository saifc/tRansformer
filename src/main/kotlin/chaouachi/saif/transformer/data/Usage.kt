package chaouachi.saif.transformer.data

class Usage(val module: String) {
    val files = mutableSetOf<String>()
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Usage

        if (module != other.module) return false

        return true
    }

    override fun hashCode(): Int {
        return module.hashCode()
    }

    override fun toString(): String {
        return "$module -> $files"
    }
}
