package app.toarbit.flutter_screen

import android.app.Activity
import android.provider.Settings
import android.view.WindowManager
import androidx.annotation.NonNull;

import io.flutter.embedding.engine.plugins.FlutterPlugin
import io.flutter.embedding.engine.plugins.activity.ActivityAware
import io.flutter.embedding.engine.plugins.activity.ActivityPluginBinding
import io.flutter.plugin.common.MethodCall
import io.flutter.plugin.common.MethodChannel
import io.flutter.plugin.common.MethodChannel.MethodCallHandler
import io.flutter.plugin.common.MethodChannel.Result
import io.flutter.plugin.common.PluginRegistry.Registrar

/** FlutterScreenPlugin */
class FlutterScreenPlugin: FlutterPlugin, MethodCallHandler, ActivityAware {
  /// The MethodChannel that will the communication between Flutter and native Android
  ///
  /// This local reference serves to register the plugin with the Flutter Engine and unregister it
  /// when the Flutter Engine is detached from the Activity
  private lateinit var channel : MethodChannel

  override fun onAttachedToEngine(@NonNull flutterPluginBinding: FlutterPlugin.FlutterPluginBinding) {
    channel = MethodChannel(flutterPluginBinding.binaryMessenger, "app.toarbit.flutter_screen")
    channel.setMethodCallHandler(this);
  }

  // This static function is optional and equivalent to onAttachedToEngine. It supports the old
  // pre-Flutter-1.12 Android projects. You are encouraged to continue supporting
  // plugin registration via this function while apps migrate to use the new Android APIs
  // post-flutter-1.12 via https://flutter.dev/go/android-project-migration.
  //
  // It is encouraged to share logic between onAttachedToEngine and registerWith to keep
  // them functionally equivalent. Only one of onAttachedToEngine or registerWith will be called
  // depending on the user's project. onAttachedToEngine or registerWith must both be defined
  // in the same class.
  companion object {
    @JvmStatic
    fun registerWith(registrar: Registrar) {
      val channel = MethodChannel(registrar.messenger(), "app.toarbit.flutter_screen")
      channel.setMethodCallHandler(FlutterScreenPlugin())
    }
  }

  override fun onMethodCall(@NonNull call: MethodCall, @NonNull result: Result) {
    when (call.method) {
      "brightness" -> result.success(brightness)
      "setBrightness" -> {
        val b: Float = call.argument<Double>("brightness")?.toFloat() ?: -1F
        activity.let {
          if (it == null) return result.error("-1", null, null)
          it.window.attributes = it.window.attributes.apply {
            screenBrightness = if (brightness > 1.0 || brightness < 0) -1F else b
          }
        }
      }
      "isKeptOn" -> {
        val flags = activity?.window?.attributes?.flags ?: return result.error("-1", null, null)
        result.success(flags.and(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON) != 0)
      }
      "keepOn" -> {
        val on = call.argument<Boolean>("on") ?: false

        activity.let {
          if (it == null) return result.error("-1", null, null)

          if (on) {
            println("Keeping screen **on**")
            it.window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
          } else {
            println("Keeping screen-on **off**")
            it.window.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
          }
          result.success(null)
        }
      }
      else -> result.notImplemented()
    }
  }

  override fun onDetachedFromEngine(@NonNull binding: FlutterPlugin.FlutterPluginBinding) {
    channel.setMethodCallHandler(null)
  }

  private var activity: Activity? = null

  override fun onAttachedToActivity(binding: ActivityPluginBinding) { activity = binding.activity }
  override fun onDetachedFromActivity() { activity = null }
  override fun onDetachedFromActivityForConfigChanges() { activity = null }
  override fun onReattachedToActivityForConfigChanges(binding: ActivityPluginBinding) { activity = binding.activity }

  private val brightness: Float
    get() {
      val result = activity?.window?.attributes?.screenBrightness ?: -1F
      if (result < 0F) { // the application is using the system brightness
        return try {
          Settings.System.getInt(activity!!.contentResolver, Settings.System.SCREEN_BRIGHTNESS) / 255F
        } catch (e: Settings.SettingNotFoundException) {
          e.printStackTrace()
          1F
        }
      }
      return result
    }
}
