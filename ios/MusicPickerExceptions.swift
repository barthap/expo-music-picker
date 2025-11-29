import ExpoModulesCore

internal final class MissingCurrentViewControllerException: Exception {
  override var reason: String {
    "Cannot determine currently presented view controller"
  }
}

internal final class UnavailableOnSimulatorException: Exception {
  override var reason: String {
    "Music Library not available on iOS Simulator. Try on real device"
  }
}

internal final class MissingPermissionException: Exception {
  override var reason: String {
    "Missing music library permission"
  }
}

internal final class PermissionsModuleNotFoundException: Exception {
  override var reason: String {
    "Permissions module not found. Are you sure that Expo modules are properly linked?"
  }
}
