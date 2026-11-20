#include <jni.h>
#include <android/bitmap.h>
#include <mgba/core/core.h>
#include <mgba/core/serialize.h>
#include <fcntl.h>
#include <mgba-util/vfs.h>
#include <mgba/core/blip_buf.h>
#include <algorithm>
#include <array>
#include <cstdio>
#include <cstdlib>
#include <mutex>
#include <string>
#include <vector>
#include <unistd.h>

namespace {
std::mutex guard;
mCore* core = nullptr;
std::array<color_t, 256 * 224> pixels{};
std::string savePath;
unsigned videoWidth=240, videoHeight=160;

std::string utf(JNIEnv* env, jstring value) {
    const char* chars = env->GetStringUTFChars(value, nullptr);
    std::string result(chars);
    env->ReleaseStringUTFChars(value, chars);
    return result;
}

bool save() {
    if (!core || savePath.empty()) return true;
    void* data = nullptr;
    size_t size = core->savedataClone(core, &data);
    if (!size) { free(data); return true; }
    std::string temp = savePath + ".tmp";
    FILE* file = fopen(temp.c_str(), "wb");
    bool ok = false;
    if (file) {
        ok = fwrite(data, 1, size, file) == size;
        ok = fflush(file) == 0 && ok;
        ok = fsync(fileno(file)) == 0 && ok;
        ok = fclose(file) == 0 && ok;
        if (ok) ok = rename(temp.c_str(), savePath.c_str()) == 0;
    }
    free(data);
    return ok;
}

void closeCore() {
    if (!core) return;
    save();
    mCoreConfigDeinit(&core->config);
    core->deinit(core);
    core = nullptr;
    savePath.clear();
}
}

extern "C" JNIEXPORT jstring JNICALL
Java_com_linkcore_emulator_NativeCore_load(JNIEnv* env, jobject, jstring rom, jstring saveFile) {
    std::lock_guard<std::mutex> lock(guard);
    closeCore();
    auto path = utf(env, rom);
    core = mCoreFind(path.c_str());
    if (!core) return env->NewStringUTF("El archivo no es una ROM GB/GBC/GBA compatible.");
    mCoreInitConfig(core, nullptr);
    if (!core->init(core)) {
        closeCore();
        return env->NewStringUTF("No se pudo iniciar el núcleo.");
    }
    mCoreConfigSetDefaultIntValue(&core->config, "skipBios", 1);
    mCoreConfigSetDefaultIntValue(&core->config, "useBios", 0);
    mCoreConfigSetDefaultIntValue(&core->config, "volume", 0x100);
    mCoreConfigSetDefaultIntValue(&core->config, "sampleRate", 48000);
    mCoreConfigSetDefaultIntValue(&core->config, "logLevel", 0);
    mCoreConfigSetOverrideIntValue(&core->config, "sgb.borders", 0);
    mCoreLoadConfig(core);
    pixels.fill(0);
    core->setVideoBuffer(core, pixels.data(), 256);
    core->setAudioBufferSize(core, 2048);
    if (!mCoreLoadFile(core, path.c_str())) {
        closeCore();
        return env->NewStringUTF("No se pudo leer la ROM.");
    }
    core->reset(core);
    core->desiredVideoDimensions(core, &videoWidth, &videoHeight);
    if(videoWidth>256 || videoHeight>224) { closeCore(); return env->NewStringUTF("Unsupported video size"); }
    for (int ch = 0; ch < 2; ++ch)
        blip_set_rates(core->getAudioChannel(core, ch), core->frequency(core), 48000);
    savePath = utf(env, saveFile);
    FILE* file = fopen(savePath.c_str(), "rb");
    if (file) {
        fseek(file, 0, SEEK_END);
        long length = ftell(file);
        rewind(file);
        if (length > 0 && length <= 1024 * 1024) {
            std::vector<uint8_t> data(length);
            if (fread(data.data(), 1, length, file) == static_cast<size_t>(length))
                core->savedataRestore(core, data.data(), data.size(), false);
        }
        fclose(file);
    }
    return nullptr;
}

extern "C" JNIEXPORT jint JNICALL
Java_com_linkcore_emulator_NativeCore_frame(JNIEnv* env, jobject, jint keys, jobject bitmap, jshortArray audio) {
    std::lock_guard<std::mutex> lock(guard);
    if (!core || env->GetArrayLength(audio) < 4096) return -1;
    AndroidBitmapInfo info{};
    if (AndroidBitmap_getInfo(env, bitmap, &info) != ANDROID_BITMAP_RESULT_SUCCESS ||
        info.width != videoWidth || info.height != videoHeight || info.format != ANDROID_BITMAP_FORMAT_RGBA_8888) return -1;
    core->setKeys(core, keys & 0x3ff);
    core->runFrame(core);
    void* destination = nullptr;
    if (AndroidBitmap_lockPixels(env, bitmap, &destination) != ANDROID_BITMAP_RESULT_SUCCESS) return -1;
    for (int y = 0; y < static_cast<int>(videoHeight); ++y) {
        auto row = reinterpret_cast<uint32_t*>(static_cast<uint8_t*>(destination) + y * info.stride);
        for (int x = 0; x < static_cast<int>(videoWidth); ++x) row[x] = pixels[y * 256 + x] | 0xff000000u;
    }
    AndroidBitmap_unlockPixels(env, bitmap);
    auto left = core->getAudioChannel(core, 0);
    auto right = core->getAudioChannel(core, 1);
    int count = std::min({blip_samples_avail(left), blip_samples_avail(right), 2048});
    std::array<int16_t, 4096> samples{};
    blip_read_samples(left, samples.data(), count, 1);
    blip_read_samples(right, samples.data() + 1, count, 1);
    env->SetShortArrayRegion(audio, 0, count * 2, samples.data());
    return count * 2;
}

extern "C" JNIEXPORT jboolean JNICALL
Java_com_linkcore_emulator_NativeCore_save(JNIEnv*, jobject) {
    std::lock_guard<std::mutex> lock(guard);
    return save();
}

extern "C" JNIEXPORT void JNICALL
Java_com_linkcore_emulator_NativeCore_close(JNIEnv*, jobject) {
    std::lock_guard<std::mutex> lock(guard);
    closeCore();
}

extern "C" JNIEXPORT jint JNICALL
Java_com_linkcore_emulator_NativeCore_videoSize(JNIEnv*, jobject) {
 std::lock_guard<std::mutex> lock(guard);
 return (videoWidth << 16) | videoHeight;
}

// BRUMA Pocket extension: save states use the existing mGBA serializer, not another core.
extern "C" JNIEXPORT jboolean JNICALL Java_com_linkcore_emulator_NativeCore_saveState(JNIEnv* env,jobject,jstring path) {
 std::lock_guard<std::mutex> lock(guard);if(!core)return false;
 auto dest=utf(env,path),tmp=dest+".tmp";
 auto vf=VFileOpen(tmp.c_str(),O_RDWR|O_CREAT|O_TRUNC);if(!vf)return false;
 bool ok=mCoreSaveStateNamed(core,vf,SAVESTATE_SAVEDATA|SAVESTATE_RTC|SAVESTATE_METADATA);
 ok=vf->close(vf)&&ok;
 if(ok){int fd=open(tmp.c_str(),O_RDONLY);if(fd>=0){ok=fsync(fd)==0;close(fd);}else ok=false;}
 if(ok){std::string bak=dest+".bak";if(access(dest.c_str(),F_OK)==0)ok=rename(dest.c_str(),bak.c_str())==0; if(ok)ok=rename(tmp.c_str(),dest.c_str())==0; if(!ok&&access(dest.c_str(),F_OK)!=0)rename(bak.c_str(),dest.c_str());}
 if(!ok)remove(tmp.c_str());return ok;
}
extern "C" JNIEXPORT jboolean JNICALL Java_com_linkcore_emulator_NativeCore_loadState(JNIEnv* env,jobject,jstring path) {
 std::lock_guard<std::mutex> lock(guard);if(!core)return false;
 auto file=utf(env,path);auto vf=VFileOpen(file.c_str(),O_RDONLY);if(!vf)return false;
 bool ok=mCoreLoadStateNamed(core,vf,SAVESTATE_SAVEDATA|SAVESTATE_RTC);vf->close(vf);return ok;
}
