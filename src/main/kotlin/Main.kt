fun main(args: Array<String>) {

    val projectDir = if (args.isNotEmpty()) args[0] else "/Users/saif/potcommun_android"
    val baseModule = if (args.size > 1) args[1] else "core"

    Migrator(projectDir,baseModule).invoke()

}





