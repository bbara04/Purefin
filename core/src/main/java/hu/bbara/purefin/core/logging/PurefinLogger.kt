package hu.bbara.purefin.core.logging

import android.content.Context
import android.content.pm.ApplicationInfo
import android.os.Process
import android.util.Log
import timber.log.Timber
import java.io.File
import java.io.FileOutputStream
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import kotlin.system.exitProcess

object PurefinLogger {
    private val lock = Any()
    private var logStore: LogFileStore? = null
    private var initialized = false

    fun initialize(context: Context) {
        synchronized(lock) {
            if (initialized) {
                return
            }

            val store = LogFileStore(File(context.filesDir, LOG_DIRECTORY))
            logStore = store
            val isDebuggable = context.applicationInfo.flags and ApplicationInfo.FLAG_DEBUGGABLE != 0
            if (isDebuggable) {
                Timber.plant(Timber.DebugTree())
            }
            Timber.plant(FileLogTree(store))
            installUncaughtExceptionHandler(store)
            initialized = true
        }
    }

    fun prepareFilesForUpload(): List<UploadLogFile> {
        return logStore?.prepareFilesForUpload().orEmpty()
    }

    fun deleteUploadedFile(uploadLogFile: UploadLogFile) {
        if (uploadLogFile.file.exists() && !uploadLogFile.file.delete()) {
            error("Uploaded log file could not be deleted: ${uploadLogFile.name}")
        }
    }

    private fun installUncaughtExceptionHandler(store: LogFileStore) {
        val previousHandler = Thread.getDefaultUncaughtExceptionHandler()
        Thread.setDefaultUncaughtExceptionHandler { thread, throwable ->
            try {
                store.writeCrash(thread, throwable)
            } catch (logError: Exception) {
                Log.e("PurefinLogger", "Failed to write uncaught exception to log file", logError)
            } finally {
                if (previousHandler != null) {
                    previousHandler.uncaughtException(thread, throwable)
                } else {
                    Process.killProcess(Process.myPid())
                    exitProcess(2)
                }
            }
        }
    }

    private const val LOG_DIRECTORY = "purefin-logs"
}

data class UploadLogFile(
    val name: String,
    val data: String,
    internal val file: File,
)

private class FileLogTree(
    private val logStore: LogFileStore,
) : Timber.Tree() {
    override fun log(priority: Int, tag: String?, message: String, t: Throwable?) {
        logStore.write(priority = priority, tag = tag, message = message, throwable = t)
    }
}

private class LogFileStore(
    private val directory: File,
) {
    private val activeFile = File(directory, ACTIVE_LOG_FILE)
    private val timestampFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS")
    private val uploadNameFormatter = DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss")

    @Synchronized
    fun write(priority: Int, tag: String?, message: String, throwable: Throwable?) {
        directory.mkdirs()
        val entry = buildEntry(priority, tag, message, throwable).toByteArray(Charsets.UTF_8)
        if (activeFile.exists() && activeFile.length() + entry.size > MAX_LOG_BYTES) {
            rotate()
        }
        FileOutputStream(activeFile, true).use { output ->
            output.write(entry)
            output.flush()
        }
    }

    @Synchronized
    fun writeCrash(thread: Thread, throwable: Throwable) {
        write(
            priority = Log.ERROR,
            tag = "UncaughtException",
            message = "Uncaught exception in ${thread.name}",
            throwable = throwable,
        )
    }

    @Synchronized
    fun flush() {
        directory.mkdirs()
    }

    @Synchronized
    fun prepareFilesForUpload(): List<UploadLogFile> {
        directory.mkdirs()
        val candidates = uploadFiles() + rotatedFilesOldestFirst() + listOfNotNull(activeFile.takeIf { it.isLogFile() })
        if (candidates.isEmpty()) {
            return emptyList()
        }

        val preparedFiles = renameForUpload(candidates)
        return preparedFiles.map { file ->
            UploadLogFile(
                name = file.name,
                data = file.readText(Charsets.UTF_8),
                file = file,
            )
        }
    }

    private fun buildEntry(priority: Int, tag: String?, message: String, throwable: Throwable?): String {
        val entry = buildString {
            append(timestampFormatter.format(LocalDateTime.now()))
            append(' ')
            append(priorityLabel(priority))
            append('/')
            append(tag ?: "Purefin")
            append(" [")
            append(Thread.currentThread().name)
            append("] ")
            append(message)
            append('\n')
            if (throwable != null) {
                append(Log.getStackTraceString(throwable))
                append('\n')
            }
        }
        return entry.truncateForLog()
    }

    private fun priorityLabel(priority: Int): String = when (priority) {
        Log.VERBOSE -> "V"
        Log.DEBUG -> "D"
        Log.INFO -> "I"
        Log.WARN -> "W"
        Log.ERROR -> "E"
        Log.ASSERT -> "A"
        else -> priority.toString()
    }

    private fun rotate() {
        File(directory, "purefin.${MAX_LOG_FILES - 1}.log").delete()
        for (index in MAX_LOG_FILES - 2 downTo 1) {
            val file = File(directory, "purefin.$index.log")
            if (file.exists()) {
                file.renameTo(File(directory, "purefin.${index + 1}.log"))
            }
        }
        if (activeFile.exists()) {
            activeFile.renameTo(File(directory, "purefin.1.log"))
        }
    }

    private fun renameForUpload(files: List<File>): List<File> {
        val uploadTimestamp = uploadNameFormatter.format(LocalDateTime.now())
        val tempFiles = files.mapIndexed { index, file ->
            val tempFile = File(directory, ".upload-$uploadTimestamp-$index.tmp")
            tempFile.delete()
            if (!file.renameTo(tempFile)) {
                error("Log file could not be prepared for upload: ${file.name}")
            }
            tempFile
        }
        return tempFiles.mapIndexed { index, file ->
            val uploadName = "${uploadTimestamp}_${(index + 1).toString().padStart(3, '0')}.log"
            val uploadFile = File(directory, uploadName)
            uploadFile.delete()
            if (!file.renameTo(uploadFile)) {
                error("Log file could not be named for upload: $uploadName")
            }
            uploadFile
        }
    }

    private fun String.truncateForLog(): String {
        if (length <= MAX_LOG_ENTRY_CHARS) {
            return this
        }
        val omittedChars = length - MAX_LOG_ENTRY_CHARS
        return take(MAX_LOG_ENTRY_CHARS) + "\n... truncated $omittedChars more chars\n"
    }

    private fun uploadFiles(): List<File> {
        return directory.listFiles()
            .orEmpty()
            .filter { it.isFile && UPLOAD_LOG_FILE_REGEX.matches(it.name) }
            .sortedWith(compareBy<File> { it.lastModified() }.thenBy { it.name })
    }

    private fun rotatedFilesOldestFirst(): List<File> {
        return (MAX_LOG_FILES - 1 downTo 1)
            .map { File(directory, "purefin.$it.log") }
            .filter { it.isLogFile() }
    }

    private fun File.isLogFile(): Boolean {
        return isFile && length() > 0L
    }

    private companion object {
        const val ACTIVE_LOG_FILE = "purefin.log"
        const val MAX_LOG_BYTES = 5 * 1024 * 1024
        const val MAX_LOG_ENTRY_CHARS = 4_000
        const val MAX_LOG_FILES = 4
        val UPLOAD_LOG_FILE_REGEX = Regex("""\d{8}_\d{6}(?:_\d{3})?\.log""")
    }
}
