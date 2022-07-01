import ExpoModulesCore
import MediaPlayer

class MusicLibraryPermissionRequester: NSObject, EXPermissionsRequester {
  static func permissionType() -> String! {
    return "musicLibrary"
  }

  func requestPermissions(resolver resolve: EXPromiseResolveBlock!, rejecter reject: EXPromiseRejectBlock!) {
    MPMediaLibrary.requestAuthorization() { [weak self] _ in
      resolve(self?.getPermissions())
    }
  }

  func getPermissions() -> [AnyHashable : Any]! {
    var systemStatus: MPMediaLibraryAuthorizationStatus
    var status: EXPermissionStatus
    let musicLibraryUsageDescription = Bundle.main.object(forInfoDictionaryKey: "NSAppleMusicUsageDescription")
    if musicLibraryUsageDescription == nil {
      EXFatal(EXErrorWithMessage("""
        This app is missing 'NSAppleMusicUsageDescription' so music picker will fail. \
        Ensure that this key exists in app's Info.plist.
        """))
      systemStatus = MPMediaLibraryAuthorizationStatus.denied
    } else {
      systemStatus = MPMediaLibrary.authorizationStatus()
    }
    
    switch systemStatus {
    case .authorized:
      status = EXPermissionStatusGranted
    case .restricted,
          .denied:
      status = EXPermissionStatusDenied
    case .notDetermined:
      fallthrough
    @unknown default:
      status = EXPermissionStatusUndetermined
    }

    return [
      "status": status.rawValue
    ]
  }
}
