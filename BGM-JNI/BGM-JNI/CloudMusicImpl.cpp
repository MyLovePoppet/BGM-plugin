#include "pch.h"
#include "CloudMusicImpl.h"
#include <Psapi.h>
#include "VersionUtils.h"

bool CloudMusicImpl::initNativeProtocol()
{
	//HWND
	hwnd = FindWindow(L"OrpheusBrowserHost", NULL);
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
							//�ҵ�cloudmusic.dll�Ļ�ַ
							if (std::wcscmp(moduleName, L"cloudmusic.dll") == 0)
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
	//����ƫ�Ƶ�ַ
	if (baseAddress != 0)
	{
		std::wstring version;
		bool res = VersionUtils::GetFileVersion(processHandle, version);
		if (res)
		{
			offsetAddress = baseAddress + VersionUtils::getOffsetByVersion(version, PlayerType::CLOUD_MUSIC);
		}
		//Ĭ��
		else
		{
			offsetAddress = 0x8669C8 + baseAddress;
		}
	}
	else
	{
		return false;
	}
	return true;
}

//����Handle
void CloudMusicImpl::nativeGc()
{
	if (processHandle != NULL)
	{
		CloseHandle(processHandle);
		processHandle = NULL;
	}
}

//��ȡtitle
std::wstring CloudMusicImpl::getTitle()
{
	wchar_t titleStr[256];
	GetWindowText(hwnd, titleStr, 256);
	return std::wstring(titleStr);
}

//��ȡPosition
double CloudMusicImpl::getPosition()
{
	double position;
	SIZE_T outNum;
	BOOL res = ReadProcessMemory(processHandle, (LPCVOID)offsetAddress, (LPVOID)(&position), 8, &outNum);
	return res ? position : 0.0;
}

CloudMusicImpl::~CloudMusicImpl()
{
	CloudMusicImpl::nativeGc();
	//std::cout << "~CloudMusicImpl" << std::endl;
}
