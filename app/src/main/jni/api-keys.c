#include <jni.h>

JNIEXPORT jstring JNICALL
Java_com_nest_nestplay_SplashScreenActivity_getKeys(JNIEnv *env, jobject thiz) {
    return (*env) -> NewStringUTF(env, "770E75DC61635CCC61A1D7D8FFF9D1B0");
}

JNIEXPORT jstring JNICALL
Java_com_nest_nestplay_DetailMovieActivity_getKeys(JNIEnv *env, jobject thiz) {
    return (*env) -> NewStringUTF(env, "770E75DC61635CCC61A1D7D8FFF9D1B0");
}

JNIEXPORT jstring JNICALL
Java_com_nest_nestplay_player_VideoPlayActivity2_getKeys(JNIEnv *env, jobject thiz) {
    return (*env) -> NewStringUTF(env, "770E75DC61635CCC61A1D7D8FFF9D1B0");
}