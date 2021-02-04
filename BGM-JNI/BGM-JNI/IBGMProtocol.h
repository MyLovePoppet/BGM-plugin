#pragma once
#include <string>
class IBGMProtocol
{
public:
	virtual ~IBGMProtocol() = default;
	virtual bool initNativeProtocol() =0;
	virtual void nativeGc() =0;
	virtual std::wstring getTitle() = 0;
	virtual double getPosition() = 0;
};
