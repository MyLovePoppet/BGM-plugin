#include "pch.h"
#include "com_shuqy_bgm_service_BGMService.h"
#include "JNIUtils.h"
#include "IBGMProtocol.h"
#include "CloudMusicImpl.h"
#include "QQMusicImpl.h"
#include "KuGouMusicImpl.h"
#include <WtsApi32.h>
#pragma comment(lib, "WtsApi32.lib")
std::shared_ptr<IBGMProtocol> bgm;
/*
 * Class:     com_shuqy_bgm_service_BGMService
 * Method:    checkPlayer
 * Signature: ()I
 */
inline JNIEXPORT jint JNICALL Java_com_shuqy_bgm_service_BGMService_checkPlayer
(JNIEnv*, jobject)
{
	//网易云音乐
	HWND hwnd = FindWindow(L"OrpheusBrowserHost", NULL);
	if (hwnd != NULL)
	{
		return 0;
	}
	//QQ音乐
	hwnd = FindWindow(L"QQMusic_Daemon_Wnd", NULL);
	if (hwnd != NULL)
	{
		return 1;
	}

	jint res = -1;
	WTS_PROCESS_INFO* pWPIs = NULL;
	DWORD dwProcCount = 0;
	if (WTSEnumerateProcesses(WTS_CURRENT_SERVER_HANDLE, NULL, 1, &pWPIs, &dwProcCount))
	{
		for (DWORD i = 0; i < dwProcCount; i++)
		{
			//pWPIs[i].pProcessName = process file name only, no path!
			//pWPIs[i].ProcessId = process ID
			//pWPIs[i].SessionId = session ID, if you need to limit it to the logged in user processes
			//pWPIs[i].pUserSid = user SID that started the process
			if (std::wcscmp(pWPIs[i].pProcessName, L"KuGou.exe") == 0)
			{
				//酷狗音乐
				res = 2;
				break;
			}
		}
	}
	//Free memory
	if (pWPIs)
	{
		WTSFreeMemory(pWPIs);
		pWPIs = NULL;
	}
	return res;
	//其他不受支持的播放器
	//return -1;
}

/*
 * Class:     com_shuqy_bgm_service_BGMService
 * Method:    initNativeDll
 * Signature: (I)I
 */
inline JNIEXPORT jint JNICALL Java_com_shuqy_bgm_service_BGMService_initNativeDll
(JNIEnv*, jobject, jint playerType)
{
	int res = -1;
	switch (playerType)
	{
	case 0:
		//网易云音乐实现
		bgm = std::make_shared<CloudMusicImpl>();
		//初始化返回结果
		if (bgm->initNativeProtocol())
		{
			res = 0;
		} //初始化失败
		else
		{
			bgm.reset();
			res = GetLastError();
		}
		break;
	case 1:
		//QQ音乐实现
		bgm = std::make_shared<QQMusicImpl>();
		//初始化返回结果
		if (bgm->initNativeProtocol())
		{
			res = 0;
		} //初始化失败
		else
		{
			bgm.reset();
			res = GetLastError();
		}
		break;
	case 2:
		bgm = std::make_shared<KuGouMusicImpl>();
		//初始化返回结果
		if (bgm->initNativeProtocol())
		{
			res = 0;
		} //初始化失败
		else
		{
			bgm.reset();
			res = GetLastError();
		}
		break;
	default:
		{
			res = -1;
		}
	}
	return res;
}

/*
 * Class:     com_shuqy_bgm_service_BGMService
 * Method:    nativeGc
 * Signature: ()V
 */
inline JNIEXPORT void JNICALL Java_com_shuqy_bgm_service_BGMService_nativeGc
(JNIEnv*, jobject)
{
	if (bgm)
	{
		bgm->nativeGc();
	}
}

/*
 * Class:     com_shuqy_bgm_service_BGMService
 * Method:    getMusicTitle
 * Signature: ()Ljava/lang/String;
 */
inline JNIEXPORT jstring JNICALL Java_com_shuqy_bgm_service_BGMService_getMusicTitle
(JNIEnv* env, jobject)
{
	if (bgm)
	{
		const std::wstring title = bgm->getTitle();
		return JNIUtils::w2js(env, title);
	}
	//空数据
	return JNIUtils::w2js(env, L"");
}

/*
 * Class:     com_shuqy_bgm_service_BGMService
 * Method:    getCurrentPosition
 * Signature: ()D
 */
inline JNIEXPORT jdouble JNICALL Java_com_shuqy_bgm_service_BGMService_getCurrentPosition
(JNIEnv*, jobject)
{
	if (bgm)
	{
		return bgm->getPosition();
	}
	//空数据
	return 0.0;
}
