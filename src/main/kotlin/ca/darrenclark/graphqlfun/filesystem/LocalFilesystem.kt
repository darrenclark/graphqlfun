package ca.darrenclark.graphqlfun.filesystem

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import java.io.File
import java.nio.file.FileSystem
import java.nio.file.FileSystems
import java.nio.file.PathMatcher

class LocalFilesystem(private val root: String): Filesystem {
  override fun listFiles(glob: String): Flow<String> = flow {
    val matcher = FileSystems.getDefault().getPathMatcher("glob:$glob")

    val rootFile = File(root)

    for (f in rootFile.walk()) {
      if (matcher.matches(f.toPath())) {
        emit(f.toRelativeString(rootFile))
      }
    }
  }

  override suspend fun getFileContents(file: String): String = File(root, file).readText()
}
