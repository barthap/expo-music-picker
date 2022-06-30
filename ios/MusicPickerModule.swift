import ExpoModulesCore
import MediaPlayer

/**
 Helper struct storing single picking operation context variables that have their own non-sharable state.
 */
struct PickingContext {
  let promise: Promise
  let options: MusicPickerOptions
  let pickerHandler: MusicPickerHandler
}

public class MusicPickerModule: Module {
  public func definition() -> ModuleDefinition {
    Name("ExpoMusicPicker")

    Constants([
      "PI": Double.pi
    ])

    // Defines event names that the module can send to JavaScript.
    Events("onChange")

    // Defines a JavaScript synchronous function that runs the native code on the JavaScript thread.
    Function("hello") {
      return "Hello world! 👋"
    }

    // Defines a JavaScript function that always returns a Promise and whose native code
    // is by default dispatched on the different thread than the JavaScript runtime runs on.
    AsyncFunction("setValueAsync") { (value: String) in
      // Send an event to JavaScript.
      self.sendEvent("onChange", [
        "value": value
      ])
    }
    
    AsyncFunction("launchMusicLibraryAsync") { (options: MusicPickerOptions, promise: Promise) in
      guard let currentViewController = self.appContext?.utilities?.currentViewController()
      else {
        promise.reject(MissingCurrentViewControllerException())
        return
      }
      
      // TODO: Support media type selection
      let picker = MPMediaPickerController(mediaTypes: .anyAudio)
      
      picker.allowsPickingMultipleItems = options.allowMultiple
      picker.showsCloudItems = options.showCloudItems
      picker.prompt = options.userPrompt
      
      currentViewController.present(picker, animated: true, completion: nil)
    }.runOnQueue(DispatchQueue.main)
  }
}
