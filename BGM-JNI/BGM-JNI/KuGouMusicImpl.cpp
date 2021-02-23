#include "pch.h"
#include "KuGouMusicImpl.h"
#include <Psapi.h>
#include <regex>
#include "VersionUtils.h"

bool KuGouMusicImpl::initNativeProtocol()
{
	HWND _hwnd = GetDesktopWindow();
	_hwnd = GetWindow(_hwnd, GW_CHILD);
	bool isBreak = false;
	while (_hwnd != NULL)
	{
		wchar_t title[256] = { 0 };
		GetWindowText(_hwnd, title, 1024);
		if (std::wcscmp(title, L"¿á¹·ÒôÀÖ") != 0)
		{
			std::wstring titleString(title);
			size_t count = 0;
			for (wchar_t c : titleString)
			{
				if (c == '-')
				{
					count++;
				}
			}
			if (titleString.find(L"¿á¹·ÒôÀÖ") != titleString.npos && count > 1)
			{
				this->hwnd = _hwnd;
				isBreak = true;
			}
		}
		if (isBreak)
		{
			break;
		}
		_hwnd = GetWindow(_hwnd, GW_HWNDNEXT);
	}
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
							//ÕÒµ½cloudmusic.dllµÄ»ùÖ·
							if (std::wcscmp(moduleName, L"kgplayer.dll") == 0)
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
	//¼ÓÉÏÆ«ÒÆµØÖ·
	if (baseAddress != 0)
	{
		std::wstring version;
		bool res = VersionUtils::GetFileVersion(processHandle, version);
		if (res)
		{
			offsetAddress = baseAddress + VersionUtils::getOffsetByVersion(version, PlayerType::KUGOU_MUSIC);
		}
		//Ä¬ÈÏ
		else
		{
			offsetAddress = 0x38CF98 + baseAddress;
		}
	}
	else
	{
		return false;
	}
	return true;
}

void KuGouMusicImpl::nativeGc()
{
	if (processHandle != NULL)
	{
		CloseHandle(processHandle);
		processHandle = NULL;
	}
}

std::wstring KuGouMusicImpl::getTitle()
{
	std::wstring separator(L" - ¿á¹·ÒôÀÖ ");
	wchar_t titleStr[256];
	GetWindowText(hwnd, titleStr, 256);
	std::wstring title(titleStr);
	std::wstring result;
	const size_t index = title.find(separator);
	if (index != title.npos)
	{
		result.append(title.substr(index + separator.size()));
		result.append(title.substr(0, index));
	}
	return result;
}

double KuGouMusicImpl::getPosition()
{
	double position;
	SIZE_T outNum;
	BOOL res = ReadProcessMemory(processHandle, (LPCVOID)offsetAddress, (LPVOID)(&position), 8, &outNum);
	return res ? position : 0.0;
}

KuGouMusicImpl::~KuGouMusicImpl()
{
	KuGouMusicImpl::nativeGc();
}
