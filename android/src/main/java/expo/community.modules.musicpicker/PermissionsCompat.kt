package expo.community.modules.musicpicker

import expo.modules.interfaces.permissions.Permissions
import expo.modules.kotlin.Promise

/**
 * Compatibility method that accepts [expo.modules.kotlin.Promise], but forward the logic to the
 * [Permissions.askForPermissionsWithPermissionsManager]
 *
 * TODO: Remove this when published expo-modules-core includes
 * [this commit](https://github.com/expo/expo/commit/1452202e6f8d73b0fc2e04f1a0a5cb5743997757)
 */
fun askForPermissionsWithPermissionsManager(
        permissionsManager: Permissions?,
        promise: Promise,
        vararg permissions: String?
) {
    Permissions.askForPermissionsWithPermissionsManager(permissionsManager, object : expo.modules.core.Promise {
        override fun resolve(value: Any?) {
            promise.resolve(value)
        }

        override fun reject(c: String?, m: String?, e: Throwable?) {
            promise.reject(c!!, m, e)
        }
    }, *permissions)
}

/**
 * Compatibility method that accepts expo.modules.kotlin.Promise, but forward the logic to the
 * [Permissions.getPermissionsWithPermissionsManager]
 *
 * TODO: Remove this when published expo-modules-core includes
 * [this commit](https://github.com/expo/expo/commit/1452202e6f8d73b0fc2e04f1a0a5cb5743997757)
 */
fun getPermissionsWithPermissionsManager(
        permissionsManager: Permissions?,
        promise: Promise,
        vararg permissions: String?
) {
    Permissions.getPermissionsWithPermissionsManager(permissionsManager, object : expo.modules.core.Promise {
        override fun resolve(value: Any?) {
            promise.resolve(value)
        }

        override fun reject(c: String?, m: String?, e: Throwable?) {
            promise.reject(c!!, m, e)
        }
    }, *permissions)
}