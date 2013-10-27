/**
 * edu_gvsu_masl_echoprint_Codegen.cpp
 * jni
 *
 * Created by Alex Restrepo on 1/22/12.
 * Copyright (C) 2012 Grand Valley State University (http://masl.cis.gvsu.edu/)
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of
 * this software and associated documentation files (the "Software"), to deal in
 * the Software without restriction, including without limitation the rights to
 * use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies
 * of the Software, and to permit persons to whom the Software is furnished to do
 * so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

#include <android/log.h>
#include <string.h>
#include <jni.h>
#include "AndroidCodegen.h"
#include "codegen/src/Codegen.h"

#define LOG_LEVEL 2
#define LOG_TAG "AndroidCodegen.cpp"
#define LOGI(level, ...) if (level <= LOG_LEVEL) {__android_log_print(ANDROID_LOG_INFO, LOG_TAG, __VA_ARGS__);}
#define LOGE(level, ...) if (level <= LOG_LEVEL + 10) {__android_log_print(ANDROID_LOG_ERROR, LOG_TAG, __VA_ARGS__);}
#define LOGW(level, ...) if (level <= LOG_LEVEL + 5) {__android_log_print(ANDROID_LOG_WARN, LOG_TAG, __VA_ARGS__);}

JNIEXPORT jstring JNICALL Java_com_appunite_ffmpeg_FFmpegPlayer_codegen(JNIEnv *env, jobject thiz, jfloatArray pcmData, jint numSamples)
{
	// get the contents of the java array as native floats
	float *data = (float *) env->GetFloatArrayElements(pcmData, 0);
	// invoke the codegen
	Codegen c = Codegen(data, (unsigned int) numSamples, 0);

	const char *code = c.getCodeString().c_str();

	// release the native array as we're done with them
	env->ReleaseFloatArrayElements(pcmData, data, 0);

	// return the fingerprint string
	return env->NewStringUTF(code);
}
