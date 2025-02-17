workspace(name = "mediapipe")

load("@bazel_tools//tools/build_defs/repo:http.bzl", "http_archive")

skylib_version = "0.8.0"

http_archive(
    name = "bazel_skylib",
    sha256 = "2ef429f5d7ce7111263289644d233707dba35e39696377ebab8b0bc701f7818e",
    type = "tar.gz",
    url = "https://github.com/bazelbuild/bazel-skylib/releases/download/{}/bazel-skylib.{}.tar.gz".format(skylib_version, skylib_version),
)

load("@bazel_skylib//lib:versions.bzl", "versions")

versions.check(
    minimum_bazel_version = "0.24.1",
    maximum_bazel_version = "1.2.1",
)

# ABSL cpp library lts_2019_08_08.
http_archive(
    name = "com_google_absl",
    patch_args = [
        "-p1",
    ],
    # Remove after https://github.com/abseil/abseil-cpp/issues/326 is solved.
    patches = [
        "@//third_party:com_google_absl_f863b622fe13612433fdf43f76547d5edda0c93001.diff",
    ],
    sha256 = "8100085dada279bf3ee00cd064d43b5f55e5d913be0dfe2906f06f8f28d5b37e",
    strip_prefix = "abseil-cpp-20190808",
    urls = [
        "https://github.com/abseil/abseil-cpp/archive/20190808.tar.gz",
    ],
)

http_archive(
    name = "rules_cc",
    strip_prefix = "rules_cc-master",
    urls = ["https://github.com/bazelbuild/rules_cc/archive/master.zip"],
)

# GoogleTest/GoogleMock framework. Used by most unit-tests.
http_archive(
    name = "com_google_googletest",
    strip_prefix = "googletest-master",
    urls = ["https://github.com/google/googletest/archive/master.zip"],
)

# Google Benchmark library.
http_archive(
    name = "com_google_benchmark",
    build_file = "@//third_party:benchmark.BUILD",
    strip_prefix = "benchmark-master",
    urls = ["https://github.com/google/benchmark/archive/master.zip"],
)

# gflags needed by glog
http_archive(
    name = "com_github_gflags_gflags",
    sha256 = "6e16c8bc91b1310a44f3965e616383dbda48f83e8c1eaa2370a215057b00cabe",
    strip_prefix = "gflags-77592648e3f3be87d6c7123eb81cbad75f9aef5a",
    urls = [
        "https://mirror.bazel.build/github.com/gflags/gflags/archive/77592648e3f3be87d6c7123eb81cbad75f9aef5a.tar.gz",
        "https://github.com/gflags/gflags/archive/77592648e3f3be87d6c7123eb81cbad75f9aef5a.tar.gz",
    ],
)

# glog
http_archive(
    name = "com_github_glog_glog",
    build_file = "@//third_party:glog.BUILD",
    patch_args = [
        "-p1",
    ],
    patches = [
        "@//third_party:com_github_glog_glog_9779e5ea6ef59562b030248947f787d1256132ae.diff",
    ],
    sha256 = "267103f8a1e9578978aa1dc256001e6529ef593e5aea38193d31c2872ee025e8",
    strip_prefix = "glog-0.3.5",
    url = "https://github.com/google/glog/archive/v0.3.5.zip",
)

# libyuv
http_archive(
    name = "libyuv",
    build_file = "@//third_party:libyuv.BUILD",
    urls = ["https://chromium.googlesource.com/libyuv/libyuv/+archive/refs/heads/master.tar.gz"],
)

http_archive(
    name = "com_google_protobuf_javalite",
    sha256 = "79d102c61e2a479a0b7e5fc167bcfaa4832a0c6aad4a75fa7da0480564931bcc",
    strip_prefix = "protobuf-384989534b2246d413dbcd750744faab2607b516",
    urls = ["https://github.com/google/protobuf/archive/384989534b2246d413dbcd750744faab2607b516.zip"],
)

http_archive(
    name = "com_google_audio_tools",
    strip_prefix = "multichannel-audio-tools-master",
    urls = ["https://github.com/google/multichannel-audio-tools/archive/master.zip"],
)

# Needed by TensorFlow
http_archive(
    name = "io_bazel_rules_closure",
    sha256 = "e0a111000aeed2051f29fcc7a3f83be3ad8c6c93c186e64beb1ad313f0c7f9f9",
    strip_prefix = "rules_closure-cf1e44edb908e9616030cc83d085989b8e6cd6df",
    urls = [
        "http://mirror.tensorflow.org/github.com/bazelbuild/rules_closure/archive/cf1e44edb908e9616030cc83d085989b8e6cd6df.tar.gz",
        "https://github.com/bazelbuild/rules_closure/archive/cf1e44edb908e9616030cc83d085989b8e6cd6df.tar.gz",  # 2019-04-04
    ],
)

# 2019-11-21
_TENSORFLOW_GIT_COMMIT = "f482488b481a799ca07e7e2d153cf47b8e91a60c"
#_TENSORFLOW_GIT_COMMIT = "c782a538b0b90d93c6070ac177cb1f542272bcce"

_TENSORFLOW_SHA256 = "8d9118c2ce186c7e1403f04b96982fe72c184060c7f7a93e30a28dca358694f0"
#_TENSORFLOW_SHA256 = "22645e778b77713c57fb581b71a320038d65411a61f672eca718e4f371667ae6"

http_archive(
    name = "org_tensorflow",
    patch_args = [
        "-p1",
    ],
    # Patch https://github.com/tensorflow/tensorflow/commit/e3a7bdbebb99352351a19e2e403136166aa52934
    patches = [
        "@//third_party:org_tensorflow_e3a7bdbebb99352351a19e2e403136166aa52934.diff",
    ],
    sha256 = _TENSORFLOW_SHA256,
    strip_prefix = "tensorflow-%s" % _TENSORFLOW_GIT_COMMIT,
    urls = [
        "https://mirror.bazel.build/github.com/tensorflow/tensorflow/archive/%s.tar.gz" % _TENSORFLOW_GIT_COMMIT,
        "https://github.com/tensorflow/tensorflow/archive/%s.tar.gz" % _TENSORFLOW_GIT_COMMIT,
    ],
)

load("@org_tensorflow//tensorflow:workspace.bzl", "tf_workspace")

tf_workspace(tf_repo_name = "org_tensorflow")

http_archive(
    name = "ceres_solver",
    patch_args = [
        "-p1",
    ],
    patches = [
        "@//third_party:ceres_solver_9bf9588988236279e1262f75d7f4d85711dfa172.diff",
    ],
    sha256 = "5ba6d0db4e784621fda44a50c58bb23b0892684692f0c623e2063f9c19f192f1",
    strip_prefix = "ceres-solver-1.14.0",
    url = "https://github.com/ceres-solver/ceres-solver/archive/1.14.0.zip",
)

# Please run
# $ sudo apt-get install libopencv-core-dev libopencv-highgui-dev \
#                        libopencv-calib3d-dev libopencv-features2d-dev \
#                        libopencv-imgproc-dev libopencv-video-dev
new_local_repository(
    name = "linux_opencv",
    build_file = "@//third_party:opencv_linux.BUILD",
    path = "/usr",
)

new_local_repository(
    name = "linux_ffmpeg",
    build_file = "@//third_party:ffmpeg_linux.BUILD",
    path = "/usr",
)

# Please run $ brew install opencv@3
new_local_repository(
    name = "macos_opencv",
    build_file = "@//third_party:opencv_macos.BUILD",
    path = "/usr",
)

new_local_repository(
    name = "macos_ffmpeg",
    build_file = "@//third_party:ffmpeg_macos.BUILD",
    path = "/usr",
)

http_archive(
    name = "android_opencv",
    build_file = "@//third_party:opencv_android.BUILD",
    strip_prefix = "OpenCV-android-sdk",
    type = "zip",
    url = "https://github.com/opencv/opencv/releases/download/3.4.3/opencv-3.4.3-android-sdk.zip",
)

# After OpenCV 3.2.0, the pre-compiled opencv2.framework has google protobuf symbols, which will
# trigger duplicate symbol errors in the linking stage of building a mediapipe ios app.
# To get a higher version of OpenCV for iOS, opencv2.framework needs to be built from source with
# '-DBUILD_PROTOBUF=OFF -DBUILD_opencv_dnn=OFF'.
http_archive(
    name = "ios_opencv",
    build_file = "@//third_party:opencv_ios.BUILD",
    sha256 = "7dd536d06f59e6e1156b546bd581523d8df92ce83440002885ec5abc06558de2",
    type = "zip",
    url = "https://github.com/opencv/opencv/releases/download/3.2.0/opencv-3.2.0-ios-framework.zip",
)

RULES_JVM_EXTERNAL_TAG = "2.2"

RULES_JVM_EXTERNAL_SHA = "f1203ce04e232ab6fdd81897cf0ff76f2c04c0741424d192f28e65ae752ce2d6"

http_archive(
    name = "rules_jvm_external",
    sha256 = RULES_JVM_EXTERNAL_SHA,
    strip_prefix = "rules_jvm_external-%s" % RULES_JVM_EXTERNAL_TAG,
    url = "https://github.com/bazelbuild/rules_jvm_external/archive/%s.zip" % RULES_JVM_EXTERNAL_TAG,
)

load("@rules_jvm_external//:defs.bzl", "maven_install")

maven_install(
    artifacts = [
        "androidx.annotation:annotation:aar:1.1.0",
        "androidx.appcompat:appcompat:aar:1.1.0-rc01",
        "androidx.camera:camera-core:aar:1.0.0-alpha06",
        "androidx.camera:camera-camera2:aar:1.0.0-alpha06",
        "androidx.constraintlayout:constraintlayout:aar:1.1.3",
        "androidx.core:core:aar:1.1.0-rc03",
        "androidx.legacy:legacy-support-v4:aar:1.0.0",
        "androidx.recyclerview:recyclerview:aar:1.1.0-beta02",
        "com.google.android.material:material:aar:1.0.0-rc01",
    ],
    repositories = [
        "https://dl.google.com/dl/android/maven2",
        "https://repo1.maven.org/maven2",
    ],
)

maven_server(
    name = "google_server",
    url = "https://dl.google.com/dl/android/maven2",
)

maven_jar(
    name = "androidx_lifecycle",
    artifact = "androidx.lifecycle:lifecycle-common:2.0.0",
    server = "google_server",
    sha1 = "e070ffae07452331bc5684734fce6831d531785c",
)

maven_jar(
    name = "androidx_concurrent_futures",
    artifact = "androidx.concurrent:concurrent-futures:1.0.0-alpha03",
    server = "google_server",
    sha1 = "b528df95c7e2fefa2210c0c742bf3e491c1818ae",
)

maven_jar(
    name = "com_google_guava_android",
    artifact = "com.google.guava:guava:27.0.1-android",
    sha1 = "b7e1c37f66ef193796ccd7ea6e80c2b05426182d",
)

maven_jar(
    name = "com_google_common_flogger",
    artifact = "com.google.flogger:flogger:0.3.1",
    sha1 = "585030fe1ec709760cbef997a459729fb965df0e",
)

maven_jar(
    name = "com_google_common_flogger_system_backend",
    artifact = "com.google.flogger:flogger-system-backend:0.3.1",
    sha1 = "287b569d76abcd82f9de87fe41829fbc7ebd8ac9",
)

maven_jar(
    name = "com_google_code_findbugs",
    artifact = "com.google.code.findbugs:jsr305:3.0.2",
    sha1 = "25ea2e8b0c338a877313bd4672d3fe056ea78f0d",
)

# You may run setup_android.sh to install Android SDK and NDK.
android_ndk_repository(
    name = "androidndk",
    path = "/home/artifutech/Android/Ndk/android-ndk-r18b",
)

android_sdk_repository(
    name = "androidsdk",
    path = "/home/artifutech/Android/Sdk",
)

# iOS basic build deps.

http_archive(
    name = "build_bazel_rules_apple",
    sha256 = "bdc8e66e70b8a75da23b79f1f8c6207356df07d041d96d2189add7ee0780cf4e",
    strip_prefix = "rules_apple-b869b0d3868d78a1d4ffd866ccb304fb68aa12c3",
    url = "https://github.com/bazelbuild/rules_apple/archive/b869b0d3868d78a1d4ffd866ccb304fb68aa12c3.tar.gz",
)

load(
    "@build_bazel_rules_apple//apple:repositories.bzl",
    "apple_rules_dependencies",
)

apple_rules_dependencies()

load(
    "@build_bazel_rules_swift//swift:repositories.bzl",
    "swift_rules_dependencies",
)

swift_rules_dependencies()

load(
    "@build_bazel_apple_support//lib:repositories.bzl",
    "apple_support_dependencies",
)

apple_support_dependencies()

# More iOS deps.

http_archive(
    name = "google_toolbox_for_mac",
    build_file = "@//third_party:google_toolbox_for_mac.BUILD",
    sha256 = "e3ac053813c989a88703556df4dc4466e424e30d32108433ed6beaec76ba4fdc",
    strip_prefix = "google-toolbox-for-mac-2.2.1",
    url = "https://github.com/google/google-toolbox-for-mac/archive/v2.2.1.zip",
)
