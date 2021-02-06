#include "pch.h"
#include "QQMusicImpl.h"
#include <Psapi.h>

bool QQMusicImpl::initNativeProtocol()
{
	//HWND
	hwnd = FindWindow(L"QQMusic_Daemon_Wnd", NULL);
	if (hwnd == NULL)
	{
		return false;
	}
	//ProcessID
	GetWindowThreadProcessId(hwnd, &processID);
	if (processID == NULL)
	{
		return false;
	}
	//offsetAddress
	DWORD_PTR baseAddress = 0;
	processHandle = OpenProcess(PROCESS_ALL_ACCESS, FALSE, processID);
	HMODULE* moduleArray;
	LPBYTE moduleArrayBytes;
	DWORD bytesRequired;
	if (processHandle)
	{
		if (EnumProcessModules(processHandle, NULL, 0, &bytesRequired))
		{
			if (bytesRequired)
			{
				moduleArrayBytes = (LPBYTE)LocalAlloc(LPTR, bytesRequired);
				if (moduleArrayBytes)
				{
					unsigned int moduleCount;
					moduleCount = bytesRequired / sizeof(HMODULE);
					moduleArray = (HMODULE*)moduleArrayBytes;
					if (EnumProcessModules(processHandle, moduleArray, bytesRequired, &bytesRequired))
					{
						wchar_t moduleName[256];
						for (unsigned int i = 0; i < moduleCount; i++)
						{
							GetModuleBaseName(processHandle, moduleArray[i], moduleName, 256);
							//找到cloudmusic.dll的基址
							if (std::wcscmp(moduleName, L"QQMusic.dll") == 0)
							{
								baseAddress = (DWORD_PTR)moduleArray[i];
								break;
							}
						}
					}
					LocalFree(moduleArrayBytes);
				}
			}
		}
	}
	else
	{
		return false;
	}
	//加上偏移地址
	if (baseAddress != 0)
	{
		offsetAddress = 0x9E5348 + baseAddress;
	}
	else
	{
		return false;
	}
	return true;
}

void QQMusicImpl::nativeGc()
{
	if (processHandle != NULL)
	{
		CloseHandle(processHandle);
		processHandle = NULL;
	}
}

std::wstring QQMusicImpl::getTitle()
{
	wchar_t titleStr[256];
	GetWindowText(hwnd, titleStr, 256);
	return std::wstring(titleStr);
}

double QQMusicImpl::getPosition()
{
	//32位的long字节大小为4
	//出来结果是ms单位
	long position;
	SIZE_T outNum;
	BOOL res = ReadProcessMemory(processHandle, (LPCVOID)offsetAddress, (LPVOID)(&position), 4, &outNum);
	return res ? position / 1000.0 : 0.0;
}

QQMusicImpl::~QQMusicImpl()
{
	QQMusicImpl::nativeGc();
}
