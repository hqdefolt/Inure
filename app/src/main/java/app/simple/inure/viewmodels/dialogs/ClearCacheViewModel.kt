package app.simple.inure.viewmodels.dialogs

import android.app.Application
import android.content.Context
import android.content.pm.IPackageDataObserver
import android.content.pm.PackageInfo
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import app.simple.inure.apk.utils.PackageUtils.isSystemApp
import app.simple.inure.constants.Misc
import app.simple.inure.constants.Warnings
import app.simple.inure.exceptions.InureShellException
import app.simple.inure.extensions.viewmodels.RootViewModel
import com.topjohnwu.superuser.Shell
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.io.File
import java.util.*

class ClearCacheViewModel(application: Application, val packageInfo: PackageInfo) : RootViewModel(application) {
    private val result: MutableLiveData<String> by lazy {
        MutableLiveData<String>()
    }

    private val success: MutableLiveData<String> by lazy {
        MutableLiveData<String>().also {
            if (applicationContext().applicationInfo.isSystemApp()) {
                applicationContext().clearCache(packageInfo.packageName)
            } else {
                initShell()
            }
        }
    }

    fun getResults(): LiveData<String> {
        return result
    }

    fun getSuccessStatus(): LiveData<String> {
        return success
    }

    private fun runCommand() {
        viewModelScope.launch(Dispatchers.IO) {
            delay(Misc.delay)

            kotlin.runCatching {
                Shell.cmd(getCommand()).submit { shellResult ->
                    kotlin.runCatching {
                        for (i in shellResult.out) {
                            result.postValue("\n" + i)
                            if (i.contains("Exception") || i.contains("not exist")) {
                                throw InureShellException("Execution Failed...")
                            }
                        }
                    }.onSuccess {
                        if (shellResult.isSuccess) {
                            success.postValue("Done")
                        } else {
                            success.postValue("Failed")
                        }
                    }.getOrElse {
                        it.printStackTrace()
                        result.postValue("\n" + it.message!!)
                        if (shellResult.isSuccess) {
                            success.postValue("Done")
                        } else {
                            success.postValue("Failed")
                        }
                    }
                }
            }.onFailure {
                it.printStackTrace()
                result.postValue("\n" + it.message!!)
                success.postValue("Failed")
            }.getOrElse {
                it.printStackTrace()
                result.postValue("\n" + it.message!!)
                success.postValue("Failed")
            }
        }
    }

    private fun getCommand(): String {
        //        return "rm -r -v /data/data/${packageInfo.packageName}/cache " +
        //                "& rm -r -v /data/data/${packageInfo.packageName}/app_cache " +
        //                "& rm -r -v /data/data/${packageInfo.packageName}/app_texture " +
        //                "& rm -r -v /data/data/${packageInfo.packageName}/app_webview " +
        //                "& rm -r -v /data/data/${packageInfo.packageName}/code_cache" +
        //                "& rm -r -v /data/data/${packageInfo.packageName}/files"

        val packageContext: Context = applicationContext().createPackageContext(packageInfo.packageName, 0)
        val directories: MutableList<File?> = ArrayList()
        directories.add(packageContext.cacheDir)
        Collections.addAll(directories, *packageContext.externalCacheDirs)
        val command = StringBuilder("rm -rf")
        for (directory in directories) {
            Log.d("ClearCacheViewModel", "getCommand: ${directory?.absolutePath}")
            command.append(" \"" + directory?.absolutePath.toString() + "\"")
        }

        return command.toString()
    }

    override fun onShellCreated(shell: Shell?) {
        runCommand()
    }

    override fun onShellDenied() {
        warning.postValue(Warnings.getInureWarning01())
        success.postValue("Failed")
    }

    /**
     * Clear cache of installed app using Android APIs
     */
    private fun Context.clearCache(packageName: String) {
        val pm = packageManager
        val method = pm.javaClass.getMethod("freeStorageAndNotify", Long::class.javaPrimitiveType, IPackageDataObserver::class.java)
        method.invoke(pm, Long.MAX_VALUE, object : IPackageDataObserver.Stub() {
            override fun onRemoveCompleted(packageName: String, succeeded: Boolean) {
                if (succeeded) {
                    success.postValue("Done")
                } else {
                    success.postValue("Failed")
                }
            }
        })
    }
}