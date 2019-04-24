package distributed.util

import java.io.File

object LoadUtil {
    fun getFreeSpacePercentage(): Double {
        val freeSpace = File("/").freeSpace
        val totalSpace = File("/").totalSpace
        print(totalSpace/1024)
        return freeSpace.toDouble() / totalSpace.toDouble()
    }
}