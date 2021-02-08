#pragma once
#include "IBGMProtocol.h"

class KuGouMusicImpl final :
	public IBGMProtocol
{
private:
	HWND hwnd = NULL;
	DWORD processID = NULL;
	HANDLE processHandle = NULL;
	DWORD_PTR offsetAddress = NULL;
public:
	KuGouMusicImpl() = default;
	bool initNativeProtocol() override;
	void nativeGc() override;
	std::wstring getTitle() override;
	double getPosition() override;
	~KuGouMusicImpl() override;
};
