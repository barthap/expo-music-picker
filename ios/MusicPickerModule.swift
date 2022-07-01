import ExpoModulesCore
import MediaPlayer

/**
 Helper struct storing single picking operation context variables that have their own non-sharable state.
 */
struct PickingContext {
  let promise: Promise
  let options: MusicPickerOptions
  let pickerDelegate: MusicPickerDelegate
}

extension MPMediaItem {
  func toDictionary() -> [String: Any?] {
    let displayName = artist != nil ? "\(artist!) - \(title ?? "Untitled")" : (title ?? "Untitled")
    
    return [
      "id": persistentID,
      "uri": assetURL?.absoluteString,
      "artist": artist,
      "title": title,
      "album": albumTitle,
      "displayName": displayName,
      "durationSeconds": playbackDuration
    ]
  }
}

public class MusicPickerModule: Module, MusicPickerResultHandler {
  private var currentPickingContext: PickingContext? = nil
  
  public func definition() -> ModuleDefinition {
    Name("ExpoMusicPicker")
    
    OnCreate {
      self.appContext?.permissions?.register([
        MusicLibraryPermissionRequester()
      ])
    }
    
    AsyncFunction("getPermissionsAsync") { (promise: Promise) -> Void in
      guard let permissions = self.appContext?.permissions else {
        return promise.reject(PermissionsModuleNotFoundException())
      }
      permissions.getPermissionUsingRequesterClass(MusicLibraryPermissionRequester.self,
                                   resolve: promise.resolver,
                                   reject: promise.legacyRejecter)
    }
    
    AsyncFunction("requestPermissionsAsync") { (promise: Promise) -> Void in
      guard let permissions = self.appContext?.permissions else {
        return promise.reject(PermissionsModuleNotFoundException())
      }
      permissions.askForPermission(usingRequesterClass: MusicLibraryPermissionRequester.self,
                                   resolve: promise.resolver,
                                   reject: promise.legacyRejecter)
    }
    
    AsyncFunction("openMusicLibraryAsync") { (options: MusicPickerOptions, promise: Promise) in
#if targetEnvironment(simulator)
      promise.reject(UnavailableOnSimulatorException())
      return
#else
      self.openMusicPicker(options: options, promise: promise)
#endif
    }.runOnQueue(DispatchQueue.main)
  }
  
  private func openMusicPicker(options: MusicPickerOptions, promise: Promise) {
    guard Thread.isMainThread else {
      fatalError("ExpoMusicPicker.openMusicPicker() must be run on main thread!")
    }
    
    guard let currentViewController = self.appContext?.utilities?.currentViewController()
    else {
      promise.reject(MissingCurrentViewControllerException())
      return
    }
    
    guard let permissions = self.appContext?.permissions else {
      return promise.reject(PermissionsModuleNotFoundException())
    }
    
    guard permissions.hasGrantedPermission(usingRequesterClass: MusicLibraryPermissionRequester.self) else {
      promise.reject(MissingPermissionException())
      return
    }
    
    // TODO: Support media type selection
    let picker = MPMediaPickerController(mediaTypes: .anyAudio)
    
    picker.allowsPickingMultipleItems = options.allowMultipleSelection
    picker.showsCloudItems = options.showCloudItems
    picker.prompt = options.userPrompt
    
    let pickerDelegate = MusicPickerDelegate(mediaPickingResultHandler: self)
    let pickingContext = PickingContext(promise: promise, options: options, pickerDelegate: pickerDelegate)
    
    picker.delegate = pickingContext.pickerDelegate
    picker.presentationController?.delegate = pickingContext.pickerDelegate
    
    self.currentPickingContext = pickingContext
    currentViewController.present(picker, animated: true, completion: nil)
  }
  
  // MARK: - MusicPickerResultHandler
  
  func didPickMedia(selectedMedia: MPMediaItemCollection) {
    guard let promise = self.currentPickingContext?.promise
    else {
      NSLog("Picking operation context has been lost.")
      return
    }

    // Cleanup the currently stored picking context
    self.currentPickingContext = nil
    
    let results = selectedMedia.items.map { $0.toDictionary() }
    promise.resolve([
      "cancelled": false,
      "items": results
    ])
  }
  
  func didCancelPicking() {
    self.currentPickingContext?.promise.resolve(["cancelled": true])
    self.currentPickingContext = nil
  }
}
