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
    valuesDirs: Sequence<File>
) : Command {
    private val refactorMoveCommand = RefactorMoveCommand(ResourceType.color, projectDir, PackageNameFinder, baseModule)
    private val refactorRemoveAndAppend =
        RefactorRemoveAndAppend(ResourceType.color, projectDir, PackageNameFinder, baseModule, valuesDirs)

    override fun invoke(resources: Map<String, MutableList<Usage>>) {

        refactorMoveCommand.invoke(resources)

        refactorRemoveAndAppend.invoke(resources)
    }
}