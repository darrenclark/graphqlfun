package ca.darrenclark.graphqlfun.filesystem

import kotlinx.coroutines.flow.Flow

interface Filesystem {
  fun listFiles(glob: String): Flow<String>

  suspend fun getFileContents(file: String): String
}
