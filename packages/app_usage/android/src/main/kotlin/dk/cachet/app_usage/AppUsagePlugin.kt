package dk.cachet.app_usage

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.provider.Settings
import android.util.Log
import androidx.annotation.NonNull
import io.flutter.embedding.engine.plugins.FlutterPlugin
import io.flutter.embedding.engine.plugins.activity.ActivityAware
import io.flutter.embedding.engine.plugins.activity.ActivityPluginBinding
import io.flutter.plugin.common.MethodCall
import io.flutter.plugin.common.MethodChannel
import io.flutter.plugin.common.MethodChannel.MethodCallHandler
import io.flutter.plugin.common.MethodChannel.Result
import io.flutter.plugin.common.PluginRegistry

/** AppUsagePlugin */
public class AppUsagePlugin : FlutterPlugin, MethodCallHandler, ActivityAware {
    /// The MethodChannel that will the communication between Flutter and native Android
    ///
    /// This local reference serves to register the plugin with the Flutter Engine and unregister it
    /// when the Flutter Engine is detached from the Activity
    private lateinit var channel: MethodChannel
    private lateinit var context: Context
    private val methodChannelName = "app_usage.methodChannel"

    override fun onAttachedToEngine(@NonNull flutterPluginBinding: FlutterPlugin.FlutterPluginBinding) {
        channel = MethodChannel(flutterPluginBinding.binaryMessenger, methodChannelName)
        channel.setMethodCallHandler(this);
        context = flutterPluginBinding.applicationContext;
    }

    override fun onMethodCall(@NonNull call: MethodCall, @NonNull result: Result) {
        /// Verify that the correct method was called
        if (call.method == "getUsage") {
            getUsage(call, result)
        }
        /// If an incorrect method was called, throw an error
        else {
            result.notImplemented()
        }
    }

    fun getUsage(@NonNull call: MethodCall, @NonNull result: Result) {
        // Parse parameters, i.e. start- and end-date
        val start: Long? = call.argument("start")
        val end: Long? = call.argument("end")

        /// Query the Usage API
        val usage = Stats.getUsageMap(context, start!!, end!!)

        /// Return the result
        result.success(usage)
    }


    override fun onDetachedFromEngine(@NonNull binding: FlutterPlugin.FlutterPluginBinding) {
        channel.setMethodCallHandler(null)
    }

    fun handlePermissions(activity: Activity) {
        /// If permissions have not yet been given, show the permission screen
        if (Stats.permissionRequired(context)) {
            val intent = Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS)
            activity.startActivity(intent)
        }
    }

    override fun onDetachedFromActivity() {
    }

    override fun onReattachedToActivityForConfigChanges(binding: ActivityPluginBinding) {
        handlePermissions(binding.activity)
    }

    override fun onAttachedToActivity(binding: ActivityPluginBinding) {
        handlePermissions(binding.activity)
    }

    override fun onDetachedFromActivityForConfigChanges() {
    }
}
