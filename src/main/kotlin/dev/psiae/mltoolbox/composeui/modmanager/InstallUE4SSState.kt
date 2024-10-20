package dev.psiae.mltoolbox.composeui.modmanager

import androidx.compose.runtime.*
import dev.psiae.mltoolbox.composeui.core.ComposeUIContext
import dev.psiae.mltoolbox.composeui.core.locals.LocalComposeUIContext
import dev.psiae.mltoolbox.java.jFile
import io.github.vinceglb.filekit.core.FileKit
import io.github.vinceglb.filekit.core.FileKitPlatformSettings
import io.github.vinceglb.filekit.core.PickerMode
import io.github.vinceglb.filekit.core.PickerType
import kotlinx.coroutines.*
import okio.FileNotFoundException
import okio.IOException
import java.io.RandomAccessFile
import net.lingala.zip4j.exception.ZipException as ZipException4j
import net.lingala.zip4j.ZipFile as ZipFile4j

@Composable
fun rememberInstallUE4SSState(
    modManagerScreenState: ModManagerScreenState
): InstallUE4SSState {
    val composeUIContext = LocalComposeUIContext.current
    val state = remember(modManagerScreenState) {
        InstallUE4SSState(modManagerScreenState, composeUIContext)
    }
    DisposableEffect(state) {
        state.stateEnter()
        onDispose { state.stateExit() }
    }
    return state
}

class InstallUE4SSState(
    val modManagerScreenState: ModManagerScreenState,
    val uiContext: ComposeUIContext
) {

    private val lifetime = SupervisorJob()
    private var _coroutineScope: CoroutineScope? = null

    private val coroutineScope
        get() = requireNotNull(_coroutineScope) {
            "_coroutineScope is null"
        }

    private var pickUE4SSArchiveCompletion: Deferred<jFile?>? = null

    var selectedUE4SSArchive by mutableStateOf<jFile?>(null)
        private set

    var statusMessage by mutableStateOf<String?>(null)
        private set

    var isLoading by mutableStateOf(false)
        private set

    var isInstalledSuccessfully by mutableStateOf(false)
        private set

    var isLastSelectedArchiveInvalid by mutableStateOf(false)
        private set

    var isInvalidGameDirectory by mutableStateOf(false)
        private set

    fun stateEnter() {
        _coroutineScope = CoroutineScope(uiContext.dispatchContext.mainDispatcher)

        init()
    }

    fun stateExit() {
        lifetime.cancel()
        coroutineScope.cancel()
    }

    private fun init() {

    }

    fun pickUE4SSArchive(awtWindow: java.awt.Window) {
        coroutineScope.launch {
            if (pickUE4SSArchiveCompletion?.isActive != true) {
                isLastSelectedArchiveInvalid = false
                isInvalidGameDirectory = false
                isInstalledSuccessfully = false
                statusMessage = null
                async {
                    val pick = FileKit.pickFile(
                        type = PickerType.File(listOf("zip")),
                        mode = PickerMode.Single,
                        title = "Select downloaded UE4SS archive (*.zip)",
                        initialDirectory = null,
                        platformSettings = FileKitPlatformSettings(
                            parentWindow = awtWindow
                        )
                    )
                    if (pick == null) {
                        return@async null
                    }
                    if (!processPickedFile(pick.file)) {
                        return@async null
                    }
                    pick.file
                }.also {
                    pickUE4SSArchiveCompletion = it
                    it.await()
                }
            }
        }
    }

    fun userDropUE4SSArchive(file: jFile) {
        coroutineScope.launch {
            if (pickUE4SSArchiveCompletion?.isActive != true) {
                isLastSelectedArchiveInvalid = false
                isInvalidGameDirectory = false
                isInstalledSuccessfully = false
                statusMessage = null
                async {
                    processPickedFile(file)
                    file
                }.also {
                    pickUE4SSArchiveCompletion = it
                    it.await()
                }
            }
        }
    }

    suspend fun processPickedFile(file: jFile): Boolean {
        isLoading = true
        isLastSelectedArchiveInvalid = false
        isInvalidGameDirectory = false
        isInstalledSuccessfully = false
        statusMessage = "..."
        withContext(Dispatchers.IO) {
            statusMessage = "preparing ..."

            val userDir = jFile(System.getProperty("user.dir"))
            val installDir = jFile(userDir.absolutePath + "\\temp\\ue4ss_install")
            run {
                var lockedFile: jFile? = null
                if (installDir.exists() && !run {
                    var open = true
                    open = installDir.walkBottomUp().all { f ->
                        if (f.isFile) {
                            if (f.canWrite()) {
                                var ch: RandomAccessFile? = null
                                try {
                                    ch = RandomAccessFile(f, "rw")
                                    ch.channel.lock()
                                } catch (ex: IOException) {
                                    open = false
                                    lockedFile = f
                                } finally {
                                    ch?.close()
                                }
                            } else {
                                // TODO
                            }
                        }
                        open
                    }
                    open
                }) {
                    isLoading = false
                    isInvalidGameDirectory = true
                    statusMessage = "unable to lock ue4ss_install directory from app directory, ${lockedFile?.let {
                        it.absolutePath
                            .drop(it.absolutePath.indexOf(userDir.absolutePath)+userDir.absolutePath.length)
                            .replace(' ', '\u00A0')
                    }} might be opened in another process"
                    return@withContext
                }
            }
            if (installDir.exists()) installDir.walkBottomUp().forEach { f ->
                if (!f.delete()) {
                    isLoading = false
                    isInvalidGameDirectory = true
                    statusMessage = "unable to delete ${f.let {
                        it.absolutePath
                            .drop(it.absolutePath.indexOf(userDir.absolutePath)+userDir.absolutePath.length)
                            .replace(' ', '\u00A0')
                    }} from app directory, it might be opened in another process"
                    return@withContext
                }
            }


            statusMessage = "extracting ..."
            runCatching {
                ZipFile4j(file).use { zipFile ->
                    zipFile.extractAll(System.getProperty("user.dir") + "\\temp\\ue4ss_install")
                }
            }.onFailure { ex ->
                if (ex is ZipException4j) {
                    isLoading = false
                    isLastSelectedArchiveInvalid = true
                    statusMessage = "unable to extract archive"
                    return@withContext
                }
                throw ex
            }

            statusMessage = "verifying ..."
            val dir = System.getProperty("user.dir") + "\\temp\\ue4ss_install"

            val dwmApiDll = jFile("$dir\\dwmapi.dll").exists()
            if (!dwmApiDll) {
                isLoading = false
                isLastSelectedArchiveInvalid = true
                statusMessage = "missing dwmapi.dll"
                return@withContext
            }
            val ue4ssDll = jFile("$dir\\ue4ss\\UE4SS.dll").exists()
            if (!ue4ssDll) {
                isLoading = false
                isLastSelectedArchiveInvalid = true
                statusMessage = "missing ue4ss\\UE4SS.dll"
                return@withContext
            }
            statusMessage = "preparing install ..."

            val gameDir = modManagerScreenState.gameBinaryFile?.parentFile
            if (gameDir == null || !gameDir.exists()) {
                isLoading = false
                isInvalidGameDirectory = true
                statusMessage = "missing game directory"
                return@withContext
            }
            runCatching {
                val gameDwmApi = jFile("$gameDir\\dwmapi.dll")
                if (gameDwmApi.exists() && !run {
                    var open = true
                    var ch: RandomAccessFile? = null
                    try {
                        ch = RandomAccessFile(gameDwmApi, "rw")
                        ch!!.channel.lock()
                    } catch (ex: IOException) {
                        open = false
                    } finally {
                        ch?.close()
                    }
                    open
                }) {
                    isLoading = false
                    isInvalidGameDirectory = true
                    statusMessage = "unable to lock dwmapi.dll from game directory, it might be opened in another process"
                    return@withContext
                }
                val ue4ssFolder = jFile("$gameDir\\ue4ss")
                var lockedFile: jFile? = null
                if (ue4ssFolder.exists() && !run {
                    var open = true
                    open = ue4ssFolder.walkBottomUp().all { f ->
                        if (f.isFile) {
                            if (f.canWrite()) {
                                var ch: RandomAccessFile? = null
                                try {
                                    ch = RandomAccessFile(f, "rw")
                                    ch!!.channel.lock()
                                } catch (ex: IOException) {
                                    open = false
                                    lockedFile = f
                                } finally {
                                    ch?.close()
                                }
                            } else {
                                // TODO
                            }
                        }
                        open
                    }
                    open
                }) {
                    isLoading = false
                    isInvalidGameDirectory = true
                    statusMessage = "unable to lock ue4ss folder from game directory, ${lockedFile?.let {
                        it.absolutePath
                            .drop(it.absolutePath.indexOf(gameDir.absolutePath)+gameDir.absolutePath.length)
                            .replace(' ', '\u00A0')
                    }} might be opened in another process"
                    return@withContext
                }
                if (gameDwmApi.exists() && !gameDwmApi.delete()) {
                    isLoading = false
                    isInvalidGameDirectory = true
                    statusMessage = "unable to delete dwmapi.dll from game directory, it might be opened in another process"
                    return@withContext
                }
                if (ue4ssFolder.exists()) ue4ssFolder.walkBottomUp().forEach { f ->
                    if (!f.delete()) {
                        isLoading = false
                        isInvalidGameDirectory = true
                        statusMessage = "unable to delete ${f.let { 
                            it.absolutePath
                                .drop(it.absolutePath.indexOf(gameDir.absolutePath)+gameDir.absolutePath.length)
                                .replace(' ', '\u00A0')
                        }} from game directory, it might be opened in another process"
                        return@withContext
                    }
                }
            }.onFailure { ex ->
                throw ex
            }
            statusMessage = "installing ..."
            runCatching {
                jFile(dir).copyRecursively(gameDir, true)
            }.onFailure { ex ->
                isLoading = false
                isInvalidGameDirectory = true
                statusMessage = when (ex) {
                    is FileNotFoundException -> {
                        "unable to copy recursively, source file is missing"
                    }
                    is FileAlreadyExistsException -> {
                        "unable to copy recursively, target file is not writeable"
                    }
                    is AccessDeniedException -> {
                        "unable to copy recursively, access denied"
                    }
                    is IOException -> {
                        "unable to copy recursively, IO error"
                    }
                    else -> throw ex
                }
                return@withContext
            }
            isLoading = false
            isInstalledSuccessfully = true
        }
        return false
    }


}