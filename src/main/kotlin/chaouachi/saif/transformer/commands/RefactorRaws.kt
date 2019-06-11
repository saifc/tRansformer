package chaouachi.saif.transformer.commands

import chaouachi.saif.transformer.PackageNameFinder
import chaouachi.saif.transformer.ResourceType
import chaouachi.saif.transformer.commands.base.Command
import chaouachi.saif.transformer.commands.base.RefactorMoveCommand

class RefactorRaws(
    projectDir: String,
    baseModule: String,
    packageNameFinder: PackageNameFinder
) : Command by RefactorMoveCommand(
    ResourceType.raw,
    projectDir,
    baseModule,
    packageNameFinder
)