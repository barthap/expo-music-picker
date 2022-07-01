import ExpoModulesCore

internal class MissingCurrentViewControllerException: Exception {
  override var reason: String {
    "Cannot determine currently presented view controller"
  }
}

internal class UnavailableOnSimulatorException: Exception {
  override var reason: String {
    "Music Library not available on iOS Simulator. Try on real device"
  }
}
