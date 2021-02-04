#pragma once
#include "pch.h"
#include <iostream>
#include <jni.h>

namespace JNIUtils
{
	/*****************************************************************
	 *wchar_t* ----->jstring
	 *******************************************************************/
	jstring w2js(JNIEnv* env, wchar_t* str)
	{
		size_t len = wcslen(str);
		jchar* str2 = (jchar*)malloc(sizeof(jchar) * (len + 1));
		for (size_t i = 0; i < len; i++)
			str2[i] = str[i];
		str2[len] = '\0';
		jstring js = env->NewString(str2, len);
		free(str2);
		return js;
	}

	/*****************************************************************
	 *wchar_t* ----->jstring
	 *******************************************************************/
	jstring w2js(JNIEnv* env, std::wstring str1)
	{
		const wchar_t* str = str1.c_str();
		size_t len = wcslen(str);
		jchar* str2 = (jchar*)malloc(sizeof(jchar) * (len + 1));
		for (size_t i = 0; i < len; i++)
			str2[i] = str[i];
		str2[len] = '\0';
		jstring js = env->NewString(str2, len);
		free(str2);
		return js;
	}
}
