package commands

import PackageNameFinder
import ResourceType
import commands.base.RefactorRemoveAndAppend
import java.io.File

class RefactorStrings(
    projectDir: String,
    packageNameFinder: PackageNameFinder,
    baseModule: String,
    valuesDirs: Sequence<File>
) :
    RefactorRemoveAndAppend(ResourceType.string, projectDir, packageNameFinder, baseModule, valuesDirs)