package commands

import PackageNameFinder
import ResourceType
import Usage
import commands.base.Command
import commands.base.RefactorMoveCommand
import commands.base.RefactorRemoveAndAppend
import java.io.File

class RefactorColors(
    projectDir: String,
    baseModule: String,
    valuesDirs: Sequence<File>,
    packageNameFinder: PackageNameFinder
) : Command {
    private val refactorMoveCommand = RefactorMoveCommand(ResourceType.color, projectDir, baseModule, packageNameFinder)
    private val refactorRemoveAndAppend =
        RefactorRemoveAndAppend(ResourceType.color, projectDir, baseModule, valuesDirs, packageNameFinder)

    override fun invoke(resources: Map<String, MutableList<Usage>>) {

        refactorMoveCommand.invoke(resources)

        refactorRemoveAndAppend.invoke(resources)
    }
}