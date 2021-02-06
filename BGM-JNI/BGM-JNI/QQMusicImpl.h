#pragma once
#include "IBGMProtocol.h"

class QQMusicImpl final :
	public IBGMProtocol
{
private:
	HWND hwnd = NULL;
	DWORD processID = NULL;
	HANDLE processHandle = NULL;
	DWORD_PTR offsetAddress = NULL;
public:
	QQMusicImpl() = default;
	bool initNativeProtocol() override;
	void nativeGc() override;
	std::wstring getTitle() override;
	double getPosition() override;
	~QQMusicImpl() override;
};

