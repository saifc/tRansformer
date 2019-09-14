package dev.saifc.transformer.subcommands

import dev.saifc.transformer.PackageNameFinder
import dev.saifc.transformer.ResourceType
import dev.saifc.transformer.data.Usage
import dev.saifc.transformer.subcommands.base.Command
import dev.saifc.transformer.subcommands.base.RefactorMoveCommand
import dev.saifc.transformer.subcommands.base.RefactorRemoveAndAppend
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