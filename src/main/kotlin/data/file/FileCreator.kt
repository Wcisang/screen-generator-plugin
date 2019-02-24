package data.file

import data.repository.SettingsRepository
import data.repository.SourceRootRepository
import model.AndroidComponent
import model.FileType
import model.Settings
import util.toCamelCase

private const val LAYOUT_DIRECTORY = "layout"

interface FileCreator {

    fun createScreenFiles(packageName: String, screenName: String, androidComponent: AndroidComponent)
}

class FileCreatorImpl(private val settingsRepository: SettingsRepository,
                      private val sourceRootRepository: SourceRootRepository) : FileCreator {

    override fun createScreenFiles(packageName: String, screenName: String, androidComponent: AndroidComponent) {
        val codeSubdirectory = findCodeSubdirectory(packageName)
        val resourcesSubdirectory = findResourcesSubdirectory()
        settingsRepository.loadSettings().apply {
            val baseClass = getAndroidComponentBaseClass(androidComponent)
            screenElements.forEach {
                if (it.fileType == FileType.LAYOUT_XML) {
                    val file = File("${androidComponent.displayName.toLowerCase()}_${screenName.toCamelCase()}", it.body(screenName, packageName, androidComponent.displayName, baseClass), it.fileType)
                    resourcesSubdirectory.addFile(file)
                } else {
                    val file = File("$screenName${it.name}", it.body(screenName, packageName, androidComponent.displayName, baseClass), it.fileType)
                    codeSubdirectory.addFile(file)
                }
            }
        }
    }

    private fun findCodeSubdirectory(packageName: String): Directory = sourceRootRepository.findCodeSourceRoot().run {
        var subdirectory = directory
        packageName.split(".").forEach {
            subdirectory = subdirectory.findSubdirectory(it) ?: subdirectory.createSubdirectory(it)
        }
        return subdirectory
    }

    private fun findResourcesSubdirectory() = sourceRootRepository.findResourcesSourceRoot().directory.run {
        findSubdirectory(LAYOUT_DIRECTORY) ?: createSubdirectory(LAYOUT_DIRECTORY)
    }

    private fun Settings.getAndroidComponentBaseClass(androidComponent: AndroidComponent) = when (androidComponent) {
        AndroidComponent.ACTIVITY -> activityBaseClass
        AndroidComponent.FRAGMENT -> fragmentBaseClass
    }
}