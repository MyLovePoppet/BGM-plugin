#include "pch.h"
#include "VersionUtils.h"

std::map<std::wstring, DWORD_PTR> VersionUtils::cloudMusicVersionMap = { {L"2.7.6.2102", 0x8BEAD8} };
//请自行修改QQ音乐与酷狗音乐的版本与对应的偏移值，使用CE修改器自行查看即可
std::map<std::wstring, DWORD_PTR> VersionUtils::qqMusicVersionMap = { {L"", 0x9E5348} };
std::map<std::wstring, DWORD_PTR> VersionUtils::kuGouMusicVersionMap = { {L"", 0x38CF98} };