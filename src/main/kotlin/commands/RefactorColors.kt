package commands

import PackageNameFinder
import ResourceType
import Usage
import commands.base.RefactorMoveCommand
import commands.base.RefactorRemoveAndAppend
import java.io.File

class RefactorColors(
    projectDir: String,
    packageNameFinder: PackageNameFinder,
    baseModule: String,
    valuesDirs: Sequence<File>
) {
    private val refactorMoveCommand = RefactorMoveCommand(ResourceType.color, projectDir, packageNameFinder, baseModule)
    private val refactorRemoveAndAppend =
        RefactorRemoveAndAppend(ResourceType.color, projectDir, packageNameFinder, baseModule, valuesDirs)

    operator fun invoke(resources: Map<String, MutableList<Usage>>) {

        refactorMoveCommand.invoke(resources)

        refactorRemoveAndAppend.invoke(resources)
    }
}