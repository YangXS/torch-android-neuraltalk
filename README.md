# torch-android-neuraltalk
## Image Captioning Android Application
This is an android application for image captioning using recurrent neural networks. It actually uses the pretrained models obtained from
[neuraltalk](https://github.com/karpathy/neuraltalk2) to caption images on device.
To reduce the model size and speed up the inference time, I retrained the neuraltalk with the [inception model](https://github.com/soumith/inception.torch)
instead of vgg. On my htc-one phone it takes 30 seconds to caption each image. For comparison [Tap TapSee](https://play.google.com/store/apps/details?id=com.msearcher.taptapsee.android&hl=en)
needs about 20 seconds for captioning on my device.
If you want to test the app you can download the [apk](https://drive.google.com/file/d/0B-tvBXu7-t0-T2xyZFRadHc0cVk/view?usp=sharing) and
install it:
```bash
adb install -r torch-neuraltalk.apk
```

## Requirements
You need [android torch](https://github.com/soumith/torch-android) to build this app. Please follow their instructions to build android
torch.

## Building App
The easiest way to build the app is to make a new folder in the demos folder of the android torch and put the code there:
```bash
$ cd $PATH_TO_ANDROID_TORCH/demos
$ mkdir torch-neuraltalk && cd torch-neuraltalk
$ git clone git@github.com:mseyed/torch-android-neuraltalk.git
$ cd application && sh build.sh
```

## Acknowledgements
Huge thanks to [Andrej Karpathy](https://github.com/karpathy) for providing NeuralTalk code and [Soumith Chintala](https://github.com/soumith)
for maintaining android torch.
