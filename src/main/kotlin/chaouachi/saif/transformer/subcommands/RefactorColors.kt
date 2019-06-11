package chaouachi.saif.transformer.subcommands

import chaouachi.saif.transformer.PackageNameFinder
import chaouachi.saif.transformer.ResourceType
import chaouachi.saif.transformer.data.Usage
import chaouachi.saif.transformer.subcommands.base.Command
import chaouachi.saif.transformer.subcommands.base.RefactorMoveCommand
import chaouachi.saif.transformer.subcommands.base.RefactorRemoveAndAppend
import java.io.File

class RefactorColors(
    projectDir: String,
    baseModule: String,
    valuesDirs: Sequence<File>,
    packageNameFinder: PackageNameFinder
) : Command {
    private val refactorMoveCommand = RefactorMoveCommand(
        ResourceType.color,
        projectDir,
        baseModule,
        packageNameFinder
    )
    private val refactorRemoveAndAppend =
        RefactorRemoveAndAppend(
            ResourceType.color,
            projectDir,
            baseModule,
            valuesDirs,
            packageNameFinder
        )

    override fun invoke(resources: Map<String, MutableList<Usage>>) {

        refactorMoveCommand.invoke(resources)

        refactorRemoveAndAppend.invoke(resources)
    }
}