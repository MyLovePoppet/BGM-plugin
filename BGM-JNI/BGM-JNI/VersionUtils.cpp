#include "pch.h"
#include "VersionUtils.h"

std::map<std::wstring, DWORD_PTR> VersionUtils::cloudMusicVersionMap = { {L"2.7.6.2102", 0x8BEAD8} };
//�������޸�QQ������ṷ���ֵİ汾���Ӧ��ƫ��ֵ��ʹ��CE�޸������в鿴����
std::map<std::wstring, DWORD_PTR> VersionUtils::qqMusicVersionMap = { {L"", 0x9E5348} };
std::map<std::wstring, DWORD_PTR> VersionUtils::kuGouMusicVersionMap = { {L"", 0x38CF98} };