#pragma once
#include <Windows.h>
#include <map>
#include <Psapi.h>
#include <string>
#pragma comment(lib, "version.lib")

enum class PlayerType :uint8_t
{
	CLOUD_MUSIC,
	QQ_MUSIC,
	KUGOU_MUSIC
};

class VersionUtils
{
private:
	static std::map<std::wstring, DWORD_PTR> cloudMusicVersionMap;
	static std::map<std::wstring, DWORD_PTR> qqMusicVersionMap;
	static std::map<std::wstring, DWORD_PTR> kuGouMusicVersionMap;
public:
	//https://stackoverflow.com/questions/19115035/getting-access-violation-error-using-getfileversioninfo
	static bool GetFileVersion(HANDLE process, std::wstring& version)
	{
		wchar_t fname[MAX_PATH] = {0};
		VS_FIXEDFILEINFO* pVi;
		DWORD dwHandle;
		if (GetModuleFileNameEx(process, NULL, fname, MAX_PATH))
		{
			DWORD dwSize = GetFileVersionInfoSizeEx(FILE_VER_GET_NEUTRAL, fname, &dwHandle); //获取大小

			if (dwSize > 0)
			{
				BYTE* buffer = new BYTE[dwSize];

				if (GetFileVersionInfo(fname, NULL, dwSize, buffer))
				{
					if (VerQueryValue(buffer, L"\\", (LPVOID*)&pVi, (PUINT)&dwSize)) //获取信息
					{
						wchar_t ver[256] = {0};
						//转化成字符串
						swprintf(ver, sizeof(ver), L"%d.%d.%d.%d",
						         HIWORD(pVi->dwFileVersionMS),
						         LOWORD(pVi->dwFileVersionMS),
						         HIWORD(pVi->dwFileVersionLS),
						         LOWORD(pVi->dwFileVersionLS));
						delete[]buffer;
						version = ver;
						return true;
					}
				}
				delete[]buffer;
			}
		}
		return false;
	}

	static DWORD_PTR getOffsetByVersion(std::wstring version, PlayerType type)
	{
		DWORD_PTR res = NULL;
		switch (type)
		{
		case PlayerType::CLOUD_MUSIC:
			if (cloudMusicVersionMap.count(version) > 0)
			{
				res = cloudMusicVersionMap[version];
			}
			else
			{
				res = cloudMusicVersionMap.begin()->second;
			}
			break;
		case PlayerType::QQ_MUSIC:
			if (qqMusicVersionMap.count(version) > 0)
			{
				res = qqMusicVersionMap[version];
			}
			else
			{
				res = qqMusicVersionMap.begin()->second;
			}
			break;
		case PlayerType::KUGOU_MUSIC:
			if (kuGouMusicVersionMap.count(version) > 0)
			{
				res = kuGouMusicVersionMap[version];
			}
			else
			{
				res = kuGouMusicVersionMap.begin()->second;
			}
			break;
		}
		return res;
	}
};

