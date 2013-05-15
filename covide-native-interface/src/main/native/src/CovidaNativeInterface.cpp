#include "stdafx.h"

#include "algorithm.h"
#include "CovidaNativeInterface.h"
#include "de_dfki_covida_NativeInterface.h"
#include "de_dfki_covida_NativeInterface_ShutdownHook.h"


// C RunTime header files
#include <stdlib.h>
#include <malloc.h>
#include <memory.h>
#include <math.h>

#include <iostream>
#include <assert.h>
#include <vector>
// Macros
#define ASSERT assert
#define INPUT_CLASS "de/dfki/covida/NativeInterface"

using namespace std;
// DPI settings

// Module
extern HMODULE		g_this;
// Multi-Touch Hook
extern HHOOK		g_hook;

// Method ID of the java callback method for pen events
jmethodID* g_pmid;

// Java VM reference
static JavaVM *jvm = NULL;
static int mtshutdown = 0;
// Reference to java class
jclass g_cls;

void jArrayToVector( const jobjectArray& arr, vector<Point*>& vec_point)
{
		
}
JNIEXPORT void JNICALL Java_de_dfki_covida_NativeInterface_onVideoOpened
 (JNIEnv *env, jobject, jstring path)
{
	const char *str = env->GetStringUTFChars(path, 0); 
	const string path_str (str);
	onVideoOpened(path_str);
	env->ReleaseStringUTFChars(path, str); 

}

JNIEXPORT void JNICALL Java_de_dfki_covida_NativeInterface_onVideoClosed
  (JNIEnv * env, jobject, jstring path)
{
	const char *str = env->GetStringUTFChars(path, 0); 
	const string path_str (str);
	onVideoClosed(path_str);
	env->ReleaseStringUTFChars(path, str); 
}

JNIEXPORT void JNICALL Java_de_dfki_covida_NativeInterface_onImageOpened
  (JNIEnv * env, jobject, jstring path)
{
	const char *str = env->GetStringUTFChars(path, 0); 
	const string path_str (str);
	onImageOpened(path_str);
	env->ReleaseStringUTFChars(path, str); 
}


JNIEXPORT void JNICALL Java_de_dfki_covida_NativeInterface_onImageClosed
  (JNIEnv * env, jobject, jstring path)
{
	const char *str = env->GetStringUTFChars(path, 0); 
	const string path_str (str);
	onImageClosed(path_str);
	env->ReleaseStringUTFChars(path, str); 
}

JNIEXPORT void JNICALL Java_de_dfki_covida_NativeInterface_circleAnnotation
  (JNIEnv * env, jobject, jstring path, jstring description, jlong start, jlong end, jfloat dx, jfloat dy, jfloat radius, jobjectArray point)
{
	const char *str = env->GetStringUTFChars(path, 0); 
	const string path_str (str);
	const char *str2 = env->GetStringUTFChars(description, 0); 
	const string desc_str (str2);
	long lstart = static_cast<long>(start);
	long lend = static_cast<long>(end);
	float fdx = static_cast<float>(dx);
	float fdy = static_cast<float>(dx);
	float fradius = static_cast<float>(dx);
	vector<Point*> vpoints;
	jArrayToVector(point, vpoints);
	circleAnnotation(path_str, desc_str, lstart, lend, fdx, fdy, fradius, vpoints); 
	env->ReleaseStringUTFChars(path, str); 
	env->ReleaseStringUTFChars(description, str2);
}

JNIEXPORT void JNICALL Java_de_dfki_covida_NativeInterface_lineAnnotation
  (JNIEnv * env, jobject, jstring path, jstring description, jlong start, jlong end, jfloat ax, jfloat ay, jfloat bx, jfloat by, jobjectArray points)
{
	const char *str = env->GetStringUTFChars(path, 0); 
	const string path_str (str);
	const char *str2 = env->GetStringUTFChars(description, 0); 
	const string desc_str (str2);
	long lstart = static_cast<long>(start);
	long lend = static_cast<long>(end);
	float fax = static_cast<float>(ax);
	float fay = static_cast<float>(ay);
	float fbx = static_cast<float>(bx);
	float fby = static_cast<float>(by);
	vector<Point*> vpoints;
	jArrayToVector(points, vpoints);
	lineAnnotation(path_str, desc_str, lstart, lend, fax, fay, fbx, fby, vpoints);
	env->ReleaseStringUTFChars(path, str); 
	env->ReleaseStringUTFChars(description, str2);

}


JNIEXPORT void JNICALL Java_de_dfki_covida_NativeInterface_polygonAnnotation
  (JNIEnv * env, jobject, jstring path, jstring description, jlong start, jlong end, jobjectArray points)
{
	const char *str = env->GetStringUTFChars(path, 0); 
	const string path_str (str);
	const char *str2 = env->GetStringUTFChars(description, 0); 
	const string desc_str (str2);
	long lstart = static_cast<long>(start);
	long lend = static_cast<long>(end);
	vector<Point*> vpoints;
	jArrayToVector(points, vpoints);
	polygonAnnotation(path_str, desc_str, lstart, lend, vpoints);
	env->ReleaseStringUTFChars(path, str); 
	env->ReleaseStringUTFChars(description, str2);
}