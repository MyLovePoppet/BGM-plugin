#include "pch.h"
#include "com_shuqy_bgm_service_BGMService.h"
#include "JNIUtils.h"
#include "IBGMProtocol.h"
#include "CloudMusicImpl.h"
std::shared_ptr<IBGMProtocol> bgm;
/*
 * Class:     com_shuqy_bgm_service_BGMService
 * Method:    checkPlayer
 * Signature: ()I
 */
JNIEXPORT jint JNICALL Java_com_shuqy_bgm_service_BGMService_checkPlayer
(JNIEnv*, jobject)
{
	//����������
	HWND hwnd = FindWindow(L"OrpheusBrowserHost", NULL);
	if (hwnd != NULL)
	{
		return 0;
	}
	//QQ����
	hwnd = FindWindow(L"QQMusic_Daemon_Wnd", NULL);
	if (hwnd != NULL)
	{
		return 1;
	}

	//��������֧�ֵĲ�����
	return -1;
}

/*
 * Class:     com_shuqy_bgm_service_BGMService
 * Method:    initNativeDll
 * Signature: (I)I
 */
JNIEXPORT jint JNICALL Java_com_shuqy_bgm_service_BGMService_initNativeDll
(JNIEnv*, jobject, jint playerType)
{
	int res = -1;
	switch (playerType)
	{
	case 0:
		//����������ʵ��
		bgm = std::make_shared<CloudMusicImpl>();
		//��ʼ�����ؽ��
		if (bgm->initNativeProtocol())
		{
			res = 0;
		} //��ʼ��ʧ��
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
JNIEXPORT void JNICALL Java_com_shuqy_bgm_service_BGMService_nativeGc
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
JNIEXPORT jstring JNICALL Java_com_shuqy_bgm_service_BGMService_getMusicTitle
(JNIEnv* env, jobject)
{
	if (bgm)
	{
		const std::wstring title = bgm->getTitle();
		return JNIUtils::w2js(env, title);
	}
	//������
	return JNIUtils::w2js(env, L"");
}

/*
 * Class:     com_shuqy_bgm_service_BGMService
 * Method:    getCurrentPosition
 * Signature: ()D
 */
JNIEXPORT jdouble JNICALL Java_com_shuqy_bgm_service_BGMService_getCurrentPosition
(JNIEnv*, jobject)
{
	if (bgm)
	{
		return bgm->getPosition();
	}
	//������
	return 0.0;
}
