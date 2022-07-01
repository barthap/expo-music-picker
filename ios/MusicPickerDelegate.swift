import MediaPlayer
import UIKit

/**
 Protocol that describes scenarios we care about while the user is picking media.
 */
protocol MusicPickerResultHandler {
  func didPickMedia(selectedMedia: MPMediaItemCollection)
  func didCancelPicking()
}

internal class MusicPickerDelegate: NSObject,
                                    MPMediaPickerControllerDelegate,
                                    UIAdaptivePresentationControllerDelegate {
  private let resultHandler: MusicPickerResultHandler
  
  init(mediaPickingResultHandler: MusicPickerResultHandler) {
    self.resultHandler = mediaPickingResultHandler
  }
  
  // MARK: - Internal handlers
  
  private func handlePickedMedia(selectedMedia: MPMediaItemCollection) {
    self.resultHandler.didPickMedia(selectedMedia: selectedMedia)
  }
  
  private func handleCancelation() {
    self.resultHandler.didCancelPicking()
  }
  
  // MARK: - MPMediaPickerControllerDelegate methods
  
  public func mediaPicker(_ mediaPicker: MPMediaPickerController,
                            didPickMediaItems mediaItemCollection: MPMediaItemCollection) {
    DispatchQueue.main.async {
      mediaPicker.dismiss(animated: true) { [weak self] in
        self?.handlePickedMedia(selectedMedia: mediaItemCollection)
      }
    }
  }
  
  func mediaPickerDidCancel(_ mediaPicker: MPMediaPickerController) {
    DispatchQueue.main.async {
      mediaPicker.dismiss(animated: true, completion: { [weak self] in
        self?.handleCancelation()
      })
    }
  }
  
  // MARK: - UIAdaptivePresentationControllerDelegate methods
  
  func presentationControllerDidDismiss(_ presentationController: UIPresentationController) {
    handleCancelation()
  }
}
