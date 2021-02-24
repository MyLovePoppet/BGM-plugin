# 歌词显示插件
主流平台音乐播放器（网易云音乐，QQ音乐，酷狗音乐）的歌词显示插件。

示例：

**网易云音乐**
![](https://i.niupic.com/images/2021/02/04/9biK.png)


**QQ音乐**
![](https://s3.ax1x.com/2021/02/06/yYrgYt.png)



**酷狗音乐**
[![yaiRqs.png](https://s3.ax1x.com/2021/02/08/yaiRqs.png)](https://imgchr.com/i/yaiRqs)


|功能                     | 时间      |
|------------------------ | --------- |
|适配网易云音乐 | 2021/2/4 |
|适配QQ音乐 | 2021/2/6 |
|适配酷狗音乐 | 2021/2/8 |
|播放器版本适配 | 2021/2/23 |
|读写锁保证效率和正确性 | 2021/2/24 |
|代码重构 | 进行中... |


如遇到position字段不准确的情况出现，请自行使用CE修改器查看offset偏移值，并修改```VersionUtils.cpp```
文件内的信息

```C++
std::map<std::wstring, DWORD_PTR> VersionUtils::cloudMusicVersionMap = { {L"2.7.6.2102", 0x8BEAD8} };
//请自行修改QQ音乐与酷狗音乐的版本与对应的偏移值，使用CE修改器自行查看即可
std::map<std::wstring, DWORD_PTR> VersionUtils::qqMusicVersionMap = { {L"", 0x9E5348} };
std::map<std::wstring, DWORD_PTR> VersionUtils::kuGouMusicVersionMap = { {L"", 0x38CF98} };
```



