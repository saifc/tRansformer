package chaouachi.saif.transformer.subcommands

import chaouachi.saif.transformer.PackageNameFinder
import chaouachi.saif.transformer.ResourceType
import chaouachi.saif.transformer.subcommands.base.Command
import chaouachi.saif.transformer.subcommands.base.RefactorMoveCommand

class RefactorDrawables(
    projectDir: String,
    baseModule: String,
    packageNameFinder : PackageNameFinder
) : Command by RefactorMoveCommand(
    ResourceType.drawable,
    projectDir,
    baseModule,
    packageNameFinder
)