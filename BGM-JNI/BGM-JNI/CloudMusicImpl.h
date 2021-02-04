#pragma once
#include "IBGMProtocol.h"


class CloudMusicImpl final :
	public IBGMProtocol
{
private:
	HWND hwnd = NULL;
	DWORD processID = NULL;
	HANDLE processHandle = NULL;
	DWORD_PTR offsetAddress = NULL;
public:
	CloudMusicImpl() = default;
	bool initNativeProtocol() override;
	void nativeGc() override;
	std::wstring getTitle() override;
	double getPosition() override;
	~CloudMusicImpl() override;
};
