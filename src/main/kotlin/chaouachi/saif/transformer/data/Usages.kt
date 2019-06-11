package chaouachi.saif.transformer.data

class Usages(private val baseModule: String) {

    private val dimens = mutableMapOf<String, MutableList<Usage>>()
    private val drawables = mutableMapOf<String, MutableList<Usage>>()
    private val colors = mutableMapOf<String, MutableList<Usage>>()
    private val strings = mutableMapOf<String, MutableList<Usage>>()
    private val raws = mutableMapOf<String, MutableList<Usage>>()

    fun putDimension(key: String, module: String, file: String) {
        putElement(key, module, file, dimens)
    }

    fun putDrawable(key: String, module: String, file: String) {
        putElement(key, module, file, drawables)
    }

    fun putColor(key: String, module: String, file: String) {
        putElement(key, module, file, colors)
    }

    fun putString(key: String, module: String, file: String) {
        putElement(key, module, file, strings)
    }

    fun putRaw(key: String, module: String, file: String) {
        putElement(key, module, file, raws)
    }

    private fun putElement(key: String, module: String, file: String, map: MutableMap<String, MutableList<Usage>>) {
        var value = map[key]
        var usage = Usage(module)

        if (value == null) {
            value = mutableListOf(usage)
            map[key] = value
        } else {
            val index = value.indexOf(usage)
            if (index != -1) {
                usage = value[index]
            } else {
                value.add(usage)
            }
        }

        usage.files.add(file)
    }

    fun getMonoModuleStrings(): Map<String, MutableList<Usage>> {
        return getMonoModuleResourcesPerType(strings)
    }

    fun getMonoModuleDimensions(): Map<String, MutableList<Usage>> {
        return getMonoModuleResourcesPerType(dimens)
    }

    fun getMonoModuleDrawables(): Map<String, MutableList<Usage>> {
        return getMonoModuleResourcesPerType(drawables)
    }

    fun getMonoModuleRaws(): Map<String, MutableList<Usage>> {
        return getMonoModuleResourcesPerType(raws)
    }

    fun getMonoModuleColors(): Map<String, MutableList<Usage>> {
        return getMonoModuleResourcesPerType(colors)
    }

    private fun getMonoModuleResourcesPerType(map: Map<String, MutableList<Usage>>) =
        map.filter { it.value.size == 1 && it.value[0].module != baseModule }
}